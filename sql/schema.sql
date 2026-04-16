-- ============================================
-- SISTEMA DE CONTROLE DE ESTOQUE
-- Script de Criação do Banco de Dados
-- ============================================

-- Criar o banco de dados
CREATE DATABASE IF NOT EXISTS sistema_estoque
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_estoque;


DROP TABLE IF EXISTS movimentacoes;
DROP TABLE IF EXISTS produtos;

CREATE TABLE produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    quantidade INT NOT NULL DEFAULT 0,
    quantidade_minima INT NOT NULL DEFAULT 5,
    categoria VARCHAR(50),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    
    INDEX idx_nome (nome),
    INDEX idx_categoria (categoria),
    INDEX idx_ativo (ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE movimentacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    produto_id INT NOT NULL,
    tipo ENUM('ENTRADA', 'SAIDA') NOT NULL,
    quantidade INT NOT NULL,
    observacao VARCHAR(255),
    data_movimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE,
    INDEX idx_produto (produto_id),
    INDEX idx_tipo (tipo),
    INDEX idx_data (data_movimentacao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO produtos (nome, descricao, preco, quantidade, quantidade_minima, categoria) VALUES
('Notebook Dell Inspiron 15', 'Notebook 15.6" Intel Core i5 8GB RAM 256GB SSD', 3500.00, 15, 3, 'Informática'),
('Mouse Logitech MX Master 3', 'Mouse sem fio ergonômico com scroll eletromagnético', 450.00, 25, 5, 'Periféricos'),
('Teclado Mecânico Redragon Kumara', 'Teclado mecânico RGB switches Outemu Blue', 280.00, 2, 5, 'Periféricos'),
('Monitor LG UltraWide 29"', 'Monitor IPS 29" 2560x1080 75Hz HDR10', 1200.00, 8, 2, 'Informática'),
('Cabo HDMI 2.0 Premium 2m', 'Cabo HDMI 2.0 4K 60Hz com ethernet', 35.00, 50, 10, 'Acessórios'),
('Webcam Logitech C920', 'Webcam Full HD 1080p com microfone integrado', 350.00, 12, 3, 'Periféricos'),
('SSD Kingston A400 480GB', 'SSD SATA III 2.5" 500MB/s leitura', 220.00, 30, 8, 'Componentes'),
('Headset HyperX Cloud II', 'Headset gamer 7.1 surround com microfone', 480.00, 0, 5, 'Periféricos'),
('Memória RAM Corsair 16GB', 'DDR4 3200MHz Vengeance LPX', 320.00, 18, 5, 'Componentes'),
('Fonte Corsair CV550', 'Fonte 550W 80 Plus Bronze', 290.00, 6, 3, 'Componentes');

-- Inserir algumas movimentações de exemplo
INSERT INTO movimentacoes (produto_id, tipo, quantidade, observacao) VALUES
(1, 'ENTRADA', 20, 'Compra inicial'),
(1, 'SAIDA', 5, 'Venda para cliente'),
(2, 'ENTRADA', 30, 'Reposição de estoque'),
(2, 'SAIDA', 5, 'Venda loja física'),
(3, 'ENTRADA', 10, 'Compra fornecedor'),
(3, 'SAIDA', 8, 'Vendas online');


CREATE OR REPLACE VIEW vw_estoque_baixo AS
SELECT 
    id,
    nome,
    categoria,
    quantidade,
    quantidade_minima,
    CASE 
        WHEN quantidade = 0 THEN 'ESGOTADO'
        WHEN quantidade <= quantidade_minima THEN 'BAIXO'
        ELSE 'NORMAL'
    END AS status_estoque
FROM produtos
WHERE ativo = TRUE 
  AND quantidade <= quantidade_minima
ORDER BY quantidade ASC;

CREATE OR REPLACE VIEW vw_resumo_estoque AS
SELECT 
    COUNT(*) AS total_produtos,
    SUM(quantidade) AS total_itens,
    SUM(preco * quantidade) AS valor_total_estoque,
    SUM(CASE WHEN quantidade <= quantidade_minima THEN 1 ELSE 0 END) AS produtos_alerta,
    SUM(CASE WHEN quantidade = 0 THEN 1 ELSE 0 END) AS produtos_esgotados
FROM produtos
WHERE ativo = TRUE;

-- Verificar dados inseridos
SELECT 'Banco de dados criado com sucesso!' AS status;
SELECT * FROM produtos;
SELECT * FROM vw_resumo_estoque;
