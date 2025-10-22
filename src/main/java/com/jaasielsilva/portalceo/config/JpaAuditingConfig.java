package com.jaasielsilva.portalceo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.jaasielsilva.portalceo.model.Usuario;

/**
 * Configuração para habilitar auditoria JPA automática.
 * Esta configuração permite que as entidades sejam automaticamente
 * auditadas com informações de criação e modificação.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaAuditingConfig {


    /**
     * Bean que fornece o auditor atual (usuário logado)
     * para as operações de auditoria automática.
     */
    @Bean
    public AuditorAware<Usuario> auditorProvider() {
        return new AuditorAwareImpl();
    }
}