package ui;
import dao.Conexao;
import model.Produto;
import service.EstoqueService;
import service.ExportacaoService;
import ui.components.StatusPanel;
import ui.dialogs.MovimentacaoDialog;
import ui.dialogs.MovimentacaoDialog.TipoMovimentacao;
import ui.dialogs.ProdutoDialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MainFrame extends JFrame {
    
    private final EstoqueService estoqueService;
    private final ExportacaoService exportacaoService;
    
    private JTable tabelaProdutos;
    private ProdutoTableModel tableModel;
    private TableRowSorter<ProdutoTableModel> sorter;
    private JTextField txtBusca;
    private StatusPanel statusPanel;
    
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnEntrada;
    private JButton btnSaida;
    
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    public MainFrame() {
        this.estoqueService = new EstoqueService();
        this.exportacaoService = new ExportacaoService();
        
        initComponents();
        configurarJanela();
        carregarDados();
        verificarAlertas();
    }
    
    private void configurarJanela() {
        setTitle("📦 Sistema de Controle de Estoque v2.0");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 700);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sair();
            }
        });
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setJMenuBar(criarMenuBar());
        add(criarToolbar(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        statusPanel = new StatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JMenuBar criarMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Arquivo
        JMenu menuArquivo = new JMenu("Arquivo");
        menuArquivo.setMnemonic('A');
        
        JMenuItem itemExportarExcel = new JMenuItem("Exportar para Excel...");
        itemExportarExcel.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
        itemExportarExcel.addActionListener(e -> exportarParaExcel());
        
        JMenuItem itemExportarPDF = new JMenuItem("Exportar para PDF...");
        itemExportarPDF.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
        itemExportarPDF.addActionListener(e -> exportarParaPDF());
        
        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        itemSair.addActionListener(e -> sair());
        
        menuArquivo.add(itemExportarExcel);
        menuArquivo.add(itemExportarPDF);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);
        
        // Menu Produtos
        JMenu menuProdutos = new JMenu("Produtos");
        menuProdutos.setMnemonic('P');
        
        JMenuItem itemNovo = new JMenuItem("Novo Produto...");
        itemNovo.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        itemNovo.addActionListener(e -> novoProduto());
        
        JMenuItem itemEditar = new JMenuItem("Editar Produto...");
        itemEditar.setAccelerator(KeyStroke.getKeyStroke("ctrl M"));
        itemEditar.addActionListener(e -> editarProduto());
        
        JMenuItem itemExcluir = new JMenuItem("Excluir Produto");
        itemExcluir.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        itemExcluir.addActionListener(e -> excluirProduto());
        
        menuProdutos.add(itemNovo);
        menuProdutos.add(itemEditar);
        menuProdutos.add(itemExcluir);
        
        // Menu Estoque
        JMenu menuEstoque = new JMenu("Estoque");
        menuEstoque.setMnemonic('E');
        
        JMenuItem itemEntrada = new JMenuItem("Registrar Entrada...");
        itemEntrada.setAccelerator(KeyStroke.getKeyStroke("F2"));
        itemEntrada.addActionListener(e -> registrarEntrada());
        
        JMenuItem itemSaida = new JMenuItem("Registrar Saída...");
        itemSaida.setAccelerator(KeyStroke.getKeyStroke("F3"));
        itemSaida.addActionListener(e -> registrarSaida());
        
        JMenuItem itemAlertas = new JMenuItem("Ver Alertas de Estoque Baixo");
        itemAlertas.setAccelerator(KeyStroke.getKeyStroke("F5"));
        itemAlertas.addActionListener(e -> mostrarAlertas());
        
        menuEstoque.add(itemEntrada);
        menuEstoque.add(itemSaida);
        menuEstoque.addSeparator();
        menuEstoque.add(itemAlertas);
        
        // Menu Ajuda
        JMenu menuAjuda = new JMenu("Ajuda");
        menuAjuda.setMnemonic('J');
        
        JMenuItem itemSobre = new JMenuItem("Sobre");
        itemSobre.addActionListener(e -> mostrarSobre());
        
        menuAjuda.add(itemSobre);
        
        menuBar.add(menuArquivo);
        menuBar.add(menuProdutos);
        menuBar.add(menuEstoque);
        menuBar.add(menuAjuda);
        
        return menuBar;
    }
    
    private JToolBar criarToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JButton btnNovo = criarBotaoToolbar("➕ Novo", "Cadastrar novo produto (Ctrl+N)");
        btnNovo.addActionListener(e -> novoProduto());
        toolbar.add(btnNovo);
        
        btnEditar = criarBotaoToolbar("✏️ Editar", "Editar produto selecionado (Ctrl+M)");
        btnEditar.setEnabled(false);
        btnEditar.addActionListener(e -> editarProduto());
        toolbar.add(btnEditar);
        
        btnExcluir = criarBotaoToolbar("🗑️ Excluir", "Excluir produto selecionado (Delete)");
        btnExcluir.setEnabled(false);
        btnExcluir.addActionListener(e -> excluirProduto());
        toolbar.add(btnExcluir);
        
        toolbar.addSeparator(new Dimension(20, 0));
        
        btnEntrada = criarBotaoToolbar("📥 Entrada", "Registrar entrada de estoque (F2)");
        btnEntrada.setBackground(new Color(40, 167, 69));
        btnEntrada.setEnabled(false);
        btnEntrada.addActionListener(e -> registrarEntrada());
        toolbar.add(btnEntrada);
        
        btnSaida = criarBotaoToolbar("📤 Saída", "Registrar saída de estoque (F3)");
        btnSaida.setBackground(new Color(220, 53, 69));
        btnSaida.setEnabled(false);
        btnSaida.addActionListener(e -> registrarSaida());
        toolbar.add(btnSaida);
        
        toolbar.addSeparator(new Dimension(20, 0));
        
        JButton btnAlertas = criarBotaoToolbar("⚠️ Alertas", "Ver produtos com estoque baixo (F5)");
        btnAlertas.setBackground(new Color(255, 193, 7));
        btnAlertas.addActionListener(e -> mostrarAlertas());
        toolbar.add(btnAlertas);
        
        toolbar.add(Box.createHorizontalGlue());
        
        toolbar.add(new JLabel("🔍 "));
        txtBusca = new JTextField(20);
        txtBusca.setMaximumSize(new Dimension(250, 30));
        txtBusca.setToolTipText("Digite para filtrar produtos...");
        txtBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabela();
            }
        });
        toolbar.add(txtBusca);
        
        toolbar.add(Box.createHorizontalStrut(10));
        
        JButton btnAtualizar = criarBotaoToolbar("🔄 Atualizar", "Recarregar dados");
        btnAtualizar.addActionListener(e -> carregarDados());
        toolbar.add(btnAtualizar);
        
        return toolbar;
    }
    
    private JButton criarBotaoToolbar(String texto, String tooltip) {
        JButton btn = new JButton(texto);
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JPanel criarPainelCentral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        tableModel = new ProdutoTableModel();
        tabelaProdutos = new JTable(tableModel);
        
        tabelaProdutos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaProdutos.setRowHeight(28);
        tabelaProdutos.setShowGrid(true);
        tabelaProdutos.setGridColor(new Color(230, 230, 230));
        tabelaProdutos.getTableHeader().setReorderingAllowed(false);
        
        sorter = new TableRowSorter<>(tableModel);
        tabelaProdutos.setRowSorter(sorter);
        
        configurarRenderizadores();
        
        tabelaProdutos.getSelectionModel().addListSelectionListener(e -> {
            boolean temSelecao = tabelaProdutos.getSelectedRow() >= 0;
            btnEditar.setEnabled(temSelecao);
            btnExcluir.setEnabled(temSelecao);
            btnEntrada.setEnabled(temSelecao);
            btnSaida.setEnabled(temSelecao);
        });
        
        tabelaProdutos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarProduto();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void configurarRenderizadores() {
        int[] larguras = {50, 250, 100, 100, 80, 60, 120, 100};
        for (int i = 0; i < larguras.length; i++) {
            tabelaProdutos.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        
        // Renderizador para valores monetários
        DefaultTableCellRenderer renderMoeda = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof BigDecimal) {
                    value = CURRENCY_FORMAT.format(value);
                }
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(RIGHT);
                return c;
            }
        };
        
        tabelaProdutos.getColumnModel().getColumn(3).setCellRenderer(renderMoeda);
        tabelaProdutos.getColumnModel().getColumn(6).setCellRenderer(renderMoeda);
        
        // Renderizador centralizado
        DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
        renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
        
        tabelaProdutos.getColumnModel().getColumn(0).setCellRenderer(renderCenter);
        tabelaProdutos.getColumnModel().getColumn(4).setCellRenderer(renderCenter);
        tabelaProdutos.getColumnModel().getColumn(5).setCellRenderer(renderCenter);
        
        // Renderizador para status com cores
        tabelaProdutos.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                
                String status = value != null ? value.toString() : "";
                if (!isSelected) {
                    if (status.contains("ESGOTADO")) {
                        setBackground(new Color(255, 200, 200));
                        setForeground(Color.RED.darker());
                    } else if (status.contains("BAIXO")) {
                        setBackground(new Color(255, 255, 200));
                        setForeground(new Color(150, 100, 0));
                    } else {
                        setBackground(new Color(200, 255, 200));
                        setForeground(new Color(0, 100, 0));
                    }
                } else {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });
    }
    
    private void carregarDados() {
        SwingWorker<List<Produto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Produto> doInBackground() {
                return estoqueService.listarProdutos();
            }
            
            @Override
            protected void done() {
                try {
                    List<Produto> produtos = get();
                    tableModel.setProdutos(produtos);
                    atualizarStatusBar();
                } catch (Exception e) {
                    mostrarErro("Erro ao carregar dados: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void atualizarStatusBar() {
        List<Produto> produtos = tableModel.getProdutos();
        int totalProdutos = produtos.size();
        int totalItens = produtos.stream().mapToInt(Produto::getQuantidade).sum();
        BigDecimal valorTotal = produtos.stream()
            .map(Produto::getValorTotalEstoque)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int alertas = (int) produtos.stream().filter(Produto::isEstoqueBaixo).count();
        
        statusPanel.atualizarEstatisticas(totalProdutos, totalItens, valorTotal, alertas);
    }
    
    private void filtrarTabela() {
        String texto = txtBusca.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }
    
    private void novoProduto() {
        ProdutoDialog dialog = new ProdutoDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            Produto produto = dialog.getProduto();
            Produto salvo = estoqueService.cadastrarProduto(
                produto.getNome(), produto.getDescricao(), produto.getPreco(),
                produto.getQuantidade(), produto.getQuantidadeMinima(), produto.getCategoria()
            );
            
            if (salvo != null) {
                carregarDados();
                mostrarSucesso("Produto cadastrado com sucesso!");
            } else {
                mostrarErro("Erro ao cadastrar produto.");
            }
        }
    }
    
    private void editarProduto() {
        Produto produto = getProdutoSelecionado();
        if (produto == null) {
            mostrarAviso("Selecione um produto para editar.");
            return;
        }
        
        ProdutoDialog dialog = new ProdutoDialog(this, produto);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            if (estoqueService.atualizarProduto(dialog.getProduto())) {
                carregarDados();
                mostrarSucesso("Produto atualizado com sucesso!");
            } else {
                mostrarErro("Erro ao atualizar produto.");
            }
        }
    }
    
    private void excluirProduto() {
        Produto produto = getProdutoSelecionado();
        if (produto == null) {
            mostrarAviso("Selecione um produto para excluir.");
            return;
        }
        
        int opcao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente excluir o produto:\n\n" + produto.getNome() + "\n\nEsta ação não pode ser desfeita.",
            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            if (estoqueService.removerProduto(produto.getId())) {
                carregarDados();
                mostrarSucesso("Produto excluído com sucesso!");
            } else {
                mostrarErro("Erro ao excluir produto.");
            }
        }
    }
    
    private void registrarEntrada() {
        Produto produto = getProdutoSelecionado();
        if (produto == null) {
            mostrarAviso("Selecione um produto para registrar entrada.");
            return;
        }
        
        MovimentacaoDialog dialog = new MovimentacaoDialog(this, produto, TipoMovimentacao.ENTRADA);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            if (estoqueService.entradaEstoque(produto.getId(), dialog.getQuantidade(), dialog.getObservacao())) {
                carregarDados();
                mostrarSucesso("Entrada registrada com sucesso!");
            } else {
                mostrarErro("Erro ao registrar entrada.");
            }
        }
    }
    
    private void registrarSaida() {
        Produto produto = getProdutoSelecionado();
        if (produto == null) {
            mostrarAviso("Selecione um produto para registrar saída.");
            return;
        }
        
        if (produto.getQuantidade() == 0) {
            mostrarAviso("Este produto está com estoque zerado!");
            return;
        }
        
        MovimentacaoDialog dialog = new MovimentacaoDialog(this, produto, TipoMovimentacao.SAIDA);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            if (estoqueService.saidaEstoque(produto.getId(), dialog.getQuantidade(), dialog.getObservacao())) {
                carregarDados();
                mostrarSucesso("Saída registrada com sucesso!");
                
                Optional<Produto> atualizado = estoqueService.buscarProduto(produto.getId());
                atualizado.ifPresent(p -> {
                    if (p.isEstoqueBaixo()) {
                        mostrarAviso("⚠️ ATENÇÃO: O estoque do produto ficou baixo!");
                    }
                });
            } else {
                mostrarErro("Erro ao registrar saída. Verifique se há estoque suficiente.");
            }
        }
    }
    
    private void mostrarAlertas() {
        List<Produto> produtosBaixos = estoqueService.listarEstoqueBaixo();
        
        if (produtosBaixos.isEmpty()) {
            mostrarSucesso("✅ Nenhum produto com estoque baixo!\nTodos os produtos estão com estoque adequado.");
            return;
        }
        
        String[] colunas = {"ID", "Nome", "Atual", "Mínimo", "Status"};
        Object[][] dados = new Object[produtosBaixos.size()][5];
        
        for (int i = 0; i < produtosBaixos.size(); i++) {
            Produto p = produtosBaixos.get(i);
            dados[i][0] = p.getId();
            dados[i][1] = p.getNome();
            dados[i][2] = p.getQuantidade();
            dados[i][3] = p.getQuantidadeMinima();
            dados[i][4] = p.isEstoqueZerado() ? "🔴 ESGOTADO" : "🟡 BAIXO";
        }
        
        JTable tabelaAlertas = new JTable(dados, colunas);
        tabelaAlertas.setEnabled(false);
        tabelaAlertas.setRowHeight(25);
        
        JScrollPane scroll = new JScrollPane(tabelaAlertas);
        scroll.setPreferredSize(new Dimension(500, Math.min(200, produtosBaixos.size() * 30 + 50)));
        
        JOptionPane.showMessageDialog(this, scroll,
            "⚠️ Alertas de Estoque Baixo (" + produtosBaixos.size() + " produtos)",
            JOptionPane.WARNING_MESSAGE);
    }
    
    private void verificarAlertas() {
        List<Produto> produtosBaixos = estoqueService.listarEstoqueBaixo();
        
        if (!produtosBaixos.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                int opcao = JOptionPane.showConfirmDialog(this,
                    "⚠️ Existem " + produtosBaixos.size() + " produto(s) com estoque baixo!\n\nDeseja ver os detalhes?",
                    "Alerta de Estoque", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (opcao == JOptionPane.YES_OPTION) {
                    mostrarAlertas();
                }
            });
        }
    }
    
    private void exportarParaExcel() {
        List<Produto> produtos = tableModel.getProdutos();
        
        if (produtos.isEmpty()) {
            mostrarAviso("Não há produtos para exportar.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar como Excel");
        fileChooser.setSelectedFile(new File(exportacaoService.gerarNomeArquivo("estoque", "xlsx")));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (*.xlsx)", "xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".xlsx")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".xlsx");
            }
            
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                exportacaoService.exportarParaExcel(produtos, arquivo, "Relatório de Estoque");
                setCursor(Cursor.getDefaultCursor());
                
                int opcao = JOptionPane.showConfirmDialog(this,
                    "Arquivo exportado com sucesso!\n\n" + arquivo.getAbsolutePath() + "\n\nDeseja abrir o arquivo?",
                    "Exportação Concluída", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                
                if (opcao == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(arquivo);
                }
            } catch (Exception e) {
                setCursor(Cursor.getDefaultCursor());
                mostrarErro("Erro ao exportar para Excel:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void exportarParaPDF() {
        List<Produto> produtos = tableModel.getProdutos();
        
        if (produtos.isEmpty()) {
            mostrarAviso("Não há produtos para exportar.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar como PDF");
        fileChooser.setSelectedFile(new File(exportacaoService.gerarNomeArquivo("estoque", "pdf")));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos PDF (*.pdf)", "pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".pdf")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".pdf");
            }
            
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                exportacaoService.exportarParaPDF(produtos, arquivo, "Relatório de Estoque");
                setCursor(Cursor.getDefaultCursor());
                
                int opcao = JOptionPane.showConfirmDialog(this,
                    "Arquivo exportado com sucesso!\n\n" + arquivo.getAbsolutePath() + "\n\nDeseja abrir o arquivo?",
                    "Exportação Concluída", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                
                if (opcao == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(arquivo);
                }
            } catch (Exception e) {
                setCursor(Cursor.getDefaultCursor());
                mostrarErro("Erro ao exportar para PDF:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private Produto getProdutoSelecionado() {
        int viewRow = tabelaProdutos.getSelectedRow();
        if (viewRow < 0) return null;
        
        int modelRow = tabelaProdutos.convertRowIndexToModel(viewRow);
        return tableModel.getProdutoAt(modelRow);
    }
    
    private void mostrarSucesso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    private void mostrarAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
    
    private void mostrarSobre() {
        String mensagem = """
            📦 Sistema de Controle de Estoque v2.0
            
            Desenvolvido com Java Swing
            
            Funcionalidades:
            • CRUD completo de produtos
            • Controle de entrada e saída
            • Alertas de estoque baixo
            • Exportação para Excel e PDF
            
            © 2024 - Todos os direitos reservados
            """;
        JOptionPane.showMessageDialog(this, mensagem, "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void sair() {
        int opcao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente sair do sistema?", "Confirmar Saída",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            Conexao.fecharConexao();
            dispose();
            System.exit(0);
        }
    }
}
