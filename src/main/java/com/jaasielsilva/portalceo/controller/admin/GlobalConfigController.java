package com.jaasielsilva.portalceo.controller.admin;

import com.jaasielsilva.portalceo.service.ConfiguracaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
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

    // --- EMAIL CONFIGURATION ---

    @GetMapping("/email")
    public ResponseEntity<Map<String, String>> getEmailConfig() {
        return ResponseEntity.ok(Map.of(
            "host", service.getValor("smtp_host", ""),
            "port", service.getValor("smtp_port", "587"),
            "username", service.getValor("smtp_username", ""),
            "from", service.getValor("smtp_from", ""),
            "password", service.getValor("smtp_password", "").isEmpty() ? "" : "******" // Mask password
        ));
    }

    @PostMapping("/email/save")
    public ResponseEntity<String> saveEmailConfig(@RequestBody Map<String, String> payload) {
        service.setValor("smtp_host", payload.getOrDefault("host", ""));
        service.setValor("smtp_port", payload.getOrDefault("port", "587"));
        service.setValor("smtp_username", payload.getOrDefault("username", ""));
        service.setValor("smtp_from", payload.getOrDefault("from", ""));
        
        String password = payload.get("password");
        if (password != null && !password.isEmpty() && !password.equals("******")) {
            service.setValor("smtp_password", password);
        }
        
        return ResponseEntity.ok("Configurações de E-mail salvas com sucesso.");
    }

    @PostMapping("/email/test")
    public ResponseEntity<String> testEmailConfig(@RequestBody Map<String, String> payload) {
        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(payload.get("host"));
            sender.setPort(Integer.parseInt(payload.get("port")));
            sender.setUsername(payload.get("username"));
            
            String password = payload.get("password");
            if (password == null || password.equals("******")) {
                 // Try to fetch existing password if masked
                 password = service.getValor("smtp_password", "");
            }
            sender.setPassword(password);

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String from = payload.get("from");
            if (from == null || from.isEmpty()) from = payload.get("username");

            helper.setFrom(from);
            helper.setTo(from); // Send to self
            helper.setSubject("Teste de Configuração SMTP - ERP");
            helper.setText("<h1>Sucesso!</h1><p>Sua configuração de e-mail está funcionando corretamente.</p>", true);

            sender.send(message);
            return ResponseEntity.ok("Conexão estabelecida e e-mail enviado com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Falha na conexão: " + e.getMessage());
        }
    }
}
