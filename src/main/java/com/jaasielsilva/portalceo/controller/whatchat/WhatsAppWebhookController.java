package com.jaasielsilva.portalceo.controller.whatchat;

import com.jaasielsilva.portalceo.service.whatchat.WhatsAppWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/integracoes/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService webhookService;

    @Value("${erp.whatsapp.verify-token:}")
    private String verifyToken;

    @Value("${erp.whatsapp.app-secret:}")
    private String appSecret;

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if ("subscribe".equals(mode) && verifyToken != null && !verifyToken.isBlank() && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge != null ? challenge : "");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature256,
            @RequestBody String body) {
        if (StringUtils.hasText(appSecret)) {
            if (!validarAssinatura(body, signature256, appSecret)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        webhookService.processarWebhook(body);
        return ResponseEntity.ok().build();
    }

    private boolean validarAssinatura(String payload, String header, String secret) {
        if (!StringUtils.hasText(header) || !header.startsWith("sha256=")) {
            return false;
        }
        String expected = header.substring("sha256=".length());
        String computed = hmacSha256Hex(secret, payload);
        return constantTimeEquals(expected, computed);
    }

    private String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

