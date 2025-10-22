package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para validar se existe um usuário logado válido
 * antes de executar operações críticas do sistema financeiro.
 */
@Component
public class UsuarioLogadoInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioLogadoInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Verificar se é uma requisição para endpoints financeiros críticos
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        if (isFinancialCriticalEndpoint(requestURI, method)) {
            
            // Validar se existe usuário logado
            Usuario usuarioLogado = getCurrentUser();
            
            if (usuarioLogado == null) {
                logger.warn("Tentativa de acesso a endpoint crítico sem usuário logado - URI: {}, Method: {}, IP: {}", 
                    requestURI, method, getClientIp(request));
                
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Usuário não autenticado para operação crítica\"}");
                response.setContentType("application/json");
                return false;
            }
            
            // Validar se o usuário está ativo
            if (usuarioLogado.getStatus() != Usuario.Status.ATIVO) {
                logger.warn("Tentativa de acesso com usuário inativo - ID: {}, Status: {}, URI: {}", 
                    usuarioLogado.getId(), usuarioLogado.getStatus(), requestURI);
                
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Usuário não está ativo no sistema\"}");
                response.setContentType("application/json");
                return false;
            }
            
            // Adicionar usuário ao request para uso posterior
            request.setAttribute("usuarioLogado", usuarioLogado);
            
            // Log de auditoria para operações críticas
            logger.info("Acesso autorizado a endpoint crítico - Usuário: {} (ID: {}), URI: {}, Method: {}", 
                usuarioLogado.getNome(), usuarioLogado.getId(), requestURI, method);
        }
        
        return true;
    }

    /**
     * Verifica se o endpoint é crítico para operações financeiras
     */
    private boolean isFinancialCriticalEndpoint(String uri, String method) {
        // Endpoints de criação, edição, exclusão e aprovação
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            return uri.contains("/financeiro/") || 
                   uri.contains("/conta-pagar/") || 
                   uri.contains("/conta-receber/") ||
                   uri.contains("/fluxo-caixa/") ||
                   uri.contains("/vendas/");
        }
        
        // Endpoints específicos de aprovação e operações críticas
        return uri.contains("/aprovar") || 
               uri.contains("/excluir") || 
               uri.contains("/pagar") ||
               uri.contains("/receber") ||
               uri.matches(".*/(editar|salvar|criar).*");
    }

    /**
     * Obtém o usuário logado atual do contexto de segurança
     */
    private Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        
        return null;
    }

    /**
     * Obtém o IP do cliente da requisição
     */
    private String getClientIp(HttpServletRequest request) {
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
}