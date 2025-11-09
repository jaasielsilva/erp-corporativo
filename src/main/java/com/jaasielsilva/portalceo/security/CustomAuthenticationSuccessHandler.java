package com.jaasielsilva.portalceo.security;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.AcaoUsuario;
import com.jaasielsilva.portalceo.repository.AcaoUsuarioRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final AcaoUsuarioRepository acaoUsuarioRepository;

    public CustomAuthenticationSuccessHandler(UsuarioRepository usuarioRepository,
                                              AcaoUsuarioRepository acaoUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.acaoUsuarioRepository = acaoUsuarioRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String email = authentication.getName();

        final String ipAddressRaw = request.getRemoteAddr();
        final String ipAddress = "0:0:0:0:0:0:0:1".equals(ipAddressRaw) ? "localhost" : ipAddressRaw;

        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            usuario.setUltimoAcesso(LocalDateTime.now());
            usuario.setOnline(true);
            usuarioRepository.save(usuario);

            try {
                AcaoUsuario acao = new AcaoUsuario(LocalDateTime.now(), "Login", usuario, null);
                acao.setIp(ipAddress);
                acaoUsuarioRepository.save(acao);
            } catch (Exception ignored) {}
        });

        // Redireciona para dashboard
        response.sendRedirect("/dashboard");
    }
}
