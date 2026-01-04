# Guia: Criação de Usuários por Departamento com Perfis e Permissões

Este guia ensina, passo a passo, como criar usuários para diferentes departamentos, atribuir perfis e permissões, e validar o acesso no sistema. Cobre tanto o fluxo pela interface quanto o fluxo programático via serviços.

## Conceitos Básicos
- Nível de Acesso (MASTER, ADMIN, GERENCIAL, etc.) controla capacidades amplas e visibilidade de áreas. Veja [NivelAcesso.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/model/NivelAcesso.java).
- Perfis agrupam permissões operacionais; os controllers fazem checagens por roles ou nomes de permissões. Construção de authorities no login: [UsuarioDetailsService.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/security/UsuarioDetailsService.java).
- Booleans de acesso por área (podeAcessarRH, podeAcessarFinanceiro, etc.) determinam visibilidade de menus para usuários não-MASTER: [GlobalControllerAdvice.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java#L306-L343).
- Perfis e permissões são manipulados por serviços: [PerfilService.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/PerfilService.java) e [PermissaoService.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/PermissaoService.java).

## Dashboards e Indicadores (Regra Obrigatória)
- ROLE_USER não libera acesso a dashboards executivos.
- Dashboards executivos, indicadores financeiros, RH e KPIs estratégicos não devem ser protegidos apenas por ROLE_USER ou por `isAuthenticated()`. Essas telas exigem permissões específicas.
- Dashboards NÃO devem ser controlados por nível de acesso nem por ROLE_USER.
- Permissões recomendadas:
  - DASHBOARD_EXECUTIVO_VISUALIZAR
  - DASHBOARD_OPERACIONAL_VISUALIZAR
  - DASHBOARD_FINANCEIRO_VISUALIZAR
- Como implementar:
  - Criar as permissões com o serviço de permissões: [PermissaoService.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/PermissaoService.java).
  - Associar as permissões a perfis adequados por departamento usando [PerfilService.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/PerfilService.java).
  - Proteger controladores e rotas de dashboards com checagem explícita de authority (ex.: `@PreAuthorize("hasAuthority('DASHBOARD_EXECUTIVO_VISUALIZAR')")`), evitando regras amplas como `isAuthenticated()` ou `hasRole('ROLE_USER')`.

## Passo a Passo pela Interface
1. Abrir Administração > Usuários no sidebar.
2. Clicar em “Cadastro” para abrir o formulário: [UsuarioController.java:cadastrar](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/controller/UsuarioController.java#L111-L148).
3. Preencher campos obrigatórios: nome, email, CPF, senha e confirmação.
4. Selecionar Cargo e Departamento compatíveis (RH, Financeiro, TI, Marketing, Jurídico, etc.).
5. Selecionar o Perfil desejado (ex.: FINANCEIRO, RH_GERENTE, JURIDICO_GERENTE). Se o perfil ainda não existir:
   - Ir em Administração > Gestão de Acesso > Perfis.
   - Criar novo perfil e selecionar permissões necessárias: [PerfilController.java:novo/salvar](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/controller/PerfilController.java#L59-L74), [PerfilController.java:salvar](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/controller/PerfilController.java#L132-L165).
6. Salvar o usuário. O sistema associará o perfil e aplicará as regras de acesso automaticamente.

## Passo a Passo Programático (Desenvolvedores)
Use este fluxo para criar perfis, permissões e usuários diretamente pelo backend (ex.: carga inicial ou integrações).

1. Garantir permissões necessárias (se não existirem ainda):
   - Criar permissões com `PermissaoService.salvar`, padronizando o nome (ex.: FINANCEIRO_VER_SALDO, ROLE_RH, ROLE_RH_GERENTE).
   - Referência: [PermissaoService.java:salvar](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/PermissaoService.java#L42-L68).
2. Criar/garantir perfil do departamento com conjunto de permissões:
   - Usar `PerfilService.criarPerfilComPermissoesPadrao(nome, Set<String> nomesPermissoes)` para montar o perfil com as permissões persistidas.
   - Referência: [PerfilService.java:criarPerfilComPermissoesPadrao](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/PerfilService.java#L287-L311).
3. Criar usuário e associar departamento e perfil:
   - Preencher dados do usuário (nome, email, cpf, senha, status, nivelAcesso).
   - Definir o departamento via `DepartamentoRepository.findByNome("Financeiro")` (ou RH, TI, etc.).
   - Associar o perfil criado com `usuario.setPerfis(Set.of(perfil))`.
   - Persistir via `UsuarioService.salvarUsuario`.
   - Referência de criação de usuário programático: [UsuarioService.java:criarUsuarioEstagiarioJuridico](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/UsuarioService.java#L286-L316).

### Exemplo: Usuário do Financeiro (Analista)
Objetivo: usuário com acesso a operações financeiras básicas (ver saldo, extrato).
1. Criar permissões caso não existam:
   - FINANCEIRO_VER_SALDO
   - FINANCEIRO_VER_EXTRATO
   - As permissões financeiras base são demonstradas em [SecurityConfig.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L334-L364).
2. Criar perfil “FINANCEIRO_ANALISTA”:
   - `criarPerfilComPermissoesPadrao("FINANCEIRO_ANALISTA", Set.of("FINANCEIRO_VER_SALDO","FINANCEIRO_VER_EXTRATO"))`.
3. Criar o usuário:
   - Definir `nivelAcesso` apropriado (ex.: GERENCIAL para visibilidade ampla, ou USER).
   - Associar `departamento = "Financeiro"` e `perfil = FINANCEIRO_ANALISTA`.
4. Validar:
   - Usuário consegue acessar módulos/relatórios financeiros.
   - Sidebar visível por `podeAcessarFinanceiro`: [GlobalControllerAdvice.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java#L72-L81).

### Exemplo: Usuário de RH (Gestor)
Objetivo: acesso a relatórios e configurações do RH.
1. Garantir roles usadas nos controllers de RH:
   - Controllers exigem `ROLE_RH` e `ROLE_RH_GERENTE` conforme a rota: [RhRelatoriosController.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/controller/rh/RhRelatoriosController.java), [RhConfiguracoesController.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/controller/rh/RhConfiguracoesController.java).
2. Criar perfil “RH_GERENTE” com permissões:
   - `Set.of("ROLE_RH","ROLE_RH_GERENTE")`.
3. Criar o usuário:
   - Departamento “Recursos Humanos”.
   - Nível de acesso GERENCIAL ou ADMIN conforme políticas internas.
4. Validar:
   - Acesso a páginas protegidas por `@PreAuthorize` de RH.
   - Sidebar visível por `podeAcessarRH`: [GlobalControllerAdvice.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java#L312-L321).

### Exemplo: Usuário de TI
Objetivo: acesso às áreas de TI (Sistemas, Suporte, Segurança).
1. Criar perfil “TI_ANALISTA” com permissões específicas (se houver) e role `ROLE_TI` para endpoints.
2. Criar usuário com departamento “TI” e perfil “TI_ANALISTA”.
3. Validar visibilidade por `podeAcessarTI`: [GlobalControllerAdvice.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java#L339-L343).

### Exemplo: Usuário do Jurídico
Objetivo: acesso ao módulo Jurídico, processos e documentos.
1. Garantir permissões/roles dos endpoints (ex.: `ROLE_JURIDICO` para controladores de previdenciário/documentos):
   - [ProcessoPrevidenciarioController.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/processo/controller/ProcessoPrevidenciarioController.java#L40-L51)
   - [DocumentoProcessoController.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/juridico/previdenciario/documentos/controller/DocumentoProcessoController.java#L26-L34)
2. Criar perfil “JURIDICO_ESTAGIARIO” com permissões limitadas (listar processos, visualizar documentos).
3. Criar usuário com departamento “Jurídico” e perfil “JURIDICO_ESTAGIARIO”.
4. Validar:
   - Acesso às páginas e ações protegidas.
    - Sidebar visível por `podeAcessarJuridico`: [GlobalControllerAdvice.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java#L340-L343).

## Perfis Padrão por Departamento (já no banco)
- Administração/Sistema:
  - ADMIN: inclui `ROLE_ADMIN`. Referência: [SecurityConfig.java:L372-L381](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L372-L381)
  - ADMINISTRADOR: `ROLE_ADMIN`, `TECNICO_ATENDER_CHAMADOS`, `CHAMADO_INICIAR`, `CHAMADO_ATRIBUIR`. Referência: [SecurityConfig.java:L441-L452](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L441-L452)
- Financeiro:
  - FINANCEIRO: `FINANCEIRO_VER_SALDO`, `FINANCEIRO_VER_EXTRATO`, `FINANCEIRO_PAGAR`. Referência: [SecurityConfig.java:L384-L394](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L384-L394)
  - DIRETOR_FINANCEIRO (CFO): `FINANCEIRO_VER_SALDO`, `FINANCEIRO_VER_EXTRATO` (sem pagar/configurar). Referência: [SecurityConfig.java:L407-L418](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L407-L418)
- Jurídico:
  - JURIDICO_GERENTE: `ROLE_USER`, `ROLE_JURIDICO_GERENTE`. Referência: [SecurityConfig.java:L396-L405](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L396-L405)
  - JURIDICO_ESTAGIARIO: authorities `MENU_JURIDICO*` (dashboard, processos, previdenciário, documentos). Criado sob demanda: [PerfilService.ensurePerfilJuridicoEstagiario](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/service/PerfilService.java#L324-L344)
- Diretoria:
  - CEO: `ROLE_USER` e `FINANCEIRO_VER_SALDO` (visão macro). Referência: [SecurityConfig.java:L420-L430](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L420-L430)
- Operacional/Suporte:
  - TECNICO: `ROLE_USER`, `TECNICO_ATENDER_CHAMADOS`, `CHAMADO_INICIAR`. Referência: [SecurityConfig.java:L454-L463](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L454-L463)
  - SUPERVISOR: `ROLE_USER`, `TECNICO_ATENDER_CHAMADOS`, `CHAMADO_INICIAR`, `CHAMADO_ATRIBUIR`. Referência: [SecurityConfig.java:L465-L475](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L465-L475)
- Usuário comum:
  - USER: `ROLE_USER`. Referência: [SecurityConfig.java:L432-L439](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L432-L439)
  - USUARIO: `ROLE_USER`. Referência: [SecurityConfig.java:L477-L484](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java#L477-L484)

### Seleção no Cadastro
- No formulário de cadastro, selecione qualquer um destes perfis no campo “Perfil” e salve: [UsuarioController.java:cadastrar](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/controller/UsuarioController.java#L111-L148).
- Se algum perfil não aparecer na lista:
  - Verifique se foi criado na inicialização (SecurityConfig) ou se é criado sob demanda (ex.: `JURIDICO_ESTAGIARIO` via PerfilService).
  - Caso ausente, crie com `PerfilService.criarPerfilComPermissoesPadrao`.

## Boas Práticas de Segurança
- Use nomes de permissões e roles consistentes com os controllers. Se a rota usa `@PreAuthorize("hasAnyRole('ROLE_RH', ...)")`, inclua essas strings no perfil do usuário.
- Não dependa apenas da visibilidade da UI; os controllers já validam as authorities no backend.
- Criptografe senhas (BCrypt) e aplique política de senha: [application-security.properties](file:///c:/Users/jasie/erp-corporativo/src/main/resources/application-security.properties).
- Para usuários MASTER, todas as authorities MENU_* são carregadas automaticamente: [UsuarioDetailsService.java](file:///c:/Users/jasie/erp-corporativo/src/main/java/com/jaasielsilva/portalceo/security/UsuarioDetailsService.java#L68-L240).

## Checklist de Validação
- [ ] Perfil existe e contém todas as permissões/roles necessárias.
- [ ] Usuário possui o perfil correto e está no departamento certo.
- [ ] Acesso ao módulo visível no sidebar via booleans de área.
- [ ] Endpoints de ações abrem sem 403 (verificar `@PreAuthorize`).
- [ ] Logs/Auditoria registram acessos conforme esperado.

## Troubleshooting
- Erro 403 em páginas de módulo:
  - Verifique se o controller exige roles específicas e se o perfil do usuário possui essas roles.
  - Confirme `nivelAcesso` adequado e os booleans de acesso do advice.
- Menu não aparece no sidebar:
  - Cheque `GlobalControllerAdvice` para a área e se o cargo/departamento/nível do usuário atende às condições.
  - Para MASTER, valide se as authorities MENU_* estão presentes no login.

