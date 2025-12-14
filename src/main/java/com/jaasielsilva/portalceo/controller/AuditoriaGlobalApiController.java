package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.AuditoriaRhLog;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaGlobalApiController {

    @Autowired
    private AuditoriaRhLogService auditoriaService;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Page<AuditoriaRhLog>> listar(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String recursoFiltrado = recurso;
        if (modulo != null && !modulo.isBlank()) {
            String prefix = modulo.startsWith("/") ? modulo : "/" + modulo;
            recursoFiltrado = (recursoFiltrado == null || recursoFiltrado.isBlank()) ? prefix : recursoFiltrado;
        }
        return ResponseEntity.ok(auditoriaService.listar(categoria, usuario, recursoFiltrado, inicio, fim, page, size));
    }

    @GetMapping("/logs/csv")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        String recursoFiltrado = recurso;
        if (modulo != null && !modulo.isBlank()) {
            String prefix = modulo.startsWith("/") ? modulo : "/" + modulo;
            recursoFiltrado = (recursoFiltrado == null || recursoFiltrado.isBlank()) ? prefix : recursoFiltrado;
        }
        var pageData = auditoriaService.listar(categoria, usuario, recursoFiltrado, inicio, fim, page, size);
        var rows = new ArrayList<String>();
        rows.add("Data,Usuario,Acao,Recurso,IP,Sucesso");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditoriaRhLog a : pageData.getContent()) {
            String data = a.getCriadoEm() != null ? a.getCriadoEm().format(fmt) : "";
            String u = a.getUsuario() != null ? a.getUsuario().replace("\"", "\"\"") : "";
            String ac = a.getAcao() != null ? a.getAcao().replace("\"", "\"\"") : "";
            String r = a.getRecurso() != null ? a.getRecurso().replace("\"", "\"\"") : "";
            String ip = a.getIpOrigem() != null ? a.getIpOrigem().replace("\"", "\"\"") : "";
            String s = a.getSucesso() != null ? (a.getSucesso() ? "OK" : "ERRO") : "";
            rows.add(String.join(",",
                    data,
                    '"' + u + '"',
                    '"' + ac + '"',
                    '"' + r + '"',
                    '"' + ip + '"',
                    s));
        }
        String csv = String.join("\n", rows);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria-global.csv");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping(value = "/logs/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        String recursoFiltrado = recurso;
        if (modulo != null && !modulo.isBlank()) {
            String prefix = modulo.startsWith("/") ? modulo : "/" + modulo;
            recursoFiltrado = (recursoFiltrado == null || recursoFiltrado.isBlank()) ? prefix : recursoFiltrado;
        }
        var pageData = auditoriaService.listar(categoria, usuario, recursoFiltrado, inicio, fim, page, size);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\\\"UTF-8\\\"><style>")
            .append("body{font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#222}")
            .append("h1{font-size:16px;margin:0 0 10px 0}")
            .append("table{width:100%;border-collapse:collapse}")
            .append("th,td{border:1px solid #ddd;padding:6px}")
            .append("th{background:#f5f5f5;text-align:left}")
            .append("</style></head><body>");
        html.append("<h1>Relatório de Auditoria</h1>");
        html.append("<table><thead><tr>")
            .append("<th>Data/Hora</th><th>Usuário</th><th>Ação</th><th>Detalhes</th>")
            .append("</tr></thead><tbody>");
        for (AuditoriaRhLog a : pageData.getContent()) {
            String data = a.getCriadoEm() != null ? a.getCriadoEm().format(fmt) : "";
            String u = a.getUsuario() != null ? a.getUsuario() : "";
            String ac = a.getAcao() != null ? a.getAcao() : "";
            String det = a.getDetalhes() != null ? a.getDetalhes() : "";
            det = det.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            html.append("<tr>")
                .append("<td>").append(data).append("</td>")
                .append("<td>").append(u).append("</td>")
                .append("<td>").append(ac).append("</td>")
                .append("<td>").append(det).append("</td>")
                .append("</tr>");
        }
        html.append("</tbody></table></body></html>");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html.toString(), null);
        builder.toStream(baos);
        try {
            builder.run();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().contentType(MediaType.TEXT_PLAIN).body(("Falha ao gerar PDF: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
        byte[] pdf = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria-global.pdf");
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
