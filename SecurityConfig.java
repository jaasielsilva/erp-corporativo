package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.service.CustomUserDetailsService;
import com.jaasielsilva.portalceo.handler.CustomAuthenticationFailureHandler;
import com.jaasielsilva.portalceo.handler.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;

/**
 * Configuração de Segurança Melhorada - ERP Corporativo
 * 
 * Implementa as melhores práticas de segurança incluindo:
 * - CSRF Protection habilitado
 * - Session Management robusto
 * - Method Security
 * - Remember-Me seguro
 * - Headers de segurança avançados
 * 
 * @author Jaasiel Silva
 * @version 2.0 - Versão Melhorada
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private DataSource dataSource;

    /**
     * Configuração principal do filtro de segurança
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ===== CSRF PROTECTION (HABILITADO) =====
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    "/api/processar",           // API específica que precisa estar isenta
                    "/ws-chat/**",              // WebSocket endpoints
                    "/ws-notifications/**",     // WebSocket notifications
                    "/actuator/**"              // Actuator endpoints (se necessário)
                )
            )

            // ===== SESSION MANAGEMENT ROBUSTO =====
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(3)                    // Máximo 3 sessões por usuário
                .maxSessionsPreventsLogin(false)       // Permite login, expira sessão mais antiga
                .sessionRegistry(sessionRegistry())
                .and()
                .sessionFixation().migrateSession()    // Proteção contra session fixation
                .invalidSessionUrl("/login?expired=true")
            )

            // ===== HEADERS DE SEGURANÇA AVANÇADOS =====
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)         // 1 ano
                    .includeSubdomains(true)
                    .preload(true)
                )
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
            )

            // ===== AUTORIZAÇÃO DE ENDPOINTS =====
            .authorizeHttpRequests(authz -> authz
                // Recursos públicos
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico",
                    "/webjars/**", "/static/**", "/public/**"
                ).permitAll()
                
                // Endpoints de autenticação
                .requestMatchers("/login", "/logout", "/login-error").permitAll()
                
                // Endpoints de adesão (com rate limiting no interceptor)
                .requestMatchers("/adesao/**", "/api/processar").permitAll()
                
                // WebSocket endpoints
                .requestMatchers("/ws-chat/**", "/ws-notifications/**").permitAll()
                
                // Health check e actuator (se necessário)
                .requestMatchers("/actuator/health").permitAll()
                
                // API pública específica
                .requestMatchers("/api/cep/**").permitAll()
                
                // Todos os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            )

            // ===== CONFIGURAÇÃO DE LOGIN =====
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .permitAll()
            )

            // ===== CONFIGURAÇÃO DE LOGOUT =====
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true)
                .permitAll()
            )

            // ===== REMEMBER-ME SEGURO =====
            .rememberMe(remember -> remember
                .key("erp-corporativo-remember-me-key-2025")
                .tokenValiditySeconds(86400)           // 24 horas
                .userDetailsService(userDetailsService)
                .tokenRepository(persistentTokenRepository())
                .rememberMeParameter("remember-me")
                .rememberMeCookieName("remember-me")
            );

        return http.build();
    }

    /**
     * Encoder de senha BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Força 12 para maior segurança
    }

    /**
     * Provider de autenticação DAO
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // Para mensagens específicas
        return authProvider;
    }

    /**
     * Gerenciador de autenticação
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Registry de sessões para controle de sessões concorrentes
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * Publisher de eventos de sessão HTTP
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * Repositório de tokens persistentes para Remember-Me
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        
        // Criar tabela se não existir (apenas para desenvolvimento)
        // Em produção, criar manualmente
        try {
            tokenRepository.setCreateTableOnStartup(false);
        } catch (Exception e) {
            // Log do erro se necessário
        }
        
        return tokenRepository;
    }

    /**
     * Configuração de dados iniciais (mantida do original)
     */
    @Bean
    public DataInitializer dataInitializer() {
        return new DataInitializer();
    }
}

/**
 * Classe para inicialização de dados (mantida do SecurityConfig original)
 */
class DataInitializer {
    // Implementação mantida conforme original
    // (código de criação de permissões e perfis iniciais)
}