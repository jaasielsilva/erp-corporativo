package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.service.AdesaoSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor de segurança para aplicar validações globais
 * e executar tarefas de limpeza automática.
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
    
    @Autowired
    private AdesaoSecurityService securityService;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // Headers de segurança
    private static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    private static final String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    private static final String HEADER_REFERRER_POLICY = "Referrer-Policy";
    private static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    
    public SecurityInterceptor() {
        // Agendar limpeza automática a cada hora
        scheduler.scheduleAtFixedRate(this::cleanupSecurityData, 1, 1, TimeUnit.HOURS);
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        addSecurityHeaders(response, requestURI);
        // Verificar se é uma requisição para APIs de adesão
        if (isAdesaoEndpoint(requestURI)) {
            String clientIp = getClientIp(request);
            
            // Endpoints de alta frequência (ex.: autosave) não devem disparar rate limit global
            if (!isHighFrequencyEndpoint(requestURI)) {
                // Verificar rate limiting para endpoints críticos
                if (!securityService.checkRateLimit(clientIp)) {
                    logger.warn("Rate limit excedido para IP: {} na URL: {}", clientIp, requestURI);
                    sendJsonError(response, 429, "Muitas requisições. Tente novamente em instantes.");
                    return false;
                }
            }
            
            // Verificar se a sessão está bloqueada (para endpoints que usam sessão)
            if (requiresSession(requestURI)) {
                String sessionId = request.getSession(false) != null ? 
                    request.getSession().getId() : null;
                    
                if (sessionId != null && securityService.isSessionBlocked(sessionId)) {
                    logger.warn("Tentativa de acesso com sessão bloqueada: {} - IP: {}", sessionId, clientIp);
                    sendJsonError(response, 423, "Sessão temporariamente bloqueada por segurança.");
                    return false;
                }
            }
            
            // Log de acesso para auditoria
            logger.debug("Acesso autorizado - IP: {}, URL: {}, User-Agent: {}", 
                clientIp, requestURI, request.getHeader("User-Agent"));
        }
        
        return true;
    }
    
    /**
     * Adiciona headers de segurança à resposta
     */
    private void addSecurityHeaders(HttpServletResponse response, String requestURI) {
        // Prevenir MIME type sniffing
        response.setHeader(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
        boolean isDocs = requestURI != null && requestURI.startsWith("/documentacao");
        response.setHeader(HEADER_X_FRAME_OPTIONS, isDocs ? "SAMEORIGIN" : "DENY");
        // Ativar proteção XSS do browser
        response.setHeader(HEADER_X_XSS_PROTECTION, "1; mode=block");
        // Política de referrer
        response.setHeader(HEADER_REFERRER_POLICY, "strict-origin-when-cross-origin");
        // Content Security Policy básica
        String csp = "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://ajax.googleapis.com https://code.jquery.com; " +
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data: https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.gstatic.com; " +
                "connect-src 'self' ws: wss: https://viacep.com.br https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                "upgrade-insecure-requests; " +
                (isDocs ? "frame-ancestors 'self';" : "frame-ancestors 'none';");
        response.setHeader(HEADER_CONTENT_SECURITY_POLICY, csp);
    }
    
    /**
     * Verifica se a URL é um endpoint de adesão que precisa de validação
     */
    private boolean isAdesaoEndpoint(String requestURI) {
        return requestURI != null && (
            requestURI.contains("/rh/colaboradores/adesao") ||
            requestURI.contains("/api/rh/colaboradores/adesao") ||
            requestURI.contains("/api/rh/processos-adesao")
        );
    }

    /**
     * Endpoints de alta frequência que não devem ser contabilizados no rate limit global
     */
    private boolean isHighFrequencyEndpoint(String requestURI) {
        if (requestURI == null) return false;
        return requestURI.contains("/api/progresso/autosave") ||
               requestURI.contains("/api/progresso/resume");
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            String payload = String.format("{\"success\":false,\"message\":\"%s\"}",
                    message.replace("\"", "\\\""));
            response.getWriter().write(payload);
            response.getWriter().flush();
        } catch (Exception ignored) {
        }
    }
    
    /**
     * Verifica se o endpoint requer sessão
     */
    private boolean requiresSession(String requestURI) {
        return requestURI != null && (
            requestURI.contains("/dados-pessoais") ||
            requestURI.contains("/documentos") ||
            (requestURI.contains("/beneficios") && !requestURI.contains("/beneficios/calcular") && !requestURI.contains("/beneficios/disponiveis")) ||
            requestURI.contains("/upload") ||
            requestURI.contains("/finalizar")
        );
    }
    
    /**
     * Extrai o IP real do cliente
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
        
        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty() && !"unknown".equalsIgnoreCase(xForwarded)) {
            return xForwarded;
        }
        
        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return forwarded;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Executa limpeza automática de dados de segurança
     */
    private void cleanupSecurityData() {
        try {
            logger.debug("Iniciando limpeza automática de dados de segurança");
            securityService.cleanupOldData();
            logger.debug("Limpeza automática concluída com sucesso");
        } catch (Exception e) {
            logger.error("Erro durante limpeza automática de dados de segurança", e);
        }
    }
    
    /**
     * Cleanup do scheduler quando o bean for destruído
     */
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
