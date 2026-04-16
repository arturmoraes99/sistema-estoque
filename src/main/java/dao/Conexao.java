package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    
    
    private static final String HOST = "localhost";
    private static final String PORTA = "3306";
    private static final String BANCO = "sistema_estoque";
    private static final String USUARIO = "root";
    private static final String SENHA = "Camisa26@";  // Coloque sua senha aqui
    
    private static final String URL = String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
        HOST, PORTA, BANCO
    );
    
    private static Connection conexao = null;
    
    public static Connection getConexao() throws SQLException {
        try {
            if (conexao == null || conexao.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
            }
            return conexao;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado. Verifique se o mysql-connector-java está no classpath.", e);
        } catch (SQLException e) {
            throw new SQLException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
    }
    
    public static void fecharConexao() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                conexao = null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
    
    public static boolean testarConexao() {
        try {
            Connection conn = getConexao();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Falha no teste de conexão: " + e.getMessage());
            return false;
        }
    }
}
