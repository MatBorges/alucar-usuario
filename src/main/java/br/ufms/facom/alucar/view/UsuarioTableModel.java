package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.model.Usuario;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapta a lista de objetos Usuario para o formato que a JTable entende.
 * Mantem a tela livre de logica de montagem de linhas e colunas.
 */
public class UsuarioTableModel extends AbstractTableModel {

    private static final String[] COLUNAS = {"Matricula", "Nome", "Login", "Tipo"};

    private List<Usuario> usuarios = new ArrayList<>();

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios == null ? new ArrayList<>() : usuarios;
        fireTableDataChanged();
    }

    public Usuario getUsuarioEm(int linha) {
        if (linha < 0 || linha >= usuarios.size()) {
            return null;
        }
        return usuarios.get(linha);
    }

    @Override
    public int getRowCount() {
        return usuarios.size();
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
        Usuario usuario = usuarios.get(linha);
        return switch (coluna) {
            case 0 -> usuario.getMatricula();
            case 1 -> usuario.getNome();
            case 2 -> usuario.getLogin();
            case 3 -> usuario.getTipoUsuario().getDescricao();
            default -> "";
        };
    }
}
