package br.ufms.facom.alucar.dao;

import br.ufms.facom.alucar.model.TipoUsuario;
import br.ufms.facom.alucar.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Realiza a persistencia da hierarquia de usuarios na tabela unica 'usuario'.
 *
 * E aqui que o mapeamento objeto-relacional acontece de fato: ao gravar, o
 * discriminador vem de usuario.getTipoUsuario(); ao ler, a coluna
 * tipo_usuario decide qual subclasse sera instanciada.
 */
public class UsuarioDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO usuario (matricula, nome, login, senha, tipo_usuario) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE usuario SET nome = ?, login = ?, senha = ?, tipo_usuario = ? WHERE matricula = ?";

    private static final String SQL_ATUALIZAR_SEM_SENHA =
            "UPDATE usuario SET nome = ?, login = ?, tipo_usuario = ? WHERE matricula = ?";

    private static final String SQL_EXCLUIR =
            "DELETE FROM usuario WHERE matricula = ?";

    private static final String SQL_LISTAR =
            "SELECT matricula, nome, login, senha, tipo_usuario FROM usuario ORDER BY nome";

    private static final String SQL_BUSCAR_POR_MATRICULA =
            "SELECT matricula, nome, login, senha, tipo_usuario FROM usuario WHERE matricula = ?";

    private static final String SQL_CONTAR_POR_MATRICULA =
            "SELECT COUNT(*) FROM usuario WHERE matricula = ?";

    private static final String SQL_CONTAR_POR_LOGIN =
            "SELECT COUNT(*) FROM usuario WHERE login = ? AND matricula <> ?";

    public void inserir(Usuario usuario) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {

            comando.setString(1, usuario.getMatricula());
            comando.setString(2, usuario.getNome());
            comando.setString(3, usuario.getLogin());
            comando.setString(4, usuario.getSenha());
            comando.setString(5, usuario.getTipoUsuario().name());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao inserir o usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza o usuario. Quando novaSenhaHash e nulo, a senha atual e
     * preservada - o atendente nao precisa redigitar a senha para corrigir
     * apenas o nome, por exemplo.
     */
    public void atualizar(Usuario usuario, boolean alterarSenha) throws DAOException {
        String sql = alterarSenha ? SQL_ATUALIZAR : SQL_ATUALIZAR_SEM_SENHA;

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getLogin());
            if (alterarSenha) {
                comando.setString(3, usuario.getSenha());
                comando.setString(4, usuario.getTipoUsuario().name());
                comando.setString(5, usuario.getMatricula());
            } else {
                comando.setString(3, usuario.getTipoUsuario().name());
                comando.setString(4, usuario.getMatricula());
            }
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao atualizar o usuario: " + e.getMessage(), e);
        }
    }

    public void excluir(String matricula) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_EXCLUIR)) {

            comando.setString(1, matricula);
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao excluir o usuario: " + e.getMessage(), e);
        }
    }

    public List<Usuario> listarTodos() throws DAOException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(montarUsuario(resultado));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar os usuarios: " + e.getMessage(), e);
        }
        return usuarios;
    }

    public Usuario buscarPorMatricula(String matricula) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_MATRICULA)) {

            comando.setString(1, matricula);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarUsuario(resultado) : null;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar o usuario: " + e.getMessage(), e);
        }
    }

    public boolean existeMatricula(String matricula) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_POR_MATRICULA)) {

            comando.setString(1, matricula);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao verificar a matricula: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se o login ja pertence a outro usuario, ignorando o proprio
     * registro em edicao.
     */
    public boolean loginEmUsoPorOutro(String login, String matriculaAtual) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_POR_LOGIN)) {

            comando.setString(1, login);
            comando.setString(2, matriculaAtual == null ? "" : matriculaAtual);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao verificar o login: " + e.getMessage(), e);
        }
    }

    /**
     * Converte uma linha da tabela no objeto da subclasse correta.
     * Este metodo e o coracao do mapeamento da heranca por tabela unica.
     */
    private Usuario montarUsuario(ResultSet resultado) throws SQLException {
        TipoUsuario tipo = TipoUsuario.valueOf(resultado.getString("tipo_usuario"));
        return Usuario.criar(
                tipo,
                resultado.getString("matricula"),
                resultado.getString("nome"),
                resultado.getString("login"),
                resultado.getString("senha"));
    }
}
