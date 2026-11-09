package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.model.Locacao;
import br.ufms.facom.alucar.util.MoedaUtil;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel para exibição de locações em JTable.
 */
public class LocacaoTableModel extends AbstractTableModel {

    private static final String[] COLUNAS = {
            "#", "Data Retirada", "Previsao Devolucao", "Cliente", "Veiculo", "Valor Estimado", "Caucao", "Status"
    };

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<Locacao> locacoes = new ArrayList<>();

    @Override
    public int getRowCount() {
        return locacoes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Locacao l = locacoes.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return l.getIdLocacao();
            case 1:
                return l.getDataRetirada() != null ? l.getDataRetirada().format(FORMATO_DATA_HORA) : "";
            case 2:
                return l.getDataDevolucaoPrevista() != null ? l.getDataDevolucaoPrevista().format(FORMATO_DATA) : "";
            case 3:
                return l.getCliente() != null ? l.getCliente().getNome() : "";
            case 4:
                return l.getVeiculo() != null ? l.getVeiculo().getPlaca() + " - " + l.getVeiculo().getModelo() : "";
            case 5:
                return MoedaUtil.formatar(l.getValorEstimado());
            case 6:
                return MoedaUtil.formatar(l.getValorCaucao());
            case 7:
                return l.getStatus() != null ? l.getStatus().getDescricao() : "";
            default:
                return null;
        }
    }

    public void definirLocacoes(List<Locacao> novas) {
        locacoes.clear();
        if (novas != null) {
            locacoes.addAll(novas);
        }
        fireTableDataChanged();
    }

    public Locacao obterLocacao(int linha) {
        if (linha >= 0 && linha < locacoes.size()) {
            return locacoes.get(linha);
        }
        return null;
    }
}
