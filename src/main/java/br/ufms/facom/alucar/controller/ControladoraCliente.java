package br.ufms.facom.alucar.controller;

import br.ufms.facom.alucar.dao.ClienteDAO;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.Cliente;
import br.ufms.facom.alucar.util.CpfUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


public class ControladoraCliente {

    private static final DateTimeFormatter FORMATO_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int IDADE_MINIMA = 18;

    private final ClienteDAO clienteDAO;

    public ControladoraCliente() {
        this.clienteDAO = new ClienteDAO();
    }

    public void cadastrarCliente(String cpf, String nome, String cnh, String validadeCnh,
                                 String dataNascimento, String telefone, String email,
                                 String endereco) throws ValidacaoException, DAOException {

        Cliente cliente = montarEValidar(cpf, nome, cnh, validadeCnh, dataNascimento,
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

        Cliente cliente = montarEValidar(cpf, nome, cnh, validadeCnh, dataNascimento,
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
        Cliente cliente = obrigarExistir(cpf);

        if (!cliente.isAtivo()) {
            throw new ValidacaoException("Este cliente ja esta inativo.");
        }
        clienteDAO.inativar(cliente.getCpf());
    }

    public void reativarCliente(String cpf) throws ValidacaoException, DAOException {
        Cliente cliente = obrigarExistir(cpf);

        if (cliente.isAtivo()) {
            throw new ValidacaoException("Este cliente ja esta ativo.");
        }
        clienteDAO.reativar(cliente.getCpf());
    }

    private Cliente obrigarExistir(String cpf) throws ValidacaoException, DAOException {
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


    private Cliente montarEValidar(String cpf, String nome, String cnh, String validadeCnh,
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
                    + validade.format(FORMATO_BR) + ").\n"
                    + "O cadastro so pode ser concluido com a habilitacao em dia.");
        }

        if (email != null && !email.isBlank() && !email.contains("@")) {
            throw new ValidacaoException("O e-mail informado e invalido.");
        }

        return cliente;
    }

    private LocalDate converterData(String texto, String nomeDoCampo)
            throws ValidacaoException {

        // Campos com mascara chegam como "  /  /    " quando vazios, por isso
        // a verificacao e por ausencia de digitos, e nao por isBlank().
        if (texto == null || texto.replaceAll("\\D", "").isEmpty()) {
            throw new ValidacaoException("Informe a " + nomeDoCampo + ".");
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_BR);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("A " + nomeDoCampo
                    + " deve estar no formato dd/mm/aaaa.");
        }
    }

    private String trim(String valor) {
        return valor == null ? null : valor.trim();
    }
}
