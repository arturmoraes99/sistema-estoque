@"
# 📦 Sistema de Controle de Estoque 

Sistema desktop para gerenciamento de estoque desenvolvido em Java com interface gráfica moderna.

## ✨ Funcionalidades

- ✅ Cadastro de produtos (CRUD completo)
- ✅ Controle de entrada e saída de estoque
- ✅ Alertas de estoque baixo
- ✅ Exportação para Excel e PDF
- ✅ Interface moderna com FlatLaf
- ✅ Conexão com MySQL

## 🛠️ Tecnologias

- Java 17
- Maven
- MySQL 8
- Swing + FlatLaf
- Apache POI (Excel)
- iText (PDF)

## 🚀 Como executar

1. Clone o repositório
2. Configure o banco MySQL (veja ``sql/schema.sql``)
3. Execute:
   ```bash
   mvn clean package
   java -jar target/sistema-estoque-2.0.0.jar
