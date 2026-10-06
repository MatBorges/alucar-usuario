package br.ufms.facom.alucar.dao;

import br.ufms.facom.alucar.model.CategoriaVeiculo;
import br.ufms.facom.alucar.model.StatusVeiculo;
import br.ufms.facom.alucar.model.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class VeiculoDAO {

    private static final String SELECT_BASE =
            "SELECT v.placa, v.renavam, v.modelo, v.marca, v.ano_fabricacao, "
            + "v.km_atual, v.valor_diaria, v.status, "
            + "c.nome AS categoria_nome, c.descricao AS categoria_descricao, "
            + "c.valor_base_diaria AS categoria_valor, c.ativo AS categoria_ativo "
            + "FROM veiculo v "
            + "JOIN categoria_veiculo c ON c.nome = v.nome_categoria ";

    private static final String SQL_INSERIR =
            "INSERT INTO veiculo (placa, renavam, modelo, marca, ano_fabricacao, "
            + "km_atual, valor_diaria, status, nome_categoria) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE veiculo SET renavam = ?, modelo = ?, marca = ?, ano_fabricacao = ?, "
            + "km_atual = ?, valor_diaria = ?, status = ?, nome_categoria = ? "
            + "WHERE placa = ?";

    private static final String SQL_ALTERAR_STATUS =
            "UPDATE veiculo SET status = ? WHERE placa = ?";

    private static final String SQL_BUSCAR_POR_PLACA =
            SELECT_BASE + "WHERE v.placa = ?";

    private static final String SQL_CONTAR_POR_PLACA =
            "SELECT COUNT(*) FROM veiculo WHERE placa = ?";

    private static final String SQL_CONTAR_POR_RENAVAM =
            "SELECT COUNT(*) FROM veiculo WHERE renavam = ? AND placa <> ?";

    public void inserir(Veiculo veiculo) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {

            comando.setString(1, veiculo.getPlaca());
            comando.setString(2, veiculo.getRenavam());
            comando.setString(3, veiculo.getModelo());
            comando.setString(4, veiculo.getMarca());
            comando.setInt(5, veiculo.getAnoFabricacao());
            comando.setInt(6, veiculo.getKmAtual());
            comando.setBigDecimal(7, veiculo.getValorDiaria());
            comando.setString(8, veiculo.getStatus().name());
            comando.setString(9, veiculo.getCategoria().getNome());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao inserir o veiculo: " + e.getMessage(), e);
        }
    }

    public void atualizar(Veiculo veiculo) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {

            comando.setString(1, veiculo.getRenavam());
            comando.setString(2, veiculo.getModelo());
            comando.setString(3, veiculo.getMarca());
            comando.setInt(4, veiculo.getAnoFabricacao());
            comando.setInt(5, veiculo.getKmAtual());
            comando.setBigDecimal(6, veiculo.getValorDiaria());
            comando.setString(7, veiculo.getStatus().name());
            comando.setString(8, veiculo.getCategoria().getNome());
            comando.setString(9, veiculo.getPlaca());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao atualizar o veiculo: " + e.getMessage(), e);
        }
    }


    public void alterarStatus(String placa, StatusVeiculo status) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ALTERAR_STATUS)) {

            comando.setString(1, status.name());
            comando.setString(2, placa);
            comando.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Erro ao alterar o status do veiculo: "
                    + e.getMessage(), e);
        }
    }

    public List<Veiculo> listarTodos() throws DAOException {
        return consultar(null, null, null, null);
    }


    public List<Veiculo> listarDisponiveis() throws DAOException {
        return consultar(null, null, null, StatusVeiculo.DISPONIVEL);
    }


    public List<Veiculo> consultar(String marca, String modelo, String categoria,
                                   StatusVeiculo status) throws DAOException {

        StringBuilder sql = new StringBuilder(SELECT_BASE);
        List<String> condicoes = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();

        if (naoVazio(marca)) {
            condicoes.add("v.marca LIKE ?");
            parametros.add("%" + marca.trim() + "%");
        }
        if (naoVazio(modelo)) {
            condicoes.add("v.modelo LIKE ?");
            parametros.add("%" + modelo.trim() + "%");
        }
        if (naoVazio(categoria)) {
            condicoes.add("v.nome_categoria = ?");
            parametros.add(categoria.trim());
        }
        if (status != null) {
            condicoes.add("v.status = ?");
            parametros.add(status.name());
        }

        if (!condicoes.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", condicoes)).append(" ");
        }
        sql.append("ORDER BY v.marca, v.modelo, v.placa");

        List<Veiculo> veiculos = new ArrayList<>();

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                comando.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    veiculos.add(montarVeiculo(resultado));
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao consultar os veiculos: " + e.getMessage(), e);
        }
        return veiculos;
    }

    public Veiculo buscarPorPlaca(String placa) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_PLACA)) {

            comando.setString(1, placa);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarVeiculo(resultado) : null;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar o veiculo: " + e.getMessage(), e);
        }
    }

    public boolean existePlaca(String placa) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_POR_PLACA)) {

            comando.setString(1, placa);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao verificar a placa: " + e.getMessage(), e);
        }
    }

    public boolean renavamEmUsoPorOutro(String renavam, String placaAtual) throws DAOException {
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_POR_RENAVAM)) {

            comando.setString(1, renavam);
            comando.setString(2, placaAtual == null ? "" : placaAtual);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new DAOException("Erro ao verificar o RENAVAM: " + e.getMessage(), e);
        }
    }

    private boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }

    private Veiculo montarVeiculo(ResultSet resultado) throws SQLException {
        CategoriaVeiculo categoria = new CategoriaVeiculo(
                resultado.getString("categoria_nome"),
                resultado.getString("categoria_descricao"),
                resultado.getBigDecimal("categoria_valor"));
        categoria.setAtivo(resultado.getBoolean("categoria_ativo"));

        return new Veiculo(
                resultado.getString("placa"),
                resultado.getString("renavam"),
                resultado.getString("modelo"),
                resultado.getString("marca"),
                resultado.getInt("ano_fabricacao"),
                resultado.getInt("km_atual"),
                resultado.getBigDecimal("valor_diaria"),
                StatusVeiculo.valueOf(resultado.getString("status")),
                categoria);
    }
}
