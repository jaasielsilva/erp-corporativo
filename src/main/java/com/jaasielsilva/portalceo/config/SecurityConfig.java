package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.security.CustomAuthenticationFailureHandler;
import com.jaasielsilva.portalceo.security.CustomAuthenticationSuccessHandler;
import com.jaasielsilva.portalceo.security.UsuarioDetailsService;
import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.CategoriaService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

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
import java.time.LocalDateTime;
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
    private final CargoHierarquiaRepository cargoHierarquiaRepository;
    private final CargoDepartamentoAssociacaoRepository cargoDepartamentoAssociacaoRepository;
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final FornecedorService fornecedorService;
    private final PlanoSaudeRepository planoSaudeRepository;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository,
                          PermissaoRepository permissaoRepository,
                          CargoRepository cargoRepository,
                          DepartamentoRepository departamentoRepository,
                          ColaboradorRepository colaboradorRepository,
                          CargoHierarquiaRepository cargoHierarquiaRepository,
                          CargoDepartamentoAssociacaoRepository cargoDepartamentoAssociacaoRepository,
                          ProdutoService produtoService,
                          CategoriaService categoriaService,
                          FornecedorService fornecedorService,
                          PlanoSaudeRepository planoSaudeRepository,
                          CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) { // ← adicionado
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.cargoHierarquiaRepository = cargoHierarquiaRepository;
        this.cargoDepartamentoAssociacaoRepository = cargoDepartamentoAssociacaoRepository;
        this.produtoService = produtoService;
        this.categoriaService = categoriaService;
        this.fornecedorService = fornecedorService;
        this.planoSaudeRepository = planoSaudeRepository;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler; // ← adicionado
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
                                                .requestMatchers("/login", "/css/**", "/js/**", "/images/**",
                                "/a81368914c.js", "/esqueci-senha", "/resetar-senha")
                                .permitAll()
                                .requestMatchers("/api/produto/**", "/api/processar", "/api/beneficios/**", "/api/rh/**", "/api/chamados/**", "/api/tempo-resolucao", "/suporte/api/avaliacoes-atendimento", "/suporte/api/categorias", "/suporte/api/public/**", "/suporte/api/metricas-sla-periodo", "/suporte/api/metricas-sla-comparativo", "/suporte/api/tempo-resolucao", "/suporte/api/tempo-medio-primeira-resposta").permitAll()
                                                .requestMatchers("/rh/colaboradores/adesao/**").permitAll()
                                                
                                                .requestMatchers("/ws-chat/**", "/ws-notifications/**").permitAll()
                                                .requestMatchers("/api/chat/**").authenticated()
                                                .requestMatchers("/api/notifications/**").authenticated()
                                                .requestMatchers("/error", "/error/**").permitAll()
                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .failureHandler(failureHandler)
                                                .permitAll())
                                .logout(logout -> logout.permitAll())
                                .authenticationProvider(authenticationProvider);

                return http.build();
        }

        /**
         * Método auxiliar para criar departamentos
         */
        private Departamento criarDepartamento(String nome) {
                return departamentoRepository.findByNome(nome)
                                .orElseGet(() -> {
                                        Departamento d = new Departamento();
                                        d.setNome(nome);
                                        return departamentoRepository.save(d);
                                });
        }

        /**
         * Método auxiliar para criar cargos
         */
        private Cargo criarCargo(String nome) {
                return cargoRepository.findByNome(nome)
                                .orElseGet(() -> {
                                        Cargo c = new Cargo();
                                        c.setNome(nome);
                                        c.setAtivo(true);
                                        return cargoRepository.save(c);
                                });
        }

        /**
         * Método auxiliar para criar hierarquia se não existir
         */
        private void criarHierarquiaSeNaoExistir(Cargo cargoSuperior, Cargo cargoSubordinado, Integer nivel,
                        String descricao) {
                boolean existe = cargoHierarquiaRepository
                                .findByCargoSuperiorAndCargoSubordinadoAndAtivoTrue(cargoSuperior, cargoSubordinado)
                                .isPresent();

                if (!existe) {
                        CargoHierarquia hierarquia = new CargoHierarquia();
                        hierarquia.setCargoSuperior(cargoSuperior);
                        hierarquia.setCargoSubordinado(cargoSubordinado);
                        hierarquia.setNivelHierarquico(nivel);
                        hierarquia.setDescricao(descricao);
                        hierarquia.setAtivo(true);
                        cargoHierarquiaRepository.save(hierarquia);
                }
        }

        /**
         * Método auxiliar para criar associação cargo-departamento se não existir
         */
        private void criarAssociacaoSeNaoExistir(Cargo cargo, Departamento departamento, boolean obrigatorio,
                        String observacoes) {
                boolean existe = cargoDepartamentoAssociacaoRepository
                                .findByCargoAndDepartamentoAndAtivoTrue(cargo, departamento).isPresent();

                if (!existe) {
                        CargoDepartamentoAssociacao associacao = new CargoDepartamentoAssociacao();
                        associacao.setCargo(cargo);
                        associacao.setDepartamento(departamento);
                        associacao.setObrigatorio(obrigatorio);
                        associacao.setObservacoes(observacoes);
                        associacao.setAtivo(true);
                        cargoDepartamentoAssociacaoRepository.save(associacao);
                }
        }

        /**
         * MÉTODOS AUXILIARES
         */

        private PlanoSaude criarPlano(String nome, String operadora, PlanoSaude.TipoPlano tipo,
                        BigDecimal valorTitular, BigDecimal valorDependente, String descricao, String observacoes) {
                PlanoSaude plano = new PlanoSaude();
                plano.setNome(nome);
                plano.setOperadora(operadora);
                plano.setTipo(tipo);
                plano.setValorTitular(valorTitular);
                plano.setValorDependente(valorDependente);
                plano.setDescricao(descricao);
                plano.setAtivo(true);
                plano.setPercentualColaborador(BigDecimal.valueOf(20));
                plano.setPercentualEmpresa(BigDecimal.valueOf(80));
                plano.setObservacoes(observacoes);

                // Usar códigos predefinidos baseados no tipo ao invés de aleatórios
                String codigo;
                switch (tipo) {
                    case BASICO:
                        codigo = "basico";
                        break;
                    case INTERMEDIARIO:
                        codigo = "intermediario";
                        break;
                    case PREMIUM:
                        codigo = "premium";
                        break;
                    case EXECUTIVO:
                        codigo = "executivo";
                        break;
                    default:
                        codigo = operadora.substring(0, Math.min(4, operadora.length())).toUpperCase()
                                + String.format("%03d", new Random().nextInt(1000));
                }
                plano.setCodigo(codigo);

                return plano;
        }

        @Bean
        public CommandLineRunner createInitialData(BCryptPasswordEncoder passwordEncoder) {
                return args -> {
                        // Verificação para evitar execução múltipla durante inicialização
                        if (usuarioRepository.count() > 0) {
                                System.out.println("Dados iniciais já existem. Pulando inicialização.");
                                return;
                        }

                        System.out.println("Iniciando criação de dados iniciais...");
                        // Permissões
                        Permissao roleAdmin = permissaoRepository.findByNome("ROLE_ADMIN")
                                        .orElseGet(() -> {
                                                Permissao p = new Permissao();
                                                p.setNome("ROLE_ADMIN");
                                                p.setCategoria("Administração");
                                                return permissaoRepository.save(p);
                                        });
                        Permissao roleUser = permissaoRepository.findByNome("ROLE_USER")
                                        .orElseGet(() -> {
                                                Permissao p = new Permissao();
                                                p.setNome("ROLE_USER");
                                                p.setCategoria("Usuário");
                                                return permissaoRepository.save(p);
                                        });

                        // Perfis
                        Perfil adminPerfil = perfilRepository.findByNome("ADMIN")
                                        .orElseGet(() -> {
                                                Perfil p = new Perfil();
                                                p.setNome("ADMIN");
                                                p.setPermissoes(new HashSet<>());
                                                p.getPermissoes().add(roleAdmin);
                                                return perfilRepository.save(p);
                                        });

                        Perfil userPerfil = perfilRepository.findByNome("USER")
                                        .orElseGet(() -> {
                                                Perfil p = new Perfil();
                                                p.setNome("USER");
                                                p.setPermissoes(new HashSet<>());
                                                p.getPermissoes().add(roleUser);
                                                return perfilRepository.save(p);
                                        });
                        List<PlanoSaude> planos = List.of(
                                        criarPlano("Plano de Saúde - Amil", "Amil", PlanoSaude.TipoPlano.BASICO,
                                                        BigDecimal.valueOf(350), BigDecimal.valueOf(200),
                                                        "Plano básico para colaboradores.",
                                                        "Observações do plano Amil"),
                                        criarPlano("Plano de Saúde - Bradesco", "Bradesco",
                                                        PlanoSaude.TipoPlano.INTERMEDIARIO,
                                                        BigDecimal.valueOf(450), BigDecimal.valueOf(250),
                                                        "Plano intermediário com cobertura ampliada.",
                                                        "Observações do plano Bradesco"),
                                        criarPlano("Plano de Saúde - Omint", "Omint", PlanoSaude.TipoPlano.PREMIUM,
                                                        BigDecimal.valueOf(600), BigDecimal.valueOf(300),
                                                        "Plano premium com cobertura completa.",
                                                        "Observações do plano Omint"));

                        for (PlanoSaude plano : planos) {
                                PlanoSaude existente = planoSaudeRepository.findByNome(plano.getNome()).orElse(null);
                                if (existente == null) {
                                        plano.setDataCriacao(LocalDateTime.now());
                                        plano.setDataUltimaEdicao(LocalDateTime.now());
                                        planoSaudeRepository.save(plano);
                                        System.out.println("Plano criado: " + plano.getNome());
                                } else {
                                        System.out.println("Plano já existe: " + plano.getNome());
                                }
                        }

                        // ===============================
                        // CRIAÇÃO DOS DEPARTAMENTOS
                        // ===============================

                        // Departamentos essenciais
                        Departamento departamentoTI = criarDepartamento("TI");
                        Departamento departamentoRH = criarDepartamento("Recursos Humanos");
                        Departamento departamentoFinanceiro = criarDepartamento("Financeiro");
                        Departamento departamentoVendas = criarDepartamento("Vendas");
                        Departamento departamentoMarketing = criarDepartamento("Marketing");
                        Departamento departamentoOperacoes = criarDepartamento("Operações");
                        Departamento departamentoJuridico = criarDepartamento("Jurídico");
                        Departamento departamentoContabilidade = criarDepartamento("Contabilidade");
                        Departamento departamentoAdministrativo = criarDepartamento("Administrativo");
                        Departamento departamentoCompras = criarDepartamento("Compras");
                        Departamento departamentoQualidade = criarDepartamento("Qualidade");
                        Departamento departamentoLogistica = criarDepartamento("Logística");

                        // ===============================
                        // CRIAÇÃO DOS CARGOS
                        // ===============================

                        // Nível Executivo
                        Cargo cargoDirectorGeral = criarCargo("Diretor Geral");
                        Cargo cargoDirectorFinanceiro = criarCargo("Diretor Financeiro");
                        Cargo cargoDirectorTecnologia = criarCargo("Diretor de Tecnologia");
                        Cargo cargoDirectorRH = criarCargo("Diretor de Recursos Humanos");
                        Cargo cargoDirectorComercial = criarCargo("Diretor Comercial");
                        Cargo cargoDirectorOperacoes = criarCargo("Diretor de Operações");

                        // Nível Gerencial
                        Cargo cargoGerenteTI = criarCargo("Gerente de TI");
                        Cargo cargoGerenteRH = criarCargo("Gerente de RH");
                        Cargo cargoGerenteFinanceiro = criarCargo("Gerente Financeiro");
                        Cargo cargoGerenteVendas = criarCargo("Gerente de Vendas");
                        Cargo cargoGerenteMarketing = criarCargo("Gerente de Marketing");
                        Cargo cargoGerenteOperacoes = criarCargo("Gerente de Operações");
                        Cargo cargoGerenteCompras = criarCargo("Gerente de Compras");
                        Cargo cargoGerenteQualidade = criarCargo("Gerente de Qualidade");

                        // Nível de Coordenação
                        Cargo cargoCoordenadorProjetos = criarCargo("Coordenador de Projetos");
                        Cargo cargoCoordenadorVendas = criarCargo("Coordenador de Vendas");
                        Cargo cargoCoordenadorMarketing = criarCargo("Coordenador de Marketing");
                        Cargo cargoCoordenadorRH = criarCargo("Coordenador de RH");
                        Cargo cargoCoordenadorFinanceiro = criarCargo("Coordenador Financeiro");
                        Cargo cargoCoordenadorProducao = criarCargo("Coordenador de Produção");

                        // Nível Técnico/Especialista
                        Cargo cargoAnalistaSistemas = criarCargo("Analista de Sistemas");
                        Cargo cargoDesenvolvedorSenior = criarCargo("Desenvolvedor Senior");
                        Cargo cargoDesenvolvedorPleno = criarCargo("Desenvolvedor Pleno");
                        Cargo cargoDesenvolvedorJunior = criarCargo("Desenvolvedor Junior");
                        Cargo cargoAnalistaFinanceiro = criarCargo("Analista Financeiro");
                        Cargo cargoAnalistaRH = criarCargo("Analista de RH");
                        Cargo cargoAnalistaMarketing = criarCargo("Analista de Marketing");
                        Cargo cargoAnalistaVendas = criarCargo("Analista de Vendas");
                        Cargo cargoAnalistaQualidade = criarCargo("Analista de Qualidade");
                        Cargo cargoContador = criarCargo("Contador");
                        Cargo cargoAdvogado = criarCargo("Advogado");
                        Cargo cargoDesigner = criarCargo("Designer");
                        Cargo cargoEspecialistaSEO = criarCargo("Especialista em SEO");

                        // Nível Operacional
                        Cargo cargoAssistenteAdministrativo = criarCargo("Assistente Administrativo");
                        Cargo cargoAssistenteFinanceiro = criarCargo("Assistente Financeiro");
                        Cargo cargoAssistenteRH = criarCargo("Assistente de RH");
                        Cargo cargoAssistenteMarketing = criarCargo("Assistente de Marketing");
                        Cargo cargoAuxiliarProducao = criarCargo("Auxiliar de Produção");
                        Cargo cargoRecepcionista = criarCargo("Recepcionista");
                        Cargo cargoOperadorMaquinas = criarCargo("Operador de Máquinas");
                        Cargo cargoTecnicoSuporte = criarCargo("Técnico de Suporte");
                        Cargo cargoVendedor = criarCargo("Vendedor");
                        Cargo cargoRepresentanteComercial = criarCargo("Representante Comercial");

                        // Nível de Apoio
                        Cargo cargoEstagiario = criarCargo("Estagiário");
                        Cargo cargoTrainee = criarCargo("Trainee");
                        Cargo cargoTerceirizado = criarCargo("Terceirizado");
                        Cargo cargoConsultor = criarCargo("Consultor");
                        Cargo cargoFreelancer = criarCargo("Freelancer");

                        // Cargo Administrador (mantido para compatibilidade)
                        Cargo cargoAdmin = criarCargo("Administrador");

                        // Criar usuário MASTER se não existir
                        if (usuarioRepository.findByEmail("master@sistema.com").isEmpty()) {
                                Usuario master = new Usuario();
                                master.setMatricula("MST001");
                                master.setNome("Master do Sistema");
                                master.setEmail("master@sistema.com");
                                master.setSenha(passwordEncoder.encode("master123"));
                                master.setCpf("11111111111");
                                master.setTelefone("(11) 99999-0000");
                                master.setDataNascimento(java.time.LocalDate.of(1975, 1, 1));
                                master.setGenero(Genero.MASCULINO);
                                master.setNivelAcesso(NivelAcesso.MASTER);
                                master.setStatus(Usuario.Status.ATIVO);
                                master.setDepartamento(departamentoTI);
                                master.setCargo(cargoAdmin);
                                master.setDataAdmissao(java.time.LocalDate.of(2020, 1, 1));
                                master.setEndereco("Rua Master, 1");
                                master.setCidade("São Paulo");
                                master.setEstado("SP");
                                master.setCep("01000-001");
                                master.setRamal("1000");
                                master.setPerfis(Set.of(adminPerfil));

                                try {
                                        ClassPathResource image = new ClassPathResource("static/img/gerente.png");
                                        byte[] imageBytes = Files.readAllBytes(image.getFile().toPath());
                                        master.setFotoPerfil(imageBytes);
                                } catch (IOException e) {
                                        e.printStackTrace();
                                        master.setFotoPerfil(null);
                                }

                                usuarioRepository.save(master);
                                System.out.println("Usuário master criado: master@sistema.com / master123");
                        }

                        // Criar usuário gerente de RH se não existir
                        if (usuarioRepository.findByEmail("rh@empresa.com").isEmpty()) {
                                Usuario gerenteRH = new Usuario();
                                gerenteRH.setMatricula("RH001");
                                gerenteRH.setNome("Gerente de RH");
                                gerenteRH.setEmail("rh@empresa.com");
                                gerenteRH.setSenha(passwordEncoder.encode("rh123"));
                                gerenteRH.setCpf("22222222222");
                                gerenteRH.setTelefone("(11) 88888-8888");
                                gerenteRH.setDataNascimento(java.time.LocalDate.of(1985, 5, 15));
                                gerenteRH.setGenero(Genero.FEMININO);
                                gerenteRH.setNivelAcesso(NivelAcesso.GERENTE);
                                gerenteRH.setStatus(Usuario.Status.ATIVO);
                                gerenteRH.setDepartamento(departamentoRH);
                                gerenteRH.setCargo(cargoGerenteRH);
                                gerenteRH.setDataAdmissao(java.time.LocalDate.of(2021, 3, 1));
                                gerenteRH.setEndereco("Rua RH, 456");
                                gerenteRH.setCidade("São Paulo");
                                gerenteRH.setEstado("SP");
                                gerenteRH.setCep("01000-002");
                                gerenteRH.setRamal("2020");
                                gerenteRH.setPerfis(Set.of(adminPerfil));
                                usuarioRepository.save(gerenteRH);
                                System.out.println("Usuário gerente RH criado: rh@empresa.com / rh123");
                        }

                        // Criar usuário coordenador se não existir
                        if (usuarioRepository.findByEmail("coordenador@empresa.com").isEmpty()) {
                                Usuario coordenador = new Usuario();
                                coordenador.setMatricula("COORD001");
                                coordenador.setNome("Coordenador de Vendas");
                                coordenador.setEmail("coordenador@empresa.com");
                                coordenador.setSenha(passwordEncoder.encode("coord123"));
                                coordenador.setCpf("33333333333");
                                coordenador.setTelefone("(11) 77777-7777");
                                coordenador.setDataNascimento(java.time.LocalDate.of(1990, 8, 20));
                                coordenador.setGenero(Genero.MASCULINO);
                                coordenador.setNivelAcesso(NivelAcesso.COORDENADOR);
                                coordenador.setStatus(Usuario.Status.ATIVO);
                                coordenador.setDepartamento(departamentoVendas);
                                coordenador.setCargo(cargoCoordenadorVendas);
                                coordenador.setDataAdmissao(java.time.LocalDate.of(2022, 1, 15));
                                coordenador.setEndereco("Rua Vendas, 789");
                                coordenador.setCidade("São Paulo");
                                coordenador.setEstado("SP");
                                coordenador.setCep("01000-003");
                                coordenador.setRamal("3030");
                                coordenador.setPerfis(Set.of(userPerfil));
                                usuarioRepository.save(coordenador);
                                System.out.println("Usuário coordenador criado: coordenador@empresa.com / coord123");
                        }

                        // Criar usuário operacional se não existir
                        if (usuarioRepository.findByEmail("operacional@empresa.com").isEmpty()) {
                                Usuario operacional = new Usuario();
                                operacional.setMatricula("OP001");
                                operacional.setNome("Operador de Sistema");
                                operacional.setEmail("operacional@empresa.com");
                                operacional.setSenha(passwordEncoder.encode("op123"));
                                operacional.setCpf("44444444444");
                                operacional.setTelefone("(11) 66666-6666");
                                operacional.setDataNascimento(java.time.LocalDate.of(1995, 12, 10));
                                operacional.setGenero(Genero.FEMININO);
                                operacional.setNivelAcesso(NivelAcesso.OPERACIONAL);
                                operacional.setStatus(Usuario.Status.ATIVO);
                                operacional.setDepartamento(departamentoOperacoes);
                                operacional.setCargo(cargoAssistenteAdministrativo);
                                operacional.setDataAdmissao(java.time.LocalDate.of(2023, 6, 1));
                                operacional.setEndereco("Rua Operações, 321");
                                operacional.setCidade("São Paulo");
                                operacional.setEstado("SP");
                                operacional.setCep("01000-004");
                                operacional.setRamal("4040");
                                operacional.setPerfis(Set.of(userPerfil));
                                usuarioRepository.save(operacional);
                                System.out.println("Usuário operacional criado: operacional@empresa.com / op123");
                        }

                        // ===============================
                        // CRIAÇÃO DE HIERARQUIAS DE EXEMPLO
                        // ===============================

                        // Hierarquia Executiva -> Gerencial
                        criarHierarquiaSeNaoExistir(cargoDirectorGeral, cargoGerenteTI, 1,
                                        "Diretor Geral supervisiona Gerente de TI");
                        criarHierarquiaSeNaoExistir(cargoDirectorGeral, cargoGerenteRH, 1,
                                        "Diretor Geral supervisiona Gerente de RH");
                        criarHierarquiaSeNaoExistir(cargoDirectorFinanceiro, cargoGerenteFinanceiro, 1,
                                        "Diretor Financeiro supervisiona Gerente Financeiro");
                        criarHierarquiaSeNaoExistir(cargoDirectorComercial, cargoGerenteVendas, 1,
                                        "Diretor Comercial supervisiona Gerente de Vendas");

                        // Hierarquia Gerencial -> Coordenação
                        criarHierarquiaSeNaoExistir(cargoGerenteTI, cargoCoordenadorProjetos, 2,
                                        "Gerente de TI supervisiona Coordenador de Projetos");
                        criarHierarquiaSeNaoExistir(cargoGerenteRH, cargoCoordenadorRH, 2,
                                        "Gerente de RH supervisiona Coordenador de RH");
                        criarHierarquiaSeNaoExistir(cargoGerenteVendas, cargoCoordenadorVendas, 2,
                                        "Gerente de Vendas supervisiona Coordenador de Vendas");

                        // Hierarquia Coordenação -> Técnico/Especialista
                        criarHierarquiaSeNaoExistir(cargoCoordenadorProjetos, cargoAnalistaSistemas, 3,
                                        "Coordenador de Projetos supervisiona Analista de Sistemas");
                        criarHierarquiaSeNaoExistir(cargoAnalistaSistemas, cargoDesenvolvedorSenior, 4,
                                        "Analista de Sistemas supervisiona Desenvolvedor Senior");
                        criarHierarquiaSeNaoExistir(cargoDesenvolvedorSenior, cargoDesenvolvedorPleno, 5,
                                        "Desenvolvedor Senior supervisiona Desenvolvedor Pleno");
                        criarHierarquiaSeNaoExistir(cargoDesenvolvedorPleno, cargoDesenvolvedorJunior, 6,
                                        "Desenvolvedor Pleno supervisiona Desenvolvedor Junior");

                        // ===============================
                        // CRIAÇÃO DE ASSOCIAÇÕES CARGO-DEPARTAMENTO DE EXEMPLO
                        // ===============================

                        // Associações obrigatórias (cargos específicos de departamento)
                        criarAssociacaoSeNaoExistir(cargoGerenteTI, departamentoTI, true,
                                        "Gerente de TI só pode atuar no departamento de TI");
                        criarAssociacaoSeNaoExistir(cargoAnalistaSistemas, departamentoTI, true,
                                        "Analista de Sistemas específico para TI");
                        criarAssociacaoSeNaoExistir(cargoDesenvolvedorSenior, departamentoTI, false,
                                        "Desenvolvedor Senior pode atuar em TI");
                        criarAssociacaoSeNaoExistir(cargoDesenvolvedorPleno, departamentoTI, false,
                                        "Desenvolvedor Pleno pode atuar em TI");
                        criarAssociacaoSeNaoExistir(cargoDesenvolvedorJunior, departamentoTI, false,
                                        "Desenvolvedor Junior pode atuar em TI");
                        criarAssociacaoSeNaoExistir(cargoTecnicoSuporte, departamentoTI, false,
                                        "Técnico de Suporte pode atuar em TI");

                        // Associações de RH
                        criarAssociacaoSeNaoExistir(cargoGerenteRH, departamentoRH, true,
                                        "Gerente de RH específico para RH");
                        criarAssociacaoSeNaoExistir(cargoAnalistaRH, departamentoRH, true,
                                        "Analista de RH específico para RH");
                        criarAssociacaoSeNaoExistir(cargoAssistenteRH, departamentoRH, false,
                                        "Assistente de RH pode atuar em RH");

                        // Associações flexíveis (cargos que podem atuar em múltiplos departamentos)
                        criarAssociacaoSeNaoExistir(cargoAssistenteAdministrativo, departamentoAdministrativo, false,
                                        "Assistente Administrativo no departamento administrativo");
                        criarAssociacaoSeNaoExistir(cargoAssistenteAdministrativo, departamentoRH, false,
                                        "Assistente Administrativo pode apoiar RH");
                        criarAssociacaoSeNaoExistir(cargoAssistenteAdministrativo, departamentoFinanceiro, false,
                                        "Assistente Administrativo pode apoiar Financeiro");

                        System.out.println("Hierarquias e associações de exemplo criadas com sucesso!");
                        System.out.println("=== INICIALIZAÇÃO DE DADOS CONCLUÍDA COM SUCESSO ===");
                };
        }
}
