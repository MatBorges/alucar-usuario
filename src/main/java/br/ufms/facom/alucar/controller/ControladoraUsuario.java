package br.ufms.facom.alucar.controller;

import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.dao.UsuarioDAO;
import br.ufms.facom.alucar.model.TipoUsuario;
import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.util.CpfUtil;
import br.ufms.facom.alucar.util.ParametrosSistema;
import br.ufms.facom.alucar.util.SenhaUtil;

import java.time.LocalDate;
import java.util.List;


public class ControladoraUsuario {

    private final UsuarioDAO usuarioDAO;

    public ControladoraUsuario() {
        this.usuarioDAO = new UsuarioDAO();
    }

//    Senhas são armazenadas em hash
    public void cadastrarUsuario(String matricula, String cpf, String nome, String login,
                                 String senha, String confirmacaoSenha,
                                 TipoUsuario tipo) throws ValidacaoException, DAOException {

        validarCamposObrigatorios(matricula, cpf, nome, login, tipo);
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

        validarCamposObrigatorios(matricula, cpf, nome, login, tipo);

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
        Usuario usuario = obrigarExistir(matricula);

        if (!usuario.isAtivo()) {
            throw new ValidacaoException("Este usuario ja esta inativo.");
        }
        usuarioDAO.inativar(usuario.getMatricula());
    }

    public void reativarUsuario(String matricula) throws ValidacaoException, DAOException {
        Usuario usuario = obrigarExistir(matricula);

        if (usuario.isAtivo()) {
            throw new ValidacaoException("Este usuario ja esta ativo.");
        }
        usuarioDAO.reativar(usuario.getMatricula());
    }

    private Usuario obrigarExistir(String matricula) throws ValidacaoException, DAOException {
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

    public List<Usuario> listarUsuarios(boolean incluirInativos) throws DAOException {
        return usuarioDAO.listar(incluirInativos);
    }

    public Usuario buscarUsuario(String matricula) throws DAOException {
        return usuarioDAO.buscarPorMatricula(matricula);
    }


    public int getPrazoExpiracaoSenha() {
        return ParametrosSistema.getPrazoExpiracaoSenhaEmDias();
    }

    private void validarCamposObrigatorios(String matricula, String cpf, String nome,
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
}
