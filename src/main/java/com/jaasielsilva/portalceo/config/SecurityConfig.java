package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.security.CustomAuthenticationFailureHandler;
import com.jaasielsilva.portalceo.security.UsuarioDetailsService;
import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
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
import java.util.Set;

@Configuration
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final ColaboradorRepository colaboradorRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository,
                          PermissaoRepository permissaoRepository,
                          CargoRepository cargoRepository,
                          DepartamentoRepository departamentoRepository,
                          ColaboradorRepository colaboradorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.colaboradorRepository = colaboradorRepository;
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
                                       DaoAuthenticationProvider authenticationProvider,
                                       CustomAuthenticationFailureHandler failureHandler) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/a81368914c.js").permitAll()
            .anyRequest().authenticated()
        )

        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .defaultSuccessUrl("/dashboard", true)
            .failureHandler(failureHandler)
            .permitAll()
        )
        .logout(logout -> logout.permitAll())
        .authenticationProvider(authenticationProvider);

    return http.build();
}


    @Bean
    public CommandLineRunner createInitialData(BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            // Permissões
            Permissao roleAdmin = permissaoRepository.findByNome("ROLE_ADMIN")
                .orElseGet(() -> permissaoRepository.save(new Permissao(null, "ROLE_ADMIN", new HashSet<>())));
            Permissao roleUser = permissaoRepository.findByNome("ROLE_USER")
                .orElseGet(() -> permissaoRepository.save(new Permissao(null, "ROLE_USER", new HashSet<>())));

            // Perfis
            Perfil adminPerfil = perfilRepository.findByNome("ADMIN")
                .orElseGet(() -> {
                    Perfil p = new Perfil();
                    p.setNome("ADMIN");
                    p.setPermissoes(new HashSet<>());
                    p.getPermissoes().add(roleAdmin);
                    return perfilRepository.save(p);
                });

            perfilRepository.findByNome("USER")
                .orElseGet(() -> {
                    Perfil p = new Perfil();
                    p.setNome("USER");
                    p.setPermissoes(new HashSet<>());
                    p.getPermissoes().add(roleUser);
                    return perfilRepository.save(p);
                });

            // Cargo "Administrador"
            Cargo cargoAdmin = cargoRepository.findByNome("Administrador")
                .orElseGet(() -> {
                    Cargo c = new Cargo();
                    c.setNome("Administrador");
                    return cargoRepository.save(c);
                });

            // Departamento "TI"
            Departamento departamentoTI = departamentoRepository.findByNome("TI")
                .orElseGet(() -> {
                    Departamento d = new Departamento();
                    d.setNome("TI");
                    return departamentoRepository.save(d);
                });

            // Criação do usuário admin
            if (usuarioRepository.findByEmail("admin@teste.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail("admin@teste.com");
                admin.setSenha(passwordEncoder.encode("12345"));
                admin.setPerfis(Set.of(adminPerfil));
                admin.setMatricula("ADM001");
                admin.setCpf("00000000000");
                admin.setTelefone("(11) 99999-9999");
                admin.setDataNascimento(java.time.LocalDate.of(1980, 1, 1));
                admin.setDepartamento(departamentoTI);
                admin.setCargo(cargoAdmin);
                admin.setDataAdmissao(java.time.LocalDate.of(2020, 1, 1));
                admin.setEndereco("Rua Exemplo, 123");
                admin.setCidade("São Paulo");
                admin.setEstado("SP");
                admin.setCep("01000-000");
                admin.setStatus(Usuario.Status.ATIVO);
                admin.setGenero(Genero.MASCULINO);
                admin.setRamal("1010");
                admin.setNivelAcesso(NivelAcesso.ADMIN);

                try {
                    ClassPathResource image = new ClassPathResource("static/img/gerente.png");
                    byte[] imageBytes = Files.readAllBytes(image.getFile().toPath());
                    admin.setFotoPerfil(imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    admin.setFotoPerfil(null);
                }

                usuarioRepository.save(admin);
                System.out.println("Usuário administrador criado: admin@teste.com / 12345");
            } else {
                System.out.println("Usuário administrador já existe.");
            }

            // Criação de colaboradores de exemplo
            if (colaboradorRepository.count() == 0) {
                // Cargo "Desenvolvedor"
                Cargo cargoDev = cargoRepository.findByNome("Desenvolvedor")
                    .orElseGet(() -> {
                        Cargo c = new Cargo();
                        c.setNome("Desenvolvedor");
                        return cargoRepository.save(c);
                    });

                // Departamento "Desenvolvimento"
                Departamento departamentoDev = departamentoRepository.findByNome("Desenvolvimento")
                    .orElseGet(() -> {
                        Departamento d = new Departamento();
                        d.setNome("Desenvolvimento");
                        return departamentoRepository.save(d);
                    });

                // Colaborador 1
                Colaborador colaborador1 = new Colaborador();
                colaborador1.setNome("João Silva");
                colaborador1.setCpf("12345678901");
                colaborador1.setEmail("joao.silva@empresa.com");
                colaborador1.setTelefone("(11) 98765-4321");
                colaborador1.setSexo(Colaborador.Sexo.MASCULINO);
                colaborador1.setDataNascimento(java.time.LocalDate.of(1990, 5, 15));
                colaborador1.setDataAdmissao(java.time.LocalDate.of(2022, 1, 10));
                colaborador1.setEstadoCivil(Colaborador.EstadoCivil.SOLTEIRO);
                colaborador1.setStatus(Colaborador.StatusColaborador.ATIVO);
                colaborador1.setAtivo(true);
                colaborador1.setCargo(cargoDev);
                colaborador1.setDepartamento(departamentoDev);
                colaboradorRepository.save(colaborador1);

                // Colaborador 2
                Colaborador colaborador2 = new Colaborador();
                colaborador2.setNome("Maria Santos");
                colaborador2.setCpf("98765432109");
                colaborador2.setEmail("maria.santos@empresa.com");
                colaborador2.setTelefone("(11) 91234-5678");
                colaborador2.setSexo(Colaborador.Sexo.FEMININO);
                colaborador2.setDataNascimento(java.time.LocalDate.of(1985, 8, 22));
                colaborador2.setDataAdmissao(java.time.LocalDate.of(2021, 6, 5));
                colaborador2.setEstadoCivil(Colaborador.EstadoCivil.CASADO);
                colaborador2.setStatus(Colaborador.StatusColaborador.ATIVO);
                colaborador2.setAtivo(true);
                colaborador2.setCargo(cargoAdmin);
                colaborador2.setDepartamento(departamentoTI);
                colaboradorRepository.save(colaborador2);

                // Colaborador 3
                Colaborador colaborador3 = new Colaborador();
                colaborador3.setNome("Pedro Oliveira");
                colaborador3.setCpf("45678912345");
                colaborador3.setEmail("pedro.oliveira@empresa.com");
                colaborador3.setTelefone("(11) 95555-1234");
                colaborador3.setSexo(Colaborador.Sexo.MASCULINO);
                colaborador3.setDataNascimento(java.time.LocalDate.of(1992, 12, 3));
                colaborador3.setDataAdmissao(java.time.LocalDate.of(2023, 3, 15));
                colaborador3.setEstadoCivil(Colaborador.EstadoCivil.SOLTEIRO);
                colaborador3.setStatus(Colaborador.StatusColaborador.ATIVO);
                colaborador3.setAtivo(true);
                colaborador3.setCargo(cargoDev);
                colaborador3.setDepartamento(departamentoDev);
                colaboradorRepository.save(colaborador3);

                System.out.println("Colaboradores de exemplo criados com sucesso!");
            } else {
                System.out.println("Colaboradores já existem no banco de dados.");
            }
        };
    }
}
