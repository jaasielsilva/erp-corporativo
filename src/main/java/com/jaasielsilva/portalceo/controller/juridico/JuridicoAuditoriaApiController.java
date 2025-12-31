package com.jaasielsilva.portalceo.controller.juridico;

import com.jaasielsilva.portalceo.model.AuditoriaJuridicoLog;
import com.jaasielsilva.portalceo.service.AuditoriaJuridicoLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/juridico/auditoria")
public class JuridicoAuditoriaApiController {

    @Autowired
    private AuditoriaJuridicoLogService auditoriaService;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_JURIDICO_GERENTE')")
    public ResponseEntity<Page<AuditoriaJuridicoLog>> listar(
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_JURIDICO_GERENTE')")
    public ResponseEntity<Map<String, Object>> registrar(
            @RequestParam String categoria,
            @RequestParam String acao,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String detalhes,
            @RequestParam(required = false) Boolean sucesso) {
        AuditoriaJuridicoLog log = auditoriaService.registrar(categoria, acao, recurso, usuario, ip, detalhes, sucesso);
        return ResponseEntity.ok(Map.of("success", true, "id", log.getId()));
    }
}
