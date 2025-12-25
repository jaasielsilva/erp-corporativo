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

@Service
@RequiredArgsConstructor
public class WhatsAppWebhookService {

    private final ObjectMapper objectMapper;
    private final WhaTchatConversaService conversaService;
    private final WhaTchatMensagemService mensagemService;
    private final DocumentoProcessoService documentoProcessoService;

    @Transactional
    public void processarWebhook(String payloadJson) {
        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            JsonNode entry = root.path("entry");
            if (!entry.isArray()) {
                return;
            }
            for (JsonNode e : entry) {
                JsonNode changes = e.path("changes");
                if (!changes.isArray()) {
                    continue;
                }
                for (JsonNode ch : changes) {
                    JsonNode value = ch.path("value");
                    JsonNode contacts = value.path("contacts");
                    String nome = null;
                    if (contacts.isArray() && contacts.size() > 0) {
                        nome = contacts.get(0).path("profile").path("name").asText(null);
                    }
                    JsonNode messages = value.path("messages");
                    if (!messages.isArray()) {
                        continue;
                    }
                    for (JsonNode msg : messages) {
                        String from = msg.path("from").asText(null);
                        String waMsgId = msg.path("id").asText(null);
                        String type = msg.path("type").asText(null);
                        Long ts = msg.has("timestamp") ? msg.path("timestamp").asLong() : null;

                        ChatConversa conversa = conversaService.obterOuCriar(from, nome);

                        if ("text".equals(type)) {
                            String body = msg.path("text").path("body").asText(null);
                            mensagemService.registrarRecebidaTexto(conversa, waMsgId, body, ts, payloadJson);
                        } else if ("image".equals(type)) {
                            String mediaId = msg.path("image").path("id").asText(null);
                            String mime = msg.path("image").path("mime_type").asText(null);
                            ChatMensagem salvo = mensagemService.registrarRecebidaMidia(conversa, waMsgId,
                                    ChatMensagemTipo.IMAGEM, mediaId, mime, null, ts, payloadJson);
                            anexarNoProcessoSeVinculado(conversa, salvo, TipoDocumentoProcesso.OUTROS);
                        } else if ("document".equals(type)) {
                            String mediaId = msg.path("document").path("id").asText(null);
                            String mime = msg.path("document").path("mime_type").asText(null);
                            String filename = msg.path("document").path("filename").asText(null);
                            ChatMensagem salvo = mensagemService.registrarRecebidaMidia(conversa, waMsgId,
                                    ChatMensagemTipo.DOCUMENTO, mediaId, mime, filename, ts, payloadJson);
                            anexarNoProcessoSeVinculado(conversa, salvo, TipoDocumentoProcesso.OUTROS);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao processar webhook do WhatsApp", e);
        }
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
}
