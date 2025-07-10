package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UsuarioRepository usuarioRepository;

    public GlobalControllerAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se está autenticado e não é usuário anonimo
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName(); // geralmente é o username (email)
            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            return usuario.orElse(null);
        }
        return null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Usuario usuario = usuarioLogado();
        if (usuario != null && usuario.getPerfis() != null) {
            return usuario.getPerfis().stream()
                    .anyMatch(perfil -> "ADMIN".equalsIgnoreCase(perfil.getNome()));
        }
        return false;
    }
}
