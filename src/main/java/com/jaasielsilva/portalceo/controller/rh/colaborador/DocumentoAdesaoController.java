package com.jaasielsilva.portalceo.controller.rh.colaborador;

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

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gestão de documentos do processo de adesão de colaboradores.
 * Responsável por upload, download, listagem e remoção de documentos.
 */
@RestController
@RequestMapping("/api/rh/colaboradores/adesao/documentos")
public class DocumentoAdesaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentoAdesaoController.class);
    
    @Autowired
    private DocumentoAdesaoService documentoService;
    
    /**
     * Upload de documento
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocumento(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipoDocumento") String tipoDocumento,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sessionId = session.getId();
            
            // Processar upload
            DocumentoInfo documentoInfo = documentoService.processarUpload(arquivo, tipoDocumento, sessionId);
            
            response.put("sucesso", true);
            response.put("mensagem", "Documento enviado com sucesso");
            response.put("documento", documentoInfo);
            
            logger.info("Upload realizado com sucesso - Tipo: {}, Sessão: {}", tipoDocumento, sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (DocumentoException e) {
            logger.warn("Erro de validação no upload: {}", e.getMessage());
            response.put("sucesso", false);
            response.put("mensagem", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            logger.error("Erro de I/O no upload: {}", e.getMessage());
            response.put("sucesso", false);
            response.put("mensagem", "Erro interno ao processar arquivo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (Exception e) {
            logger.error("Erro inesperado no upload: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("mensagem", "Erro inesperado ao processar arquivo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Listar documentos da sessão
     */
    @GetMapping("/listar")
    public ResponseEntity<Map<String, Object>> listarDocumentos(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sessionId = session.getId();
            List<DocumentoInfo> documentos = documentoService.listarDocumentos(sessionId);
            boolean documentosObrigatoriosCompletos = documentoService.verificarDocumentosObrigatorios(sessionId);
            
            response.put("sucesso", true);
            response.put("documentos", documentos);
            response.put("documentosObrigatoriosCompletos", documentosObrigatoriosCompletos);
            response.put("totalDocumentos", documentos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao listar documentos: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao carregar lista de documentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Remover documento
     */
    @DeleteMapping("/remover/{tipoDocumento}")
    public ResponseEntity<Map<String, Object>> removerDocumento(
            @PathVariable String tipoDocumento,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sessionId = session.getId();
            boolean removido = documentoService.removerDocumento(sessionId, tipoDocumento);
            
            if (removido) {
                response.put("sucesso", true);
                response.put("mensagem", "Documento removido com sucesso");
                logger.info("Documento {} removido da sessão {}", tipoDocumento, sessionId);
            } else {
                response.put("sucesso", false);
                response.put("mensagem", "Documento não encontrado");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao remover documento {}: {}", tipoDocumento, e.getMessage(), e);
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao remover documento");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Download de documento
     */
    @GetMapping("/download/{tipoDocumento}")
    public ResponseEntity<Resource> downloadDocumento(
            @PathVariable String tipoDocumento,
            HttpSession session) {
        
        try {
            String sessionId = session.getId();
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
            HttpSession session) {
        
        try {
            String sessionId = session.getId();
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
    public ResponseEntity<Map<String, Object>> verificarStatus(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sessionId = session.getId();
            List<DocumentoInfo> documentos = documentoService.listarDocumentos(sessionId);
            boolean documentosObrigatoriosCompletos = documentoService.verificarDocumentosObrigatorios(sessionId);
            
            // Contar documentos por tipo
            long documentosObrigatorios = documentos.stream()
                    .filter(DocumentoInfo::isObrigatorio)
                    .count();
            
            long documentosOpcionais = documentos.stream()
                    .filter(doc -> !doc.isObrigatorio())
                    .count();
            
            response.put("sucesso", true);
            response.put("documentosObrigatoriosCompletos", documentosObrigatoriosCompletos);
            response.put("totalDocumentos", documentos.size());
            response.put("documentosObrigatorios", documentosObrigatorios);
            response.put("documentosOpcionais", documentosOpcionais);
            response.put("podeProximaEtapa", documentosObrigatoriosCompletos);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao verificar status dos documentos: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao verificar status dos documentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Limpar todos os documentos da sessão
     */
    @DeleteMapping("/limpar")
    public ResponseEntity<Map<String, Object>> limparDocumentos(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sessionId = session.getId();
            documentoService.limparSessao(sessionId);
            
            response.put("sucesso", true);
            response.put("mensagem", "Todos os documentos foram removidos");
            
            logger.info("Documentos da sessão {} limpos", sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao limpar documentos da sessão: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("mensagem", "Erro ao limpar documentos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}