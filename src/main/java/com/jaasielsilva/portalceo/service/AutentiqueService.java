package com.jaasielsilva.portalceo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AutentiqueService {

    @Value("${autentique.api.token}")
    private String token;

    @Value("${autentique.api.url}")
    private String apiUrl;

    @Value("${autentique.api.company-email:silvajasiel30@gmail.com}")
    private String companyEmail;

    private final WebClient webClient;

    public AutentiqueService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Envia um documento para assinatura na Autentique.
     * 
     * @param filePath     Caminho do arquivo PDF
     * @param documentName Nome do documento
     * @param signerEmail  Email do signatário (Cliente)
     * @return O ID do documento na Autentique ou null em caso de erro
     */
    public Map<String, String> enviarDocumento(String filePath, String documentName, String signerEmail) {
        log.info("Iniciando envio de documento para Autentique: {} para {}", documentName, signerEmail);

        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                log.error("Arquivo não encontrado: {}", filePath);
                return null;
            }

            // O email da empresa já está definido como campo da classe

            // Document e Signers inputs para a mutation
            String operations = "{\"query\": \"mutation createDocument($document: DocumentInput!, $signers: [SignerInput!]!, $file: Upload!) { createDocument(document: $document, signers: $signers, file: $file) { id name signatures { public_id user { email } link { short_link } } } }\", \"variables\": { \"document\": { \"name\": \""
                    + documentName + "\" }, \"signers\": [ { \"email\": \"" + signerEmail
                    + "\", \"action\": \"SIGN\" }, { \"email\": \"" + companyEmail
                    + "\", \"action\": \"SIGN\" } ], \"file\": null } }";
            String map = "{ \"0\": [\"variables.file\"] }";

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("operations", operations);
            builder.part("map", map);
            builder.part("0", new FileSystemResource(pdfFile));

            MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

            Map<String, Object> response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> createDocument = (Map<String, Object>) data.get("createDocument");

                List<Map<String, Object>> signatures = (List<Map<String, Object>>) createDocument.get("signatures");
                String publicId = null;
                String signatureLink = null;
                String companyLink = null;

                if (signatures != null) {
                    for (Map<String, Object> sig : signatures) {
                        Map<String, Object> user = (Map<String, Object>) sig.get("user");
                        if (user != null) {
                            String email = (String) user.get("email");
                            Map<String, Object> linkObj = (Map<String, Object>) sig.get("link");
                            String link = linkObj != null ? (String) linkObj.get("short_link") : null;

                            if (signerEmail.equalsIgnoreCase(email)) {
                                publicId = (String) sig.get("public_id");
                                signatureLink = link;
                            } else if (companyEmail.equalsIgnoreCase(email)) {
                                companyLink = link;
                            }
                        }
                    }

                    // Fallback para o primeiro se não encontrar o publicId
                    if (publicId == null && !signatures.isEmpty()) {
                        publicId = (String) signatures.get(0).get("public_id");
                    }
                }

                Map<String, String> result = new HashMap<>();
                result.put("id", (String) createDocument.get("id"));
                result.put("publicId", publicId);
                result.put("link", signatureLink != null ? signatureLink
                        : "https://www.autentique.com.br/v2/documento/" + publicId);
                result.put("linkEmpresa", companyLink);

                log.info("Documento enviado com sucesso. ID: {}", result.get("id"));
                return result;
            } else {
                log.error("Erro na resposta da Autentique: {}", response);
                return null;
            }

        } catch (Exception e) {
            log.error("Erro ao enviar documento para Autentique", e);
            return null;
        }
    }

    /**
     * Verifica se um documento foi assinado por todos os signatários.
     * 
     * @param documentId ID do documento na Autentique
     * @return true se todos assinaram, false caso contrário
     */
    public boolean verificarSeEstaAssinado(String documentId) {
        log.info("Consultando status do documento na Autentique: {}", documentId);

        try {
            String query = "{\"query\": \"query { document(id: \\\"" + documentId
                    + "\\\") { id name signatures { signed { created_at } } } }\"}";

            Map<String, Object> response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(query))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data == null || data.get("document") == null) {
                    return false;
                }
                Map<String, Object> document = (Map<String, Object>) data.get("document");
                List<Map<String, Object>> signatures = (List<Map<String, Object>>) document.get("signatures");

                if (signatures == null || signatures.isEmpty()) {
                    return false;
                }

                // Verifica se TODOS os signatários já assinaram
                for (Map<String, Object> sig : signatures) {
                    if (sig.get("signed") == null) {
                        return false;
                    }
                }

                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Erro ao consultar status do documento na Autentique", e);
            return false;
        }
    }
}
