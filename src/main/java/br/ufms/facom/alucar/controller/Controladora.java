package br.ufms.facom.alucar.controller;

import br.ufms.facom.alucar.dao.CategoriaVeiculoDAO;
import br.ufms.facom.alucar.dao.ClienteDAO;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.dao.LocacaoDAO;
import br.ufms.facom.alucar.dao.UsuarioDAO;
import br.ufms.facom.alucar.dao.VeiculoDAO;
import br.ufms.facom.alucar.model.CategoriaVeiculo;
import br.ufms.facom.alucar.model.Cliente;
import br.ufms.facom.alucar.model.Locacao;
import br.ufms.facom.alucar.model.StatusLocacao;
import br.ufms.facom.alucar.model.StatusVeiculo;
import br.ufms.facom.alucar.model.TipoUsuario;
import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.model.ValoresDaDevolucao;
import br.ufms.facom.alucar.model.Veiculo;
import br.ufms.facom.alucar.util.CpfUtil;
import br.ufms.facom.alucar.util.MoedaUtil;
import br.ufms.facom.alucar.util.ParametrosSistema;
import br.ufms.facom.alucar.util.SenhaUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controladora unica do sistema (padrao GRASP Controller, variante
 * "controladora de fachada").
 *
 * Toda a camada de interface com o usuario conversa somente com esta classe:
 * ela recebe os eventos de sistema de todos os casos de uso, valida as
 * pre-condicoes e as regras de negocio, e delega a persistencia a camada DAO.
 * Nenhuma tela acessa DAO ou entidade de dominio diretamente.
 *
 * Organizacao interna, por caso de uso:
 *   1. Gerenciar Usuarios            (RF01)
 *   2. Gerenciar Clientes            (RF02)
 *   3. Gerenciar Categorias          (RF03)
 *   4. Gerenciar Veiculos            (RF04/RF05)
 *   5. Realizar Locacao              (RF08)
 */
public class Controladora {

    // ==================================================================
    // Constantes de validacao
    // ==================================================================

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern PLACA =
            Pattern.compile("^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$");

    private static final int IDADE_MINIMA = 18;
    private static final int ANO_MINIMO = 1900;
    private static final int KM_MAXIMO = 9_999_999;
    private static final BigDecimal VALOR_MAXIMO_DIARIA = new BigDecimal("99999.99");

    // ==================================================================
    // Camada de persistencia
    // ==================================================================

    private final UsuarioDAO usuarioDAO;
    private final ClienteDAO clienteDAO;
    private final CategoriaVeiculoDAO categoriaDAO;
    private final VeiculoDAO veiculoDAO;
    private final LocacaoDAO locacaoDAO;

    /**
     * Atendente autenticado na sessao. Modela a pre-condicao
     * "o atendente esta autenticado no sistema" dos contratos de operacao,
     * e por isso nao aparece como parametro de efetuarLocacao.
     */
    private Usuario atendenteLogado;

    public Controladora() {
        this.usuarioDAO = new UsuarioDAO();
        this.clienteDAO = new ClienteDAO();
        this.categoriaDAO = new CategoriaVeiculoDAO();
        this.veiculoDAO = new VeiculoDAO();
        this.locacaoDAO = new LocacaoDAO();
    }

    // ==================================================================
    // Sessao do atendente
    // ==================================================================

    public void autenticarAtendente(String matricula) throws ValidacaoException, DAOException {
        Usuario usuario = obrigarUsuarioExistir(matricula);
        if (!usuario.isAtivo()) {
            throw new ValidacaoException("Este usuario esta inativo e nao pode operar o sistema.");
        }
        this.atendenteLogado = usuario;
    }

    public Usuario getAtendenteLogado() {
        return atendenteLogado;
    }

    // ==================================================================
    // 1. GERENCIAR USUARIOS (RF01)
    // ==================================================================

    public void cadastrarUsuario(String matricula, String cpf, String nome, String login,
                                 String senha, String confirmacaoSenha,
                                 TipoUsuario tipo) throws ValidacaoException, DAOException {

        validarCamposObrigatoriosDoUsuario(matricula, cpf, nome, login, tipo);
        validarSenha(senha, confirmacaoSenha, true);

        String cpfDigitos = CpfUtil.limpar(cpf);

        Usuario existente = usuarioDAO.buscarPorMatricula(matricula.trim());
        if (existente != null && !existente.isAtivo()) {
            throw new ValidacaoException("Ja existe um usuario INATIVO com a matricula "
                    + matricula.trim() + ".\n\n"
                    + "Marque 'Mostrar inativos', selecione-o na lista e use Reativar.");
        }
        if (existente != null) {
            throw new ValidacaoException("Ja existe um usuario cadastrado com a matricula "
                    + matricula.trim() + ".");
        }
        if (usuarioDAO.loginEmUsoPorOutro(login.trim(), null)) {
            throw new ValidacaoException("O login informado ja esta em uso por outro usuario.");
        }
        if (usuarioDAO.cpfEmUsoPorOutro(cpfDigitos, null)) {
            throw new ValidacaoException("O CPF informado ja pertence a outro usuario.");
        }

        Usuario usuario = Usuario.criar(
                tipo,
                matricula.trim(),
                cpfDigitos,
                nome.trim(),
                login.trim(),
                SenhaUtil.gerarHash(senha));

        usuario.setDataUltimaTrocaSenha(LocalDate.now());
        usuarioDAO.inserir(usuario);
    }

