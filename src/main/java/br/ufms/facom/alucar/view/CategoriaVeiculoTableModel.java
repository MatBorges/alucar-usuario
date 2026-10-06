package br.ufms.facom.alucar.view;

import br.ufms.facom.alucar.model.CategoriaVeiculo;
import br.ufms.facom.alucar.util.MoedaUtil;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class CategoriaVeiculoTableModel extends AbstractTableModel {

    private static final String[] COLUNAS =
            {"Nome", "Descricao", "Valor base da diaria", "Situacao"};

    private List<CategoriaVeiculo> categorias = new ArrayList<>();

    public void setCategorias(List<CategoriaVeiculo> categorias) {
        this.categorias = categorias == null ? new ArrayList<>() : categorias;
        fireTableDataChanged();
    }

    public CategoriaVeiculo getCategoriaEm(int linha) {
        if (linha < 0 || linha >= categorias.size()) {
            return null;
        }
        return categorias.get(linha);
    }

    @Override
    public int getRowCount() {
        return categorias.size();
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
        CategoriaVeiculo categoria = categorias.get(linha);
        return switch (coluna) {
            case 0 -> categoria.getNome();
            case 1 -> categoria.getDescricao() == null ? "" : categoria.getDescricao();
            case 2 -> MoedaUtil.formatar(categoria.getValorBaseDiaria());
            case 3 -> categoria.isAtivo() ? "Ativa" : "Inativa";
            default -> "";
        };
    }
}
