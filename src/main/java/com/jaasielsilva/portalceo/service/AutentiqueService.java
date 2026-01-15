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

    public Map<String, String> enviarDocumento(String filePath, String documentName, String clienteEmail,
            String advogadoEmail, java.util.List<String> extraEmails) {
        log.info("Iniciando envio de documento para Autentique: {} cliente={} advogado={} extras={}", documentName,
                clienteEmail, advogadoEmail, extraEmails);

        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                log.error("Arquivo não encontrado: {}", filePath);
                return null;
            }

            java.util.LinkedHashSet<String> allSigners = new java.util.LinkedHashSet<>();
            if (clienteEmail != null && !clienteEmail.isBlank()) {
                allSigners.add(clienteEmail.trim());
            }
            if (advogadoEmail != null && !advogadoEmail.isBlank()) {
                allSigners.add(advogadoEmail.trim());
            }
            if (extraEmails != null) {
                for (String e : extraEmails) {
                    if (e != null && !e.isBlank()) {
                        allSigners.add(e.trim());
                    }
                }
            }
            if (allSigners.isEmpty()) {
                log.error("Nenhum e-mail de signatário informado");
                return null;
            }

            StringBuilder signersJson = new StringBuilder("[");
            for (String s : allSigners) {
                if (signersJson.length() > 1) {
                    signersJson.append(",");
                }
                String email = s.replace("\"", "\\\"");
                signersJson.append("{ \"email\": \"").append(email).append("\", \"action\": \"SIGN\" }");
            }
            signersJson.append("]");

            String safeName = documentName != null ? documentName.replace("\"", "\\\"") : "Documento";

            String operations = "{\"query\": \"mutation createDocument($document: DocumentInput!, $signers: [SignerInput!]!, $file: Upload!) { createDocument(document: $document, signers: $signers, file: $file) { id name signatures { public_id user { email } link { short_link } } } }\", \"variables\": { \"document\": { \"name\": \""
                    + safeName + "\" }, \"signers\": " + signersJson.toString() + ", \"file\": null } }";
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
                    String clientEmailLower = clienteEmail != null ? clienteEmail.toLowerCase() : null;
                    java.util.Set<String> extrasLower = new java.util.HashSet<>();
                    if (extraEmails != null) {
                        for (String e : extraEmails) {
                            if (e != null && !e.isBlank()) {
                                extrasLower.add(e.trim().toLowerCase());
                            }
                        }
                    }

                    for (Map<String, Object> sig : signatures) {
                        Map<String, Object> user = (Map<String, Object>) sig.get("user");
                        if (user != null) {
                            String email = (String) user.get("email");
                            String emailLower = email != null ? email.toLowerCase() : null;
                            Map<String, Object> linkObj = (Map<String, Object>) sig.get("link");
                            String link = linkObj != null ? (String) linkObj.get("short_link") : null;

                            if (clientEmailLower != null && clientEmailLower.equals(emailLower)) {
                                publicId = (String) sig.get("public_id");
                                signatureLink = link;
                            } else if (extrasLower.contains(emailLower) && companyLink == null) {
                                companyLink = link;
                            }
                        }
                    }

                    if (publicId == null && !signatures.isEmpty()) {
                        publicId = (String) signatures.get(0).get("public_id");
                    }
                }

                String documentId = (String) createDocument.get("id");

                Map<String, String> result = new HashMap<>();
                result.put("id", documentId);
                result.put("publicId", publicId);

                String defaultViewerLink = publicId != null
                        ? "https://www.autentique.com.br/v2/documento/" + publicId
                        : null;
                result.put("link", signatureLink != null ? signatureLink : defaultViewerLink);

                String companyPanelLink = documentId != null
                        ? "https://painel.autentique.com.br/documentos/" + documentId
                        : defaultViewerLink;
                result.put("linkEmpresa", companyPanelLink);

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
        Map<String, Object> resumo = obterResumoAssinaturas(documentId);
        Object todosAssinaram = resumo.get("todosAssinaram");
        if (todosAssinaram instanceof Boolean) {
            return (Boolean) todosAssinaram;
        }
        return false;
    }

    /**
     * Versão antiga mantida para compatibilidade (contratos, etc.).
     * Usa apenas o e-mail principal e o e-mail padrão da empresa.
     */
    public Map<String, String> enviarDocumento(String filePath, String documentName, String signerEmail) {
        java.util.List<String> extras = java.util.List.of(companyEmail);
        return enviarDocumento(filePath, documentName, signerEmail, null, extras);
    }

    public Map<String, String> enviarDocumento(String filePath, String documentName, String clienteEmail,
            java.util.List<String> extraEmails) {
        return enviarDocumento(filePath, documentName, clienteEmail, null, extraEmails);
    }

    public Map<String, Object> obterResumoAssinaturas(String documentId) {
        log.info("Consultando detalhes de assinaturas na Autentique: {}", documentId);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("todosAssinaram", false);
        resultado.put("pendentes", List.of());

        try {
            String query = "{\"query\": \"query { document(id: \\\"" + documentId
                    + "\\\") { id name signatures { user { name email } signed { created_at } } } }\"}";

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
                    return resultado;
                }
                Map<String, Object> document = (Map<String, Object>) data.get("document");
                List<Map<String, Object>> signatures = (List<Map<String, Object>>) document.get("signatures");

                if (signatures == null || signatures.isEmpty()) {
                    return resultado;
                }

                java.util.List<String> pendentes = new java.util.ArrayList<>();
                boolean todosAssinaram = true;
                String companyEmailLower = companyEmail != null ? companyEmail.toLowerCase() : null;

                for (Map<String, Object> sig : signatures) {
                    Map<String, Object> user = (Map<String, Object>) sig.get("user");
                    String nome = user != null ? (String) user.get("name") : null;
                    String email = user != null ? (String) user.get("email") : null;
                    String emailLower = email != null ? email.toLowerCase() : null;

                    if (companyEmailLower != null && companyEmailLower.equals(emailLower)) {
                        continue;
                    }

                    Map<String, Object> signed = (Map<String, Object>) sig.get("signed");
                    if (signed == null) {
                        todosAssinaram = false;
                        String identificador = nome != null && !nome.isBlank() ? nome : email;
                        if (identificador != null && !identificador.isBlank()) {
                            pendentes.add(identificador);
                        }
                    }
                }

                resultado.put("todosAssinaram", todosAssinaram);
                resultado.put("pendentes", pendentes);
                return resultado;
            }

            return resultado;
        } catch (Exception e) {
            log.error("Erro ao consultar detalhes de assinaturas na Autentique", e);
            return resultado;
        }
    }

    /**
     * Baixa o documento assinado da Autentique.
     * 
     * @param documentId ID do documento na Autentique
     * @return Conteúdo do arquivo em bytes ou null se falhar
     */
    public byte[] baixarDocumentoAssinado(String documentId) {
        log.info("Baixando documento assinado da Autentique: {}", documentId);

        try {
            // Consulta para obter a URL do arquivo assinado
            String query = "{\"query\": \"query { document(id: \\\"" + documentId
                    + "\\\") { files { signed } } }\"}";

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
                    return null;
                }
                Map<String, Object> document = (Map<String, Object>) data.get("document");
                Map<String, Object> files = (Map<String, Object>) document.get("files");
                
                if (files != null && files.get("signed") != null) {
                    String signedUrl = (String) files.get("signed");
                    
                    // Baixar o arquivo da URL assinada (URL externa, usa novo cliente)
                    return WebClient.create().get()
                            .uri(signedUrl)
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .block();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Erro ao baixar documento da Autentique", e);
            return null;
        }
    }
}
