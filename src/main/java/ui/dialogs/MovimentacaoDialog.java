package ui.dialogs;
import model.Produto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MovimentacaoDialog extends JDialog {
    
    public enum TipoMovimentacao {
        ENTRADA("Entrada de Estoque", "Adicionar ao estoque"),
        SAIDA("Saída de Estoque", "Retirar do estoque");
        
        private final String titulo;
        private final String descricao;
        
        TipoMovimentacao(String titulo, String descricao) {
            this.titulo = titulo;
            this.descricao = descricao;
        }
        
        public String getTitulo() { return titulo; }
        public String getDescricao() { return descricao; }
    }
    
    private final Produto produto;
    private final TipoMovimentacao tipo;
    
    private JSpinner spnQuantidade;
    private JTextArea txtObservacao;
    private JLabel lblPreview;
    
    private int quantidade = 0;
    private String observacao = "";
    private boolean confirmado = false;
    
    public MovimentacaoDialog(Frame parent, Produto produto, TipoMovimentacao tipo) {
        super(parent, tipo.getTitulo(), true);
        this.produto = produto;
        this.tipo = tipo;
        
        initComponents();
        
        pack();
        setMinimumSize(new Dimension(420, 380));
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel de informações do produto
        JPanel panelInfo = new JPanel(new GridLayout(3, 2, 10, 5));
        panelInfo.setBorder(new TitledBorder("Produto Selecionado"));
        
        panelInfo.add(new JLabel("Nome:"));
        panelInfo.add(new JLabel(produto.getNome()));
        
        panelInfo.add(new JLabel("Estoque Atual:"));
        JLabel lblEstoque = new JLabel(String.valueOf(produto.getQuantidade()));
        if (produto.isEstoqueZerado()) {
            lblEstoque.setForeground(Color.RED);
            lblEstoque.setText(produto.getQuantidade() + " (ESGOTADO)");
        } else if (produto.isEstoqueBaixo()) {
            lblEstoque.setForeground(new Color(200, 150, 0));
            lblEstoque.setText(produto.getQuantidade() + " (BAIXO)");
        }
        panelInfo.add(lblEstoque);
        
        panelInfo.add(new JLabel("Qtd. Mínima:"));
        panelInfo.add(new JLabel(String.valueOf(produto.getQuantidadeMinima())));
        
        // Painel de movimentação
        JPanel panelMov = new JPanel(new GridBagLayout());
        panelMov.setBorder(new TitledBorder(tipo.getDescricao()));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Quantidade
        gbc.gridx = 0; gbc.gridy = 0;
        panelMov.add(new JLabel("Quantidade:"), gbc);
        
        gbc.gridx = 1;
        int maxValue = tipo == TipoMovimentacao.SAIDA ? Math.max(1, produto.getQuantidade()) : 999999;
        spnQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, maxValue, 1));
        ((JSpinner.DefaultEditor) spnQuantidade.getEditor()).getTextField().setColumns(10);
        panelMov.add(spnQuantidade, gbc);
        
        // Observação
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelMov.add(new JLabel("Observação:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1;
        txtObservacao = new JTextArea(3, 20);
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);
        panelMov.add(new JScrollPane(txtObservacao), gbc);
        
        // Preview
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        lblPreview = new JLabel();
        atualizarPreview();
        panelMov.add(lblPreview, gbc);
        
        spnQuantidade.addChangeListener(e -> atualizarPreview());
        
        // Painel principal
        JPanel panelMain = new JPanel(new BorderLayout(10, 10));
        panelMain.setBorder(new EmptyBorder(15, 15, 5, 15));
        panelMain.add(panelInfo, BorderLayout.NORTH);
        panelMain.add(panelMov, BorderLayout.CENTER);
        
        add(panelMain, BorderLayout.CENTER);
        
        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        String textoBotao = tipo == TipoMovimentacao.ENTRADA ? "📥 Confirmar Entrada" : "📤 Confirmar Saída";
        JButton btnConfirmar = new JButton(textoBotao);
        btnConfirmar.setBackground(tipo == TipoMovimentacao.ENTRADA ? new Color(40, 167, 69) : new Color(220, 53, 69));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.addActionListener(e -> confirmar());
        
        panelBotoes.add(btnCancelar);
        panelBotoes.add(btnConfirmar);
        
        add(panelBotoes, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(btnConfirmar);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void atualizarPreview() {
        int qtd = (Integer) spnQuantidade.getValue();
        int novoEstoque;
        
        if (tipo == TipoMovimentacao.ENTRADA) {
            novoEstoque = produto.getQuantidade() + qtd;
            lblPreview.setText(String.format("📦 Novo estoque: %d → %d (+%d)", produto.getQuantidade(), novoEstoque, qtd));
            lblPreview.setForeground(new Color(40, 167, 69));
        } else {
            novoEstoque = produto.getQuantidade() - qtd;
            lblPreview.setText(String.format("📦 Novo estoque: %d → %d (-%d)", produto.getQuantidade(), novoEstoque, qtd));
            
            if (novoEstoque == 0) {
                lblPreview.setForeground(Color.RED);
                lblPreview.setText(lblPreview.getText() + " ⚠️ FICARÁ ESGOTADO!");
            } else if (novoEstoque <= produto.getQuantidadeMinima()) {
                lblPreview.setForeground(new Color(200, 100, 0));
                lblPreview.setText(lblPreview.getText() + " ⚠️ Estoque baixo!");
            } else {
                lblPreview.setForeground(new Color(220, 53, 69));
            }
        }
    }
    
    private void confirmar() {
        quantidade = (Integer) spnQuantidade.getValue();
        observacao = txtObservacao.getText().trim();
        
        if (tipo == TipoMovimentacao.SAIDA && quantidade > produto.getQuantidade()) {
            JOptionPane.showMessageDialog(this,
                "Quantidade insuficiente em estoque!\nDisponível: " + produto.getQuantidade(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        confirmado = true;
        dispose();
    }
    
    public boolean isConfirmado() { return confirmado; }
    public int getQuantidade() { return quantidade; }
    public String getObservacao() { return observacao; }
}