    public void alterarUsuario(String matricula, String cpf, String nome, String login,
                               String senha, String confirmacaoSenha,
                               TipoUsuario tipo) throws ValidacaoException, DAOException {

        validarCamposObrigatoriosDoUsuario(matricula, cpf, nome, login, tipo);

        String cpfDigitos = CpfUtil.limpar(cpf);

        Usuario existente = usuarioDAO.buscarPorMatricula(matricula.trim());
        if (existente == null) {
            throw new ValidacaoException("Nenhum usuario encontrado com a matricula "
                    + matricula.trim() + ".");
        }
        if (usuarioDAO.loginEmUsoPorOutro(login.trim(), matricula.trim())) {
            throw new ValidacaoException("O login informado ja esta em uso por outro usuario.");
        }
        if (usuarioDAO.cpfEmUsoPorOutro(cpfDigitos, matricula.trim())) {
            throw new ValidacaoException("O CPF informado ja pertence a outro usuario.");
        }

        boolean alterarSenha = senha != null && !senha.isBlank();
        if (alterarSenha) {
            validarSenha(senha, confirmacaoSenha, true);
        }

        Usuario usuario = Usuario.criar(
                tipo,
                matricula.trim(),
                cpfDigitos,
                nome.trim(),
                login.trim(),
                alterarSenha ? SenhaUtil.gerarHash(senha) : existente.getSenha());

        usuario.setDataUltimaTrocaSenha(alterarSenha
                ? LocalDate.now()
                : existente.getDataUltimaTrocaSenha());

        usuarioDAO.atualizar(usuario, alterarSenha);
    }

    public void inativarUsuario(String matricula) throws ValidacaoException, DAOException {
        Usuario usuario = obrigarUsuarioExistir(matricula);

        if (!usuario.isAtivo()) {
            throw new ValidacaoException("Este usuario ja esta inativo.");
        }
        usuarioDAO.inativar(usuario.getMatricula());
    }

    public void reativarUsuario(String matricula) throws ValidacaoException, DAOException {
        Usuario usuario = obrigarUsuarioExistir(matricula);

        if (usuario.isAtivo()) {
            throw new ValidacaoException("Este usuario ja esta ativo.");
        }
        usuarioDAO.reativar(usuario.getMatricula());
    }

    public List<Usuario> listarUsuarios(boolean incluirInativos) throws DAOException {
        return usuarioDAO.listar(incluirInativos);
    }

    public Usuario buscarUsuario(String matricula) throws DAOException {
        return usuarioDAO.buscarPorMatricula(matricula);
    }

    public int getPrazoExpiracaoSenha() {
        return ParametrosSistema.getPrazoExpiracaoSenhaEmDias();
    }

    // ==================================================================
    // 2. GERENCIAR CLIENTES (RF02)
    // ==================================================================

    public void cadastrarCliente(String cpf, String nome, String cnh, String validadeCnh,
                                 String dataNascimento, String telefone, String email,
                                 String endereco) throws ValidacaoException, DAOException {

        Cliente cliente = montarEValidarCliente(cpf, nome, cnh, validadeCnh, dataNascimento,
                telefone, email, endereco);

        // O CPF e chave primaria: um registro inativo continua ocupando o
        // valor, por isso a mensagem distingue os dois casos.
        Cliente existente = clienteDAO.buscarPorCpf(cliente.getCpf());
        if (existente != null && !existente.isAtivo()) {
            throw new ValidacaoException("Ja existe um cliente INATIVO com o CPF "
                    + CpfUtil.formatar(cliente.getCpf()) + ".\n\n"
                    + "Marque 'Mostrar inativos', selecione-o na lista e use Reativar.");
        }
        if (existente != null) {
            throw new ValidacaoException("Ja existe um cliente cadastrado com o CPF "
                    + CpfUtil.formatar(cliente.getCpf()) + ".");
        }
        if (clienteDAO.cnhEmUsoPorOutro(cliente.getCnh(), null)) {
            throw new ValidacaoException("A CNH informada ja pertence a outro cliente.");
        }

        clienteDAO.inserir(cliente);
    }

