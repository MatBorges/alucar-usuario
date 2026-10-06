package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.model.Veiculo;
import br.ufms.facom.alucar.util.MoedaUtil;

import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class VeiculoTableModel extends AbstractTableModel {

    private static final String[] COLUNAS =
            {"Placa", "Marca", "Modelo", "Ano", "Km atual", "Categoria",
             "Diaria", "Status", "Depreciacao"};

    private static final NumberFormat FORMATO_KM =
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR"));

    private List<Veiculo> veiculos = new ArrayList<>();

    public void setVeiculos(List<Veiculo> veiculos) {
        this.veiculos = veiculos == null ? new ArrayList<>() : veiculos;
        fireTableDataChanged();
    }

    public Veiculo getVeiculoEm(int linha) {
        if (linha < 0 || linha >= veiculos.size()) {
            return null;
        }
        return veiculos.get(linha);
    }

    @Override
    public int getRowCount() {
        return veiculos.size();
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
        Veiculo veiculo = veiculos.get(linha);
        return switch (coluna) {
            case 0 -> veiculo.getPlaca();
            case 1 -> veiculo.getMarca();
            case 2 -> veiculo.getModelo();
            case 3 -> veiculo.getAnoFabricacao();
            case 4 -> FORMATO_KM.format(veiculo.getKmAtual());
            case 5 -> veiculo.getCategoria() == null ? "" : veiculo.getCategoria().getNome();
            case 6 -> MoedaUtil.formatar(veiculo.getValorDiaria());
            case 7 -> veiculo.getStatus().getDescricao();
            case 8 -> veiculo.atingiuLimiteDepreciacao() ? "No limite" : "";
            default -> "";
        };
    }
}
