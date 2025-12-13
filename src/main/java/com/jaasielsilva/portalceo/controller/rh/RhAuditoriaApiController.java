package com.jaasielsilva.portalceo.controller.rh;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/rh/auditoria")
public class RhAuditoriaApiController {

    @Autowired
    private AuditoriaRhLogService auditoriaService;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public ResponseEntity<Page<AuditoriaRhLog>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditoriaService.listar(categoria, usuario, recurso, inicio, fim, page, size));
    }

    @PostMapping("/logs")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String, Object>> registrar(
            @RequestParam String categoria,
            @RequestParam String acao,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String detalhes,
            @RequestParam(required = false) Boolean sucesso) {
        AuditoriaRhLog log = auditoriaService.registrar(categoria, acao, recurso, usuario, ip, detalhes, sucesso);
        return ResponseEntity.ok(Map.of("success", true, "id", log.getId()));
    }

    @GetMapping("/logs/csv")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        var pageData = auditoriaService.listar(categoria, usuario, recurso, inicio, fim, page, size);
        var rows = new ArrayList<String>();
        rows.add("Data,Usuario,Acao,Recurso,IP,Sucesso");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditoriaRhLog a : pageData.getContent()) {
            String data = a.getCriadoEm() != null ? a.getCriadoEm().format(fmt) : "";
            String u = a.getUsuario() != null ? a.getUsuario().replace("\"", "\"\"") : "";
            String ac = a.getAcao() != null ? a.getAcao().replace("\"", "\"\"") : "";
            String r = a.getRecurso() != null ? a.getRecurso().replace("\"", "\"\"") : "";
            String ip = a.getIpOrigem() != null ? a.getIpOrigem().replace("\"", "\"\"") : "";
            String suc = Boolean.TRUE.equals(a.getSucesso()) ? "OK" : "Falha";
            rows.add(String.join(",",
                    '"' + data + '"',
                    '"' + u + '"',
                    '"' + ac + '"',
                    '"' + r + '"',
                    '"' + ip + '"',
                    '"' + suc + '"'
            ));
        }
        String csv = String.join("\n", rows) + "\n";
        String filename = "auditoria-" + (categoria != null ? categoria.toLowerCase() : "todos") + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        return ResponseEntity.ok().headers(headers).body(csv.getBytes());
    }

    @GetMapping("/metrics/acessos-24h")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public ResponseEntity<Map<String, Object>> acessosUltimas24h(@RequestParam(required = false) String usuario,
                                                                 @RequestParam(required = false) String recurso) {
        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = fim.minusHours(24);
        var pageData = auditoriaService.listar("ACESSO", usuario, recurso, inicio, fim, 0, 2000);
        int[] counts = new int[24];
        String[] labels = new String[24];
        for (int i = 0; i < 24; i++) {
            labels[i] = inicio.plusHours(i).format(DateTimeFormatter.ofPattern("HH'h'"));
        }
        for (AuditoriaRhLog a : pageData.getContent()) {
            if (a.getCriadoEm() == null) continue;
            long diff = java.time.Duration.between(inicio, a.getCriadoEm()).toHours();
            if (diff >= 0 && diff < 24) counts[(int) diff]++;
        }
        return ResponseEntity.ok(Map.of("labels", labels, "series", counts));
    }
}
