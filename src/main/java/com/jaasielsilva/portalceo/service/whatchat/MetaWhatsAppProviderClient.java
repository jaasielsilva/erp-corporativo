package com.jaasielsilva.portalceo.service.whatchat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.multipart.MultipartHttpMessageWriter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MetaWhatsAppProviderClient implements WhatsAppProviderClient {

    private final ObjectMapper objectMapper;

    @Value("${erp.whatsapp.base-url:https://graph.facebook.com/v18.0}")
    private String baseUrl;

    @Value("${erp.whatsapp.access-token:}")
    private String accessToken;

    @Value("${erp.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${erp.whatsapp.timeout-ms:15000}")
    private long timeoutMs;

    @Override
    public String enviarTexto(String waId, String texto) {
        if (waId == null || waId.isBlank()) {
            throw new IllegalArgumentException("waId é obrigatório");
        }
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Texto é obrigatório");
        }
        JsonNode resp = webClient()
                .post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"messaging_product":"whatsapp","to":"%s","type":"text","text":{"body":%s}}
                        """.formatted(waId, objectMapper.valueToTree(texto).toString()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofMillis(timeoutMs));

        return resp != null && resp.path("messages").isArray() && resp.path("messages").size() > 0
                ? resp.path("messages").get(0).path("id").asText(null)
                : null;
    }

    @Override
    public String enviarImagem(String waId, MultipartFile arquivo) {
        String mediaId = uploadMedia(arquivo);
        JsonNode resp = webClient()
                .post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"messaging_product":"whatsapp","to":"%s","type":"image","image":{"id":"%s"}}
                        """.formatted(waId, mediaId))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofMillis(timeoutMs));
        return resp != null && resp.path("messages").isArray() && resp.path("messages").size() > 0
                ? resp.path("messages").get(0).path("id").asText(null)
                : null;
    }

    @Override
    public String enviarDocumento(String waId, MultipartFile arquivo) {
        String mediaId = uploadMedia(arquivo);
        String filename = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "documento";
        JsonNode resp = webClient()
                .post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"messaging_product":"whatsapp","to":"%s","type":"document","document":{"id":"%s","filename":%s}}
                        """.formatted(waId, mediaId, objectMapper.valueToTree(filename).toString()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofMillis(timeoutMs));
        return resp != null && resp.path("messages").isArray() && resp.path("messages").size() > 0
                ? resp.path("messages").get(0).path("id").asText(null)
                : null;
    }

    @Override
    public WhatsAppMediaDownloadResult baixarMidia(String mediaId) {
        if (mediaId == null || mediaId.isBlank()) {
            throw new IllegalArgumentException("mediaId é obrigatório");
        }
        JsonNode meta = webClient()
                .get()
                .uri("/{mediaId}", mediaId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofMillis(timeoutMs));
        if (meta == null) {
            throw new IllegalStateException("Falha ao buscar metadados da mídia");
        }
        String url = meta.path("url").asText(null);
        String mime = meta.path("mime_type").asText(null);
        String sha = meta.path("sha256").asText(null);
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("URL de mídia ausente");
        }

        byte[] bytes = webClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .block(Duration.ofMillis(timeoutMs));

        String fileName = null;
        return new WhatsAppMediaDownloadResult(bytes != null ? bytes : new byte[0], mime, fileName, sha);
    }

    private String uploadMedia(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo é obrigatório");
        }
        String filename = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "arquivo";
        String contentType = arquivo.getContentType() != null ? arquivo.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        data.add("messaging_product", "whatsapp");
        data.add("file", new ByteArrayResource(readBytes(arquivo)) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        JsonNode resp = webClient()
                .post()
                .uri("/{phoneNumberId}/media", phoneNumberId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(data))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                .header("X-File-Content-Type", contentType)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofMillis(timeoutMs));

        String id = resp != null ? resp.path("id").asText(null) : null;
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Falha no upload do arquivo para o WhatsApp");
        }
        return id;
    }

    private byte[] readBytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler arquivo", e);
        }
    }

    private WebClient webClient() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("erp.whatsapp.access-token não configurado");
        }
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            throw new IllegalStateException("erp.whatsapp.phone-number-id não configurado");
        }
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(timeoutMs));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(25 * 1024 * 1024))
                .build();
    }
}

