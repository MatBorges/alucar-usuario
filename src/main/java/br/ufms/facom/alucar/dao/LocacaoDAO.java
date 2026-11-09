package br.ufms.facom.alucar.dao;

import br.ufms.facom.alucar.model.Cliente;
import br.ufms.facom.alucar.model.Locacao;
import br.ufms.facom.alucar.model.StatusLocacao;
import br.ufms.facom.alucar.model.StatusVeiculo;
import br.ufms.facom.alucar.model.TipoUsuario;
import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.model.Veiculo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de Persistência JDBC para Locações (RF08).
 * Realiza operações transacionais atômicas alterando o status do veículo.
 */
public class LocacaoDAO {

    private static final String SELECT_BASE =
            "SELECT l.id_locacao, l.data_retirada, l.data_devolucao_prevista, l.km_inicial, "
            + "l.valor_estimado, l.valor_caucao, l.status_locacao, "
            + "c.cpf AS c_cpf, c.nome AS c_nome, c.cnh AS c_cnh, c.validade_cnh AS c_validade_cnh, "
            + "v.placa AS v_placa, v.modelo AS v_modelo, v.marca AS v_marca, v.status AS v_status, v.valor_diaria AS v_valor_diaria, "
            + "u.matricula AS u_matricula, u.nome AS u_nome, u.tipo_usuario AS u_tipo "
            + "FROM locacao l "
            + "JOIN cliente c ON c.cpf = l.cpf_cliente "
            + "JOIN veiculo v ON v.placa = l.placa_veiculo "
            + "JOIN usuario u ON u.matricula = l.matricula_atendente ";

    private static final String SQL_INSERIR_LOCACAO =
            "INSERT INTO locacao (data_retirada, data_devolucao_prevista, km_inicial, "
            + "valor_estimado, valor_caucao, status_locacao, cpf_cliente, placa_veiculo, matricula_atendente) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR_STATUS_VEICULO =
            "UPDATE veiculo SET status = ? WHERE placa = ?";

    private static final String SQL_ATUALIZAR_STATUS_LOCACAO =
            "UPDATE locacao SET status_locacao = ? WHERE id_locacao = ?";

