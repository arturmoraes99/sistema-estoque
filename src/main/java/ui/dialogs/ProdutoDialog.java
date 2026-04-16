package ui.dialogs;
import model.Produto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class ProdutoDialog extends JDialog {
    
    private JTextField txtNome;
    private JTextArea txtDescricao;
    private JTextField txtCategoria;
    private JTextField txtPreco;
    private JSpinner spnQuantidade;
    private JSpinner spnQuantidadeMinima;
    
    private Produto produto;
    private boolean confirmado = false;
    
    public ProdutoDialog(Frame parent) {
        this(parent, null);
    }
    
    public ProdutoDialog(Frame parent, Produto produtoExistente) {
        super(parent, produtoExistente == null ? "Novo Produto" : "Editar Produto", true);
        this.produto = produtoExistente;
        
        initComponents();
        
        if (produtoExistente != null) {
            preencherCampos(produtoExistente);
        }
        
        pack();
        setMinimumSize(new Dimension(450, 420));
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel do formulário
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        panelForm.add(new JLabel("Nome: *"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtNome = new JTextField(25);
        panelForm.add(txtNome, gbc);
        
        // Descrição
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(new JLabel("Descrição:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1;
        txtDescricao = new JTextArea(4, 25);
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescricao);
        panelForm.add(scrollDesc, gbc);
        
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Categoria
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelForm.add(new JLabel("Categoria:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtCategoria = new JTextField(25);
        panelForm.add(txtCategoria, gbc);
        
        // Preço
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelForm.add(new JLabel("Preço (R$): *"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtPreco = new JTextField(15);
        txtPreco.setToolTipText("Use ponto ou vírgula como separador decimal");
        panelForm.add(txtPreco, gbc);
        
        // Quantidade
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelForm.add(new JLabel("Quantidade:"), gbc);
        
        gbc.gridx = 1;
        spnQuantidade = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        ((JSpinner.DefaultEditor) spnQuantidade.getEditor()).getTextField().setColumns(10);
        panelForm.add(spnQuantidade, gbc);
        
        // Quantidade Mínima
        gbc.gridx = 0; gbc.gridy = 5;
        panelForm.add(new JLabel("Qtd. Mínima (alerta):"), gbc);
        
        gbc.gridx = 1;
        spnQuantidadeMinima = new JSpinner(new SpinnerNumberModel(5, 0, 999999, 1));
        ((JSpinner.DefaultEditor) spnQuantidadeMinima.getEditor()).getTextField().setColumns(10);
        panelForm.add(spnQuantidadeMinima, gbc);
        
        // Nota
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JLabel lblObrigatorio = new JLabel("* Campos obrigatórios");
        lblObrigatorio.setFont(lblObrigatorio.getFont().deriveFont(Font.ITALIC, 11f));
        lblObrigatorio.setForeground(Color.GRAY);
        panelForm.add(lblObrigatorio, gbc);
        
        add(panelForm, BorderLayout.CENTER);
        
        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotoes.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });
        
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(new Color(0, 120, 215));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.addActionListener(e -> salvar());
        
        panelBotoes.add(btnCancelar);
        panelBotoes.add(btnSalvar);
        
        add(panelBotoes, BorderLayout.SOUTH);
        
        // Atalhos
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        getRootPane().setDefaultButton(btnSalvar);
    }
    
    private void preencherCampos(Produto produto) {
        txtNome.setText(produto.getNome());
        txtDescricao.setText(produto.getDescricao());
        txtCategoria.setText(produto.getCategoria());
        txtPreco.setText(produto.getPreco().toString());
        spnQuantidade.setValue(produto.getQuantidade());
        spnQuantidadeMinima.setValue(produto.getQuantidadeMinima());
    }
    
    private void salvar() {
        // Validação do nome
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            mostrarErro("O nome do produto é obrigatório!");
            txtNome.requestFocus();
            return;
        }
        
        // Validação do preço
        BigDecimal preco;
        try {
            String precoStr = txtPreco.getText().trim().replace(",", ".");
            if (precoStr.isEmpty()) {
                mostrarErro("O preço é obrigatório!");
                txtPreco.requestFocus();
                return;
            }
            preco = new BigDecimal(precoStr);
            if (preco.compareTo(BigDecimal.ZERO) < 0) {
                mostrarErro("O preço não pode ser negativo!");
                txtPreco.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            mostrarErro("Preço inválido! Use números com ponto ou vírgula.");
            txtPreco.requestFocus();
            return;
        }
        
        // Cria ou atualiza o produto
        if (produto == null) {
            produto = new Produto();
        }
        
        produto.setNome(nome);
        produto.setDescricao(txtDescricao.getText().trim());
        produto.setCategoria(txtCategoria.getText().trim());
        produto.setPreco(preco);
        produto.setQuantidade((Integer) spnQuantidade.getValue());
        produto.setQuantidadeMinima((Integer) spnQuantidadeMinima.getValue());
        
        confirmado = true;
        dispose();
    }
    
    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro de Validação", JOptionPane.ERROR_MESSAGE);
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public Produto getProduto() {
        return produto;
    }
}
