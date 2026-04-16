package service;

import dao.ProdutoDAO;
import model.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class EstoqueService {
    
    private final ProdutoDAO produtoDAO;
    
    public EstoqueService() {
        this.produtoDAO = new ProdutoDAO();
    }
    
    
    public Produto cadastrarProduto(String nome, String descricao, BigDecimal preco,
                                    int quantidade, int quantidadeMinima, String categoria) {
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório!");
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço deve ser maior ou igual a zero!");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa!");
        }
        
        Produto produto = new Produto(nome.trim(), descricao, preco, quantidade, quantidadeMinima, categoria);
        return produtoDAO.inserir(produto);
    }
    
    public boolean atualizarProduto(Produto produto) {
        if (produto.getId() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório para atualização!");
        }
        return produtoDAO.atualizar(produto);
    }
    
    public boolean removerProduto(int id) {
        return produtoDAO.deletar(id);
    }
    
    public Optional<Produto> buscarProduto(int id) {
        return produtoDAO.buscarPorId(id);
    }
    
    public List<Produto> listarProdutos() {
        return produtoDAO.listarTodos();
    }
    
    public List<Produto> buscarPorNome(String nome) {
        return produtoDAO.buscarPorNome(nome);
    }
    
    public boolean entradaEstoque(int produtoId, int quantidade, String observacao) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero!");
        }
        return produtoDAO.registrarEntrada(produtoId, quantidade, observacao);
    }
    
    public boolean saidaEstoque(int produtoId, int quantidade, String observacao) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero!");
        }
        return produtoDAO.registrarSaida(produtoId, quantidade, observacao);
    }
    
    public List<Produto> listarEstoqueBaixo() {
        return produtoDAO.listarEstoqueBaixo();
    }
    
    
    public BigDecimal calcularValorTotalEstoque() {
        return listarProdutos().stream()
            .map(Produto::getValorTotalEstoque)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int contarTotalItens() {
        return listarProdutos().stream()
            .mapToInt(Produto::getQuantidade)
            .sum();
    }
    
    public int contarProdutosAlerta() {
        return (int) listarProdutos().stream()
            .filter(Produto::isEstoqueBaixo)
            .count();
    }
}
