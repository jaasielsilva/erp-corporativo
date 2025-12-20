package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Verificar se é uma requisição para endpoints financeiros
        // A configuração de quais URLs são interceptadas está em WebSecurityConfig
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // Ignorar recursos estáticos se passarem pelo filtro (embora WebConfig deva excluir)
        if (isStaticResource(requestURI)) {
            return true;
        }

        // Validar se existe usuário logado
        Usuario usuarioLogado = getCurrentUser();
        
        if (usuarioLogado == null) {
            // Se for endpoint crítico (POST/PUT/DELETE), bloquear
            if (isModifyingOperation(method)) {
                logger.warn("Tentativa de modificação sem usuário logado - URI: {}, Method: {}, IP: {}", 
                    requestURI, method, getClientIp(request));
                
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Usuário não autenticado\"}");
                response.setContentType("application/json");
                return false;
            }
            // Para GET, apenas não injeta o usuário (ou poderia redirecionar login)
            // Mas como Spring Security cuida do login, assumimos que pode ser acesso anônimo permitido ou erro de config
            return true; 
        }
        
        // Validar se o usuário está ativo
        if (usuarioLogado.getStatus() != Usuario.Status.ATIVO) {
            logger.warn("Tentativa de acesso com usuário inativo - ID: {}, Status: {}, URI: {}", 
                usuarioLogado.getId(), usuarioLogado.getStatus(), requestURI);
            
            if (isModifyingOperation(method)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Usuário inativo\"}");
                response.setContentType("application/json");
                return false;
            }
        }
        
        // Adicionar usuário ao request para uso em Controllers e Views (Thymeleaf)
        request.setAttribute("usuarioLogado", usuarioLogado);
        
        return true;
    }

    private boolean isModifyingOperation(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }

    private boolean isStaticResource(String uri) {
        return uri.contains("/css/") || uri.contains("/js/") || uri.contains("/images/") || uri.contains("/fonts/");
    }

    /**
     * Obtém o usuário logado atual do contexto de segurança
     */
    private Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String name = authentication.getName();
        if (name == null || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        try {
            return usuarioRepository.findByEmail(name).orElse(null);
        } catch (Exception e) {
            logger.warn("Falha ao obter usuário por email '{}': {}", name, e.getMessage());
            return null;
        }
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