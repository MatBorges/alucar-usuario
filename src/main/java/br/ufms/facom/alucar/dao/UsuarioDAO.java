package br.ufms.facom.alucar.dao;

import br.ufms.facom.alucar.model.TipoUsuario;
import br.ufms.facom.alucar.model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDAO {

    private static final String COLUNAS =
            "matricula, cpf, nome, login, senha, tipo_usuario, data_ultima_troca_senha, ativo";

    private static final String SQL_INSERIR =
            "INSERT INTO usuario (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE usuario SET cpf = ?, nome = ?, login = ?, senha = ?, tipo_usuario = ?, "
            + "data_ultima_troca_senha = ? WHERE matricula = ?";

    private static final String SQL_ATUALIZAR_SEM_SENHA =
            "UPDATE usuario SET cpf = ?, nome = ?, login = ?, tipo_usuario = ? "
            + "WHERE matricula = ?";

    private static final String SQL_ALTERAR_SITUACAO =
            "UPDATE usuario SET ativo = ? WHERE matricula = ?";

    private static final String SQL_LISTAR_ATIVOS =
            "SELECT " + COLUNAS + " FROM usuario WHERE ativo = TRUE ORDER BY nome";

    private static final String SQL_LISTAR_TODOS =
            "SELECT " + COLUNAS + " FROM usuario ORDER BY ativo DESC, nome";

    private static final String SQL_BUSCAR_POR_MATRICULA =
            "SELECT " + COLUNAS + " FROM usuario WHERE matricula = ?";

    private static final String SQL_CONTAR_POR_MATRICULA =
            "SELECT COUNT(*) FROM usuario WHERE matricula = ?";

    private static final String SQL_CONTAR_POR_LOGIN =
            "SELECT COUNT(*) FROM usuario WHERE login = ? AND matricula <> ?";

    private static final String SQL_CONTAR_POR_CPF =
            "SELECT COUNT(*) FROM usuario WHERE cpf = ? AND matricula <> ?";

    public void inserir(Usuario usuario) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {

            comando.setString(1, usuario.getMatricula());
            comando.setString(2, usuario.getCpf());
            comando.setString(3, usuario.getNome());
            comando.setString(4, usuario.getLogin());
            comando.setString(5, usuario.getSenha());
            comando.setString(6, usuario.getTipoUsuario().name());
            comando.setDate(7, Date.valueOf(usuario.getDataUltimaTrocaSenha()));
            comando.setBoolean(8, usuario.isAtivo());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao inserir o usuario: " + e.getMessage(), e);
        }
    }


    public void atualizar(Usuario usuario, boolean alterarSenha) throws DAOException {
        String sql = alterarSenha ? SQL_ATUALIZAR : SQL_ATUALIZAR_SEM_SENHA;

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, usuario.getCpf());
            comando.setString(2, usuario.getNome());
            comando.setString(3, usuario.getLogin());

            if (alterarSenha) {
                comando.setString(4, usuario.getSenha());
                comando.setString(5, usuario.getTipoUsuario().name());
                comando.setDate(6, Date.valueOf(usuario.getDataUltimaTrocaSenha()));
                comando.setString(7, usuario.getMatricula());
            } else {
                comando.setString(4, usuario.getTipoUsuario().name());
                comando.setString(5, usuario.getMatricula());
            }
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao atualizar o usuario: " + e.getMessage(), e);
        }
    }


    public void inativar(String matricula) throws DAOException {
        alterarSituacao(matricula, false);
    }

    public void reativar(String matricula) throws DAOException {
        alterarSituacao(matricula, true);
    }

    private void alterarSituacao(String matricula, boolean ativo) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ALTERAR_SITUACAO)) {

            comando.setBoolean(1, ativo);
            comando.setString(2, matricula);
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao alterar a situacao do usuario: "
                    + e.getMessage(), e);
        }
    }

    public List<Usuario> listar(boolean incluirInativos) throws DAOException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = incluirInativos ? SQL_LISTAR_TODOS : SQL_LISTAR_ATIVOS;

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(montarUsuario(resultado));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar os usuarios: " + e.getMessage(), e);
        }
        return usuarios;
    }

//    usca todos
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


    public boolean loginEmUsoPorOutro(String login, String matriculaAtual) throws DAOException {
        return contarComFiltro(SQL_CONTAR_POR_LOGIN, login, matriculaAtual,
                "Erro ao verificar o login: ");
    }


    public boolean cpfEmUsoPorOutro(String cpf, String matriculaAtual) throws DAOException {
        return contarComFiltro(SQL_CONTAR_POR_CPF, cpf, matriculaAtual,
                "Erro ao verificar o CPF: ");
    }

    private boolean contarComFiltro(String sql, String valor, String matriculaAtual,
                                    String prefixoDoErro) throws DAOException {

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, valor);
            comando.setString(2, matriculaAtual == null ? "" : matriculaAtual);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DAOException(prefixoDoErro + e.getMessage(), e);
        }
    }


    private Usuario montarUsuario(ResultSet resultado) throws SQLException {
        TipoUsuario tipo = TipoUsuario.valueOf(resultado.getString("tipo_usuario"));

        Usuario usuario = Usuario.criar(
                tipo,
                resultado.getString("matricula"),
                resultado.getString("cpf"),
                resultado.getString("nome"),
                resultado.getString("login"),
                resultado.getString("senha"));

        Date dataTroca = resultado.getDate("data_ultima_troca_senha");
        if (dataTroca != null) {
            usuario.setDataUltimaTrocaSenha(dataTroca.toLocalDate());
        }
        usuario.setAtivo(resultado.getBoolean("ativo"));
        return usuario;
    }
}
