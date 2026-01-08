package com.jaasielsilva.portalceo.service.juridico;

import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.repository.ContratoLegalRepository;
import com.jaasielsilva.portalceo.repository.juridico.ProcessoJuridicoRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.repository.ProcessoPrevidenciarioRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class JuridicoRelatorioService {

    @Autowired
    private ContratoLegalRepository contratoRepo;

    @Autowired
    private ProcessoJuridicoRepository processoRepo;

    @Autowired
    private ProcessoPrevidenciarioRepository previdRepo;

    public byte[] exportarContratosExcel() throws IOException {
        List<ContratoLegal> contratos = contratoRepo.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contratos");

            // Estilo do cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] columns = { "ID", "Nº Contrato", "Título", "Contraparte", "Status", "Valor Total", "Data Início",
                    "Data Vencimento" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ContratoLegal c : contratos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getNumeroContrato() != null ? c.getNumeroContrato() : "");
                row.createCell(2).setCellValue(c.getTitulo() != null ? c.getTitulo() : "");
                row.createCell(3).setCellValue(c.getNomeContraparte() != null ? c.getNomeContraparte() : "");
                row.createCell(4).setCellValue(c.getStatus() != null ? c.getStatus().getDescricao() : "");
                row.createCell(5).setCellValue(c.getValorContrato() != null ? c.getValorContrato().doubleValue() : 0.0);
                row.createCell(6).setCellValue(c.getDataInicio() != null ? c.getDataInicio().toString() : "");
                row.createCell(7).setCellValue(c.getDataVencimento() != null ? c.getDataVencimento().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] exportarProcessosExcel() throws IOException {
        List<ProcessoJuridico> processos = processoRepo.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Processos Jurídicos");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] columns = { "ID", "Número", "Cliente", "Tipo", "Tribunal", "Parte Contrária", "Status",
                    "Data Abertura" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ProcessoJuridico p : processos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getNumero() != null ? p.getNumero() : "");
                row.createCell(2).setCellValue(p.getCliente() != null ? p.getCliente().getNome() : "");
                row.createCell(3).setCellValue(p.getTipo() != null ? p.getTipo().getDescricao() : "");
                row.createCell(4).setCellValue(p.getTribunal() != null ? p.getTribunal() : "");
                row.createCell(5).setCellValue(p.getParte() != null ? p.getParte() : "");
                row.createCell(6).setCellValue(p.getStatus() != null ? p.getStatus().name() : "");
                row.createCell(7).setCellValue(p.getDataAbertura() != null ? p.getDataAbertura().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] exportarPrevidenciarioExcel() throws IOException {
        List<ProcessoPrevidenciario> processos = previdRepo.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Processos Previdenciários");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] columns = { "ID", "Cliente", "Responsável", "Etapa Atual", "Status", "Data Abertura",
                    "Valor Causa", "Valor Concedido", "Protocolo" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ProcessoPrevidenciario p : processos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getCliente() != null ? p.getCliente().getNome() : "");
                row.createCell(2).setCellValue(p.getResponsavel() != null ? p.getResponsavel().getNome() : "");
                row.createCell(3).setCellValue(p.getEtapaAtual() != null ? p.getEtapaAtual().name() : "");
                row.createCell(4).setCellValue(p.getStatusAtual() != null ? p.getStatusAtual().name() : "");
                row.createCell(5).setCellValue(p.getDataAbertura() != null ? p.getDataAbertura().toString() : "");
                row.createCell(6).setCellValue(p.getValorCausa() != null ? p.getValorCausa().doubleValue() : 0.0);
                row.createCell(7)
                        .setCellValue(p.getValorConcedido() != null ? p.getValorConcedido().doubleValue() : 0.0);
                row.createCell(8).setCellValue(p.getNumeroProtocolo() != null ? p.getNumeroProtocolo() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }
}
