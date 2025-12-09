package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.service.RhRelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/rh/relatorios")
public class RhRelatoriosApiController {

    @Autowired
    private RhRelatorioService service;

    @GetMapping("/turnover")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.RelatorioTurnover> turnover(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarRelatorioTurnover(inicio, fim));
    }

    @GetMapping("/turnover/detalhado")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.RelatorioTurnoverDetalhado> turnoverDetalhado(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) String tipoMovimento,
            @RequestParam(required = false) Double meta,
            @RequestParam(defaultValue = "false") boolean comparar) {
        return ResponseEntity.ok(
                service.gerarRelatorioTurnoverDetalhado(inicio, fim, departamento, cargo, tipoMovimento, meta, comparar)
        );
    }

    @GetMapping("/admissoes-demissoes")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.RelatorioAdmissoesDemissoes> admissoesDemissoes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarRelatorioAdmissoesDemissoes(inicio, fim));
    }

    @GetMapping("/ferias-beneficios")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.RelatorioFeriasBeneficios> feriasBeneficios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarRelatorioFeriasBeneficios(inicio, fim));
    }

    @GetMapping("/ferias-beneficios/orcamento")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.OrcamentoFeriasBeneficios> orcamentoFeriasBeneficios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarOrcamentoFeriasBeneficios(inicio, fim));
    }

    @GetMapping("/indicadores")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.RelatorioIndicadoresDesempenho> indicadores(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarRelatorioIndicadores(inicio, fim));
    }

    @GetMapping("/indicadores/serie-12m")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.SerieIndicadores12Meses> indicadoresSerie(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarSerieIndicadores12Meses(fim));
    }

    @GetMapping("/absenteismo/detalhado")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.RelatorioAbsenteismoDetalhado> absenteismoDetalhado(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) String tipoAusencia,
            @RequestParam(required = false) Double meta,
            @RequestParam(defaultValue = "false") boolean comparar) {
        return ResponseEntity.ok(
                service.gerarRelatorioAbsenteismoDetalhado(inicio, fim, departamento, cargo, tipoAusencia, meta, comparar)
        );
    }

    @GetMapping("/headcount")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<RhRelatorioService.RelatorioHeadcount> headcount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String tipoContrato,
            @RequestParam(required = false, defaultValue = "mes") String periodo) {
        return ResponseEntity.ok(
                service.gerarRelatorioHeadcount(inicio, fim, departamento, tipoContrato, periodo)
        );
    }

    @GetMapping("/headcount/export")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public org.springframework.http.ResponseEntity<byte[]> exportHeadcount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String tipoContrato,
            @RequestParam(required = false, defaultValue = "mes") String periodo,
            @RequestParam(required = false, defaultValue = "excel") String format) throws Exception {
        var rel = service.gerarRelatorioHeadcount(inicio, fim, departamento, tipoContrato, periodo);

        if ("excel".equalsIgnoreCase(format)) {
            org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            var sheet = wb.createSheet("Headcount");
            int r = 0;
            var header = sheet.createRow(r++);
            header.createCell(0).setCellValue("Resumo");
            sheet.createRow(r++).createCell(0).setCellValue("Total Ativos: " + rel.getTotalAtivos());
            sheet.createRow(r++).createCell(0).setCellValue("Taxa Turnover (%): " + String.format(java.util.Locale.US, "%.2f", rel.getTaxaTurnover()));
            r++;
            var depHeader = sheet.createRow(r++);
            depHeader.createCell(0).setCellValue("Departamentos"); depHeader.createCell(1).setCellValue("Qtd");
            for (var e : rel.getPorDepartamento().entrySet()) { var row = sheet.createRow(r++); row.createCell(0).setCellValue(e.getKey()); row.createCell(1).setCellValue(e.getValue()); }
            r++;
            var cargoHeader = sheet.createRow(r++); cargoHeader.createCell(0).setCellValue("Cargos"); cargoHeader.createCell(1).setCellValue("Qtd");
            for (var e : rel.getPorCargo().entrySet()) { var row = sheet.createRow(r++); row.createCell(0).setCellValue(e.getKey()); row.createCell(1).setCellValue(e.getValue()); }
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            wb.write(bos); wb.close();
            byte[] bytes = bos.toByteArray();
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=headcount.xlsx")
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } else if ("pdf".equalsIgnoreCase(format)) {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new com.lowagie.text.Paragraph("Relatório de Headcount"));
            document.add(new com.lowagie.text.Paragraph("Período: " + rel.getInicio() + " a " + rel.getFim()));
            document.add(new com.lowagie.text.Paragraph("Total Ativos: " + rel.getTotalAtivos()));
            document.add(new com.lowagie.text.Paragraph(String.format(java.util.Locale.US, "Taxa Turnover: %.2f%%", rel.getTaxaTurnover())));
            document.add(new com.lowagie.text.Paragraph("\nDepartamentos:"));
            for (var e : rel.getPorDepartamento().entrySet()) { document.add(new com.lowagie.text.Paragraph("- " + e.getKey() + ": " + e.getValue())); }
            document.close();
            byte[] bytes = baos.toByteArray();
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=headcount.pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(bytes);
        }
        return org.springframework.http.ResponseEntity.status(415).body(new byte[0]);
    }
}
