package com.jaasielsilva.portalceo.controller.whatchat;

import com.jaasielsilva.portalceo.service.whatchat.EvolutionWebhookService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/whatsapp/evolution")
@RequiredArgsConstructor
public class EvolutionWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(EvolutionWebhookController.class);

    private final EvolutionWebhookService webhookService;

    @Value("${erp.evolution.webhook-token:}")
    private String webhookToken;

    @PostMapping({ "", "/**" })
    public ResponseEntity<Void> receive(
            @RequestHeader(name = "X-Webhook-Token", required = false) String tokenHeader,
            @RequestHeader(name = "apikey", required = false) String apiKeyHeader,
            @RequestBody String body) {
        if (StringUtils.hasText(webhookToken)) {
            String candidate = StringUtils.hasText(tokenHeader) ? tokenHeader : apiKeyHeader;
            if (!StringUtils.hasText(candidate) || !constantTimeEquals(webhookToken, candidate)) {
                logger.warn("Webhook Evolution bloqueado: token ausente ou inválido");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        logger.info("Webhook Evolution recebido: payloadChars={}", body != null ? body.length() : 0);
        webhookService.processarWebhook(body);
        return ResponseEntity.ok().build();
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
