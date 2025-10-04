package com.jaasielsilva.portalceo.controller.rh.colaborador;

import com.jaasielsilva.portalceo.service.AdesaoSecurityService;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService.DocumentoInfo;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService.DocumentoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gestão de documentos do processo de adesão de colaboradores.
 * Responsável por upload, download, listagem, remoção e status de documentos.
 * Todos os endpoints usam o parâmetro 'sessionId' para identificação segura da sessão.
 */
@RestController
@RequestMapping("/api/rh/colaboradores/adesao/documentos")
public class DocumentoAdesaoController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoAdesaoController.class);

    @Autowired
    private DocumentoAdesaoService documentoService;

    @Autowired
    private AdesaoSecurityService securityService;

    /**
     * Upload de documento
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocumento(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("sessionId") String sessionId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        String clientIp = getClientIp(request);

        try {
            // Verificar rate limiting
            if (!securityService.checkRateLimit(clientIp)) {
                logger.warn("Rate limit excedido para IP: {}", clientIp);
                response.put("success", false);
                response.put("message", "Muitas tentativas. Tente novamente em alguns minutos.");
                return ResponseEntity.status(429).body(response);
            }

            // Verificar se sessão está bloqueada
            if (securityService.isSessionBlocked(sessionId)) {
                logger.warn("Sessão bloqueada: {} - IP: {}", sessionId, clientIp);
                response.put("success", false);
                response.put("message", "Sessão temporariamente bloqueada por motivos de segurança");
                return ResponseEntity.status(423).body(response);
            }

            // Validar arquivo com segurança
            AdesaoSecurityService.ValidationResult validation = securityService.validateFileUpload(arquivo, sessionId);
            if (!validation.isValid()) {
                logger.warn("Upload rejeitado - Sessão: {}, Erro: {}, Arquivo: {}, IP: {}", 
                    sessionId, validation.getFirstError(), arquivo.getOriginalFilename(), clientIp);
                response.put("success", false);
                response.put("message", validation.getFirstError());
                response.put("errors", validation.getErrors());
                return ResponseEntity.badRequest().body(response);
            }

            // Processar upload
            DocumentoInfo documentoInfo = documentoService.processarUpload(arquivo, tipoDocumento, sessionId);

            // Log de auditoria
            logger.info("Documento enviado - Sessão: {}, Tipo: {}, Arquivo: {}, Tamanho: {}, IP: {}", 
                sessionId, tipoDocumento, arquivo.getOriginalFilename(), arquivo.getSize(), clientIp);

            response.put("success", true);
            response.put("message", "Documento enviado com sucesso");
            response.put("documento", documentoInfo);

            logger.info("Upload realizado com sucesso - Tipo: {}, Sessão: {}, IP: {}", tipoDocumento, sessionId, clientIp);

            return ResponseEntity.ok(response);

        } catch (DocumentoException e) {
            logger.warn("Erro de validação no upload - IP: {}: {}", clientIp, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            logger.error("Erro de I/O no upload - IP: {}: {}", clientIp, e.getMessage());
            response.put("success", false);
            response.put("message", "Erro interno ao processar arquivo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            logger.error("Erro inesperado no upload - IP: {}: {}", clientIp, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erro inesperado ao processar arquivo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar documentos da sessão
     */
    @GetMapping("/listar")
    public ResponseEntity<Map<String, Object>> listarDocumentos(@RequestParam("sessionId") String sessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<DocumentoInfo> documentos = documentoService.listarDocumentos(sessionId);
            boolean documentosObrigatoriosCompletos = documentoService.verificarDocumentosObrigatorios(sessionId);

            response.put("success", true);
            response.put("documentos", documentos);
            response.put("documentosObrigatoriosCompletos", documentosObrigatoriosCompletos);
            response.put("totalDocumentos", documentos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao listar documentos: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erro ao carregar lista de documentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Remover documento
     */
    @DeleteMapping("/remover/{tipoDocumento}")
    public ResponseEntity<Map<String, Object>> removerDocumento(
            @PathVariable String tipoDocumento,
            @RequestParam("sessionId") String sessionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean removido = documentoService.removerDocumento(sessionId, tipoDocumento);

            if (removido) {
                response.put("success", true);
                response.put("message", "Documento removido com sucesso");
                logger.info("Documento {} removido da sessão {}", tipoDocumento, sessionId);
            } else {
                response.put("success", false);
                response.put("message", "Documento não encontrado");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao remover documento {}: {}", tipoDocumento, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erro ao remover documento");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Download de documento
     */
    @GetMapping("/download/{tipoDocumento}")
    public ResponseEntity<Resource> downloadDocumento(
            @PathVariable String tipoDocumento,
            @RequestParam("sessionId") String sessionId) {

        try {
            List<DocumentoInfo> documentos = documentoService.listarDocumentos(sessionId);

            // Buscar documento do tipo especificado
            DocumentoInfo documento = documentos.stream()
                    .filter(doc -> doc.getTipoInterno().equals(tipoDocumento))
                    .findFirst()
                    .orElse(null);

            if (documento == null) {
                return ResponseEntity.notFound().build();
            }

            Path caminhoArquivo = Paths.get(documento.getCaminhoArquivo());

            if (!Files.exists(caminhoArquivo)) {
                logger.warn("Arquivo não encontrado: {}", caminhoArquivo);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(caminhoArquivo);

            // Determinar content type
            String contentType = documento.getContentType();
            if (contentType == null) {
                contentType = Files.probeContentType(caminhoArquivo);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + documento.getNomeArquivo() + "\"")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Erro ao fazer download do documento {}: {}", tipoDocumento, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Visualizar documento (inline)
     */
    @GetMapping("/visualizar/{tipoDocumento}")
    public ResponseEntity<Resource> visualizarDocumento(
            @PathVariable String tipoDocumento,
            @RequestParam("sessionId") String sessionId) {

        try {
            List<DocumentoInfo> documentos = documentoService.listarDocumentos(sessionId);

            // Buscar documento do tipo especificado
            DocumentoInfo documento = documentos.stream()
                    .filter(doc -> doc.getTipoInterno().equals(tipoDocumento))
                    .findFirst()
                    .orElse(null);

            if (documento == null) {
                return ResponseEntity.notFound().build();
            }

            Path caminhoArquivo = Paths.get(documento.getCaminhoArquivo());

            if (!Files.exists(caminhoArquivo)) {
                logger.warn("Arquivo não encontrado: {}", caminhoArquivo);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(caminhoArquivo);

            // Determinar content type
            String contentType = documento.getContentType();
            if (contentType == null) {
                contentType = Files.probeContentType(caminhoArquivo);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Erro ao visualizar documento {}: {}", tipoDocumento, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar status dos documentos obrigatórios
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> verificarStatus(@RequestParam("sessionId") String sessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<DocumentoInfo> documentos = documentoService.listarDocumentos(sessionId);
            boolean documentosObrigatoriosCompletos = documentoService.verificarDocumentosObrigatorios(sessionId);

            // Contar documentos por tipo
            long documentosObrigatorios = documentos.stream()
                    .filter(DocumentoInfo::isObrigatorio)
                    .count();

            long documentosOpcionais = documentos.stream()
                    .filter(doc -> !doc.isObrigatorio())
                    .count();

            response.put("success", true);
            response.put("documentosObrigatoriosCompletos", documentosObrigatoriosCompletos);
            response.put("totalDocumentos", documentos.size());
            response.put("documentosObrigatorios", documentosObrigatorios);
            response.put("documentosOpcionais", documentosOpcionais);
            response.put("podeProximaEtapa", documentosObrigatoriosCompletos);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao verificar status dos documentos: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erro ao verificar status dos documentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Limpar todos os documentos da sessão
     */
    @DeleteMapping("/limpar")
    public ResponseEntity<Map<String, Object>> limparDocumentos(@RequestParam("sessionId") String sessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            documentoService.limparSessao(sessionId);

            response.put("success", true);
            response.put("message", "Todos os documentos foram removidos");

            logger.info("Documentos da sessão {} limpos", sessionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao limpar documentos da sessão: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erro ao limpar documentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Extrai o IP real do cliente considerando proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}