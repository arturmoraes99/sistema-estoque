package service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;

import model.Produto;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportacaoService {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    public void exportarParaExcel(List<Produto> produtos, File arquivo, String titulo) throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Estoque");
            
            // ===== ESTILOS =====
            CellStyle estiloTitulo = criarEstiloTitulo(workbook);
            CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);
            CellStyle estiloCelula = criarEstiloCelula(workbook);
            CellStyle estiloMoeda = criarEstiloMoeda(workbook);
            CellStyle estiloAlerta = criarEstiloAlerta(workbook);
            CellStyle estiloCritico = criarEstiloCritico(workbook);
            
            DataFormat formato = workbook.createDataFormat();
            
            int linhaAtual = 0;
            
            // ===== TÍTULO =====
            Row rowTitulo = sheet.createRow(linhaAtual++);
            org.apache.poi.ss.usermodel.Cell cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue(titulo);
            cellTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            
            // Data de geração
            Row rowData = sheet.createRow(linhaAtual++);
            org.apache.poi.ss.usermodel.Cell cellData = rowData.createCell(0);
            cellData.setCellValue("Gerado em: " + LocalDateTime.now().format(DATE_FORMATTER));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
            
            linhaAtual++; // Linha em branco
            
            // ===== CABEÇALHO =====
            String[] colunas = {"ID", "Nome", "Categoria", "Preço", "Quantidade", "Qtd. Mínima", "Valor em Estoque", "Status"};
            
            Row rowCabecalho = sheet.createRow(linhaAtual++);
            for (int i = 0; i < colunas.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = rowCabecalho.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(estiloCabecalho);
            }
            
            // ===== DADOS =====
            BigDecimal valorTotalEstoque = BigDecimal.ZERO;
            int totalItens = 0;
            
            for (Produto produto : produtos) {
                Row row = sheet.createRow(linhaAtual++);
                
                CellStyle estiloLinha = estiloCelula;
                if (produto.isEstoqueZerado()) {
                    estiloLinha = estiloCritico;
                } else if (produto.isEstoqueBaixo()) {
                    estiloLinha = estiloAlerta;
                }
                
                // ID
                org.apache.poi.ss.usermodel.Cell cellId = row.createCell(0);
                cellId.setCellValue(produto.getId());
                cellId.setCellStyle(estiloLinha);
                
                // Nome
                org.apache.poi.ss.usermodel.Cell cellNome = row.createCell(1);
                cellNome.setCellValue(produto.getNome());
                cellNome.setCellStyle(estiloLinha);
                
                // Categoria
                org.apache.poi.ss.usermodel.Cell cellCategoria = row.createCell(2);
                cellCategoria.setCellValue(produto.getCategoria() != null ? produto.getCategoria() : "");
                cellCategoria.setCellStyle(estiloLinha);
                
                // Preço
                org.apache.poi.ss.usermodel.Cell cellPreco = row.createCell(3);
                cellPreco.setCellValue(produto.getPreco().doubleValue());
                CellStyle estiloMoedaLinha = workbook.createCellStyle();
                estiloMoedaLinha.cloneStyleFrom(estiloLinha);
                estiloMoedaLinha.setDataFormat(formato.getFormat("R$ #,##0.00"));
                cellPreco.setCellStyle(estiloMoedaLinha);
                
                // Quantidade
                org.apache.poi.ss.usermodel.Cell cellQtd = row.createCell(4);
                cellQtd.setCellValue(produto.getQuantidade());
                cellQtd.setCellStyle(estiloLinha);
                
                // Quantidade Mínima
                org.apache.poi.ss.usermodel.Cell cellQtdMin = row.createCell(5);
                cellQtdMin.setCellValue(produto.getQuantidadeMinima());
                cellQtdMin.setCellStyle(estiloLinha);
                
                // Valor em Estoque
                BigDecimal valorEstoque = produto.getValorTotalEstoque();
                org.apache.poi.ss.usermodel.Cell cellValorEstoque = row.createCell(6);
                cellValorEstoque.setCellValue(valorEstoque.doubleValue());
                CellStyle estiloMoedaLinha2 = workbook.createCellStyle();
                estiloMoedaLinha2.cloneStyleFrom(estiloLinha);
                estiloMoedaLinha2.setDataFormat(formato.getFormat("R$ #,##0.00"));
                cellValorEstoque.setCellStyle(estiloMoedaLinha2);
                
                // Status
                org.apache.poi.ss.usermodel.Cell cellStatus = row.createCell(7);
                cellStatus.setCellValue(produto.getStatusEstoque());
                cellStatus.setCellStyle(estiloLinha);
                
                valorTotalEstoque = valorTotalEstoque.add(valorEstoque);
                totalItens += produto.getQuantidade();
            }
            
            linhaAtual++;
            
            // ===== TOTAIS =====
            Font fonteTotal = workbook.createFont();
            fonteTotal.setBold(true);
            CellStyle estiloTotal = workbook.createCellStyle();
            estiloTotal.setFont(fonteTotal);
            
            Row rowResumo = sheet.createRow(linhaAtual++);
            org.apache.poi.ss.usermodel.Cell cellResumo = rowResumo.createCell(0);
            cellResumo.setCellValue("RESUMO:");
            cellResumo.setCellStyle(estiloTotal);
            
            Row rowTotalProdutos = sheet.createRow(linhaAtual++);
            rowTotalProdutos.createCell(0).setCellValue("Total de Produtos:");
            rowTotalProdutos.createCell(1).setCellValue(produtos.size());
            
            Row rowTotalItens = sheet.createRow(linhaAtual++);
            rowTotalItens.createCell(0).setCellValue("Total de Itens:");
            rowTotalItens.createCell(1).setCellValue(totalItens);
            
            Row rowValorTotal = sheet.createRow(linhaAtual++);
            rowValorTotal.createCell(0).setCellValue("Valor Total do Estoque:");
            org.apache.poi.ss.usermodel.Cell cellValorTotal = rowValorTotal.createCell(1);
            cellValorTotal.setCellValue(valorTotalEstoque.doubleValue());
            cellValorTotal.setCellStyle(estiloMoeda);
            
            // Ajusta largura das colunas
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }
            
            // Salva o arquivo
            try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                workbook.write(fos);
            }
        }
    }
    
    private CellStyle criarEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fonte = workbook.createFont();
        fonte.setBold(true);
        fonte.setFontHeightInPoints((short) 16);
        fonte.setColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFont(fonte);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }
    
    private CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fonte = workbook.createFont();
        fonte.setBold(true);
        fonte.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fonte);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }
    
    private CellStyle criarEstiloCelula(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }
    
    private CellStyle criarEstiloMoeda(Workbook workbook) {
        CellStyle estilo = criarEstiloCelula(workbook);
        DataFormat formato = workbook.createDataFormat();
        estilo.setDataFormat(formato.getFormat("R$ #,##0.00"));
        return estilo;
    }
    
    private CellStyle criarEstiloAlerta(Workbook workbook) {
        CellStyle estilo = criarEstiloCelula(workbook);
        estilo.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }
    
    private CellStyle criarEstiloCritico(Workbook workbook) {
        CellStyle estilo = criarEstiloCelula(workbook);
        estilo.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }
        

    public void exportarParaPDF(List<Produto> produtos, File arquivo, String titulo) throws IOException {
        
        try (PdfWriter writer = new PdfWriter(arquivo);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4.rotate())) {
            
            document.setMargins(20, 20, 20, 20);
            
            PdfFont fonteTitulo = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fonteNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fonteBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            
            DeviceRgb corCabecalho = new DeviceRgb(0, 51, 102);
            DeviceRgb corAlerta = new DeviceRgb(255, 255, 200);
            DeviceRgb corCritico = new DeviceRgb(255, 200, 200);
            
            // Título
            Paragraph pTitulo = new Paragraph(titulo)
                .setFont(fonteTitulo)
                .setFontSize(18)
                .setFontColor(corCabecalho)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
            document.add(pTitulo);
            
            // Data
            Paragraph pData = new Paragraph("Gerado em: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFont(fonteNormal)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(pData);
            
            // Tabela
            float[] columnWidths = {40, 180, 80, 70, 60, 60, 90, 70};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            // Cabeçalhos
            String[] headers = {"ID", "Nome", "Categoria", "Preço", "Qtd", "Mín", "Valor Est.", "Status"};
            
            for (String header : headers) {
                Cell cell = new Cell()
                    .add(new Paragraph(header).setFont(fonteBold).setFontSize(10))
                    .setBackgroundColor(corCabecalho)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5);
                table.addHeaderCell(cell);
            }
            
            // Dados
            BigDecimal valorTotalEstoque = BigDecimal.ZERO;
            int totalItens = 0;
            
            for (Produto produto : produtos) {
                DeviceRgb corFundo = null;
                if (produto.isEstoqueZerado()) {
                    corFundo = corCritico;
                } else if (produto.isEstoqueBaixo()) {
                    corFundo = corAlerta;
                }
                
                adicionarCelulaPDF(table, String.valueOf(produto.getId()), fonteNormal, TextAlignment.CENTER, corFundo);
                adicionarCelulaPDF(table, produto.getNome(), fonteNormal, TextAlignment.LEFT, corFundo);
                adicionarCelulaPDF(table, produto.getCategoria() != null ? produto.getCategoria() : "", fonteNormal, TextAlignment.CENTER, corFundo);
                adicionarCelulaPDF(table, String.format("R$ %.2f", produto.getPreco()), fonteNormal, TextAlignment.RIGHT, corFundo);
                adicionarCelulaPDF(table, String.valueOf(produto.getQuantidade()), fonteNormal, TextAlignment.CENTER, corFundo);
                adicionarCelulaPDF(table, String.valueOf(produto.getQuantidadeMinima()), fonteNormal, TextAlignment.CENTER, corFundo);
                
                BigDecimal valorEstoque = produto.getValorTotalEstoque();
                adicionarCelulaPDF(table, String.format("R$ %.2f", valorEstoque), fonteNormal, TextAlignment.RIGHT, corFundo);
                adicionarCelulaPDF(table, produto.getStatusEstoque(), fonteBold, TextAlignment.CENTER, corFundo);
                
                valorTotalEstoque = valorTotalEstoque.add(valorEstoque);
                totalItens += produto.getQuantidade();
            }
            
            document.add(table);
            
            // Legenda
            document.add(new Paragraph("\n"));
            Table tabelaLegenda = new Table(2);
            tabelaLegenda.addCell(new Cell().setBackgroundColor(corAlerta).setWidth(20).setHeight(15));
            tabelaLegenda.addCell(new Cell().add(new Paragraph(" Estoque Baixo").setFont(fonteNormal).setFontSize(9)).setBorder(null));
            tabelaLegenda.addCell(new Cell().setBackgroundColor(corCritico).setWidth(20).setHeight(15));
            tabelaLegenda.addCell(new Cell().add(new Paragraph(" Estoque Esgotado").setFont(fonteNormal).setFontSize(9)).setBorder(null));
            document.add(tabelaLegenda);
            
            // Resumo
            document.add(new Paragraph("\n"));
            Paragraph pResumo = new Paragraph("RESUMO DO ESTOQUE")
                .setFont(fonteBold)
                .setFontSize(12)
                .setMarginTop(20);
            document.add(pResumo);
            
            Table tabelaResumo = new Table(2);
            tabelaResumo.setWidth(UnitValue.createPercentValue(40));
            
            adicionarCelulaResumoPDF(tabelaResumo, "Total de Produtos:", String.valueOf(produtos.size()), fonteBold, fonteNormal);
            adicionarCelulaResumoPDF(tabelaResumo, "Total de Itens:", String.valueOf(totalItens), fonteBold, fonteNormal);
            adicionarCelulaResumoPDF(tabelaResumo, "Valor Total:", String.format("R$ %.2f", valorTotalEstoque), fonteBold, fonteBold);
            
            long produtosBaixos = produtos.stream().filter(Produto::isEstoqueBaixo).count();
            adicionarCelulaResumoPDF(tabelaResumo, "Produtos em Alerta:", String.valueOf(produtosBaixos), fonteBold, fonteNormal);
            
            document.add(tabelaResumo);
            
            // Rodapé
            document.add(new Paragraph("\n"));
            Paragraph rodape = new Paragraph("Sistema de Controle de Estoque v2.0")
                .setFont(fonteNormal)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(rodape);
        }
    }
    
    private void adicionarCelulaPDF(Table table, String texto, PdfFont fonte, TextAlignment alinhamento, DeviceRgb corFundo) {
        Cell cell = new Cell()
            .add(new Paragraph(texto).setFont(fonte).setFontSize(9))
            .setTextAlignment(alinhamento)
            .setPadding(4);
        if (corFundo != null) {
            cell.setBackgroundColor(corFundo);
        }
        table.addCell(cell);
    }
    
    private void adicionarCelulaResumoPDF(Table table, String label, String valor, PdfFont fonteLabel, PdfFont fonteValor) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(fonteLabel).setFontSize(10)).setTextAlignment(TextAlignment.LEFT));
        table.addCell(new Cell().add(new Paragraph(valor).setFont(fonteValor).setFontSize(10)).setTextAlignment(TextAlignment.RIGHT));
    }
    

    public String gerarNomeArquivo(String prefixo, String extensao) {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
        return prefixo + "_" + timestamp + "." + extensao;
    }
}
