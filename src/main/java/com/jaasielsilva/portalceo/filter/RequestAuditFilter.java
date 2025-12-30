package com.jaasielsilva.portalceo.filter;

import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class RequestAuditFilter extends OncePerRequestFilter {

    @Autowired
    private AuditoriaRhLogService auditoriaService;

    private static final Set<String> IGNORES = Set.of(
            "/css/", "/js/", "/images/", "/webjars/", "/ws/", "/favicon.ico",
            "/rh/ponto-escalas/api/escalas/gerar-automatico/status"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (shouldIgnore(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String usuario = null;
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    usuario = auth.getName();
                }
            } catch (Exception ignored) {}

            String ip = request.getRemoteAddr();
            String metodo = request.getMethod();
            boolean sucesso = response.getStatus() < 400;

            String categoria = categorize(metodo, uri);
            String detalhes = "status=" + response.getStatus() + ", duracaoMs=" + duration;

            try {
                auditoriaService.registrar(categoria, "REQUISICAO", uri, usuario, ip, detalhes, sucesso);
            } catch (Exception ignored) {}
        }
    }

    private boolean shouldIgnore(String uri) {
        if (uri == null) return true;
        String u = uri.toLowerCase();
        for (String p : IGNORES) {
            if (u.startsWith(p)) return true;
        }
        return false;
    }

    private String categorize(String method, String uri) {
        if (uri == null) return "ACESSO";
        if (uri.startsWith("/rh")) return "RH";
        if (uri.startsWith("/financeiro")) return "FINANCEIRO";
        if (uri.startsWith("/ti")) return "TI";
        if (uri.startsWith("/estoque")) return "ESTOQUE";
        if (uri.startsWith("/solicitacoes")) return "SERVICOS";
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) return "OPERACAO";
        return "ACESSO";
    }
}
