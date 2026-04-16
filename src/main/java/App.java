import com.formdev.flatlaf.FlatLightLaf;
import dao.Conexao;
import ui.MainFrame;
import javax.swing.*;
import java.awt.*;

public class App {
    
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", true);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            
        } catch (Exception e) {
            System.err.println("Erro ao configurar Look and Feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            JDialog splash = criarSplash();
            splash.setVisible(true);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        Thread.sleep(500); // Pequena pausa para mostrar splash
                    } catch (InterruptedException ignored) {}
                    return Conexao.testarConexao();
                }
                
                @Override
                protected void done() {
                    splash.dispose();
                    try {
                        if (get()) {
                            MainFrame frame = new MainFrame();
                            frame.setVisible(true);
                        } else {
                            mostrarErroConexao();
                        }
                    } catch (Exception e) {
                        mostrarErroConexao();
                    }
                }
            };
            worker.execute();
        });
    }
    
    private static JDialog criarSplash() {
        JDialog splash = new JDialog();
        splash.setUndecorated(true);
        splash.setSize(380, 160);
        splash.setLocationRelativeTo(null);
        splash.setModal(false);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 120, 215), 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        panel.setBackground(Color.WHITE);
        
        JLabel lblTitulo = new JLabel("📦 Sistema de Controle de Estoque");
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTitulo.setForeground(new Color(0, 80, 150));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblVersao = new JLabel("v2.0");
        lblVersao.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblVersao.setForeground(Color.GRAY);
        lblVersao.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(300, 20));
        
        JLabel lblStatus = new JLabel("Conectando ao banco de dados...");
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblStatus.setForeground(Color.GRAY);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblVersao);
        panel.add(Box.createVerticalStrut(20));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblStatus);
        
        splash.add(panel);
        
        return splash;
    }
    
    private static void mostrarErroConexao() {
        String mensagem = """
            ❌ Não foi possível conectar ao banco de dados!
            
            Verifique se:
            • O MySQL está rodando
            • O banco 'sistema_estoque' foi criado
            • As credenciais em dao/Conexao.java estão corretas
            
            Execute o script sql/schema.sql para criar o banco.
            
            O sistema será encerrado.
            """;
        
        JOptionPane.showMessageDialog(null, mensagem, "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
