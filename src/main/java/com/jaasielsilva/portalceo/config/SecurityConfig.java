package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import com.jaasielsilva.portalceo.security.UsuarioDetailsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

@Configuration
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository,
                          PermissaoRepository permissaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                            BCryptPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout.permitAll())
            .authenticationProvider(authenticationProvider);

        return http.build();
    }

    @Bean
    public CommandLineRunner createAdminUser(BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            Permissao roleAdmin = permissaoRepository.findByNome("ROLE_ADMIN")
                .orElseGet(() -> permissaoRepository.save(new Permissao(null, "ROLE_ADMIN", new HashSet<>())));

            Perfil adminPerfil = perfilRepository.findByNome("ADMIN")
                .orElseGet(() -> {
                    Perfil p = new Perfil();
                    p.setNome("ADMIN");
                    p.setPermissoes(new HashSet<>());
                    p.getPermissoes().add(roleAdmin);
                    return perfilRepository.save(p);
                });

            if (usuarioRepository.findByEmail("admin@teste.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail("admin@teste.com");
                admin.setSenha(passwordEncoder.encode("12345"));
                admin.setPerfis(new HashSet<>());
                admin.getPerfis().add(adminPerfil);

                // Carrega imagem da pasta static/img
                try {
                    ClassPathResource image = new ClassPathResource("static/img/jaasiel.jpg");
                    byte[] imageBytes = Files.readAllBytes(image.getFile().toPath());
                    admin.setFotoPerfil(imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    admin.setFotoPerfil(null);
                }

                usuarioRepository.save(admin);
                System.out.println("Usuário administrador criado: admin@teste.com / 123456");
            } else {
                System.out.println("Usuário administrador já existe.");
            }
        };
    }
}
