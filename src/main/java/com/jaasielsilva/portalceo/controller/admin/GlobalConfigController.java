package com.jaasielsilva.portalceo.controller.admin;

import com.jaasielsilva.portalceo.service.ConfiguracaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
public class GlobalConfigController {

    private final ConfiguracaoService service;

    public GlobalConfigController(ConfiguracaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfigs() {
        return ResponseEntity.ok(Map.of(
            "maintenanceMode", service.isModoManutencaoAtivo(),
            "reportEmail", service.getEmailRelatorio()
        ));
    }

    @PostMapping("/maintenance")
    public ResponseEntity<String> setMaintenanceMode(@RequestBody Map<String, Boolean> payload) {
        boolean active = payload.getOrDefault("active", false);
        service.setValor("maintenance_mode", String.valueOf(active));
        return ResponseEntity.ok("Modo Manutenção atualizado para: " + active);
    }

    @PostMapping("/report-email")
    public ResponseEntity<String> setReportEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("E-mail inválido.");
        }
        service.setValor("report_email", email);
        return ResponseEntity.ok("E-mail de relatórios atualizado.");
    }
}
