package com.jaasielsilva.portalceo.service.whatchat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.entity.TipoDocumentoProcesso;
import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.service.DocumentoProcessoService;
import com.jaasielsilva.portalceo.model.whatchat.ChatConversa;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagem;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvolutionWebhookService {

    private final ObjectMapper objectMapper;
    private final WhaTchatConversaService conversaService;
    private final WhaTchatMensagemService mensagemService;
    private final WhaTchatAutomationService automationService;
    private final DocumentoProcessoService documentoProcessoService;

    @Transactional
    public void processarWebhook(String payloadJson) {
        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            List<JsonNode> messages = extrairMensagens(root);
            for (JsonNode msg : messages) {
                JsonNode key = msg.path("key");
                boolean fromMe = key.path("fromMe").asBoolean(false);
                if (fromMe) {
                    continue;
                }

                String remoteJid = key.path("remoteJid").asText(null);
                String waId = PhoneNormalizer.whatsAppRemoteJidToDigits(remoteJid);
                if (waId == null) {
                    continue;
                }

                String pushName = msg.path("pushName").asText(null);
                String whatsappMessageId = key.path("id").asText(null);
                Long ts = extrairTimestampEpochSeconds(msg);

                String texto = extrairTexto(msg);
                ChatConversa conversa = conversaService.obterOuCriar(waId, pushName);
                if (texto != null && !texto.isBlank()) {
                    var salvo = mensagemService.registrarRecebidaTexto(conversa, whatsappMessageId, texto, ts,
                            payloadJson);
                    automationService.onMensagemRecebida(conversa, salvo, texto);
                    continue;
                }

                MidiaInfo midia = extrairMidia(msg);
                if (midia == null) {
                    continue;
                }
                ChatMensagem salvo = mensagemService.registrarRecebidaMidia(
                        conversa,
                        whatsappMessageId,
                        midia.tipo(),
                        midia.url(),
                        midia.mimeType(),
                        midia.fileName(),
                        ts,
                        payloadJson);
                anexarNoProcessoSeVinculado(conversa, salvo, TipoDocumentoProcesso.OUTROS);
                automationService.onMensagemRecebida(conversa, salvo, midia.caption());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao processar webhook do Evolution API", e);
        }
    }

    private List<JsonNode> extrairMensagens(JsonNode root) {
        List<JsonNode> out = new ArrayList<>();

        if (root.hasNonNull("key")) {
            out.add(root);
            return out;
        }

        JsonNode data = root.path("data");
        if (data.hasNonNull("key")) {
            out.add(data);
            return out;
        }

        JsonNode body = root.path("body");
        if (body.hasNonNull("key")) {
            out.add(body);
            return out;
        }

        JsonNode list = root.path("messages");
        if (list.isArray()) {
            for (JsonNode m : list) {
                if (m.hasNonNull("key")) {
                    out.add(m);
                }
            }
            return out;
        }

        JsonNode event = root.path("event");
        if (event.isTextual() && (event.asText("").toUpperCase().contains("MESSAGE")
                || event.asText("").toUpperCase().contains("MESSAGES"))) {
            Iterator<String> fields = root.fieldNames();
            while (fields.hasNext()) {
                String f = fields.next();
                JsonNode v = root.get(f);
                if (v != null && v.isObject() && v.hasNonNull("key")) {
                    out.add(v);
                }
            }
        }

        return out;
    }

    private String extrairTexto(JsonNode msg) {
        String type = msg.path("messageType").asText(null);
        JsonNode message = msg.path("message");

        if ("conversation".equals(type)) {
            return message.path("conversation").asText(null);
        }
        if ("extendedTextMessage".equals(type)) {
            return message.path("extendedTextMessage").path("text").asText(null);
        }

        String text = message.path("conversation").asText(null);
        if (text != null) {
            return text;
        }
        text = message.path("extendedTextMessage").path("text").asText(null);
        if (text != null) {
            return text;
        }

        if (message.isObject()) {
            Iterator<String> it = message.fieldNames();
            while (it.hasNext()) {
                String k = it.next();
                JsonNode v = message.path(k);
                if (v != null && v.isObject()) {
                    String maybe = v.path("text").asText(null);
                    if (maybe != null) {
                        return maybe;
                    }
                }
            }
        }

        return null;
    }

    private MidiaInfo extrairMidia(JsonNode msg) {
        String type = msg.path("messageType").asText(null);
        JsonNode message = msg.path("message");

        if ("imageMessage".equals(type) || message.hasNonNull("imageMessage")) {
            JsonNode img = message.path("imageMessage");
            String url = img.path("url").asText(null);
            if (url == null || url.isBlank()) {
                return null;
            }
            String mime = img.path("mimetype").asText(null);
            String fileName = img.path("fileName").asText(null);
            String caption = img.path("caption").asText(null);
            return new MidiaInfo(ChatMensagemTipo.IMAGEM, url, mime, fileName, caption);
        }

        if ("documentMessage".equals(type) || message.hasNonNull("documentMessage")) {
            JsonNode doc = message.path("documentMessage");
            String url = doc.path("url").asText(null);
            if (url == null || url.isBlank()) {
                return null;
            }
            String mime = doc.path("mimetype").asText(null);
            String fileName = doc.path("fileName").asText(null);
            String caption = doc.path("caption").asText(null);
            return new MidiaInfo(ChatMensagemTipo.DOCUMENTO, url, mime, fileName, caption);
        }

        return null;
    }

    private Long extrairTimestampEpochSeconds(JsonNode msg) {
        if (msg.has("messageTimestamp")) {
            return msg.path("messageTimestamp").asLong();
        }
        if (msg.path("message").has("messageTimestamp")) {
            return msg.path("message").path("messageTimestamp").asLong();
        }
        if (msg.path("message").has("messageTimestampMs")) {
            long ms = msg.path("message").path("messageTimestampMs").asLong();
            return ms > 0 ? ms / 1000 : null;
        }
        return null;
    }

    private void anexarNoProcessoSeVinculado(ChatConversa conversa, ChatMensagem mensagem,
            TipoDocumentoProcesso tipoDocumento) {
        if (conversa.getProcessoPrevidenciario() == null || conversa.getAtendente() == null || mensagem == null) {
            return;
        }
        try {
            if (mensagem.getMediaCaminho() == null || mensagem.getMediaCaminho().isBlank()) {
                return;
            }
            byte[] bytes = Files.readAllBytes(Path.of(mensagem.getMediaCaminho()));
            String fileName = mensagem.getMediaFileName() != null ? mensagem.getMediaFileName() : "arquivo";
            documentoProcessoService.anexarDocumentoBytes(
                    conversa.getProcessoPrevidenciario().getId(),
                    bytes,
                    fileName,
                    tipoDocumento != null ? tipoDocumento : TipoDocumentoProcesso.OUTROS,
                    conversa.getAtendente());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao anexar mídia do WhatsApp no processo", e);
        }
    }

    private record MidiaInfo(ChatMensagemTipo tipo, String url, String mimeType, String fileName, String caption) {
    }
}
