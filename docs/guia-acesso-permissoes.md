# Guia de Acesso e Permissões do ERP Corporativo

## Visão Geral
O controle de acesso do sistema é baseado em dois conceitos complementares:
- **Nível de Acesso (NivelAcesso)**: define a hierarquia do usuário (ex.: MASTER, ADMIN, GERENCIAL, VISITANTE) e concede capacidades amplas como gerência de usuários e acesso gerencial às áreas.
- **Perfis e Permissões (Perfil/Permissao)**: representam os papéis funcionais e authorities usados pelo Spring Security.

Toda página e controller têm acesso aos atributos globais de sessão injetados por um `@ControllerAdvice` central (`GlobalControllerAdvice`). Esses atributos são usados tanto para exibir/ocultar elementos na interface quanto para proteger endpoints no servidor.

## Atributos Globais Disponíveis
- `usuarioLogado`: entidade completa do usuário autenticado.
- `usuarioLogadoId`: ID do usuário autenticado.
- `usuarioLogadoNome`: nome do usuário autenticado.
- `temUsuarioLogado`: booleano indicando presença de usuário válido.
- `nivelAcesso`: descrição textual do nível de acesso.
- `nivelAcessoEnum`: enum `NivelAcesso` do usuário.
- `isAdmin`, `isMaster`, `isGerencial`: flags hierárquicas derivadas de `Perfil` e `NivelAcesso`.
- Flags de área (acesso): `podeAcessarFinanceiro`, `podeAcessarRH`, `podeAcessarVendas`, `podeAcessarEstoque`, `podeAcessarCompras`, `podeAcessarMarketing`, `podeAcessarTI`, `podeAcessarJuridico`, `podeAcessarProjetos`.
- Flags de área (gerência): `podeGerenciarUsuarios`, `podeGerenciarRH`, `podeGerenciarVendas`, `podeGerenciarProjetos`.

## Quem Pode Fazer o Quê
- **MASTER**
  - Acesso total às áreas; pode gerenciar usuários e projetos; considerado gerencial.
- **ADMIN**
  - Acesso amplo (TI, Jurídico, RH, Compras, Estoque, Vendas, Marketing, Projetos); pode gerenciar usuários; considerado gerencial.
- **GERENCIAL**
  - Acesso gerencial às áreas de negócio (Vendas, Marketing, Compras, Estoque, Jurídico, Projetos); pode gerenciar projetos.
- **Financeiro**
  - Acesso concedido por `podeAcessarFinanceiro` (cargo/nível compatível) e também por `isAdmin/isMaster`.
- **RH**
  - Acesso por `podeAcessarRH`; gerência por `podeGerenciarRH`.
- **Vendas**
  - Acesso por `podeAcessarVendas`; gerência por `podeGerenciarVendas`.
- **Estoque e Compras**
  - Acesso por `podeAcessarEstoque` e `podeAcessarCompras`; usuários dessas áreas e gerenciais têm acesso.
- **Marketing**
  - Acesso por `podeAcessarMarketing`; gerência via `isGerencial/ADMIN/MASTER`.
- **TI**
  - Acesso por `podeAcessarTI`; ADMIN/MASTER também possuem acesso.
- **Jurídico**
  - Acesso por `podeAcessarJuridico`; gerenciais/ADMIN/MASTER.
- **Projetos**
  - Acesso por `podeAcessarProjetos`; gerência por `podeGerenciarProjetos`.

## Fluxo de Autenticação
- Login via Spring Security com `UserDetailsService` e senha criptografada (`BCrypt`).
- Após login, o `SecurityContext` fornece o email do usuário; o `GlobalControllerAdvice` carrega a entidade completa e injeta os atributos acima no `Model`.
- Handlers de sucesso/falha de autenticação realizam os redirecionamentos.

## Proteção no Servidor
- Controllers podem usar `@PreAuthorize("@globalControllerAdvice.podeAcessarFinanceiro()")` (e equivalentes) para blindar endpoints.
- Mesmo quando a interface oculta menus via `th:if`, o backend deve ter `@PreAuthorize` para garantir segurança.

## Uso na Interface (Thymeleaf)
- Menus condicionados por `th:if="${podeAcessar...}"` e `th:if="${isAdmin || isMaster}"`.
- Exibição de nome/foto via `usuarioLogado`/`usuarioLogadoNome`.

## Boas Práticas
- Evitar duplicidade de advices que injetam `usuarioLogado`.
- Nunca depender apenas de gating visual: proteger ações sensíveis com `@PreAuthorize`.
- Manter nomenclatura consistente (`podeAcessar*` para acesso; `podeGerenciar*` para capacidade de gerência).

## Perguntas Frequentes
- "Por que alguns menus aparecem para cargos específicos?" — Há heurísticas por nome de cargo (ex.: contém "financeiro", "juridico" etc.) além dos níveis de acesso.
- "Como conceder acesso a uma nova área?" — Adicionar flags equivalentes no advice e usar `@PreAuthorize` nos controllers; atualizar menus com `th:if`.

## Referências Técnicas
- Segurança: `src/main/java/com/jaasielsilva/portalceo/config/SecurityConfig.java`.
- Advice global: `src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java`.
- Modelo de usuário: `src/main/java/com/jaasielsilva/portalceo/model/Usuario.java` e `NivelAcesso.java`.