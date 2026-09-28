package br.ufms.facom.alucar.controller;

import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.dao.UsuarioDAO;
import br.ufms.facom.alucar.model.TipoUsuario;
import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.util.SenhaUtil;

import java.util.List;

/**
 * Controladora do caso de uso "Manter Usuarios" (RF04).
 *
 * Cumpre o papel de Controller do GRASP: recebe os eventos disparados pela
 * tela, valida as regras, e delega a persistencia ao DAO. A tela nunca
 * conversa diretamente com o DAO nem com o banco.
 */
public class ControladoraUsuario {

    private final UsuarioDAO usuarioDAO;

    public ControladoraUsuario() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Cadastra um novo usuario. A senha chega em texto puro e e convertida em
     * hash antes de seguir para o DAO.
     */
    public void cadastrarUsuario(String matricula, String nome, String login,
                                 String senha, String confirmacaoSenha,
                                 TipoUsuario tipo) throws ValidacaoException, DAOException {

        validarCamposObrigatorios(matricula, nome, login, tipo);
        validarSenha(senha, confirmacaoSenha, true);

        // A matricula e chave primaria: um registro inativo continua ocupando
        // o valor, por isso a mensagem distingue os dois casos e aponta o
        // caminho da reativacao.
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

        Usuario usuario = Usuario.criar(
                tipo,
                matricula.trim(),
                nome.trim(),
                login.trim(),
                SenhaUtil.gerarHash(senha));

        usuarioDAO.inserir(usuario);
    }

    /**
     * Altera um usuario existente. Senha em branco significa "manter a senha
     * atual", por isso a validacao dela e condicional.
     */
    public void alterarUsuario(String matricula, String nome, String login,
                               String senha, String confirmacaoSenha,
                               TipoUsuario tipo) throws ValidacaoException, DAOException {

        validarCamposObrigatorios(matricula, nome, login, tipo);

        Usuario existente = usuarioDAO.buscarPorMatricula(matricula.trim());
        if (existente == null) {
            throw new ValidacaoException("Nenhum usuario encontrado com a matricula "
                    + matricula.trim() + ".");
        }
        if (usuarioDAO.loginEmUsoPorOutro(login.trim(), matricula.trim())) {
            throw new ValidacaoException("O login informado ja esta em uso por outro usuario.");
        }

        boolean alterarSenha = senha != null && !senha.isBlank();
        if (alterarSenha) {
            validarSenha(senha, confirmacaoSenha, true);
        }

        Usuario usuario = Usuario.criar(
                tipo,
                matricula.trim(),
                nome.trim(),
                login.trim(),
                alterarSenha ? SenhaUtil.gerarHash(senha) : existente.getSenha());

        usuarioDAO.atualizar(usuario, alterarSenha);
    }

    /**
     * Exclusao logica. O usuario deixa de aparecer nas listagens e nao
     * autentica mais, mas a linha permanece no banco: as locacoes que ele
     * realizou continuam apontando para ela.
     */
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

    private void validarCamposObrigatorios(String matricula, String nome,
                                           String login, TipoUsuario tipo)
            throws ValidacaoException {

        if (matricula == null || matricula.isBlank()) {
            throw new ValidacaoException("Informe a matricula do usuario.");
        }
        if (matricula.trim().length() > 20) {
            throw new ValidacaoException("A matricula deve ter no maximo 20 caracteres.");
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
