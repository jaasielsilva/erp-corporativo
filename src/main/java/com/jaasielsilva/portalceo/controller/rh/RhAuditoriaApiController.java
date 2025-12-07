package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.model.AuditoriaRhLog;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
}