    /**
     * Insere a locação e atualiza o veículo para LOCADO na mesma transação atômica.
     */
    public void inserir(Locacao locacao) throws DAOException {
        Connection conexao = null;
        try {
            conexao = ConexaoBD.obterConexao();
            conexao.setAutoCommit(false);

            try (PreparedStatement cmdLoc = conexao.prepareStatement(SQL_INSERIR_LOCACAO, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement cmdVeic = conexao.prepareStatement(SQL_ATUALIZAR_STATUS_VEICULO)) {

                cmdLoc.setTimestamp(1, Timestamp.valueOf(locacao.getDataRetirada()));
                cmdLoc.setDate(2, Date.valueOf(locacao.getDataDevolucaoPrevista()));
                cmdLoc.setInt(3, locacao.getKmInicial());
                cmdLoc.setBigDecimal(4, locacao.getValorEstimado());
                cmdLoc.setBigDecimal(5, locacao.getValorCaucao());
                cmdLoc.setString(6, locacao.getStatus().name());
                cmdLoc.setString(7, locacao.getCliente().getCpf());
                cmdLoc.setString(8, locacao.getVeiculo().getPlaca());
                cmdLoc.setString(9, locacao.getAtendente().getMatricula());

                cmdLoc.executeUpdate();

                try (ResultSet chaves = cmdLoc.getGeneratedKeys()) {
                    if (chaves.next()) {
                        locacao.setIdLocacao(chaves.getInt(1));
                    }
                }

                cmdVeic.setString(1, StatusVeiculo.LOCADO.name());
                cmdVeic.setString(2, locacao.getVeiculo().getPlaca());
                cmdVeic.executeUpdate();

                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Erro ao registrar a locacao no banco: " + e.getMessage(), e);
        } finally {
            if (conexao != null) {
                try {
                    conexao.setAutoCommit(true);
                    conexao.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public Locacao buscarPorId(int idLocacao) throws DAOException {
        String sql = SELECT_BASE + "WHERE l.id_locacao = ?";
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idLocacao);
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return montarLocacao(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Erro ao buscar locacao por ID: " + e.getMessage(), e);
        }
    }

    public List<Locacao> listarTodas() throws DAOException {
        String sql = SELECT_BASE + "ORDER BY l.data_retirada DESC";
        List<Locacao> lista = new ArrayList<>();
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {

            while (rs.next()) {
                lista.add(montarLocacao(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Erro ao listar locacoes: " + e.getMessage(), e);
        }
    }

    public List<Locacao> listarPorStatus(StatusLocacao statusFiltro) throws DAOException {
        if (statusFiltro == null) {
            return listarTodas();
        }
        String sql = SELECT_BASE + "WHERE l.status_locacao = ? ORDER BY l.data_retirada DESC";
        List<Locacao> lista = new ArrayList<>();
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, statusFiltro.name());
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarLocacao(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Erro ao listar locacoes por status: " + e.getMessage(), e);
        }
    }

    public void cancelar(int idLocacao, String placaVeiculo) throws DAOException {
        Connection conexao = null;
        try {
            conexao = ConexaoBD.obterConexao();
            conexao.setAutoCommit(false);

            try (PreparedStatement cmdLoc = conexao.prepareStatement(SQL_ATUALIZAR_STATUS_LOCACAO);
                 PreparedStatement cmdVeic = conexao.prepareStatement(SQL_ATUALIZAR_STATUS_VEICULO)) {

                cmdLoc.setString(1, StatusLocacao.CANCELADA.name());
                cmdLoc.setInt(2, idLocacao);
                cmdLoc.executeUpdate();

                cmdVeic.setString(1, StatusVeiculo.DISPONIVEL.name());
                cmdVeic.setString(2, placaVeiculo);
                cmdVeic.executeUpdate();

                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Erro ao cancelar locacao: " + e.getMessage(), e);
        } finally {
            if (conexao != null) {
                try {
                    conexao.setAutoCommit(true);
                    conexao.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private Locacao montarLocacao(ResultSet rs) throws SQLException {
        Locacao locacao = new Locacao();
        locacao.setIdLocacao(rs.getInt("id_locacao"));
        locacao.setDataRetirada(rs.getTimestamp("data_retirada").toLocalDateTime());
        locacao.setDataDevolucaoPrevista(rs.getDate("data_devolucao_prevista").toLocalDate());
        locacao.setKmInicial(rs.getInt("km_inicial"));
        locacao.setValorEstimado(rs.getBigDecimal("valor_estimado"));
        locacao.setValorCaucao(rs.getBigDecimal("valor_caucao"));
        locacao.setStatus(StatusLocacao.valueOf(rs.getString("status_locacao")));

        Cliente cliente = new Cliente();
        cliente.setCpf(rs.getString("c_cpf"));
        cliente.setNome(rs.getString("c_nome"));
        cliente.setCnh(rs.getString("c_cnh"));
        cliente.setValidadeCnh(rs.getDate("c_validade_cnh").toLocalDate());
        locacao.setCliente(cliente);

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(rs.getString("v_placa"));
        veiculo.setModelo(rs.getString("v_modelo"));
        veiculo.setMarca(rs.getString("v_marca"));
        veiculo.setStatus(StatusVeiculo.valueOf(rs.getString("v_status")));
        veiculo.setValorDiaria(rs.getBigDecimal("v_valor_diaria"));
        locacao.setVeiculo(veiculo);

        TipoUsuario tipo = TipoUsuario.valueOf(rs.getString("u_tipo"));
        Usuario atendente = Usuario.criar(
                tipo,
                rs.getString("u_matricula"),
                null,
                rs.getString("u_nome"),
                null,
                null
        );
        locacao.setAtendente(atendente);

        return locacao;
    }
}
