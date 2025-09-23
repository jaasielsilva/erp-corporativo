package com.jaasielsilva.portalceo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Serviço de auditoria para registrar ações importantes
 * no processo de adesão de colaboradores.
 */
@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    // Cache de eventos de auditoria por sessão
    private final Map<String, ConcurrentLinkedQueue<AuditEvent>> sessionEvents = new ConcurrentHashMap<>();
    
    // Tipos de evento
    public enum EventType {
        INICIO_PROCESSO,
        DADOS_PESSOAIS_SALVOS,
        DOCUMENTO_ENVIADO,
        DOCUMENTO_REMOVIDO,
        BENEFICIO_SELECIONADO,
        BENEFICIO_REMOVIDO,
        PROCESSO_FINALIZADO,
        PROCESSO_CANCELADO,
        ERRO_VALIDACAO,
        TENTATIVA_ACESSO_BLOQUEADO,
        RATE_LIMIT_EXCEDIDO,
        UPLOAD_REJEITADO,
        SESSAO_EXPIRADA
    }
    
    /**
     * Registra evento de auditoria
     */
    public void logEvent(String sessionId, EventType eventType, String description, 
                        String clientIp, String userAgent, Map<String, Object> additionalData) {
        
        AuditEvent event = new AuditEvent(
            sessionId,
            eventType,
            description,
            clientIp,
            userAgent,
            LocalDateTime.now(),
            additionalData
        );
        
        // Adicionar ao cache da sessão
        sessionEvents.computeIfAbsent(sessionId, k -> new ConcurrentLinkedQueue<>()).add(event);
        
        // Log estruturado para auditoria
        auditLogger.info("AUDIT_EVENT: {} | SESSION: {} | IP: {} | DESCRIPTION: {} | TIMESTAMP: {} | DATA: {}",
            eventType,
            sessionId,
            clientIp,
            description,
            event.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            additionalData != null ? additionalData.toString() : "N/A"
        );
        
        // Log adicional para eventos críticos
        if (isCriticalEvent(eventType)) {
            logger.warn("EVENTO CRÍTICO - {}: {} - Sessão: {} - IP: {}", 
                eventType, description, sessionId, clientIp);
        }
    }
    
    /**
     * Registra evento simples sem dados adicionais
     */
    public void logEvent(String sessionId, EventType eventType, String description, String clientIp) {
        logEvent(sessionId, eventType, description, clientIp, null, null);
    }
    
    /**
     * Registra início do processo de adesão
     */
    public void logInicioProcesso(String sessionId, String cpf, String email, String clientIp, String userAgent) {
        Map<String, Object> data = new HashMap<>();
        data.put("cpf", maskCpf(cpf));
        data.put("email", maskEmail(email));
        
        logEvent(sessionId, EventType.INICIO_PROCESSO, 
            "Processo de adesão iniciado", clientIp, userAgent, data);
    }
    
    /**
     * Registra salvamento de dados pessoais
     */
    public void logDadosPessoaisSalvos(String sessionId, String cpf, String nome, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("cpf", maskCpf(cpf));
        data.put("nome", nome);
        
        logEvent(sessionId, EventType.DADOS_PESSOAIS_SALVOS, 
            "Dados pessoais salvos com sucesso", clientIp, null, data);
    }
    
    /**
     * Registra upload de documento
     */
    public void logDocumentoEnviado(String sessionId, String tipoDocumento, String nomeArquivo, 
                                   long tamanhoArquivo, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("tipoDocumento", tipoDocumento);
        data.put("nomeArquivo", nomeArquivo);
        data.put("tamanhoArquivo", tamanhoArquivo);
        
        logEvent(sessionId, EventType.DOCUMENTO_ENVIADO, 
            "Documento enviado: " + tipoDocumento, clientIp, null, data);
    }
    
    /**
     * Registra remoção de documento
     */
    public void logDocumentoRemovido(String sessionId, String tipoDocumento, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("tipoDocumento", tipoDocumento);
        
        logEvent(sessionId, EventType.DOCUMENTO_REMOVIDO, 
            "Documento removido: " + tipoDocumento, clientIp, null, data);
    }
    
    /**
     * Registra seleção de benefício
     */
    public void logBeneficioSelecionado(String sessionId, String tipoBeneficio, String plano, 
                                       Double valor, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("tipoBeneficio", tipoBeneficio);
        data.put("plano", plano);
        data.put("valor", valor);
        
        logEvent(sessionId, EventType.BENEFICIO_SELECIONADO, 
            "Benefício selecionado: " + tipoBeneficio, clientIp, null, data);
    }
    
    /**
     * Registra finalização do processo
     */
    public void logProcessoFinalizado(String sessionId, Long colaboradorId, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("colaboradorId", colaboradorId);
        
        logEvent(sessionId, EventType.PROCESSO_FINALIZADO, 
            "Processo de adesão finalizado com sucesso", clientIp, null, data);
    }
    
    /**
     * Registra cancelamento do processo
     */
    public void logProcessoCancelado(String sessionId, String motivo, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("motivo", motivo);
        
        logEvent(sessionId, EventType.PROCESSO_CANCELADO, 
            "Processo de adesão cancelado", clientIp, null, data);
    }
    
    /**
     * Registra erro de validação
     */
    public void logErroValidacao(String sessionId, String campo, String erro, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("campo", campo);
        data.put("erro", erro);
        
        logEvent(sessionId, EventType.ERRO_VALIDACAO, 
            "Erro de validação: " + campo, clientIp, null, data);
    }
    
    /**
     * Registra tentativa de acesso bloqueado
     */
    public void logAcessoBloqueado(String sessionId, String motivo, String clientIp, String userAgent) {
        Map<String, Object> data = new HashMap<>();
        data.put("motivo", motivo);
        
        logEvent(sessionId, EventType.TENTATIVA_ACESSO_BLOQUEADO, 
            "Tentativa de acesso bloqueado", clientIp, userAgent, data);
    }
    
    /**
     * Registra rate limit excedido
     */
    public void logRateLimitExcedido(String clientIp, String endpoint, String userAgent) {
        Map<String, Object> data = new HashMap<>();
        data.put("endpoint", endpoint);
        
        logEvent("N/A", EventType.RATE_LIMIT_EXCEDIDO, 
            "Rate limit excedido", clientIp, userAgent, data);
    }
    
    /**
     * Registra upload rejeitado
     */
    public void logUploadRejeitado(String sessionId, String motivo, String nomeArquivo, 
                                  String tipoArquivo, long tamanho, String clientIp) {
        Map<String, Object> data = new HashMap<>();
        data.put("motivo", motivo);
        data.put("nomeArquivo", nomeArquivo);
        data.put("tipoArquivo", tipoArquivo);
        data.put("tamanho", tamanho);
        
        logEvent(sessionId, EventType.UPLOAD_REJEITADO, 
            "Upload rejeitado: " + motivo, clientIp, null, data);
    }
    
    /**
     * Obtém histórico de eventos de uma sessão
     */
    public ConcurrentLinkedQueue<AuditEvent> getSessionEvents(String sessionId) {
        return sessionEvents.getOrDefault(sessionId, new ConcurrentLinkedQueue<>());
    }
    
    /**
     * Remove eventos de uma sessão (após finalização)
     */
    public void clearSessionEvents(String sessionId) {
        sessionEvents.remove(sessionId);
        logger.debug("Eventos de auditoria removidos para sessão: {}", sessionId);
    }
    
    /**
     * Verifica se é um evento crítico
     */
    private boolean isCriticalEvent(EventType eventType) {
        return eventType == EventType.TENTATIVA_ACESSO_BLOQUEADO ||
               eventType == EventType.RATE_LIMIT_EXCEDIDO ||
               eventType == EventType.UPLOAD_REJEITADO ||
               eventType == EventType.PROCESSO_CANCELADO;
    }
    
    /**
     * Mascara CPF para logs
     */
    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***";
        return cpf.substring(0, 3) + ".***.**" + cpf.substring(cpf.length() - 2);
    }
    
    /**
     * Mascara email para logs
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "***@" + domain;
        }
        
        return localPart.substring(0, 2) + "***@" + domain;
    }
    
    /**
     * Classe para representar um evento de auditoria
     */
    public static class AuditEvent {
        private final String sessionId;
        private final EventType eventType;
        private final String description;
        private final String clientIp;
        private final String userAgent;
        private final LocalDateTime timestamp;
        private final Map<String, Object> additionalData;
        
        public AuditEvent(String sessionId, EventType eventType, String description, 
                         String clientIp, String userAgent, LocalDateTime timestamp, 
                         Map<String, Object> additionalData) {
            this.sessionId = sessionId;
            this.eventType = eventType;
            this.description = description;
            this.clientIp = clientIp;
            this.userAgent = userAgent;
            this.timestamp = timestamp;
            this.additionalData = additionalData != null ? new HashMap<>(additionalData) : new HashMap<>();
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public EventType getEventType() { return eventType; }
        public String getDescription() { return description; }
        public String getClientIp() { return clientIp; }
        public String getUserAgent() { return userAgent; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getAdditionalData() { return additionalData; }
    }
}