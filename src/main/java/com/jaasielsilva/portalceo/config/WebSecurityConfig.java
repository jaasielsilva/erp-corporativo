package com.jaasielsilva.portalceo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de segurança web para registrar interceptors
 * e aplicar validações de segurança globalmente.
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {
    
    @Autowired
    private SecurityInterceptor securityInterceptor;
    
    @Autowired
    private UsuarioLogadoInterceptor usuarioLogadoInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar interceptor de segurança para todas as URLs
        registry.addInterceptor(securityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/fonts/**",
                    "/favicon.ico",
                    "/error",
                    "/actuator/**",
                    "/api/categorias/**",
                    "/ws/**"
                );
        
        // Registrar interceptor de validação de usuário logado para endpoints críticos
        registry.addInterceptor(usuarioLogadoInterceptor)
                .addPathPatterns(
                    "/financeiro/**",
                    "/conta-pagar/**",
                    "/conta-receber/**",
                    "/fluxo-caixa/**",
                    "/vendas/**",
                    "/api/financeiro/**",
                    "/api/conta-pagar/**",
                    "/api/conta-receber/**"
                )
                .excludePathPatterns(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/fonts/**",
                    "/favicon.ico",
                    "/error"
                );
    }
}