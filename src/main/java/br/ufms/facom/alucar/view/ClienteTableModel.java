package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.model.Cliente;
import br.ufms.facom.alucar.util.CpfUtil;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapta a lista de clientes para a JTable, ja formatando CPF e datas para
 * leitura humana. A formatacao pertence a camada de apresentacao.
 */
public class ClienteTableModel extends AbstractTableModel {

    private static final String[] COLUNAS =
            {"CPF", "Nome", "CNH", "Validade CNH", "Telefone", "Habilitacao", "Situacao"};

    private static final DateTimeFormatter FORMATO_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private List<Cliente> clientes = new ArrayList<>();

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes == null ? new ArrayList<>() : clientes;
        fireTableDataChanged();
    }

    public Cliente getClienteEm(int linha) {
        if (linha < 0 || linha >= clientes.size()) {
            return null;
        }
        return clientes.get(linha);
    }

    @Override
    public int getRowCount() {
        return clientes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int coluna) {
        return COLUNAS[coluna];
    }

    @Override
    public boolean isCellEditable(int linha, int coluna) {
        return false;
    }

    @Override
    public Object getValueAt(int linha, int coluna) {
        Cliente cliente = clientes.get(linha);
        return switch (coluna) {
            case 0 -> CpfUtil.formatar(cliente.getCpf());
            case 1 -> cliente.getNome();
            case 2 -> cliente.getCnh();
            case 3 -> cliente.getValidadeCnh().format(FORMATO_BR);
            case 4 -> cliente.getTelefone() == null ? "" : cliente.getTelefone();
            case 5 -> cliente.possuiCnhValida() ? "Valida" : "Vencida";
            case 6 -> cliente.isAtivo() ? "Ativo" : "Inativo";
            default -> "";
        };
    }
}
