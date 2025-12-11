package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.juridico.AssinaturaDocumento;
import com.jaasielsilva.portalceo.repository.juridico.AssinaturaDocumentoRepository;
import com.jaasielsilva.portalceo.repository.juridico.DocumentoJuridicoRepository;
import com.jaasielsilva.portalceo.service.AuditService;
import com.jaasielsilva.portalceo.service.NotificationService;
import com.jaasielsilva.portalceo.model.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/assinaturas")
public class AssinaturasController {

    @Autowired
    private AssinaturaDocumentoRepository assinaturaRepo;

    @Autowired
    private DocumentoJuridicoRepository documentoRepo;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @org.springframework.beans.factory.annotation.Value("${signature.webhook.secret:}")
    private String webhookSecret;

    @PostMapping("/sessoes")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_JURIDICO')")
    public ResponseEntity<?> criarSessaoAssinatura(@RequestBody Map<String, Object> payload,
                                                   @AuthenticationPrincipal UserDetails user) {
        try {
            Long documentoId = Long.valueOf(payload.get("documentoId").toString());
            String tipo = payload.getOrDefault("tipo", "ELETRONICA").toString();
            String partes = payload.getOrDefault("partes", "{}").toString();
            String sessionId = java.util.UUID.randomUUID().toString();

            if (documentoRepo.findById(documentoId).isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("erro", "Documento não encontrado"));
            }

            AssinaturaDocumento s = new AssinaturaDocumento();
            s.setDocumentoId(documentoId);
            s.setTipo(tipo);
            s.setStatus("PENDENTE");
            s.setPartes(partes);
            s.setExternalId(null);
            s.setEvidencias(null);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            assinaturaRepo.save(s);

            auditService.logEvent(sessionId, AuditService.EventType.INICIO_PROCESSO,
                    "Sessão de assinatura criada (" + tipo + ") para documento " + documentoId,
                    null);

            Notification n = notificationService.createGlobalNotification(
                    "info",
                    "Sessão de assinatura criada",
                    "Documento " + documentoId + " preparado para assinatura",
                    Notification.Priority.MEDIUM
            );
            messagingTemplate.convertAndSend("/topic/notifications", n);

            return ResponseEntity.ok(Map.of("sessaoId", s.getId(), "status", s.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/sessoes/{id}/status")
    @ResponseBody
    public ResponseEntity<?> statusSessao(@PathVariable Long id) {
        return assinaturaRepo.findById(id)
                .map(s -> ResponseEntity.ok(Map.of(
                        "id", s.getId(),
                        "status", s.getStatus(),
                        "tipo", s.getTipo(),
                        "documentoId", s.getDocumentoId()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("erro", "Sessão não encontrada")));
    }

    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<?> webhook(@RequestHeader(value = "X-Signature", required = false) String signature,
                                     @RequestBody String rawBody) {
        try {
            if (webhookSecret != null && !webhookSecret.isBlank()) {
                String computed = computeHmacSha256(rawBody, webhookSecret);
                if (signature == null || !signature.equals(computed)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Assinatura inválida"));
                }
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> payload = mapper.readValue(rawBody, Map.class);

            Long sessaoId = Long.valueOf(String.valueOf(payload.get("sessaoId")));
            String status = String.valueOf(payload.get("status"));
            String externalId = payload.get("externalId") != null ? String.valueOf(payload.get("externalId")) : null;
            Object evid = payload.get("evidencias");

            AssinaturaDocumento s = assinaturaRepo.findById(sessaoId).orElse(null);
            if (s == null) return ResponseEntity.status(404).body(Map.of("erro", "Sessão não encontrada"));

            s.setStatus(status);
            s.setExternalId(externalId);
            if (evid != null) {
                s.setEvidencias(mapper.writeValueAsString(evid));
            } else {
                s.setEvidencias(rawBody);
            }
            s.setUpdatedAt(LocalDateTime.now());
            assinaturaRepo.save(s);

            auditService.logEvent(String.valueOf(sessaoId), AuditService.EventType.DOCUMENTO_ENVIADO,
                    "Webhook recebido: status atualizado para '" + status + "'",
                    null);

            Notification n = notificationService.createGlobalNotification(
                    "info",
                    "Status de assinatura atualizado",
                    "Sessão " + s.getId() + ": " + status,
                    Notification.Priority.MEDIUM
            );
            messagingTemplate.convertAndSend("/topic/notifications", n);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", e.getMessage()));
        }
    }

    private String computeHmacSha256(String data, String secret) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : rawHmac) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
