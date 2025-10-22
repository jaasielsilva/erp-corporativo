package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementação do AuditorAware para capturar automaticamente
 * o usuário logado nas operações de auditoria JPA.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<Usuario> {

    @Override
    public Optional<Usuario> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        
        // Se o principal é uma instância de Usuario (nosso UserDetails customizado)
        if (principal instanceof Usuario) {
            return Optional.of((Usuario) principal);
        }
        
        // Se o principal é um UserDetails padrão, tentamos buscar pelo email/username
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            // Aqui você pode implementar uma busca no repositório se necessário
            // Por enquanto, retornamos empty para evitar dependência circular
            return Optional.empty();
        }
        
        return Optional.empty();
    }
}