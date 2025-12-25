package com.jaasielsilva.portalceo.service.whatchat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "erp.whatsapp.provider", havingValue = "evolution")
public class EvolutionWhatsAppProviderClient implements WhatsAppProviderClient {

    private final ObjectMapper objectMapper;

    @Value("${erp.evolution.base-url:}")
    private String baseUrl;

    @Value("${erp.evolution.apikey:}")
    private String apiKey;

    @Value("${erp.evolution.instance:}")
    private String instance;

    @Value("${erp.evolution.timeout-ms:15000}")
    private long timeoutMs;

    @Value("${erp.evolution.insecure-ssl:false}")
    private boolean insecureSsl;

    @Value("${erp.whatchat.default-country-code:55}")
    private String defaultCountryCode;

    @Override
    public String enviarTexto(String waId, String texto) {
        if (waId == null || waId.isBlank()) {
            throw new IllegalArgumentException("waId é obrigatório");
        }
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Texto é obrigatório");
        }
        String number = PhoneNormalizer.toE164(waId, defaultCountryCode);
        JsonNode resp = webClient()
                .post()
                .uri("/message/sendText/{instance}", instance)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"number":%s,"text":%s}
                        """.formatted(objectMapper.valueToTree(number).toString(),
                        objectMapper.valueToTree(texto).toString()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofMillis(timeoutMs));

        return resp != null ? resp.path("key").path("id").asText(null) : null;
    }

    @Override
    public String enviarImagem(String waId, MultipartFile arquivo) {
        return enviarMedia(waId, arquivo, "image");
    }

    @Override
    public String enviarDocumento(String waId, MultipartFile arquivo) {
        return enviarMedia(waId, arquivo, "document");
    }

    private String enviarMedia(String waId, MultipartFile arquivo, String mediaType) {
        if (waId == null || waId.isBlank()) {
            throw new IllegalArgumentException("waId é obrigatório");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo é obrigatório");
        }
        String number = PhoneNormalizer.toE164(waId, defaultCountryCode);
        String mimetype = arquivo.getContentType() != null ? arquivo.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String fileName = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "arquivo";
        String media = Base64.getEncoder().encodeToString(readBytes(arquivo));

        JsonNode resp = webClient()
                .post()
                .uri("/message/sendMedia/{instance}", instance)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"number":%s,"mediatype":%s,"mimetype":%s,"media":%s,"fileName":%s}
                        """.formatted(
                        objectMapper.valueToTree(number).toString(),
                        objectMapper.valueToTree(mediaType).toString(),
                        objectMapper.valueToTree(mimetype).toString(),
                        objectMapper.valueToTree(media).toString(),
                        objectMapper.valueToTree(fileName).toString()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofMillis(timeoutMs));

        return resp != null ? resp.path("key").path("id").asText(null) : null;
    }

    @Override
    public WhatsAppMediaDownloadResult baixarMidia(String mediaId) {
        if (mediaId == null || mediaId.isBlank()) {
            throw new IllegalArgumentException("mediaId é obrigatório");
        }
        URI mediaUri = resolveMediaUri(mediaId);

        WhatsAppMediaDownloadResult result = webClient()
                .get()
                .uri(mediaUri)
                .exchangeToMono(resp -> resp.bodyToMono(byte[].class)
                        .defaultIfEmpty(new byte[0])
                        .map(bytes -> new WhatsAppMediaDownloadResult(
                                bytes,
                                resp.headers().contentType().map(MediaType::toString).orElse(null),
                                null,
                                null)))
                .block(Duration.ofMillis(timeoutMs));

        if (result == null) {
            throw new IllegalStateException("Falha ao baixar mídia");
        }
        return result;
    }

    private URI resolveMediaUri(String mediaRef) {
        String trimmed = mediaRef.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            URI uri = URI.create(trimmed);
            if (uri.getHost() != null && isLocalHost(uri.getHost())) {
                URI base = URI.create(baseUrl);
                if (base.getHost() == null) {
                    return uri;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(base.getScheme()).append("://").append(base.getAuthority());
                if (uri.getRawPath() != null) {
                    sb.append(uri.getRawPath());
                }
                if (uri.getRawQuery() != null) {
                    sb.append('?').append(uri.getRawQuery());
                }
                if (uri.getRawFragment() != null) {
                    sb.append('#').append(uri.getRawFragment());
                }
                return URI.create(sb.toString());
            }
            return uri;
        }

        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        return URI.create(base + path);
    }

    private boolean isLocalHost(String host) {
        String h = host.toLowerCase();
        return "localhost".equals(h) || "127.0.0.1".equals(h) || "0.0.0.0".equals(h);
    }

    private byte[] readBytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler arquivo", e);
        }
    }

    private WebClient webClient() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("erp.evolution.base-url não configurado");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("erp.evolution.apikey não configurado");
        }
        if (instance == null || instance.isBlank()) {
            throw new IllegalStateException("erp.evolution.instance não configurado");
        }
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(timeoutMs));
        if (insecureSsl) {
            try {
                SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                httpClient = httpClient.secure(spec -> spec.sslContext(sslContext));
            } catch (Exception e) {
                throw new IllegalStateException("Falha ao configurar SSL inseguro para Evolution", e);
            }
        }
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("apikey", apiKey)
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(25 * 1024 * 1024))
                .build();
    }
}
