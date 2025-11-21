# Documentação de Acesso ao Sidebar por Perfis e Permissões

## Objetivo
- Explicar quais opções do menu lateral (sidebar) estão disponíveis para cada perfil de usuário, considerando permissões e nível de acesso.
- Apontar as variáveis de controle usadas na renderização e os pontos do backend que as calculam.
- Incluir exemplos objetivos e uma tabela relacionando cada opção aos perfis/permissões.

## Como o Acesso é Controlado
- Visibilidade no frontend via variáveis Thymeleaf calculadas no backend.
- Variáveis e regras no backend: `src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java:124-262`.
- Estrutura do menu: `src/main/resources/templates/components/sidebar.html`.

### Variáveis Principais (Backend)
- Usuário logado: `src/main/java/com/jaasielsilva/portalceo/config/GlobalControllerAdvice.java:24-47`.
- Administração e níveis:
  - `isAdmin`: `GlobalControllerAdvice.java:49-57`
  - `isMaster`: `GlobalControllerAdvice.java:59-63`
  - `isGerencial`: `GlobalControllerAdvice.java:65-69`
  - `podeGerenciarUsuarios`: `GlobalControllerAdvice.java:71-75`
  - `nivelAcesso`, `nivelAcessoEnum`: `GlobalControllerAdvice.java:95-105`
- Por área/cargo:
  - `isRH`: `GlobalControllerAdvice.java:126-135`
  - `isFinanceiro`: `GlobalControllerAdvice.java:137-146`
  - `isVendas`: `GlobalControllerAdvice.java:148-156`
  - `isEstoque`: `GlobalControllerAdvice.java:159-167`
  - `isCompras`: `GlobalControllerAdvice.java:170-179`
  - `isMarketing`: `GlobalControllerAdvice.java:181-190`
  - `isTI`: `GlobalControllerAdvice.java:192-201`
  - `isJuridico`: `GlobalControllerAdvice.java:204-213`
- Acesso por módulo:
  - `podeAcessarRH`: `GlobalControllerAdvice.java:217-221`
  - `podeAcessarVendas`: `GlobalControllerAdvice.java:222-225`
  - `podeAcessarEstoque`: `GlobalControllerAdvice.java:227-230`
  - `podeAcessarCompras`: `GlobalControllerAdvice.java:232-235`
  - `podeAcessarFinanceiro`: `GlobalControllerAdvice.java:77-81`
  - `podeAcessarMarketing`: `GlobalControllerAdvice.java:237-240`
  - `podeAcessarTI`: `GlobalControllerAdvice.java:242-245`
  - `podeAcessarJuridico`: `GlobalControllerAdvice.java:247-250`
  - `podeAcessarProjetos`: `GlobalControllerAdvice.java:252-255`
  - `podeGerenciarProjetos`: `GlobalControllerAdvice.java:257-260`

### Regras por Nível (`NivelAcesso`)
- Definições: `src/main/java/com/jaasielsilva/portalceo/model/NivelAcesso.java:3-110`.
- `ehGerencial`: MASTER, ADMIN, GERENTE, COORDENADOR, SUPERVISOR (`NivelAcesso.java:67-69`).
- `podeGerenciarUsuarios`: MASTER, ADMIN, GERENTE (`NivelAcesso.java:74-76`).
- `podeAcessarFinanceiro`: MASTER, ADMIN, GERENTE, COORDENADOR (`NivelAcesso.java:81-83`).
- `podeGerenciarRH`: MASTER, ADMIN, GERENTE (`NivelAcesso.java:88-90`).
- `podeGerenciarVendas`: MASTER, ADMIN, GERENTE (`NivelAcesso.java:95-97`).

