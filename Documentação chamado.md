# Documentação da Arquitetura - ERP Corporativo

## Visão Geral

O **ERP Corporativo** é um sistema de gestão empresarial desenvolvido em **Spring Boot 3.5.5** com **Java 17**, utilizando **Thymeleaf** para renderização de templates, **MySQL** como banco de dados e **Bootstrap 5** para interface responsiva.

## Tecnologias Utilizadas

### Backend
- **Spring Boot 3.5.5** - Framework principal
- **Java 17** - Linguagem de programação
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM
- **Spring Boot Actuator** - Monitoramento
- **Spring Boot Validation** - Validações
- **Lombok** - Redução de boilerplate

### Frontend
- **Thymeleaf** - Template engine
- **Bootstrap 5.3.0** - Framework CSS
- **Font Awesome 6.0.0** - Ícones
- **jQuery** - Manipulação DOM e AJAX

### Banco de Dados
- **MySQL** - Banco principal
- **PostgreSQL** - Suporte adicional (driver incluído)

### Bibliotecas Adicionais
- **iTextPDF** - Geração de PDFs
- **ZXing** - Geração de QR Codes
- **Jakarta Validation** - Validações

## Estrutura do Projeto

```
src/main/java/com/jaasielsilva/portalceo/
├── config/
│   ├── DatabaseInitializer.java
│   ├── SecurityConfig.java
│   └── WebSecurityConfig.java
├── controller/
│   ├── agenda/
│   ├── ajuda/
│   ├── categoria/
│   ├── chat/
│   ├── clientes/
│   ├── configuracoes/
│   ├── contrato/
│   ├── dashboard/
│   ├── documentos/
│   ├── email/
│   ├── estoque/
│   ├── favoritos/
│   ├── financeiro/
│   ├── fornecedor/
│   ├── juridico/
│   ├── marketing/
│   ├── metas/
│   ├── meus-pedidos/
│   ├── meus-servicos/
│   ├── perfis/
│   ├── permissoes/
│   ├── produto/
│   ├── recomendados/
│   ├── relatorios/
│   ├── rh/
│   ├── senha/
│   ├── servicos/
│   ├── solicitacoes/
│   ├── suporte/
│   ├── termos/
│   ├── ti/
│   ├── usuarios/
│   ├── vendas/
│   └── SuporteController.java
├── dto/
├── exception/
├── formatter/
├── model/
│   ├── Chamado.java
│   ├── Usuario.java
│   ├── Perfil.java
│   └── [47+ entidades]
├── repository/
│   ├── ChamadoRepository.java
│   ├── UsuarioRepository.java
│   └── [47+ repositórios]
├── security/
│   ├── CustomAuthenticationFailureHandler.java
│   ├── CustomAuthenticationSuccessHandler.java
│   └── UsuarioDetailsService.java
├── service/
│   ├── agenda/
│   ├── rh/
│   ├── ChamadoService.java
│   └── [47+ serviços]
└── PortalCeoApplication.java
```

## Arquitetura de Camadas

### 1. Camada de Apresentação (Controllers)
- **Responsabilidade**: Receber requisições HTTP, processar dados e retornar views
- **Padrão**: MVC com Thymeleaf
- **Localização**: `controller/`

### 2. Camada de Negócio (Services)
- **Responsabilidade**: Lógica de negócio e regras da aplicação
- **Padrão**: Service Layer
- **Localização**: `service/`

### 3. Camada de Persistência (Repositories)
- **Responsabilidade**: Acesso a dados
- **Padrão**: Repository Pattern com Spring Data JPA
- **Localização**: `repository/`

### 4. Camada de Modelo (Entities)
- **Responsabilidade**: Representação das entidades de domínio
- **Padrão**: Domain Model
- **Localização**: `model/`

## Entidades Principais

### Sistema de Usuários e Segurança

#### Usuario
```java
@Entity
public class Usuario {
    @Id @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String matricula;
    
    @Column(unique = true)
    private String email;
    
    private String senha;
    
    @ManyToOne
    private Colaborador colaborador;
    
    @ManyToOne
    private Perfil perfil;
    
    @Enumerated(EnumType.STRING)
    private Genero genero;
    
    @Enumerated(EnumType.STRING)
    private NivelAcesso nivelAcesso;
}
```

#### Perfil
```java
@Entity
public class Perfil {
    @Id @GeneratedValue
    private Long id;
    
    private String nome;
    
    @ManyToMany
    private Set<Permissao> permissoes;
    
    @OneToMany(mappedBy = "perfil")
    private List<Usuario> usuarios;
}
```

### Sistema de Chamados

