package br.ufms.facom.alucar.controller;

import br.ufms.facom.alucar.dao.CategoriaVeiculoDAO;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.dao.VeiculoDAO;
import br.ufms.facom.alucar.model.CategoriaVeiculo;
import br.ufms.facom.alucar.model.StatusVeiculo;
import br.ufms.facom.alucar.model.Veiculo;
import br.ufms.facom.alucar.util.MoedaUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;


public class ControladoraVeiculo {


    private static final Pattern PLACA =
            Pattern.compile("^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$");

    private static final int ANO_MINIMO = 1900;
    private static final BigDecimal VALOR_MAXIMO_DIARIA = new BigDecimal("99999.99");
    private static final int KM_MAXIMO = 9_999_999;

    private final VeiculoDAO veiculoDAO;
    private final CategoriaVeiculoDAO categoriaDAO;

    public ControladoraVeiculo() {
        this.veiculoDAO = new VeiculoDAO();
        this.categoriaDAO = new CategoriaVeiculoDAO();
    }

    public void cadastrarVeiculo(String placa, String renavam, String modelo, String marca,
                                 String anoFabricacao, String kmAtual, String valorDiaria,
                                 StatusVeiculo status, String nomeCategoria)
            throws ValidacaoException, DAOException {

        Veiculo veiculo = montarEValidar(placa, renavam, modelo, marca, anoFabricacao,
                kmAtual, valorDiaria, status, nomeCategoria);

        if (veiculoDAO.existePlaca(veiculo.getPlaca())) {
            throw new ValidacaoException("Ja existe um veiculo cadastrado com a placa "
                    + veiculo.getPlaca() + ".");
        }
        if (veiculoDAO.renavamEmUsoPorOutro(veiculo.getRenavam(), null)) {
            throw new ValidacaoException("O RENAVAM informado ja pertence a outro veiculo.");
        }

        veiculoDAO.inserir(veiculo);
    }


    public void alterarVeiculo(String placa, String renavam, String modelo, String marca,
                               String anoFabricacao, String kmAtual, String valorDiaria,
                               StatusVeiculo status, String nomeCategoria)
            throws ValidacaoException, DAOException {

        Veiculo veiculo = montarEValidar(placa, renavam, modelo, marca, anoFabricacao,
                kmAtual, valorDiaria, status, nomeCategoria);

        Veiculo existente = veiculoDAO.buscarPorPlaca(veiculo.getPlaca());
        if (existente == null) {
            throw new ValidacaoException("Nenhum veiculo encontrado com a placa "
                    + veiculo.getPlaca() + ".");
        }
        if (veiculoDAO.renavamEmUsoPorOutro(veiculo.getRenavam(), veiculo.getPlaca())) {
            throw new ValidacaoException("O RENAVAM informado ja pertence a outro veiculo.");
        }


        if (veiculo.getKmAtual() < existente.getKmAtual()) {
            throw new ValidacaoException("A quilometragem informada ("
                    + veiculo.getKmAtual() + " km) e menor que a registrada ("
                    + existente.getKmAtual() + " km).\n"
                    + "A quilometragem de um veiculo nao diminui.");
        }


        if ((existente.getStatus() == StatusVeiculo.LOCADO
                || existente.getStatus() == StatusVeiculo.RESERVADO)
                && veiculo.getStatus() != existente.getStatus()) {

            throw new ValidacaoException("Este veiculo esta "
                    + existente.getStatus().getDescricao().toUpperCase() + ".\n\n"
                    + "O status so muda quando a locacao ou a reserva for encerrada,\n"
                    + "pelo caso de uso correspondente.");
        }

        veiculoDAO.atualizar(veiculo);
    }

    public List<Veiculo> listarVeiculos() throws DAOException {
        return veiculoDAO.listarTodos();
    }


    public List<Veiculo> listarDisponiveis() throws DAOException {
        return veiculoDAO.listarDisponiveis();
    }


    public List<Veiculo> consultarVeiculos(String marca, String modelo, String categoria,
                                           StatusVeiculo status) throws DAOException {
        return veiculoDAO.consultar(marca, modelo, categoria, status);
    }

    public Veiculo buscarVeiculo(String placa) throws DAOException {
        return veiculoDAO.buscarPorPlaca(normalizarPlaca(placa));
    }


    public List<CategoriaVeiculo> listarCategoriasAtivas() throws DAOException {
        return categoriaDAO.listar(false);
    }