    public void alterarCliente(String cpf, String nome, String cnh, String validadeCnh,
                               String dataNascimento, String telefone, String email,
                               String endereco) throws ValidacaoException, DAOException {

        Cliente cliente = montarEValidarCliente(cpf, nome, cnh, validadeCnh, dataNascimento,
                telefone, email, endereco);

        if (!clienteDAO.existeCpf(cliente.getCpf())) {
            throw new ValidacaoException("Nenhum cliente encontrado com o CPF "
                    + CpfUtil.formatar(cliente.getCpf()) + ".");
        }
        if (clienteDAO.cnhEmUsoPorOutro(cliente.getCnh(), cliente.getCpf())) {
            throw new ValidacaoException("A CNH informada ja pertence a outro cliente.");
        }

        clienteDAO.atualizar(cliente);
    }

    public void inativarCliente(String cpf) throws ValidacaoException, DAOException {
        Cliente cliente = obrigarClienteExistir(cpf);

        if (!cliente.isAtivo()) {
            throw new ValidacaoException("Este cliente ja esta inativo.");
        }
        clienteDAO.inativar(cliente.getCpf());
    }

    public void reativarCliente(String cpf) throws ValidacaoException, DAOException {
        Cliente cliente = obrigarClienteExistir(cpf);

        if (cliente.isAtivo()) {
            throw new ValidacaoException("Este cliente ja esta ativo.");
        }
        clienteDAO.reativar(cliente.getCpf());
    }

    public List<Cliente> listarClientes(boolean incluirInativos) throws DAOException {
        return clienteDAO.listar(incluirInativos);
    }

    public List<Cliente> buscarPorNome(String trechoDoNome, boolean incluirInativos)
            throws DAOException {

        if (trechoDoNome == null || trechoDoNome.isBlank()) {
            return clienteDAO.listar(incluirInativos);
        }
        return clienteDAO.buscarPorNome(trechoDoNome.trim(), incluirInativos);
    }

    public Cliente buscarCliente(String cpf) throws DAOException {
        return clienteDAO.buscarPorCpf(CpfUtil.limpar(cpf));
    }

    // ==================================================================
    // 3. GERENCIAR CATEGORIAS DE VEICULO (RF03)
    // ==================================================================

    public void cadastrarCategoria(String nome, String descricao, String valorBaseDiaria)
            throws ValidacaoException, DAOException {

        String nomeLimpo = limparNomeDaCategoria(nome);
        BigDecimal valor = converterValorDaDiaria(valorBaseDiaria, "valor base da diaria");
        validarDescricaoDaCategoria(descricao);

        // O nome e chave primaria: uma categoria inativa continua ocupando o
        // valor, por isso a mensagem distingue os dois casos.
        CategoriaVeiculo existente = categoriaDAO.buscarPorNome(nomeLimpo);
        if (existente != null && !existente.isAtivo()) {
            throw new ValidacaoException("Ja existe uma categoria INATIVA chamada '"
                    + nomeLimpo + "'.\n\n"
                    + "Marque 'Mostrar inativas', selecione-a na lista e use Reativar.");
        }
        if (existente != null) {
            throw new ValidacaoException("Ja existe uma categoria cadastrada com o nome '"
                    + nomeLimpo + "'.");
        }

        categoriaDAO.inserir(new CategoriaVeiculo(nomeLimpo, trim(descricao), valor));
    }

    public void alterarCategoria(String nome, String descricao, String valorBaseDiaria)
            throws ValidacaoException, DAOException {

        String nomeLimpo = limparNomeDaCategoria(nome);
        BigDecimal valor = converterValorDaDiaria(valorBaseDiaria, "valor base da diaria");
        validarDescricaoDaCategoria(descricao);

        CategoriaVeiculo existente = categoriaDAO.buscarPorNome(nomeLimpo);
        if (existente == null) {
            throw new ValidacaoException("Nenhuma categoria encontrada com o nome '"
                    + nomeLimpo + "'.");
        }

        categoriaDAO.atualizar(new CategoriaVeiculo(nomeLimpo, trim(descricao), valor));
    }

    public void inativarCategoria(String nome) throws ValidacaoException, DAOException {
        CategoriaVeiculo categoria = obrigarCategoriaExistir(nome);

        if (!categoria.isAtivo()) {
            throw new ValidacaoException("Esta categoria ja esta inativa.");
        }
        categoriaDAO.inativar(categoria.getNome());
    }

