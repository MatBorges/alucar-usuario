package br.ufms.facom.alucar.controller;

import br.ufms.facom.alucar.dao.ClienteDAO;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.dao.LocacaoDAO;
import br.ufms.facom.alucar.dao.UsuarioDAO;
import br.ufms.facom.alucar.dao.VeiculoDAO;
import br.ufms.facom.alucar.model.Cliente;
import br.ufms.facom.alucar.model.Locacao;
import br.ufms.facom.alucar.model.StatusLocacao;
import br.ufms.facom.alucar.model.StatusVeiculo;
import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.model.Veiculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Padrao GRASP Controller para o caso de uso Realizar Locacao (RF08).
 * Valida pre-condicoes, contratos de operacao e regras de negocio.
 */
public class ControladoraLocacao {

    private static final BigDecimal DESCONTO_LONGA_DURACAO = new BigDecimal("0.10"); // 10% de desconto para >= 30 dias (RN05)
    private static final int DIAS_PARA_DESCONTO = 30;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LocacaoDAO locacaoDAO;
    private final ClienteDAO clienteDAO;
    private final VeiculoDAO veiculoDAO;
    private final UsuarioDAO usuarioDAO;

    public ControladoraLocacao() {
        this.locacaoDAO = new LocacaoDAO();
        this.clienteDAO = new ClienteDAO();
        this.veiculoDAO = new VeiculoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public Locacao realizarLocacao(String cpfCliente, String placaVeiculo, String matriculaAtendente,
                                   String dataDevolucaoTexto, String valorCaucaoTexto)
            throws ValidacaoException, DAOException {

        if (dataDevolucaoTexto == null || dataDevolucaoTexto.isBlank()
                || dataDevolucaoTexto.replace("/", "").isBlank()) {
            throw new ValidacaoException("A data de devolucao prevista e obrigatoria.");
        }

        LocalDate dataDevolucao;
        try {
            dataDevolucao = LocalDate.parse(dataDevolucaoTexto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data de devolucao invalida. Utilize o formato dd/mm/aaaa.");
        }

        BigDecimal caucao;
        if (valorCaucaoTexto == null || valorCaucaoTexto.isBlank()) {
            caucao = BigDecimal.ZERO;
        } else {
            try {
                caucao = br.ufms.facom.alucar.util.MoedaUtil.converter(valorCaucaoTexto);
            } catch (IllegalArgumentException e) {
                throw new ValidacaoException("Valor do caucao invalido.");
            }
        }

        return realizarLocacao(cpfCliente, placaVeiculo, matriculaAtendente, dataDevolucao, caucao);
    }

    public Locacao realizarLocacao(String cpfCliente, String placaVeiculo, String matriculaAtendente,
                                   LocalDate dataDevolucaoPrevista, BigDecimal valorCaucao)
            throws ValidacaoException, DAOException {

        if (cpfCliente == null || cpfCliente.isBlank()) {
            throw new ValidacaoException("O cliente deve ser selecionado.");
        }
        if (placaVeiculo == null || placaVeiculo.isBlank()) {
            throw new ValidacaoException("O veiculo deve ser selecionado.");
        }
        if (matriculaAtendente == null || matriculaAtendente.isBlank()) {
            throw new ValidacaoException("O atendente responsavel deve ser informado.");
        }
        if (dataDevolucaoPrevista == null) {
            throw new ValidacaoException("A data de devolucao prevista e obrigatoria.");
        }
        if (!dataDevolucaoPrevista.isAfter(LocalDate.now())) {
            throw new ValidacaoException("A data de devolucao prevista deve ser posterior a hoje.");
        }
        if (valorCaucao == null || valorCaucao.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException("O valor do caucao nao pode ser negativo.");
        }

        Cliente cliente = clienteDAO.buscarPorCpf(cpfCliente);
        if (cliente == null || !cliente.isAtivo()) {
            throw new ValidacaoException("Cliente nao encontrado ou inativo no sistema.");
        }

        // RN01 - CNH valida e nao vencida
        if (cliente.getValidadeCnh() == null || cliente.getValidadeCnh().isBefore(LocalDate.now())) {
            throw new ValidacaoException("A CNH do cliente esta vencida (Validade: "
                    + cliente.getValidadeCnh() + "). Nao e permitido efetuar a locacao.");
        }

        Veiculo veiculo = veiculoDAO.buscarPorPlaca(placaVeiculo);
        if (veiculo == null) {
            throw new ValidacaoException("Veiculo nao encontrado.");
        }

        // RN02 - Veiculo deve estar com status DISPONIVEL
        if (!veiculo.estaDisponivel()) {
            throw new ValidacaoException("O veiculo selecionado nao esta disponivel (Status atual: "
                    + veiculo.getStatus() + ").");
        }

        Usuario atendente = usuarioDAO.buscarPorMatricula(matriculaAtendente);
        if (atendente == null || !atendente.isAtivo()) {
            throw new ValidacaoException("Atendente nao encontrado ou inativo.");
        }

        BigDecimal valorEstimado = calcularEstimativa(veiculo.getValorDiaria(), dataDevolucaoPrevista);

        Locacao locacao = new Locacao();
        locacao.setDataRetirada(LocalDateTime.now());
        locacao.setDataDevolucaoPrevista(dataDevolucaoPrevista);
        locacao.setKmInicial(veiculo.getKmAtual());
        locacao.setValorEstimado(valorEstimado);
        locacao.setValorCaucao(valorCaucao);
        locacao.setStatus(StatusLocacao.ATIVA);
        locacao.setCliente(cliente);
        locacao.setVeiculo(veiculo);
        locacao.setAtendente(atendente);

        locacaoDAO.inserir(locacao);
        return locacao;
    }

    public BigDecimal calcularEstimativa(BigDecimal valorDiaria, LocalDate dataDevolucaoPrevista) {
        if (valorDiaria == null || dataDevolucaoPrevista == null) {
            return BigDecimal.ZERO;
        }
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), dataDevolucaoPrevista);
        if (dias <= 0) {
            dias = 1;
        }

        BigDecimal total = valorDiaria.multiply(BigDecimal.valueOf(dias));

        // RN05 - Desconto para periodos >= 30 dias
        if (dias >= DIAS_PARA_DESCONTO) {
            BigDecimal desconto = total.multiply(DESCONTO_LONGA_DURACAO);
            total = total.subtract(desconto);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Locacao> listarTodas() throws DAOException {
        return locacaoDAO.listarTodas();
    }

    public List<Locacao> listarPorStatus(StatusLocacao status) throws DAOException {
        return locacaoDAO.listarPorStatus(status);
    }

    public List<Cliente> listarClientesAtivos() throws DAOException {
        return clienteDAO.listar(false);
    }

    public List<Veiculo> listarVeiculosDisponiveis() throws DAOException {
        return veiculoDAO.listarDisponiveis();
    }

    public List<Usuario> listarAtendentesAtivos() throws DAOException {
        return usuarioDAO.listar(false);
    }

    public void cancelarLocacao(int idLocacao, String placaVeiculo) throws DAOException {
        locacaoDAO.cancelar(idLocacao, placaVeiculo);
    }
}