    private Veiculo montarEValidar(String placa, String renavam, String modelo, String marca,
                                   String anoFabricacao, String kmAtual, String valorDiaria,
                                   StatusVeiculo status, String nomeCategoria)
            throws ValidacaoException, DAOException {

        String placaLimpa = normalizarPlaca(placa);
        if (placaLimpa.isEmpty()) {
            throw new ValidacaoException("Informe a placa do veiculo.");
        }
        if (!PLACA.matcher(placaLimpa).matches()) {
            throw new ValidacaoException("A placa informada e invalida.\n\n"
                    + "Formatos aceitos:\n"
                    + "  ABC1234  (modelo antigo)\n"
                    + "  ABC1D23  (modelo Mercosul)");
        }

        String renavamLimpo = renavam == null ? "" : renavam.replaceAll("\\D", "");
        if (renavamLimpo.isEmpty()) {
            throw new ValidacaoException("Informe o RENAVAM do veiculo.");
        }
        if (renavamLimpo.length() != 11) {
            throw new ValidacaoException("O RENAVAM deve ter 11 digitos.");
        }

        if (modelo == null || modelo.isBlank()) {
            throw new ValidacaoException("Informe o modelo do veiculo.");
        }
        if (marca == null || marca.isBlank()) {
            throw new ValidacaoException("Informe a marca do veiculo.");
        }

        int ano = converterInteiro(anoFabricacao, "ano de fabricacao");
        int anoLimite = LocalDate.now().getYear() + 1;
        if (ano < ANO_MINIMO || ano > anoLimite) {
            throw new ValidacaoException("O ano de fabricacao deve estar entre "
                    + ANO_MINIMO + " e " + anoLimite + ".");
        }

        int km = converterInteiro(kmAtual, "quilometragem");
        if (km < 0) {
            throw new ValidacaoException("A quilometragem nao pode ser negativa.");
        }
        if (km > KM_MAXIMO) {
            throw new ValidacaoException("A quilometragem informada parece alta demais.");
        }

        BigDecimal valor = converterValor(valorDiaria);

        if (status == null) {
            throw new ValidacaoException("Selecione o status do veiculo.");
        }

        if (nomeCategoria == null || nomeCategoria.isBlank()) {
            throw new ValidacaoException("Selecione a categoria do veiculo.");
        }
        CategoriaVeiculo categoria = categoriaDAO.buscarPorNome(nomeCategoria.trim());
        if (categoria == null) {
            throw new ValidacaoException("A categoria '" + nomeCategoria.trim()
                    + "' nao existe.");
        }
        if (!categoria.isAtivo()) {
            throw new ValidacaoException("A categoria '" + categoria.getNome()
                    + "' esta inativa e nao pode receber veiculos.");
        }

        Veiculo veiculo = new Veiculo(placaLimpa, renavamLimpo, modelo.trim(), marca.trim(),
                ano, km, valor, status, categoria);

        // RN04 - a depreciacao prevalece sobre o status escolhido na tela.
        if (veiculo.atingiuLimiteDepreciacao()) {
            veiculo.alterarStatus(StatusVeiculo.DESATIVADO);
        }

        return veiculo;
    }


    public String verificarDepreciacao(String anoFabricacao, String kmAtual) {
        try {
            int ano = Integer.parseInt(anoFabricacao.trim());
            int km = Integer.parseInt(kmAtual.trim().replaceAll("\\D", ""));

            Veiculo simulado = new Veiculo();
            simulado.setAnoFabricacao(ano);
            simulado.setKmAtual(km);

            return simulado.atingiuLimiteDepreciacao()
                    ? simulado.descreverMotivoDaDepreciacao()
                    : null;

        } catch (RuntimeException e) {
            return null;
        }
    }

    private String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private int converterInteiro(String texto, String nomeDoCampo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Informe a " + nomeDoCampo + ".");
        }
        try {
            return Integer.parseInt(texto.trim().replaceAll("[.\\s]", ""));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("A " + nomeDoCampo + " deve ser um numero inteiro.");
        }
    }

    private BigDecimal converterValor(String texto) throws ValidacaoException {
        BigDecimal valor;
        try {
            valor = MoedaUtil.converter(texto);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Informe o valor da diaria.\n"
                    + "Use o formato 240,00.");
        }

        if (valor.signum() <= 0) {
            throw new ValidacaoException("O valor da diaria deve ser maior que zero.");
        }
        if (valor.compareTo(VALOR_MAXIMO_DIARIA) > 0) {
            throw new ValidacaoException("O valor da diaria parece alto demais.\n"
                    + "Limite aceito: " + MoedaUtil.formatar(VALOR_MAXIMO_DIARIA) + ".");
        }
        return valor;
    }
}
