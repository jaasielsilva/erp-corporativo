package com.jaasielsilva.portalceo.interceptor;

import com.jaasielsilva.portalceo.service.AdesaoSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SecurityInterceptor Melhorado - ERP Corporativo
 * 
 * Implementa camadas adicionais de segurança incluindo:
 * - Headers de segurança avançados (HSTS, CSP melhorado, Permissions Policy)
 * - Rate limiting aprimorado
 * - Detecção de ataques automatizada
 * - Logging de segurança detalhado
 * - Limpeza automática de dados
 * 
 * @author Jaasiel Silva
 * @version 2.0 - Versão Melhorada
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    @Autowired
    private AdesaoSecurityService adesaoSecurityService;

    // ===== HEADERS DE SEGURANÇA AVANÇADOS =====
    private static final String HSTS_HEADER = "Strict-Transport-Security";
    private static final String HSTS_VALUE = "max-age=31536000; includeSubDomains; preload";
    
    private static final String CSP_HEADER = "Content-Security-Policy";
    private static final String CSP_VALUE = 
        "default-src 'self'; " +
        "script-src 'self' 'nonce-{NONCE}' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
        "img-src 'self' data: https: blob:; " +
        "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
        "connect-src 'self' https://viacep.com.br wss: ws:; " +
        "media-src 'self'; " +
        "object-src 'none'; " +
        "frame-ancestors 'none'; " +
        "base-uri 'self'; " +
        "form-action 'self'; " +
        "upgrade-insecure-requests;";
    
    private static final String PERMISSIONS_POLICY_HEADER = "Permissions-Policy";
    private static final String PERMISSIONS_POLICY_VALUE = 
        "geolocation=(), microphone=(), camera=(), payment=(), " +
        "usb=(), magnetometer=(), gyroscope=(), speaker=(), " +
        "vibrate=(), fullscreen=(self), sync-xhr=()";

    private static final String COEP_HEADER = "Cross-Origin-Embedder-Policy";
    private static final String COEP_VALUE = "require-corp";
    
    private static final String COOP_HEADER = "Cross-Origin-Opener-Policy";
    private static final String COOP_VALUE = "same-origin";

    // ===== RATE LIMITING E MONITORAMENTO =====
    private final ConcurrentHashMap<String, AtomptInfo> ipAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SuspiciousActivity> suspiciousActivities = new ConcurrentHashMap<>();
    
    // Configurações de rate limiting
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int SUSPICIOUS_THRESHOLD = 100;

    /**
     * Construtor - Agenda limpeza automática
     */
    public SecurityInterceptor() {
        logger.info("SecurityInterceptor Melhorado inicializado com proteções avançadas");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // ===== ADICIONAR HEADERS DE SEGURANÇA AVANÇADOS =====
        addAdvancedSecurityHeaders(request, response);
        
        // ===== EXTRAIR INFORMAÇÕES DA REQUISIÇÃO =====
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        // ===== RATE LIMITING AVANÇADO =====
        if (!checkAdvancedRateLimit(clientIp, requestUri)) {
            securityLogger.warn("Rate limit excedido - IP: {}, URI: {}, UserAgent: {}", 
                clientIp, requestUri, userAgent);
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
            return false;
        }
        
        // ===== DETECÇÃO DE ATIVIDADES SUSPEITAS =====
        if (detectSuspiciousActivity(clientIp, userAgent, requestUri)) {
            securityLogger.error("Atividade suspeita detectada - IP: {}, URI: {}, UserAgent: {}", 
                clientIp, requestUri, userAgent);
            // Não bloquear imediatamente, apenas logar para análise
        }
        
        // ===== VERIFICAÇÃO ESPECÍFICA PARA ENDPOINTS DE ADESÃO =====
        if (isAdesaoEndpoint(requestUri)) {
            if (!adesaoSecurityService.isRequestAllowed(clientIp)) {
                securityLogger.warn("Requisição bloqueada para endpoint de adesão - IP: {}", clientIp);
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"Too many requests for adhesion endpoint\",\"code\":\"ADESAO_RATE_LIMIT\"}");
                return false;
            }
        }
        
        // ===== VERIFICAÇÃO DE SESSÃO BLOQUEADA =====
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            if (adesaoSecurityService.isSessionBlocked(sessionId)) {
                securityLogger.warn("Sessão bloqueada detectada - SessionID: {}, IP: {}", sessionId, clientIp);
                session.invalidate();
                response.sendRedirect("/login?blocked=true");
                return false;
            }
        }
        
        // ===== LOG DE ACESSO DETALHADO =====
        logDetailedAccess(clientIp, userAgent, requestUri, method, session);
        
        return true;
    }

    /**
     * Adiciona headers de segurança avançados
     */
    private void addAdvancedSecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
        
        // Headers básicos (mantidos do original)
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // ===== NOVOS HEADERS AVANÇADOS =====
        
        // HSTS - Força HTTPS
        if (request.isSecure()) {
            response.setHeader(HSTS_HEADER, HSTS_VALUE);
        }
        
        // CSP melhorado com nonce
        String nonce = generateNonce();
        request.setAttribute("cspNonce", nonce);
        String cspWithNonce = CSP_VALUE.replace("{NONCE}", nonce);
        response.setHeader(CSP_HEADER, cspWithNonce);
        
        // Permissions Policy
        response.setHeader(PERMISSIONS_POLICY_HEADER, PERMISSIONS_POLICY_VALUE);
        
        // Cross-Origin Policies
        response.setHeader(COEP_HEADER, COEP_VALUE);
        response.setHeader(COOP_HEADER, COOP_VALUE);
        
        // Cache Control para páginas sensíveis
        if (isSensitivePage(request.getRequestURI())) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
    }

    /**
     * Rate limiting avançado com múltiplas janelas de tempo
     */
    private boolean checkAdvancedRateLimit(String clientIp, String requestUri) {
        AttemptInfo attemptInfo = ipAttempts.computeIfAbsent(clientIp, k -> new AttemptInfo());
        
        LocalDateTime now = LocalDateTime.now();
        
        // Limpar tentativas antigas
        attemptInfo.cleanOldAttempts(now);
        
        // Verificar limite por minuto
        if (attemptInfo.getRequestsInLastMinute(now) >= MAX_REQUESTS_PER_MINUTE) {
            attemptInfo.addSuspiciousActivity("RATE_LIMIT_MINUTE_EXCEEDED");
            return false;
        }
        
        // Verificar limite por hora
        if (attemptInfo.getRequestsInLastHour(now) >= MAX_REQUESTS_PER_HOUR) {
            attemptInfo.addSuspiciousActivity("RATE_LIMIT_HOUR_EXCEEDED");
            return false;
        }
        
        // Registrar tentativa
        attemptInfo.addAttempt(now);
        
        return true;
    }

    /**
     * Detecção de atividades suspeitas
     */
    private boolean detectSuspiciousActivity(String clientIp, String userAgent, String requestUri) {
        SuspiciousActivity activity = suspiciousActivities.computeIfAbsent(clientIp, k -> new SuspiciousActivity());
        
        boolean suspicious = false;
        
        // Detectar User-Agent suspeito
        if (userAgent == null || userAgent.trim().isEmpty() || 
            userAgent.toLowerCase().contains("bot") || 
            userAgent.toLowerCase().contains("crawler") ||
            userAgent.toLowerCase().contains("scanner")) {
            activity.addFlag("SUSPICIOUS_USER_AGENT");
            suspicious = true;
        }
        
        // Detectar tentativas de path traversal
        if (requestUri.contains("../") || requestUri.contains("..\\") || 
            requestUri.contains("%2e%2e") || requestUri.contains("%252e%252e")) {
            activity.addFlag("PATH_TRAVERSAL_ATTEMPT");
            suspicious = true;
        }
        
        // Detectar tentativas de SQL injection
        if (requestUri.toLowerCase().contains("union") || 
            requestUri.toLowerCase().contains("select") ||
            requestUri.toLowerCase().contains("drop") ||
            requestUri.toLowerCase().contains("insert")) {
            activity.addFlag("SQL_INJECTION_ATTEMPT");
            suspicious = true;
        }
        
        // Detectar tentativas de XSS
        if (requestUri.contains("<script") || requestUri.contains("javascript:") ||
            requestUri.contains("onload=") || requestUri.contains("onerror=")) {
            activity.addFlag("XSS_ATTEMPT");
            suspicious = true;
        }
        
        return suspicious;
    }

    /**
     * Gera nonce para CSP
     */
    private String generateNonce() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Verifica se é uma página sensível
     */
    private boolean isSensitivePage(String uri) {
        return uri.contains("/admin") || uri.contains("/rh") || 
               uri.contains("/financeiro") || uri.contains("/configuracoes") ||
               uri.contains("/relatorios") || uri.contains("/auditoria");
    }

    /**
     * Verifica se é endpoint de adesão
     */
    private boolean isAdesaoEndpoint(String requestUri) {
        return requestUri.startsWith("/adesao") || requestUri.equals("/api/processar");
    }

    /**
     * Extrai IP do cliente considerando proxies
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Log detalhado de acesso
     */
    private void logDetailedAccess(String clientIp, String userAgent, String requestUri, 
                                 String method, HttpSession session) {
        String sessionId = session != null ? session.getId() : "NO_SESSION";
        
        securityLogger.info("ACCESS - IP: {}, Method: {}, URI: {}, Session: {}, UserAgent: {}", 
            clientIp, method, requestUri, sessionId, userAgent);
    }

    /**
     * Limpeza automática de dados de segurança (a cada hora)
     */
    @Scheduled(fixedRate = 3600000) // 1 hora
    public void cleanupSecurityData() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        // Limpar tentativas antigas
        ipAttempts.entrySet().removeIf(entry -> {
            entry.getValue().cleanOldAttempts(LocalDateTime.now());
            return entry.getValue().isEmpty();
        });
        
        // Limpar atividades suspeitas antigas
        suspiciousActivities.entrySet().removeIf(entry -> 
            entry.getValue().getLastActivity().isBefore(cutoff));
        
        logger.info("Limpeza de dados de segurança concluída. IPs monitorados: {}, Atividades suspeitas: {}", 
            ipAttempts.size(), suspiciousActivities.size());
    }

    // ===== CLASSES AUXILIARES =====

    /**
     * Classe para rastrear tentativas por IP
     */
    private static class AttemptInfo {
        private final ConcurrentHashMap<LocalDateTime, Integer> attempts = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Integer> suspiciousFlags = new ConcurrentHashMap<>();
        
        public void addAttempt(LocalDateTime time) {
            attempts.put(time, 1);
        }
        
        public void addSuspiciousActivity(String flag) {
            suspiciousFlags.merge(flag, 1, Integer::sum);
        }
        
        public int getRequestsInLastMinute(LocalDateTime now) {
            LocalDateTime oneMinuteAgo = now.minusMinutes(1);
            return (int) attempts.keySet().stream()
                .filter(time -> time.isAfter(oneMinuteAgo))
                .count();
        }
        
        public int getRequestsInLastHour(LocalDateTime now) {
            LocalDateTime oneHourAgo = now.minusHours(1);
            return (int) attempts.keySet().stream()
                .filter(time -> time.isAfter(oneHourAgo))
                .count();
        }
        
        public void cleanOldAttempts(LocalDateTime now) {
            LocalDateTime cutoff = now.minusHours(24);
            attempts.entrySet().removeIf(entry -> entry.getKey().isBefore(cutoff));
        }
        
        public boolean isEmpty() {
            return attempts.isEmpty() && suspiciousFlags.isEmpty();
        }
    }

    /**
     * Classe para rastrear atividades suspeitas
     */
    private static class SuspiciousActivity {
        private final ConcurrentHashMap<String, Integer> flags = new ConcurrentHashMap<>();
        private LocalDateTime lastActivity = LocalDateTime.now();
        
        public void addFlag(String flag) {
            flags.merge(flag, 1, Integer::sum);
            lastActivity = LocalDateTime.now();
        }
        
        public LocalDateTime getLastActivity() {
            return lastActivity;
        }
        
        public int getTotalFlags() {
            return flags.values().stream().mapToInt(Integer::intValue).sum();
        }
    }
}