    public void reativarCategoria(String nome) throws ValidacaoException, DAOException {
        CategoriaVeiculo categoria = obrigarCategoriaExistir(nome);

        if (categoria.isAtivo()) {
            throw new ValidacaoException("Esta categoria ja esta ativa.");
        }
        categoriaDAO.reativar(categoria.getNome());
    }

    public List<CategoriaVeiculo> listarCategorias(boolean incluirInativas) throws DAOException {
        return categoriaDAO.listar(incluirInativas);
    }

    public List<CategoriaVeiculo> listarCategoriasAtivas() throws DAOException {
        return categoriaDAO.listar(false);
    }

    public CategoriaVeiculo buscarCategoria(String nome) throws DAOException {
        return categoriaDAO.buscarPorNome(nome == null ? "" : nome.trim());
    }

    public int contarVeiculosNaCategoria(String nome) throws DAOException {
        return categoriaDAO.contarVeiculosNaCategoria(nome);
    }

    // ==================================================================
    // 4. GERENCIAR VEICULOS (RF04 / RF05)
    // ==================================================================

    public void cadastrarVeiculo(String placa, String renavam, String modelo, String marca,
                                 String anoFabricacao, String kmAtual, String valorDiaria,
                                 StatusVeiculo status, String nomeCategoria)
            throws ValidacaoException, DAOException {

        Veiculo veiculo = montarEValidarVeiculo(placa, renavam, modelo, marca, anoFabricacao,
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

        Veiculo veiculo = montarEValidarVeiculo(placa, renavam, modelo, marca, anoFabricacao,
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

    public List<Veiculo> consultarVeiculos(String marca, String modelo, String categoria,
                                           StatusVeiculo status) throws DAOException {
        return veiculoDAO.consultar(marca, modelo, categoria, status);
    }

    public Veiculo buscarVeiculo(String placa) throws DAOException {
        return veiculoDAO.buscarPorPlaca(normalizarPlaca(placa));
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

    // ==================================================================
    // 5. REALIZAR LOCACAO (RF08)
    //
    // Os metodos publicos desta secao correspondem, um a um, aos eventos
    // de sistema do DSS de Realizar Locacao.
    // ==================================================================

    /**
     * DSS mensagem 2: dadosCliente = validaCNH(CNH)
     *
     * Localiza o cliente pela CNH e verifica a RN01. Retorna os dados do
     * cliente para a tela exibir; lanca ValidacaoException quando a CNH nao
     * existe, o cliente esta inativo ou a habilitacao esta vencida.
     */
    public Cliente validaCNH(String cnh) throws ValidacaoException, DAOException {
        String cnhDigitos = cnh == null ? "" : cnh.replaceAll("\\D", "");

        if (cnhDigitos.isEmpty()) {
            throw new ValidacaoException("Informe o numero da CNH do cliente.");
        }
        if (cnhDigitos.length() != 11) {
            throw new ValidacaoException("O numero da CNH deve ter 11 digitos.");
        }

        Cliente cliente = clienteDAO.buscarPorCnh(cnhDigitos);
        if (cliente == null) {
            throw new ValidacaoException("Nenhum cliente cadastrado com a CNH informada.\n\n"
                    + "Cadastre o cliente antes de realizar a locacao.");
        }
        if (!cliente.isAtivo()) {
            throw new ValidacaoException("O cliente " + cliente.getNome()
                    + " esta inativo no sistema.");
        }

        // RN01 - CNH valida e nao vencida.
        if (!cliente.possuiCnhValida()) {
            throw new ValidacaoException("CNH invalida, locacao nao autorizada.\n\n"
                    + "A habilitacao de " + cliente.getNome() + " venceu em "
                    + cliente.getValidadeCnh().format(FORMATO_DATA) + ".");
        }

        return cliente;
    }

    /**
     * DSS mensagem 3: listaVeiculos = consultarVeiculosDisponiveis()
     */
    public List<Veiculo> consultarVeiculosDisponiveis() throws DAOException {
        return veiculoDAO.listarDisponiveis();
    }

    /**
     * DSS mensagem 5: dadosVeiculo = buscarDadosVeiculo(placa)
     *
     * Verifica a RN02 e devolve o veiculo para a tela exibir o valor da diaria.
     */
    public Veiculo buscarDadosVeiculo(String placa) throws ValidacaoException, DAOException {
        String placaLimpa = normalizarPlaca(placa);

        if (placaLimpa.isEmpty()) {
            throw new ValidacaoException("Selecione o veiculo da locacao.");
        }

        Veiculo veiculo = veiculoDAO.buscarPorPlaca(placaLimpa);
        if (veiculo == null) {
            throw new ValidacaoException("Nenhum veiculo encontrado com a placa " + placaLimpa + ".");
        }

        // RN02 - o veiculo precisa estar disponivel.
        if (!veiculo.estaDisponivel()) {
            throw new ValidacaoException("O veiculo " + veiculo.getPlaca()
                    + " nao esta disponivel (situacao atual: "
                    + veiculo.getStatus().getDescricao() + ").");
        }

        return veiculo;
    }

    /**
     * Consulta auxiliar da tela: valor estimado do periodo, com a RN05 aplicada.
     * O calculo em si pertence a Locacao (Information Expert); aqui apenas se
     * monta uma locacao transitoria, que nao e gravada.
     */
    public BigDecimal calcularValorEstimado(Veiculo veiculo, LocalDate dataDevolucaoPrevista) {
        if (veiculo == null || dataDevolucaoPrevista == null) {
            return BigDecimal.ZERO;
        }
        Locacao simulada = new Locacao();
        simulada.setVeiculo(veiculo);
        simulada.setDataDevolucaoPrevista(dataDevolucaoPrevista);

        return simulada.calcularValorEstimado();
    }

    /**
     * DSS mensagem 9 (operacao mais complexa do DSS):
     * efetuarLocacao(CNH, placa, endereco, dataDevolucaoPrevista, valorCaucao)
     *
     * O atendente nao e parametro: ele vem da sessao, conforme a pre-condicao
     * "o atendente esta autenticado no sistema" do contrato de operacao.
     */
    public Locacao efetuarLocacao(String cnh, String placa, String endereco,
                                  LocalDate dataDevolucaoPrevista, BigDecimal valorCaucao)
            throws ValidacaoException, DAOException {

        if (atendenteLogado == null) {
            throw new ValidacaoException("Nenhum atendente autenticado na sessao.");
        }

        // Revalida as duas regras no momento da confirmacao: entre a consulta
        // e o "confirmar" outro atendente pode ter locado o mesmo veiculo.
        Cliente cliente = validaCNH(cnh);
        Veiculo veiculo = buscarDadosVeiculo(placa);

        if (dataDevolucaoPrevista == null) {
            throw new ValidacaoException("A data de devolucao prevista e obrigatoria.");
        }
        if (!dataDevolucaoPrevista.isAfter(LocalDate.now())) {
            throw new ValidacaoException("A data de devolucao prevista deve ser posterior a hoje.");
        }

        if (valorCaucao == null) {
            throw new ValidacaoException("Informe o valor do caucao.");
        }

        // RN03 - garantia minima exigida.
        BigDecimal caucaoMinima = ParametrosSistema.getCaucaoMinima();
        if (valorCaucao.compareTo(caucaoMinima) < 0) {
            throw new ValidacaoException("O caucao informado ("
                    + MoedaUtil.formatar(valorCaucao) + ") e menor que a garantia minima\n"
                    + "exigida pela locadora (" + MoedaUtil.formatar(caucaoMinima) + ").");
        }

        // Pos-condicao do contrato: o endereco informado no atendimento
        // atualiza o cadastro do cliente.
        String enderecoInformado = trim(endereco);
        if (enderecoInformado == null || enderecoInformado.isEmpty()) {
            throw new ValidacaoException("Informe o endereco do cliente para o contrato.");
        }
        if (!enderecoInformado.equals(cliente.getEndereco())) {
            cliente.setEndereco(enderecoInformado);
            clienteDAO.atualizar(cliente);
        }

        Locacao locacao = new Locacao();
        locacao.setDataDevolucaoPrevista(dataDevolucaoPrevista);
        locacao.setKmInicial(veiculo.getKmAtual());
        locacao.setValorCaucao(valorCaucao);
        locacao.setStatus(StatusLocacao.ATIVA);
        locacao.setCliente(cliente);
        locacao.setVeiculo(veiculo);
        locacao.setAtendente(atendenteLogado);

        // RN05 - quem calcula e a propria locacao.
        locacao.setValorEstimado(locacao.calcularValorEstimado());

        // Transacao: grava a locacao e passa o veiculo para LOCADO.
        locacaoDAO.inserir(locacao);
        return locacao;
    }

    /**
     * Fluxo alternativo do DSS: cancelarLocacao().
     * Encerra a locacao e devolve o veiculo para DISPONIVEL.
     */
    public void cancelarLocacao(int idLocacao, String placaVeiculo) throws DAOException {
        locacaoDAO.cancelar(idLocacao, placaVeiculo);
    }

    public List<Locacao> listarLocacoes() throws DAOException {
        return locacaoDAO.listarTodas();
    }

    public List<Locacao> listarLocacoesPorStatus(StatusLocacao status) throws DAOException {
        return locacaoDAO.listarPorStatus(status);
    }

    public List<Usuario> listarAtendentesAtivos() throws DAOException {
        return usuarioDAO.listar(false);
    }

    /** RN03 - garantia minima, para a tela sugerir o valor e avisar o atendente. */
    public BigDecimal getCaucaoMinima() {
        return ParametrosSistema.getCaucaoMinima();
    }

    // ==================================================================
    // 6. REGISTRAR DEVOLUCAO (RF09)
    //
    // Os metodos publicos desta secao correspondem, um a um, aos eventos
    // de sistema do DSS de Registrar Devolucao.
    // ==================================================================

    /** Locacao que esta sendo devolvida no atendimento em andamento. */
    private Locacao locacaoEmDevolucao;

    /**
     * DSS mensagem 2: dadosLocacao = buscaLocacaoAtiva(placa)
     *
     * Localiza a locacao ATIVA do veiculo. Corresponde ao fragmento opt
     * "[dadosLocacao == null]" do DSS.
     */
    public Locacao buscaLocacaoAtiva(String placa) throws ValidacaoException, DAOException {
        String placaLimpa = normalizarPlaca(placa);

        if (placaLimpa.isEmpty()) {
            throw new ValidacaoException("Informe a placa do veiculo devolvido.");
        }

        Locacao locacao = locacaoDAO.buscaLocacaoAtiva(placaLimpa);
        if (locacao == null) {
            throw new ValidacaoException("Nenhuma locacao ativa encontrada para esse veiculo.");
        }

        this.locacaoEmDevolucao = locacao;
        return locacao;
    }

    /**
     * DSS mensagem 5: valores = calculaValorFinal(placa, kmFinal, custoAvarias)
     *
     * Calcula as parcelas da devolucao sem encerrar nada, para o atendente
     * conferir antes de confirmar.
     */
    public ValoresDaDevolucao calculaValorFinal(String placa, int kmFinal,
                                                BigDecimal custoAvarias)
            throws ValidacaoException, DAOException {

        Locacao locacao = obrigarLocacaoAtiva(placa);
        validarDadosDaDevolucao(locacao, kmFinal, custoAvarias);

        return locacao.calcularValoresDaDevolucao(kmFinal, custoAvarias);
    }

    /**
     * DSS mensagem 7: cancelarDevolucao()
     * Abandona o atendimento em andamento. Nada foi gravado ate aqui.
     */
    public void cancelarDevolucao() {
        this.locacaoEmDevolucao = null;
    }

    /**
     * DSS mensagem 9: registrarDevolucao(placa, kmFinal, custoAvarias, descricao)
     *
     * Operacao mais complexa do DSS. Cumpre as pos-condicoes do contrato:
     * encerra a locacao, calcula o valor final e devolve o veiculo ao patio
     * na situacao correta - tudo numa unica transacao.
     */
    public Locacao registrarDevolucao(String placa, int kmFinal,
                                      BigDecimal custoAvarias, String descricao)
            throws ValidacaoException, DAOException {

        if (atendenteLogado == null) {
            throw new ValidacaoException("Nenhum atendente autenticado na sessao.");
        }

        Locacao locacao = obrigarLocacaoAtiva(placa);
        validarDadosDaDevolucao(locacao, kmFinal, custoAvarias);

        // RN02 - a pre-condicao exige o veiculo LOCADO.
        if (locacao.getVeiculo().getStatus() != StatusVeiculo.LOCADO) {
            throw new ValidacaoException("O veiculo " + locacao.getVeiculo().getPlaca()
                    + " nao consta como LOCADO (situacao atual: "
                    + locacao.getVeiculo().getStatus().getDescricao() + ").");
        }

        locacao.registrarDevolucao(kmFinal, custoAvarias, trim(descricao));
        locacaoDAO.registrarDevolucao(locacao);

        this.locacaoEmDevolucao = null;
        return locacao;
    }

    private Locacao obrigarLocacaoAtiva(String placa) throws ValidacaoException, DAOException {
        String placaLimpa = normalizarPlaca(placa);

        if (locacaoEmDevolucao != null
                && locacaoEmDevolucao.getVeiculo().getPlaca().equals(placaLimpa)) {
            return locacaoEmDevolucao;
        }
        return buscaLocacaoAtiva(placa);
    }

    /**
     * Validacoes comuns ao calculo e ao registro. Cobre a pre-condicao
     * "kmFinal maior ou igual ao KmInicial" e o fragmento opt do DSS
     * "[KmFinal < KmInicial]".
     */
    private void validarDadosDaDevolucao(Locacao locacao, int kmFinal, BigDecimal custoAvarias)
            throws ValidacaoException {

        if (kmFinal < locacao.getKmInicial()) {
            throw new ValidacaoException("Km final nao pode ser menor que o Km inicial.\n\n"
                    + "Km na retirada: " + locacao.getKmInicial() + " km.");
        }
        if (custoAvarias != null && custoAvarias.signum() < 0) {
            throw new ValidacaoException("O custo das avarias nao pode ser negativo.");
        }
    }

    public List<Locacao> listarLocacoesAtivas() throws DAOException {
        return locacaoDAO.listarPorStatus(StatusLocacao.ATIVA);
    }

    // ==================================================================
    // Validacoes internas - Usuario
    // ==================================================================

    private Usuario obrigarUsuarioExistir(String matricula)
            throws ValidacaoException, DAOException {

        if (matricula == null || matricula.isBlank()) {
            throw new ValidacaoException("Selecione um usuario na lista.");
        }
        Usuario usuario = usuarioDAO.buscarPorMatricula(matricula.trim());
        if (usuario == null) {
            throw new ValidacaoException("Nenhum usuario encontrado com a matricula "
                    + matricula.trim() + ".");
        }
        return usuario;
    }

    private void validarCamposObrigatoriosDoUsuario(String matricula, String cpf, String nome,
                                                    String login, TipoUsuario tipo)
            throws ValidacaoException {

        if (matricula == null || matricula.isBlank()) {
            throw new ValidacaoException("Informe a matricula do usuario.");
        }
        if (matricula.trim().length() > 20) {
            throw new ValidacaoException("A matricula deve ter no maximo 20 caracteres.");
        }

        String cpfDigitos = CpfUtil.limpar(cpf);
        if (cpfDigitos.isEmpty()) {
            throw new ValidacaoException("Informe o CPF do usuario.");
        }
        if (!CpfUtil.ehValido(cpfDigitos)) {
            throw new ValidacaoException("O CPF informado e invalido.\n"
                    + "Confira os digitos e tente novamente.");
        }

        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Informe o nome do usuario.");
        }
        if (nome.trim().length() < 3) {
            throw new ValidacaoException("O nome deve ter ao menos 3 caracteres.");
        }
        if (login == null || login.isBlank()) {
            throw new ValidacaoException("Informe o login do usuario.");
        }
        if (login.trim().length() < 4) {
            throw new ValidacaoException("O login deve ter ao menos 4 caracteres.");
        }
        if (login.trim().contains(" ")) {
            throw new ValidacaoException("O login nao pode conter espacos.");
        }
        if (tipo == null) {
            throw new ValidacaoException("Selecione o tipo do usuario.");
        }
    }

    private void validarSenha(String senha, String confirmacao, boolean obrigatoria)
            throws ValidacaoException {

        if (senha == null || senha.isBlank()) {
            if (obrigatoria) {
                throw new ValidacaoException("Informe a senha do usuario.");
            }
            return;
        }
        if (senha.length() < 6) {
            throw new ValidacaoException("A senha deve ter ao menos 6 caracteres.");
        }
        if (!senha.equals(confirmacao)) {
            throw new ValidacaoException("A senha e a confirmacao nao coincidem.");
        }
    }

    // ==================================================================
    // Validacoes internas - Cliente
    // ==================================================================

    private Cliente obrigarClienteExistir(String cpf) throws ValidacaoException, DAOException {
        String digitos = CpfUtil.limpar(cpf);

        if (digitos.isEmpty()) {
            throw new ValidacaoException("Selecione um cliente na lista.");
        }
        Cliente cliente = clienteDAO.buscarPorCpf(digitos);
        if (cliente == null) {
            throw new ValidacaoException("Nenhum cliente encontrado com esse CPF.");
        }
        return cliente;
    }

    private Cliente montarEValidarCliente(String cpf, String nome, String cnh, String validadeCnh,
                                          String dataNascimento, String telefone, String email,
                                          String endereco) throws ValidacaoException {

        String cpfDigitos = CpfUtil.limpar(cpf);
        if (cpfDigitos.isEmpty()) {
            throw new ValidacaoException("Informe o CPF do cliente.");
        }
        if (!CpfUtil.ehValido(cpfDigitos)) {
            throw new ValidacaoException("O CPF informado e invalido.\n"
                    + "Confira os digitos e tente novamente.");
        }

        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Informe o nome do cliente.");
        }
        if (nome.trim().length() < 3) {
            throw new ValidacaoException("O nome deve ter ao menos 3 caracteres.");
        }

        String cnhDigitos = cnh == null ? "" : cnh.replaceAll("\\D", "");
        if (cnhDigitos.isEmpty()) {
            throw new ValidacaoException("Informe o numero da CNH.");
        }
        if (cnhDigitos.length() != 11) {
            throw new ValidacaoException("O numero da CNH deve ter 11 digitos.");
        }

        LocalDate validade = converterData(validadeCnh, "validade da CNH");
        LocalDate nascimento = converterData(dataNascimento, "data de nascimento");

        if (nascimento.isAfter(LocalDate.now())) {
            throw new ValidacaoException("A data de nascimento nao pode estar no futuro.");
        }

        Cliente cliente = new Cliente(cpfDigitos, nome.trim(), cnhDigitos, validade,
                nascimento, trim(telefone), trim(email), trim(endereco));

        if (cliente.getIdade() < IDADE_MINIMA) {
            throw new ValidacaoException("O cliente deve ter ao menos " + IDADE_MINIMA
                    + " anos para ser cadastrado.");
        }

        // RN01 - o sistema nao aceita cliente com habilitacao vencida.
        if (!cliente.possuiCnhValida()) {
            throw new ValidacaoException("A CNH esta vencida (validade em "
                    + validade.format(FORMATO_DATA) + ").\n"
                    + "O cadastro so pode ser concluido com a habilitacao em dia.");
        }

        if (email != null && !email.isBlank() && !email.contains("@")) {
            throw new ValidacaoException("O e-mail informado e invalido.");
        }

        return cliente;
    }

    // ==================================================================
    // Validacoes internas - Categoria
    // ==================================================================

    private CategoriaVeiculo obrigarCategoriaExistir(String nome)
            throws ValidacaoException, DAOException {

        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Selecione uma categoria na lista.");
        }
        CategoriaVeiculo categoria = categoriaDAO.buscarPorNome(nome.trim());
        if (categoria == null) {
            throw new ValidacaoException("Nenhuma categoria encontrada com o nome '"
                    + nome.trim() + "'.");
        }
        return categoria;
    }

    private String limparNomeDaCategoria(String nome) throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Informe o nome da categoria.");
        }
        String limpo = nome.trim();
        if (limpo.length() < 3) {
            throw new ValidacaoException("O nome da categoria deve ter ao menos 3 caracteres.");
        }
        if (limpo.length() > 50) {
            throw new ValidacaoException("O nome da categoria deve ter no maximo 50 caracteres.");
        }
        return limpo;
    }

    private void validarDescricaoDaCategoria(String descricao) throws ValidacaoException {
        if (descricao != null && descricao.trim().length() > 200) {
            throw new ValidacaoException("A descricao deve ter no maximo 200 caracteres.");
        }
    }

    // ==================================================================
    // Validacoes internas - Veiculo
    // ==================================================================

    private Veiculo montarEValidarVeiculo(String placa, String renavam, String modelo, String marca,
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

        BigDecimal valor = converterValorDaDiaria(valorDiaria, "valor da diaria");

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

    private String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    // ==================================================================
    // Conversoes de texto compartilhadas
    // ==================================================================

    private LocalDate converterData(String texto, String nomeDoCampo) throws ValidacaoException {
        // Campos com mascara chegam como "  /  /    " quando vazios, por isso
        // a verificacao e por ausencia de digitos, e nao por isBlank().
        if (texto == null || texto.replaceAll("\\D", "").isEmpty()) {
            throw new ValidacaoException("Informe a " + nomeDoCampo + ".");
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("A " + nomeDoCampo
                    + " deve estar no formato dd/mm/aaaa.");
        }
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

    private BigDecimal converterValorDaDiaria(String texto, String nomeDoCampo)
            throws ValidacaoException {

        BigDecimal valor;
        try {
            valor = MoedaUtil.converter(texto);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Informe o " + nomeDoCampo + ".\n"
                    + "Use o formato 240,00.");
        }

        if (valor.signum() <= 0) {
            throw new ValidacaoException("O " + nomeDoCampo + " deve ser maior que zero.");
        }
        if (valor.compareTo(VALOR_MAXIMO_DIARIA) > 0) {
            throw new ValidacaoException("O " + nomeDoCampo + " parece alto demais.\n"
                    + "Limite aceito: " + MoedaUtil.formatar(VALOR_MAXIMO_DIARIA) + ".");
        }
        return valor;
    }

    /**
     * Converte um valor monetario digitado na tela (formato brasileiro).
     * Usado pela tela de locacao, que recebe o caucao como texto.
     */
    public BigDecimal converterValorMonetario(String texto, String nomeDoCampo)
            throws ValidacaoException {

        try {
            return MoedaUtil.converter(texto);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Informe o " + nomeDoCampo + ".\n"
                    + "Use o formato 500,00.");
        }
    }

    /**
     * Converte uma data digitada na tela (dd/mm/aaaa).
     * Usado pela tela de locacao, que recebe a data como texto mascarado.
     */
    public LocalDate converterDataDoFormulario(String texto, String nomeDoCampo)
            throws ValidacaoException {

        return converterData(texto, nomeDoCampo);
    }

    private String trim(String valor) {
        return valor == null ? null : valor.trim();
    }
}
