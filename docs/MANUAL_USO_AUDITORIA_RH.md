# Manual de Uso — Auditoria RH (5 páginas)

## Visão Geral
- Público: perfis `ADMIN`, `MASTER`, `RH_GERENTE`.
- Navegação: Sidebar → Auditoria RH → Início, Log de Acessos, Alterações de Dados, Exportações, Revisões Periódicas.
- Backend: dados carregados via `GET /api/rh/auditoria/logs`.
- Página de início e rotas: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaController.java`.

## Padrões Comuns
- Layout: `Sidebar`, `Topbar`, `Footer` com Bootstrap.
- Filtros: `Usuário`, `Recurso`, `Início`, `Fim` (datetime), `Filtrar`, `Limpar`.
- Tabela: `Data`, `Usuário`, `Ação`, `Recurso`, `IP`, `Sucesso`.
- Erros: `403 Acesso negado`, `500 Erro ao carregar` renderizados na tabela e via notificações.

## 1) Início
- Objetivo: ponto de entrada, orientação e navegação para as páginas de auditoria.
- Acesso: `GET /rh/auditoria` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaController.java:13-19`.
- O que você vê:
  - Título “Auditoria RH”
  - Descrição breve e layout consistente
  - Navegação pelo Sidebar para as demais páginas
- Ações:
  - Usar o menu “Auditoria RH” (Sidebar) para abrir Acessos, Alterações, Exportações, Revisões.

## 2) Log de Acessos
- Objetivo: auditar autenticações/autorizações no módulo RH.
- Acesso: `GET /rh/auditoria/acessos` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaController.java:21-28`.
- Página: `src/main/resources/templates/rh/auditoria/acessos.html`.
- Campos:
  - `Usuário` (texto) — email ou usuário. Ex.: `ana@empresa.com`.
  - `Recurso` (texto) — rota acessada. Ex.: `/login`, `/rh/folha-pagamento`.
  - `Início` (datetime-local) — início do intervalo.
  - `Fim` (datetime-local) — fim do intervalo.
- Botões:
  - `Filtrar` — carrega dados com os filtros aplicados.
  - `Limpar` — zera os filtros e recarrega.
  - `Voltar` — retorna ao índice de Auditoria RH.
- Ações:
  - Pesquisar por usuário/rota dentro de um período.
  - Conferir status (`OK` ou `Falha`), IP e ação.
- Como funciona:
  - A categoria padrão de pesquisa é `ACESSO` (aplicada pelo controlador).
  - Chama `GET /api/rh/auditoria/logs?categoria=ACESSO&usuario&recurso&inicio&fim&page&size` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaApiController.java:26-37`.
- Erros:
  - Sem permissão: tabela exibe “Acesso negado” (403).
  - Falha backend: “Erro ao carregar”.

## 3) Alterações de Dados
- Objetivo: rastrear edições em dados de RH (ex.: colaboradores, parâmetros).
- Acesso: `GET /rh/auditoria/alteracoes` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaController.java:30-37`.
- Página: `src/main/resources/templates/rh/auditoria/alteracoes.html`.
- Campos (idênticos aos de Acessos): `Usuário`, `Recurso`, `Início`, `Fim`.
- Botões (idênticos): `Filtrar`, `Limpar`, `Voltar`.
- Ações:
  - Filtrar edições por usuário e rota impactada (ex.: `/rh/colaboradores/editar`).
  - Conferir ação e sucesso.
- Como funciona:
  - Categoria padrão `ALTERACAO` definida pelo controlador.
  - Chama `GET /api/rh/auditoria/logs?categoria=ALTERACAO&...`.

## 4) Exportações
- Objetivo: auditar exportações (CSV, PDF, Excel) iniciadas pelos usuários no RH.
- Acesso: `GET /rh/auditoria/exportacoes` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaController.java:39-46`.
- Página: `src/main/resources/templates/rh/auditoria/exportacoes.html`.
- Campos: `Usuário`, `Recurso` (ex.: `/relatorios/export.xlsx`), `Início`, `Fim`.
- Botões: `Filtrar`, `Limpar`, `Voltar`.
- Ações:
  - Identificar quem exportou, qual rota, quando e se houve sucesso.
- Como funciona:
  - Categoria padrão `EXPORTACAO` definida pelo controlador.
  - Chama `GET /api/rh/auditoria/logs?categoria=EXPORTACAO&...`.

## 5) Revisões Periódicas
- Objetivo: acompanhar revisões formais (compliance/processos) registradas como auditorias.
- Acesso: `GET /rh/auditoria/revisoes` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaController.java:48-55`.
- Página: `src/main/resources/templates/rh/auditoria/revisoes.html`.
- Campos: `Usuário`, `Recurso`, `Início`, `Fim`.
- Botões: `Filtrar`, `Limpar`, `Voltar`.
- Ações:
  - Mapear histórico de revisões por usuário e rota revisada.
- Como funciona:
  - Categoria padrão `REVISAO` definida pelo controlador.
  - Chama `GET /api/rh/auditoria/logs?categoria=REVISAO&...`.

## Fluxo Típico de Uso
1. Abrir “Início” de Auditoria RH pelo Sidebar.
2. Escolher a página desejada (ex.: “Log de Acessos”).
3. Preencher filtros conforme a investigação.
4. Clicar `Filtrar` para ver resultados na tabela.
5. Opcional: clicar `Limpar` para reiniciar.
6. Usar `Voltar` para retornar ao índice.

## Exemplos
- Investigação de login falho:
  - “Log de Acessos” → `Usuário=joao@empresa.com`, período do dia.
  - Ver `Sucesso=Falha`, `Recurso=/login`, `IP`.
- Auditoria de exportações de folha:
  - “Exportações” → `Recurso=/rh/folha-pagamento/relatorios`, semana corrente.
  - Validar horários e usuários responsáveis.
- Revisões de processos:
  - “Revisões Periódicas” → período mensal.
  - Conferir títulos/rotas revisadas e status.

## Permissões
- Páginas protegidas por `@PreAuthorize`:
  - Início, Acessos, Alterações, Exportações, Revisões → `ADMIN`, `MASTER`, `RH_GERENTE`.
- Sidebar controla visibilidade: `src/main/resources/templates/components/sidebar.html:307-319`.

## Boas Práticas
- Sempre defina o intervalo de datas para consultas objetivas.
- Use `Recurso` para afinar a busca (rota exata ou prefixos).
- Valide `Sucesso` e `Ação` para diferenciar acessos legítimos de falhas.

## Troubleshooting
- “Acesso negado” (403): verifique perfil do usuário.
- “Erro ao carregar”: tente novamente; se persistir, contate o administrador.
- Tabela vazia: ajuste filtros; pode não haver registros no período.

## Referências
- Controlador MVC: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaController.java:13-55`.
- API de Logs: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaApiController.java:26-37`.
- Página “Acessos” (exemplo de filtros e renderização): `src/main/resources/templates/rh/auditoria/acessos.html:29-51`, `90-141`.
