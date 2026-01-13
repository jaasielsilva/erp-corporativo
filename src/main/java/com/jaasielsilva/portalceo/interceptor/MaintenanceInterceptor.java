package com.jaasielsilva.portalceo.interceptor;

import com.jaasielsilva.portalceo.service.ConfiguracaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MaintenanceInterceptor implements HandlerInterceptor {

    private final ConfiguracaoService configuracaoService;

    public MaintenanceInterceptor(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (configuracaoService.isModoManutencaoAtivo()) {
            String uri = request.getRequestURI();
            
            // Permite recursos estáticos e login
            if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images") || 
                uri.startsWith("/login") || uri.startsWith("/api/admin") || uri.equals("/manutencao")) {
                return true;
            }

            // Verifica se o usuário é MASTER (Não pode ser bloqueado)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                boolean isMaster = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));
                
                if (isMaster) {
                    return true; // MASTER acessa livremente mesmo em manutenção
                }
            }

            // Bloqueia demais usuários
            response.sendRedirect("/manutencao");
            return false;
        }
        return true;
    }
}
