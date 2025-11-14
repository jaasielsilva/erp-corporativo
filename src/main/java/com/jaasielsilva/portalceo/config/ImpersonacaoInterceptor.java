package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.security.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ImpersonacaoInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ImpersonacaoInterceptor.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        boolean ativa = session != null && Boolean.TRUE.equals(session.getAttribute("impersonacaoAtiva"));

        if (!ativa) {
            return true;
        }

        String method = request.getMethod();
        String emailAlvo = (String) session.getAttribute("impersonacaoEmail");

        if (emailAlvo == null || emailAlvo.isBlank()) {
            return true;
        }

        // Bloqueia escrita
        if (isWriteMethod(method)) {
            logger.warn("Bloqueio de escrita em modo leitura - URI: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\":\"Modo leitura ativo — ação bloqueada\"}");
            return false;
        }

        // Aplicar impersonação apenas em GET/HEAD
        Authentication atual = SecurityContextHolder.getContext().getAuthentication();

        if (atual != null) {
            request.setAttribute("__auth_original__", atual);

            var userDetails = usuarioDetailsService.loadUserByUsername(emailAlvo);

            // 🔥 CORREÇÃO AQUI
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.addAll(userDetails.getAuthorities());
            authorities.add(new SimpleGrantedAuthority("ROLE_READONLY"));

            UsernamePasswordAuthenticationToken impersonado =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            "N/A",
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(impersonado);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        // restaura o authentication original
        Object original = request.getAttribute("__auth_original__");
        if (original instanceof Authentication) {
            SecurityContextHolder.getContext().setAuthentication((Authentication) original);
            request.removeAttribute("__auth_original__");
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

        HttpSession session = request.getSession(false);
        if (modelAndView == null || session == null) return;

        boolean ativa = Boolean.TRUE.equals(session.getAttribute("impersonacaoAtiva"));
        String emailAlvo = (String) session.getAttribute("impersonacaoEmail");

        if (ativa && emailAlvo != null && !emailAlvo.isBlank()) {
            Optional<Usuario> alvoOpt = usuarioRepository.findByEmailSimple(emailAlvo);
            alvoOpt.ifPresent(usuario -> modelAndView.addObject("usuarioVisao", usuario));
            modelAndView.addObject("modoLeitura", true);
        } else {
            modelAndView.addObject("modoLeitura", false);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean podeTrocarVisao = false;
            if (auth != null && auth.getAuthorities() != null) {
                podeTrocarVisao = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .anyMatch(a -> a.equalsIgnoreCase("ROLE_ADMIN") || a.equalsIgnoreCase("ROLE_MASTER") || a.equalsIgnoreCase("ROLE_ADMINISTRADOR"));
            }
            modelAndView.addObject("podeTrocarVisao", podeTrocarVisao);
        }
    }

    private boolean isWriteMethod(String method) {
        return method.equalsIgnoreCase("POST")
                || method.equalsIgnoreCase("PUT")
                || method.equalsIgnoreCase("DELETE")
                || method.equalsIgnoreCase("PATCH");
    }
}
