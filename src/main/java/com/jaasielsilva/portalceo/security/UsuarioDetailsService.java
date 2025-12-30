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
import java.util.Set;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final Logger log = LoggerFactory.getLogger(UsuarioDetailsService.class);

    private final com.github.benmanes.caffeine.cache.Cache<String, Set<SimpleGrantedAuthority>> authoritiesCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .maximumSize(10000)
                    .expireAfterWrite(java.time.Duration.ofMinutes(3))
                    .build();

    public void evictAuthorities(String email) {
        if (email != null) {
            authoritiesCache.invalidate(email);
        }
    }

    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    long t0 = System.nanoTime();
    // Tentar buscar por email primeiro (com perfis pré-carregados)
    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailWithPerfis(username);

    // Se não encontrar por email, tentar por matrícula
    if (usuarioOpt.isEmpty()) {
        usuarioOpt = usuarioRepository.findByMatriculaWithPerfis(username);
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

    Set<SimpleGrantedAuthority> authorities = authoritiesCache.getIfPresent(usuario.getEmail());
    if (authorities == null) {
        var built = new java.util.HashSet<SimpleGrantedAuthority>();
        if (usuario.getNivelAcesso() != null && usuario.getNivelAcesso().name().equalsIgnoreCase("MASTER")) {
            built.add(new SimpleGrantedAuthority("ROLE_MASTER"));
            built.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            built.add(new SimpleGrantedAuthority("ROLE_USER"));
            built.add(new SimpleGrantedAuthority("CHAMADO_CRIAR"));
            built.add(new SimpleGrantedAuthority("CHAMADO_VISUALIZAR"));
            built.add(new SimpleGrantedAuthority("CHAMADO_ATRIBUIR"));
            built.add(new SimpleGrantedAuthority("CHAMADO_INICIAR"));
            built.add(new SimpleGrantedAuthority("CHAMADO_RESOLVER"));
            built.add(new SimpleGrantedAuthority("CHAMADO_FECHAR"));
            built.add(new SimpleGrantedAuthority("CHAMADO_REABRIR"));
            built.add(new SimpleGrantedAuthority("CHAMADO_AVALIAR"));
            built.add(new SimpleGrantedAuthority("TECNICO_ATENDER_CHAMADOS"));
            built.add(new SimpleGrantedAuthority("TECNICO_GERENCIAR_PROPRIOS_CHAMADOS"));
            built.add(new SimpleGrantedAuthority("ADMIN_GERENCIAR_USUARIOS"));
            built.add(new SimpleGrantedAuthority("FINANCEIRO_VER_SALDO"));
            built.add(new SimpleGrantedAuthority("FINANCEIRO_VER_EXTRATO"));
            built.add(new SimpleGrantedAuthority("FINANCEIRO_PAGAR"));
            built.add(new SimpleGrantedAuthority("FINANCEIRO_CONFIGURAR"));
        } else {
            built.addAll(
                usuario.getPerfis().stream()
                        .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.getNome()))
                        .collect(Collectors.toSet())
            );

            // Regras dinâmicas de permissão baseadas no Cargo (Auto-Roles)
            // Garante que estagiários e funcionários tenham acesso aos seus módulos
            if (usuario.getCargo() != null) {
                String cargo = usuario.getCargo().getNome().toLowerCase();
                
                // Jurídico: Acesso a processos e documentos
                if (cargo.contains("juridico") || cargo.contains("advogado") || cargo.contains("legal")) {
                    built.add(new SimpleGrantedAuthority("ROLE_JURIDICO"));
                }
                
                // RH: Acesso a dados de colaboradores (básico)
                if (cargo.contains("rh") || cargo.contains("recursos humanos")) {
                    built.add(new SimpleGrantedAuthority("ROLE_RH"));
                }
                
                // Vendas: Acesso a clientes (se necessário explicitamente)
                if (cargo.contains("vendas") || cargo.contains("comercial")) {
                    built.add(new SimpleGrantedAuthority("ROLE_VENDAS"));
                }
            }
        }
        authoritiesCache.put(usuario.getEmail(), built);
        authorities = built;
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