#### Chamado
```java
@Entity
public class Chamado {
    @Id @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String numero;
    
    @NotBlank
    private String assunto;
    
    @Lob
    private String descricao;
    
    @Enumerated(EnumType.STRING)
    private StatusChamado status;
    
    @Enumerated(EnumType.STRING)
    private PrioridadeChamado prioridade;
    
    private String categoria;
    private String subcategoria;
    
    private String solicitanteNome;
    private String solicitanteEmail;
    private String tecnicoResponsavel;
    
    private LocalDateTime dataAbertura;
    private LocalDateTime dataVencimento;
    private LocalDateTime dataResolucao;
    private LocalDateTime dataFechamento;
    
    private Integer tempoResolucaoMinutos;
    private Integer slaRestante;
    private Integer avaliacao;
}
```

## Configuração de Segurança

### SecurityConfig
- **Autenticação**: Form-based com BCryptPasswordEncoder
- **Autorização**: Baseada em URLs e perfis
- **Handlers**: CustomAuthenticationSuccessHandler e CustomAuthenticationFailureHandler
- **Inicialização**: CommandLineRunner para dados iniciais

### Rotas Públicas
```java
.requestMatchers("/", "/login", "/css/**", "/js/**", "/img/**", "/sounds/**").permitAll()
.requestMatchers("/api/produto/**", "/api/processar", "/api/beneficios/**").permitAll()
.requestMatchers("/rh/colaboradores/adesao/**").permitAll()
```

### Rotas Protegidas
- Todas as demais rotas requerem autenticação
- Controle de acesso baseado em perfis (implementação básica)

## Sistema de Templates

### Estrutura de Templates
```
templates/
├── components/
│   ├── sidebar.html
│   ├── topbar.html
│   └── footer.html
├── suporte/
│   ├── index.html (Dashboard)
│   ├── chamados.html (Lista)
│   └── novo.html (Criação)
├── dashboard/
├── usuarios/
├── clientes/
├── estoque/
├── financeiro/
└── [outros módulos]
```

### Componentes Reutilizáveis
- **Sidebar**: Navegação principal modular
- **Topbar**: Cabeçalho com notificações e perfil
- **Footer**: Rodapé consistente

## Funcionalidades Implementadas

### Sistema de Chamados
- ✅ Criação de chamados
- ✅ Listagem com filtros básicos
- ✅ Dashboard com estatísticas
- ✅ Controle de status (ABERTO, EM_ANDAMENTO, RESOLVIDO, FECHADO)
- ✅ Sistema de prioridades (BAIXA, MEDIA, ALTA, URGENTE)
- ✅ Cálculo de SLA
- ✅ Sistema de avaliação (1-5 estrelas)
- ✅ Interface responsiva

### Gestão de Usuários
- ✅ Autenticação básica
- ✅ Perfis e permissões
- ✅ Cadastro de colaboradores
- ✅ Hierarquia organizacional

### Interface
- ✅ Design responsivo com Bootstrap 5
- ✅ Sidebar modular
- ✅ Notificações visuais
- ✅ Filtros dinâmicos com JavaScript

## Configurações

### Banco de Dados (application.properties)
```properties
spring.profiles.active=dev
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/erp_corporativo
spring.datasource.username=root
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

### E-mail
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Upload de Arquivos
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Pontos de Melhoria Identificados

### 1. Segurança
- ❌ **JWT não implementado** - Apenas autenticação por sessão
- ❌ **Controle de permissões limitado** - Falta granularidade
- ❌ **API endpoints sem proteção adequada**

### 2. Arquitetura
- ❌ **Falta de DTOs** - Exposição direta das entidades
- ❌ **Tratamento de erros global ausente**
- ❌ **Validações inconsistentes**
- ❌ **Serviços com responsabilidades misturadas**

### 3. Funcionalidades
- ❌ **Paginação ausente** - Listas podem crescer indefinidamente
- ❌ **Filtros limitados** - Apenas filtros básicos implementados
- ❌ **Documentação API ausente** - Sem Swagger/OpenAPI
- ❌ **Funcionalidades de chamados incompletas** (comentários, escalação, reabertura)

### 4. Código
- ❌ **Código duplicado** - Padrões repetidos entre controllers
- ❌ **Falta de comentários** - Documentação inline limitada
- ❌ **Testes ausentes** - Sem cobertura de testes

## Próximos Passos

1. **Implementar JWT** para autenticação stateless
2. **Criar DTOs** para todas as operações
3. **Adicionar tratamento global de erros**
4. **Implementar paginação e filtros avançados**
5. **Adicionar documentação Swagger**
6. **Completar funcionalidades do sistema de chamados**
7. **Refatorar código duplicado**
8. **Adicionar testes unitários e de integração**

## Conclusão

O projeto possui uma base sólida com arquitetura MVC bem estruturada, mas necessita de melhorias em segurança, padronização e funcionalidades avançadas para se tornar um sistema enterprise-ready.