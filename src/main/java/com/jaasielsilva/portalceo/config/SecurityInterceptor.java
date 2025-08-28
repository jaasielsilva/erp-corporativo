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
        
        // Adicionar headers de segurança
        addSecurityHeaders(response);
        
        // Verificar se é uma requisição para APIs de adesão
        String requestURI = request.getRequestURI();
        if (isAdesaoEndpoint(requestURI)) {
            
            // Verificar rate limiting para endpoints críticos
            String clientIp = getClientIp(request);
            if (!securityService.checkRateLimit(clientIp)) {
                logger.warn("Rate limit excedido para IP: {} na URL: {}", clientIp, requestURI);
                response.setStatus(429); // Too Many Requests
                return false;
            }
            
            // Verificar se a sessão está bloqueada (para endpoints que usam sessão)
            if (requiresSession(requestURI)) {
                String sessionId = request.getSession(false) != null ? 
                    request.getSession().getId() : null;
                    
                if (sessionId != null && securityService.isSessionBlocked(sessionId)) {
                    logger.warn("Tentativa de acesso com sessão bloqueada: {} - IP: {}", sessionId, clientIp);
                    response.setStatus(423); // Locked
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
    private void addSecurityHeaders(HttpServletResponse response) {
        // Prevenir MIME type sniffing
        response.setHeader(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
        
        // Prevenir clickjacking
        response.setHeader(HEADER_X_FRAME_OPTIONS, "DENY");
        
        // Ativar proteção XSS do browser
        response.setHeader(HEADER_X_XSS_PROTECTION, "1; mode=block");
        
        // Política de referrer
        response.setHeader(HEADER_REFERRER_POLICY, "strict-origin-when-cross-origin");
        
        // Content Security Policy básica
        response.setHeader(HEADER_CONTENT_SECURITY_POLICY, 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none';");
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
     * Verifica se o endpoint requer sessão
     */
    private boolean requiresSession(String requestURI) {
        return requestURI != null && (
            requestURI.contains("/dados-pessoais") ||
            requestURI.contains("/documentos") ||
            requestURI.contains("/beneficios") ||
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