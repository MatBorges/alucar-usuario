package br.ufms.facom.alucar.dao;

import br.ufms.facom.alucar.model.Cliente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de clientes na tabela 'cliente'.
 *
 * As datas trafegam como java.time.LocalDate no modelo e como java.sql.Date
 * no banco; a conversao acontece apenas aqui, para que nem a tela nem a
 * controladora precisem conhecer tipos de JDBC.
 *
 * A exclusao e LOGICA: nenhum metodo emite DELETE. Inativar preserva as
 * chaves estrangeiras das locacoes ja feitas pelo cliente.
 */
public class ClienteDAO {

    private static final String COLUNAS =
            "cpf, nome, cnh, validade_cnh, data_nascimento, telefone, email, endereco, ativo";

    private static final String SQL_INSERIR =
            "INSERT INTO cliente (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE cliente SET nome = ?, cnh = ?, validade_cnh = ?, data_nascimento = ?, "
            + "telefone = ?, email = ?, endereco = ? WHERE cpf = ?";

    private static final String SQL_ALTERAR_SITUACAO =
            "UPDATE cliente SET ativo = ? WHERE cpf = ?";

    private static final String SQL_LISTAR_ATIVOS =
            "SELECT " + COLUNAS + " FROM cliente WHERE ativo = TRUE ORDER BY nome";

    private static final String SQL_LISTAR_TODOS =
            "SELECT " + COLUNAS + " FROM cliente ORDER BY ativo DESC, nome";

    private static final String SQL_BUSCAR_POR_CPF =
            "SELECT " + COLUNAS + " FROM cliente WHERE cpf = ?";

    private static final String SQL_BUSCAR_POR_NOME_ATIVOS =
            "SELECT " + COLUNAS + " FROM cliente WHERE nome LIKE ? AND ativo = TRUE "
            + "ORDER BY nome";

    private static final String SQL_BUSCAR_POR_NOME_TODOS =
            "SELECT " + COLUNAS + " FROM cliente WHERE nome LIKE ? ORDER BY ativo DESC, nome";

    private static final String SQL_CONTAR_POR_CPF =
            "SELECT COUNT(*) FROM cliente WHERE cpf = ?";

    private static final String SQL_CONTAR_POR_CNH =
            "SELECT COUNT(*) FROM cliente WHERE cnh = ? AND cpf <> ?";

    public void inserir(Cliente cliente) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {

            comando.setString(1, cliente.getCpf());
            comando.setString(2, cliente.getNome());
            comando.setString(3, cliente.getCnh());
            comando.setDate(4, Date.valueOf(cliente.getValidadeCnh()));
            comando.setDate(5, Date.valueOf(cliente.getDataNascimento()));
            comando.setString(6, cliente.getTelefone());
            comando.setString(7, cliente.getEmail());
            comando.setString(8, cliente.getEndereco());
            comando.setBoolean(9, cliente.isAtivo());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao inserir o cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza os dados cadastrais. A situacao (ativo) nao e tocada aqui:
     * inativar e reativar sao operacoes de negocio proprias.
     */
    public void atualizar(Cliente cliente) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {

            comando.setString(1, cliente.getNome());
            comando.setString(2, cliente.getCnh());
            comando.setDate(3, Date.valueOf(cliente.getValidadeCnh()));
            comando.setDate(4, Date.valueOf(cliente.getDataNascimento()));
            comando.setString(5, cliente.getTelefone());
            comando.setString(6, cliente.getEmail());
            comando.setString(7, cliente.getEndereco());
            comando.setString(8, cliente.getCpf());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao atualizar o cliente: " + e.getMessage(), e);
        }
    }

    /** Exclusao logica: marca como inativo, sem remover a linha. */
    public void inativar(String cpf) throws DAOException {
        alterarSituacao(cpf, false);
    }

    public void reativar(String cpf) throws DAOException {
        alterarSituacao(cpf, true);
    }

    private void alterarSituacao(String cpf, boolean ativo) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ALTERAR_SITUACAO)) {

            comando.setBoolean(1, ativo);
            comando.setString(2, cpf);
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao alterar a situacao do cliente: "
                    + e.getMessage(), e);
        }
    }

    public List<Cliente> listar(boolean incluirInativos) throws DAOException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = incluirInativos ? SQL_LISTAR_TODOS : SQL_LISTAR_ATIVOS;

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                clientes.add(montarCliente(resultado));
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao listar os clientes: " + e.getMessage(), e);
        }
        return clientes;
    }

    public List<Cliente> buscarPorNome(String trechoDoNome, boolean incluirInativos)
            throws DAOException {

        List<Cliente> clientes = new ArrayList<>();
        String sql = incluirInativos ? SQL_BUSCAR_POR_NOME_TODOS : SQL_BUSCAR_POR_NOME_ATIVOS;

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, "%" + trechoDoNome + "%");
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    clientes.add(montarCliente(resultado));
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar clientes: " + e.getMessage(), e);
        }
        return clientes;
    }

    /** Busca independente da situacao: necessaria para reativar um inativo. */
    public Cliente buscarPorCpf(String cpf) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_CPF)) {

            comando.setString(1, cpf);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarCliente(resultado) : null;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar o cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Considera tambem os inativos: o CPF e chave primaria, entao um
     * registro inativo continua ocupando o valor.
     */
    public boolean existeCpf(String cpf) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_POR_CPF)) {

            comando.setString(1, cpf);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao verificar o CPF: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se a CNH ja pertence a outro cliente, ignorando o proprio.
     * Inativos contam: a restricao UNIQUE do banco vale para eles tambem.
     */
    public boolean cnhEmUsoPorOutro(String cnh, String cpfAtual) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_POR_CNH)) {

            comando.setString(1, cnh);
            comando.setString(2, cpfAtual == null ? "" : cpfAtual);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao verificar a CNH: " + e.getMessage(), e);
        }
    }

    private Cliente montarCliente(ResultSet resultado) throws SQLException {
        Cliente cliente = new Cliente(
                resultado.getString("cpf"),
                resultado.getString("nome"),
                resultado.getString("cnh"),
                resultado.getDate("validade_cnh").toLocalDate(),
                resultado.getDate("data_nascimento").toLocalDate(),
                resultado.getString("telefone"),
                resultado.getString("email"),
                resultado.getString("endereco"));

        cliente.setAtivo(resultado.getBoolean("ativo"));
        return cliente;
    }
}
