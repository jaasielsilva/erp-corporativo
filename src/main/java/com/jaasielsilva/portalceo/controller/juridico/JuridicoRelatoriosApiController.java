package com.jaasielsilva.portalceo.controller.juridico;

import com.jaasielsilva.portalceo.service.AuditService;
import com.jaasielsilva.portalceo.service.juridico.JuridicoRelatorioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para endpoints de exportação de relatórios do módulo Jurídico.
 * Implementa segregação de funções via anotações @PreAuthorize.
 */
@RestController
@RequestMapping("/api/juridico/relatorios")
public class JuridicoRelatoriosApiController {

    @Autowired
    private JuridicoRelatorioService relatorioService;

    @Autowired
    private AuditService auditService;

    @GetMapping("/contratos/export")
    @PreAuthorize("hasAuthority('JURIDICO_CONTRATOS_EXPORTAR')")
    public ResponseEntity<byte[]> exportarContratos(HttpServletRequest request) throws Exception {
        byte[] bytes = relatorioService.exportarContratosExcel();

        auditService.logEvent(request.getSession().getId(), AuditService.EventType.DOCUMENTO_ENVIADO,
                "Exportação de relatórios: Contratos Jurídicos", request.getRemoteAddr());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contratos_juridico.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/processos/export")
    @PreAuthorize("hasAuthority('JURIDICO_PROCESSOS_EXPORTAR')")
    public ResponseEntity<byte[]> exportarProcessos(HttpServletRequest request) throws Exception {
        byte[] bytes = relatorioService.exportarProcessosExcel();

        auditService.logEvent(request.getSession().getId(), AuditService.EventType.DOCUMENTO_ENVIADO,
                "Exportação de relatórios: Processos Jurídicos", request.getRemoteAddr());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processos_juridico.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/previdenciario/export")
    @PreAuthorize("hasAuthority('JURIDICO_PREVIDENCIARIO_EXPORTAR')")
    public ResponseEntity<byte[]> exportarPrevidenciario(HttpServletRequest request) throws Exception {
        byte[] bytes = relatorioService.exportarPrevidenciarioExcel();

        auditService.logEvent(request.getSession().getId(), AuditService.EventType.DOCUMENTO_ENVIADO,
                "Exportação de relatórios: Processos Previdenciários", request.getRemoteAddr());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processos_previdenciarios.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
