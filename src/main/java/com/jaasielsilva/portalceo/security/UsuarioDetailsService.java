package com.jaasielsilva.portalceo.security;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final Logger log = LoggerFactory.getLogger(UsuarioDetailsService.class);

    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    long t0 = System.nanoTime();
    // Tentar buscar por email primeiro
    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);

    // Se não encontrar por email, tentar por matrícula
    if (usuarioOpt.isEmpty()) {
        usuarioOpt = usuarioRepository.findByMatricula(username);
    }
    long tLookup = System.nanoTime();

    if (usuarioOpt.isEmpty()) {
        throw new UsernameNotFoundException("Usuário não encontrado com email ou matrícula: " + username);
    }

    Usuario usuario = usuarioOpt.get();

    // Validação do status do usuário com mensagens específicas
    switch (usuario.getStatus()) {
        case DEMITIDO -> throw new DisabledException("Usuário demitido não pode acessar o sistema.");
        case INATIVO -> throw new LockedException("Usuário inativo não pode fazer login.");
        case BLOQUEADO -> throw new LockedException("Usuário bloqueado não pode fazer login.");
    }

    // 🔹 Se o usuário for MASTER, dar todas as permissões
    var authorities = new java.util.HashSet<SimpleGrantedAuthority>();
    if (usuario.getNivelAcesso() != null && usuario.getNivelAcesso().name().equalsIgnoreCase("MASTER")) {
        authorities.add(new SimpleGrantedAuthority("ROLE_MASTER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_CRIAR"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_VISUALIZAR"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_ATRIBUIR"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_INICIAR"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_RESOLVER"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_FECHAR"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_REABRIR"));
        authorities.add(new SimpleGrantedAuthority("CHAMADO_AVALIAR"));
        authorities.add(new SimpleGrantedAuthority("TECNICO_ATENDER_CHAMADOS"));
        authorities.add(new SimpleGrantedAuthority("TECNICO_GERENCIAR_PROPRIOS_CHAMADOS"));
        authorities.add(new SimpleGrantedAuthority("ADMIN_GERENCIAR_USUARIOS"));
    } else {
        // 🔹 Caso não seja MASTER, pegar as permissões normais dos perfis
        authorities.addAll(
            usuario.getPerfis().stream()
                    .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.getNome()))
                    .collect(Collectors.toSet())
        );
    }

    long tAuthorities = System.nanoTime();
    if (log.isDebugEnabled()) {
        long lookupMs = (tLookup - t0) / 1_000_000;
        long authBuildMs = (tAuthorities - tLookup) / 1_000_000;
        log.debug("Login timing: lookup={}ms, authorities={}ms for username={}", lookupMs, authBuildMs, username);
    }

    return new org.springframework.security.core.userdetails.User(
            usuario.getEmail(),
            usuario.getSenha(),
            true,
            true,
            true,
            true,
            authorities
    );
}

}
