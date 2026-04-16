package dao;

import model.Produto;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoDAO {
    
    
    private static final String SQL_INSERT = 
        "INSERT INTO produtos (nome, descricao, preco, quantidade, quantidade_minima, categoria) VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE = 
        "UPDATE produtos SET nome = ?, descricao = ?, preco = ?, quantidade = ?, quantidade_minima = ?, categoria = ? WHERE id = ?";
    
    private static final String SQL_SOFT_DELETE = 
        "UPDATE produtos SET ativo = FALSE WHERE id = ?";
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM produtos WHERE id = ? AND ativo = TRUE";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM produtos WHERE ativo = TRUE ORDER BY nome";
    
    private static final String SQL_SELECT_ESTOQUE_BAIXO = 
        "SELECT * FROM produtos WHERE ativo = TRUE AND quantidade <= quantidade_minima ORDER BY quantidade ASC";
    
    private static final String SQL_BUSCAR_POR_NOME = 
        "SELECT * FROM produtos WHERE ativo = TRUE AND nome LIKE ? ORDER BY nome";
    
    private static final String SQL_ATUALIZAR_QUANTIDADE = 
        "UPDATE produtos SET quantidade = quantidade + ? WHERE id = ?";
    
    private static final String SQL_REGISTRAR_MOVIMENTACAO = 
        "INSERT INTO movimentacoes (produto_id, tipo, quantidade, observacao) VALUES (?, ?, ?, ?)";
    

    public Produto inserir(Produto produto) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            preencherStatement(stmt, produto);
            
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setId(rs.getInt(1));
                        produto.setDataCadastro(LocalDateTime.now());
                        return produto;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir produto: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean atualizar(Produto produto) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            preencherStatement(stmt, produto);
            stmt.setInt(7, produto.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deletar(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_SOFT_DELETE)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public Optional<Produto> buscarPorId(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<Produto> listarTodos() {
        List<Produto> produtos = new ArrayList<>();
        
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                produtos.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
            e.printStackTrace();
        }
        return produtos;
    }
    
    public List<Produto> listarEstoqueBaixo() {
        List<Produto> produtos = new ArrayList<>();
        
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ESTOQUE_BAIXO);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                produtos.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar estoque baixo: " + e.getMessage());
            e.printStackTrace();
        }
        return produtos;
    }
    
    public List<Produto> buscarPorNome(String nome) {
        List<Produto> produtos = new ArrayList<>();
        
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_POR_NOME)) {
            
            stmt.setString(1, "%" + nome + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produtos: " + e.getMessage());
            e.printStackTrace();
        }
        return produtos;
    }
    
    public boolean registrarEntrada(int produtoId, int quantidade, String observacao) {
        return registrarMovimentacao(produtoId, quantidade, "ENTRADA", observacao);
    }
    

    public boolean registrarSaida(int produtoId, int quantidade, String observacao) {
        Optional<Produto> produto = buscarPorId(produtoId);
        if (produto.isPresent() && produto.get().getQuantidade() >= quantidade) {
            return registrarMovimentacao(produtoId, -quantidade, "SAIDA", observacao);
        }
        System.err.println("Estoque insuficiente para esta operação!");
        return false;
    }
    

    private boolean registrarMovimentacao(int produtoId, int quantidade, String tipo, String observacao) {
        Connection conn = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);
            
            // Atualiza a quantidade no produto
            try (PreparedStatement stmtUpdate = conn.prepareStatement(SQL_ATUALIZAR_QUANTIDADE)) {
                stmtUpdate.setInt(1, quantidade);
                stmtUpdate.setInt(2, produtoId);
                stmtUpdate.executeUpdate();
            }
            
            // Registra a movimentação no histórico
            try (PreparedStatement stmtMov = conn.prepareStatement(SQL_REGISTRAR_MOVIMENTACAO)) {
                stmtMov.setInt(1, produtoId);
                stmtMov.setString(2, tipo);
                stmtMov.setInt(3, Math.abs(quantidade));
                stmtMov.setString(4, observacao);
                stmtMov.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erro na movimentação: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erro no rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erro ao restaurar autoCommit: " + e.getMessage());
                }
            }
        }
    }
    
    
    private void preencherStatement(PreparedStatement stmt, Produto produto) throws SQLException {
        stmt.setString(1, produto.getNome());
        stmt.setString(2, produto.getDescricao());
        stmt.setBigDecimal(3, produto.getPreco());
        stmt.setInt(4, produto.getQuantidade());
        stmt.setInt(5, produto.getQuantidadeMinima());
        stmt.setString(6, produto.getCategoria());
    }
    
    private Produto mapearResultSet(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getInt("id"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setPreco(rs.getBigDecimal("preco"));
        produto.setQuantidade(rs.getInt("quantidade"));
        produto.setQuantidadeMinima(rs.getInt("quantidade_minima"));
        produto.setCategoria(rs.getString("categoria"));
        produto.setAtivo(rs.getBoolean("ativo"));
        
        Timestamp dataCadastro = rs.getTimestamp("data_cadastro");
        if (dataCadastro != null) {
            produto.setDataCadastro(dataCadastro.toLocalDateTime());
        }
        
        Timestamp dataAtualizacao = rs.getTimestamp("data_atualizacao");
        if (dataAtualizacao != null) {
            produto.setDataAtualizacao(dataAtualizacao.toLocalDateTime());
        }
        
        return produto;
    }
}
