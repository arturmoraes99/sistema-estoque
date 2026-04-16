package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Produto {
    
    private Integer id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private int quantidade;
    private int quantidadeMinima;
    private String categoria;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private boolean ativo;
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    
    public Produto() {
        this.ativo = true;
        this.quantidadeMinima = 5;
        this.preco = BigDecimal.ZERO;
        this.quantidade = 0;
    }
    
    public Produto(String nome, BigDecimal preco, int quantidade) {
        this();
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }
    
    public Produto(String nome, String descricao, BigDecimal preco, 
                   int quantidade, int quantidadeMinima, String categoria) {
        this(nome, preco, quantidade);
        this.descricao = descricao;
        this.quantidadeMinima = quantidadeMinima;
        this.categoria = categoria;
    }
    
    public boolean isEstoqueBaixo() {
        return this.quantidade <= this.quantidadeMinima;
    }
    
    public boolean isEstoqueZerado() {
        return this.quantidade == 0;
    }
    
    public String getStatusEstoque() {
        if (isEstoqueZerado()) {
            return "ESGOTADO";
        } else if (isEstoqueBaixo()) {
            return "BAIXO";
        }
        return "NORMAL";
    }
    
    public String getStatusEstoqueFormatado() {
        if (isEstoqueZerado()) {
            return "🔴 ESGOTADO";
        } else if (isEstoqueBaixo()) {
            return "🟡 BAIXO";
        }
        return "🟢 NORMAL";
    }
    
    public BigDecimal getValorTotalEstoque() {
        return this.preco.multiply(BigDecimal.valueOf(this.quantidade));
    }
    
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public BigDecimal getPreco() {
        return preco;
    }
    
    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
    
    public int getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
    
    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }
    
    public void setQuantidadeMinima(int quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    
    @Override
    public String toString() {
        return String.format("Produto[id=%d, nome='%s', qtd=%d, status=%s]",
            id, nome, quantidade, getStatusEstoque());
    }
    
    public String toStringDetalhado() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n══════════════════════════════════════════\n");
        sb.append(String.format("  PRODUTO #%d\n", id));
        sb.append("══════════════════════════════════════════\n");
        sb.append(String.format("  Nome: %s\n", nome));
        sb.append(String.format("  Descrição: %s\n", descricao != null ? descricao : "N/A"));
        sb.append(String.format("  Categoria: %s\n", categoria != null ? categoria : "N/A"));
        sb.append(String.format("  Preço: R$ %.2f\n", preco));
        sb.append(String.format("  Quantidade: %d (Mín: %d)\n", quantidade, quantidadeMinima));
        sb.append(String.format("  Status: %s\n", getStatusEstoqueFormatado()));
        sb.append(String.format("  Valor em Estoque: R$ %.2f\n", getValorTotalEstoque()));
        if (dataCadastro != null) {
            sb.append(String.format("  Cadastro: %s\n", dataCadastro.format(FORMATTER)));
        }
        sb.append("══════════════════════════════════════════");
        return sb.toString();
    }
}
