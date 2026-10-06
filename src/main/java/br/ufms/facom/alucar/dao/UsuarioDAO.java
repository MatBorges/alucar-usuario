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

/**
 * Realiza a persistencia da hierarquia de usuarios na tabela unica 'usuario'.
 *
 * E aqui que o mapeamento objeto-relacional acontece de fato: ao gravar, o
 * discriminador vem de usuario.getTipoUsuario(); ao ler, a coluna
 * tipo_usuario decide qual subclasse sera instanciada.
 *
 * A exclusao e LOGICA: nenhum metodo emite DELETE. Inativar preserva as
 * chaves estrangeiras das locacoes ja realizadas pelo usuario.
 */
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

    /**
     * Atualiza o usuario. Quando alterarSenha e falso, a senha atual e a data
     * da ultima troca sao preservadas - o gerente nao precisa redigitar a
     * senha para corrigir apenas o nome, e corrigir o nome nao pode renovar
     * o prazo de expiracao da senha (RNF03).
     *
     * A situacao (ativo) nao e alterada aqui: para isso existem os metodos
     * inativar e reativar, que representam operacoes de negocio distintas
     * de uma simples edicao de cadastro.
     */
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

    /** Exclusao logica: marca como inativo, sem remover a linha. */
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

    /** Busca independente da situacao: necessaria para reativar um inativo. */
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

    /**
     * Considera tambem os inativos: a matricula e chave primaria, entao um
     * registro inativo continua ocupando o valor.
     */
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
     * registro em edicao. Inativos contam: a restricao UNIQUE do banco vale
     * para eles tambem.
     */
    public boolean loginEmUsoPorOutro(String login, String matriculaAtual) throws DAOException {
        return contarComFiltro(SQL_CONTAR_POR_LOGIN, login, matriculaAtual,
                "Erro ao verificar o login: ");
    }

    /** Mesma logica do login, aplicada ao CPF (RF04). */
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

    /**
     * Converte uma linha da tabela no objeto da subclasse correta.
     * Este metodo e o coracao do mapeamento da heranca por tabela unica.
     */
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
