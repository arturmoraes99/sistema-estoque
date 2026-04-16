package ui;

import model.Produto;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProdutoTableModel extends AbstractTableModel {
    
    private final String[] colunas = {
        "ID", "Nome", "Categoria", "Preço", "Quantidade", "Mín.", "Valor Estoque", "Status"
    };
    
    private final Class<?>[] tiposColunas = {
        Integer.class, String.class, String.class, BigDecimal.class, 
        Integer.class, Integer.class, BigDecimal.class, String.class
    };
    
    private List<Produto> produtos;
    
    public ProdutoTableModel() {
        this.produtos = new ArrayList<>();
    }
    
    public ProdutoTableModel(List<Produto> produtos) {
        this.produtos = new ArrayList<>(produtos);
    }
    
    @Override
    public int getRowCount() {
        return produtos.size();
    }
    
    @Override
    public int getColumnCount() {
        return colunas.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return colunas[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return tiposColunas[columnIndex];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Produto produto = produtos.get(rowIndex);
        
        return switch (columnIndex) {
            case 0 -> produto.getId();
            case 1 -> produto.getNome();
            case 2 -> produto.getCategoria() != null ? produto.getCategoria() : "";
            case 3 -> produto.getPreco();
            case 4 -> produto.getQuantidade();
            case 5 -> produto.getQuantidadeMinima();
            case 6 -> produto.getValorTotalEstoque();
            case 7 -> produto.getStatusEstoqueFormatado();
            default -> null;
        };
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public Produto getProdutoAt(int row) {
        if (row >= 0 && row < produtos.size()) {
            return produtos.get(row);
        }
        return null;
    }
    
    public void setProdutos(List<Produto> produtos) {
        this.produtos = new ArrayList<>(produtos);
        fireTableDataChanged();
    }
    
    public void addProduto(Produto produto) {
        produtos.add(produto);
        fireTableRowsInserted(produtos.size() - 1, produtos.size() - 1);
    }
    
    public void removeProduto(int row) {
        if (row >= 0 && row < produtos.size()) {
            produtos.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    public void updateProduto(int row, Produto produto) {
        if (row >= 0 && row < produtos.size()) {
            produtos.set(row, produto);
            fireTableRowsUpdated(row, row);
        }
    }
    
    public List<Produto> getProdutos() {
        return new ArrayList<>(produtos);
    }
    
    public void clear() {
        int size = produtos.size();
        produtos.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }
}