### Roles e Permissões (Spring Security)
- Roles atribuídas no login: `ROLE_MASTER`, `ROLE_ADMIN` (`src/main/java/com/jaasielsilva/portalceo/security/UsuarioDetailsService.java:44-54`).
- Exemplo de autorização por endpoint: `@PreAuthorize` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:203-220` e `src/main/java/com/jaasielsilva/portalceo/controller/TermosController.java:177-185`.
- Permissões operacionais (Suporte/Chamados): `src/main/java/com/jaasielsilva/portalceo/security/Permissao.java:7-46` e perfis operacionais em `src/main/java/com/jaasielsilva/portalceo/security/PerfilUsuario.java:12-96`.

## Matriz de Acesso do Sidebar
| Opção | Perfis que veem/acessam | Condição/Permissão |
|---|---|---|
| Dashboard (`sidebar.html:25`) | Todos | Livre |
| Notificações (`sidebar.html:28-35`) | Todos | Livre |
| Chat (`sidebar.html:38-45`) | Todos | Livre |
| Comercial > Clientes (`sidebar.html:53`) | Vendas, MASTER, ADMIN, Gerenciais | `podeAcessarVendas` (`GlobalControllerAdvice.java:222-225`) |
| Clientes > Geral > Novo (`sidebar.html:65`) | MASTER, ADMIN, GERENTE | `podeGerenciarVendas` (`NivelAcesso.java:95-97`) |
| Comercial > Vendas (`sidebar.html:105`) | Vendas, MASTER, ADMIN, Gerenciais | `podeAcessarVendas` |
| Fornecedores (`sidebar.html:118`) | Compras, MASTER, ADMIN, Gerenciais | `podeAcessarCompras` (`GlobalControllerAdvice.java:232-235`) |
| Operacional > Produtos/Estoque/Categorias (`sidebar.html:121-135`) | Estoque, Compras, MASTER, ADMIN, Gerenciais | `podeAcessarEstoque` (`GlobalControllerAdvice.java:227-230`) |
| RH > Colaboradores (`sidebar.html:137-157`) | RH, MASTER, ADMIN, GERENTE | `podeAcessarRH` (`GlobalControllerAdvice.java:217-221`) |
| RH > Colaboradores > Novo/Adesão (`sidebar.html:149-152`) | MASTER, ADMIN, GERENTE | `podeGerenciarRH` (`NivelAcesso.java:88-90`) |
| RH > Folha de Pagamento (`sidebar.html:160-173`) | MASTER, ADMIN, GERENTE | `podeGerenciarRH` |
| RH > Benefícios (`sidebar.html:175-186`) | Gerenciais | `isGerencial` (`GlobalControllerAdvice.java:65-69`) |
| RH > Workflow (`sidebar.html:189-198`) | Gerenciais | `isGerencial` |
| RH > Ponto e Escalas (`sidebar.html:201-214`) | RH e Gerenciais | Parte livre; `Correções`/`Escalas`: `isGerencial`; `Relatórios`: `podeGerenciarRH` |
| RH > Férias (`sidebar.html:216-228`) | RH e Gerenciais | `Solicitar`: livre; `Aprovar`: `isGerencial`; `Planejamento`: `podeGerenciarRH` |
| Gestão > Financeiro (`sidebar.html:291`) | MASTER, ADMIN, GERENTE, COORDENADOR | `podeAcessarFinanceiro` (`GlobalControllerAdvice.java:77-81`) |
| Financeiro > Pagar/Receber/Fluxo/Transferências/Relatórios (`sidebar.html:304-337`) | Idem acima | `podeAcessarFinanceiro` |
| Gestão > Marketing (`sidebar.html:345-357`) | Marketing, MASTER, ADMIN, Gerenciais | `podeAcessarMarketing` (`GlobalControllerAdvice.java:237-240`) |
| Gestão > TI (`sidebar.html:359-371`) | TI, MASTER, ADMIN | `podeAcessarTI` (`GlobalControllerAdvice.java:242-245`) |
| Gestão > Jurídico (`sidebar.html:373-385`) | Jurídico, MASTER, ADMIN, Gerenciais | `podeAcessarJuridico` (`GlobalControllerAdvice.java:247-250`) |
| Gestão > Projetos (`sidebar.html:386-437`) | TI, MASTER, ADMIN, Gerenciais | `podeAcessarProjetos` (`GlobalControllerAdvice.java:252-255`) |
| Projetos > Geral > Novo (`sidebar.html:399-402`) | MASTER, ADMIN, Gerenciais | `podeGerenciarProjetos` (`GlobalControllerAdvice.java:257-260`) |
| Serviços > Solicitações (`sidebar.html:446-459`) | Todos | Livre |
| Solicitações > Pendentes/Dashboard (`sidebar.html:454-457`) | MASTER, ADMIN, GERENTE | `podeGerenciarUsuarios` (`GlobalControllerAdvice.java:71-75`) |
| Serviços (`sidebar.html:461`) | Todos | Livre |
| Agenda (`sidebar.html:462`) | Todos | Livre |
| Administração > Usuários (`sidebar.html:469-471`) | MASTER, ADMIN, GERENTE | `podeGerenciarUsuarios` |
| Administração > Gestão de Acesso > Perfis/Permissões (`sidebar.html:472-481`) | MASTER, ADMIN | `isAdmin or isMaster` |
| Administração > Relatórios (`sidebar.html:483`) | Gerenciais | `isGerencial` |
| Administração > Configurações (`sidebar.html:484`) | MASTER, ADMIN | `isAdmin or isMaster` |
| Administração > Metas (`sidebar.html:485`) | Gerenciais | `isGerencial` |
| Pessoal > Meus Pedidos/Serviços/Favoritos/Recomendados (`sidebar.html:487-497`) | Não-gerenciais | `!isGerencial` |
| Suporte & Documentos > Central de Ajuda (`sidebar.html:503`) | Não-gerenciais | `!isGerencial` |
| Suporte & Documentos > Documentos Gerenciais (`sidebar.html:504`) | Gerenciais | `isGerencial` |
| Suporte & Documentos > Meus Documentos (`sidebar.html:505`) | Não-gerenciais | `!isGerencial` |
| Suporte & Documentos > Termos de Uso (`sidebar.html:506`) | Não-gerenciais | `!isGerencial` |
| Suporte (`sidebar.html:507`) | Todos | Livre |
| Sair (`sidebar.html:510-512`) | Todos | Livre |

## Exemplos
- Perfil GERENTE: atende `podeGerenciarUsuarios` e vê “Usuários” no sidebar (`GlobalControllerAdvice.java:71-75`, `sidebar.html:469-471`).
- Perfil COORDENADOR: contempla `podeAcessarFinanceiro`, acessando “Financeiro” (`NivelAcesso.java:81-83`, `sidebar.html:291`).
- Perfil MASTER: recebe `ROLE_MASTER` no login e vê “Gestão de Acesso > Perfis/Permissões” (`UsuarioDetailsService.java:44-54`, `sidebar.html:472-481`).
- Usuário com cargo de Marketing: `isMarketing` verdadeiro, vê “Marketing” (`GlobalControllerAdvice.java:181-190`, `sidebar.html:345-357`).

## Observações
- A visibilidade do menu não substitui a autorização de backend. Endpoints críticos usam `@PreAuthorize` e verificações adicionais.
- Perfis gerenciais agregam visibilidade ampla (`ehGerencial`), mas ações sensíveis ainda requerem validação por nível/role/permissão.