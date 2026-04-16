package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class StatusPanel extends JPanel {
    
    private JLabel lblTotalProdutos;
    private JLabel lblTotalItens;
    private JLabel lblValorEstoque;
    private JLabel lblAlertasBaixo;
    
    private static final NumberFormat CURRENCY_FORMAT = 
        NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    public StatusPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        setBorder(new EmptyBorder(5, 10, 5, 10));
        setBackground(new Color(245, 245, 245));
        
        Font fonteBold = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        Font fonteNormal = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        
        // Total de produtos
        add(criarLabel("📦 Produtos:", fonteBold));
        lblTotalProdutos = criarLabel("0", fonteNormal);
        add(lblTotalProdutos);
        
        add(criarSeparador());
        
        // Total de itens
        add(criarLabel("📋 Itens:", fonteBold));
        lblTotalItens = criarLabel("0", fonteNormal);
        add(lblTotalItens);
        
        add(criarSeparador());
        
        // Valor do estoque
        add(criarLabel("💰 Valor Total:", fonteBold));
        lblValorEstoque = criarLabel("R$ 0,00", fonteNormal);
        lblValorEstoque.setForeground(new Color(0, 100, 0));
        add(lblValorEstoque);
        
        add(criarSeparador());
        
        // Alertas
        add(criarLabel("⚠️ Alertas:", fonteBold));
        lblAlertasBaixo = criarLabel("0", fonteNormal);
        add(lblAlertasBaixo);
    }
    
    private JLabel criarLabel(String texto, Font fonte) {
        JLabel label = new JLabel(texto);
        label.setFont(fonte);
        return label;
    }
    
    private JSeparator criarSeparador() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 20));
        return sep;
    }
    
    public void atualizarEstatisticas(int totalProdutos, int totalItens, 
                                       BigDecimal valorEstoque, int alertas) {
        lblTotalProdutos.setText(String.valueOf(totalProdutos));
        lblTotalItens.setText(String.valueOf(totalItens));
        lblValorEstoque.setText(CURRENCY_FORMAT.format(valorEstoque));
        
        lblAlertasBaixo.setText(String.valueOf(alertas));
        if (alertas > 0) {
            lblAlertasBaixo.setForeground(Color.RED);
            lblAlertasBaixo.setText(alertas + " ⚠️");
        } else {
            lblAlertasBaixo.setForeground(new Color(0, 128, 0));
            lblAlertasBaixo.setText("0 ✓");
        }
    }
}
