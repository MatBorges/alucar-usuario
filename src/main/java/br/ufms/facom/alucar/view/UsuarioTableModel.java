package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.model.Usuario;
import br.ufms.facom.alucar.util.CpfUtil;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapta a lista de objetos Usuario para o formato que a JTable entende.
 * Mantem a tela livre de logica de montagem de linhas e colunas.
 */
public class UsuarioTableModel extends AbstractTableModel {

    private static final String[] COLUNAS =
            {"Matricula", "CPF", "Nome", "Login", "Tipo", "Senha", "Situacao"};

    private List<Usuario> usuarios = new ArrayList<>();

    /** Prazo do RNF03, usado para calcular a coluna "Senha". */
    private int prazoExpiracaoSenha;

    public void setUsuarios(List<Usuario> usuarios, int prazoExpiracaoSenha) {
        this.usuarios = usuarios == null ? new ArrayList<>() : usuarios;
        this.prazoExpiracaoSenha = prazoExpiracaoSenha;
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
            case 1 -> CpfUtil.formatar(usuario.getCpf());
            case 2 -> usuario.getNome();
            case 3 -> usuario.getLogin();
            case 4 -> usuario.getTipoUsuario().getDescricao();
            case 5 -> descreverSituacaoDaSenha(usuario);
            case 6 -> usuario.isAtivo() ? "Ativo" : "Inativo";
            default -> "";
        };
    }

    /** RNF03 - traduz o prazo restante em um texto curto para a tabela. */
    private String descreverSituacaoDaSenha(Usuario usuario) {
        if (prazoExpiracaoSenha <= 0) {
            return "Sem prazo";
        }
        if (usuario.senhaExpirada(prazoExpiracaoSenha)) {
            return "Expirada";
        }
        long dias = usuario.diasAteExpirarSenha(prazoExpiracaoSenha);
        if (dias <= 7) {
            return "Expira em " + dias + "d";
        }
        return "Valida";
    }
}
