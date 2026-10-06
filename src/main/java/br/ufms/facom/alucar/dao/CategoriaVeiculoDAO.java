package br.ufms.facom.alucar.dao;

import br.ufms.facom.alucar.model.CategoriaVeiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CategoriaVeiculoDAO {

    private static final String COLUNAS = "nome, descricao, valor_base_diaria, ativo";

    private static final String SQL_INSERIR =
            "INSERT INTO categoria_veiculo (" + COLUNAS + ") VALUES (?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE categoria_veiculo SET descricao = ?, valor_base_diaria = ? WHERE nome = ?";

    private static final String SQL_ALTERAR_SITUACAO =
            "UPDATE categoria_veiculo SET ativo = ? WHERE nome = ?";

    private static final String SQL_LISTAR_ATIVAS =
            "SELECT " + COLUNAS + " FROM categoria_veiculo WHERE ativo = TRUE ORDER BY nome";

    private static final String SQL_LISTAR_TODAS =
            "SELECT " + COLUNAS + " FROM categoria_veiculo ORDER BY ativo DESC, nome";

    private static final String SQL_BUSCAR_POR_NOME =
            "SELECT " + COLUNAS + " FROM categoria_veiculo WHERE nome = ?";

    private static final String SQL_CONTAR_VEICULOS =
            "SELECT COUNT(*) FROM veiculo WHERE nome_categoria = ?";

    public void inserir(CategoriaVeiculo categoria) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {

            comando.setString(1, categoria.getNome());
            comando.setString(2, categoria.getDescricao());
            comando.setBigDecimal(3, categoria.getValorBaseDiaria());
            comando.setBoolean(4, categoria.isAtivo());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao inserir a categoria: " + e.getMessage(), e);
        }
    }

    public void atualizar(CategoriaVeiculo categoria) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {

            comando.setString(1, categoria.getDescricao());
            comando.setBigDecimal(2, categoria.getValorBaseDiaria());
            comando.setString(3, categoria.getNome());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao atualizar a categoria: " + e.getMessage(), e);
        }
    }


    public void inativar(String nome) throws DAOException {
        alterarSituacao(nome, false);
    }

    public void reativar(String nome) throws DAOException {
        alterarSituacao(nome, true);
    }

    private void alterarSituacao(String nome, boolean ativo) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ALTERAR_SITUACAO)) {

            comando.setBoolean(1, ativo);
            comando.setString(2, nome);
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao alterar a situacao da categoria: "
                    + e.getMessage(), e);
        }
    }

    public List<CategoriaVeiculo> listar(boolean incluirInativas) throws DAOException {
        List<CategoriaVeiculo> categorias = new ArrayList<>();
        String sql = incluirInativas ? SQL_LISTAR_TODAS : SQL_LISTAR_ATIVAS;

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                categorias.add(montarCategoria(resultado));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar as categorias: " + e.getMessage(), e);
        }
        return categorias;
    }


    public CategoriaVeiculo buscarPorNome(String nome) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_NOME)) {

            comando.setString(1, nome);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarCategoria(resultado) : null;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar a categoria: " + e.getMessage(), e);
        }
    }


    public int contarVeiculosNaCategoria(String nome) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_VEICULOS)) {

            comando.setString(1, nome);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }

        } catch (SQLException e) {
            return 0;
        }
    }

    private CategoriaVeiculo montarCategoria(ResultSet resultado) throws SQLException {
        CategoriaVeiculo categoria = new CategoriaVeiculo(
                resultado.getString("nome"),
                resultado.getString("descricao"),
                resultado.getBigDecimal("valor_base_diaria"));

        categoria.setAtivo(resultado.getBoolean("ativo"));
        return categoria;
    }
}
