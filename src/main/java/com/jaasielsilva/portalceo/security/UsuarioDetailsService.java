package com.jaasielsilva.portalceo.security;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado com email: " + email);
        }

        Usuario usuario = usuarioOpt.get();

        // Validação do status do usuário com mensagens específicas
        if (usuario.getStatus() == Usuario.Status.DEMITIDO) {
            throw new DisabledException("Usuário demitido não pode acessar o sistema.");
        }

        if (usuario.getStatus() == Usuario.Status.INATIVO) {
            throw new LockedException("Usuário inativo não pode fazer login.");
        }

        if (usuario.getStatus() == Usuario.Status.BLOQUEADO) {
            throw new LockedException("Usuário bloqueado não pode fazer login.");
        }

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(), 
                usuario.getSenha(),
                true, 
                true, 
                true, 
                true, 
                usuario.getPerfis().stream()
                        .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.getNome()))
                        .collect(Collectors.toList())
        );
    }
}
