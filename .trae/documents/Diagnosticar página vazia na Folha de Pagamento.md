## Objetivo
Investigar por que `/rh/folha-pagamento/visualizar/3` aparece “vazia” e validar o fluxo completo de geração/visualização da folha, incluindo login e banco MySQL.

## Entendimento do Fluxo Atual
- Backend exibe a página e injeta dados:
  - Controller de visualização: `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:279` chama `buscarPorId(id)`; injeta `folha` e `resumo`; retorna o template.
  - API de holerites: `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:295` lista holerites paginados.
  - Resumo: `src/main/java/com/jaasielsilva/portalceo/service/HoleriteService.java:150` soma valores e quantidade de holerites.
- Frontend (Thymeleaf):
  - Define `window.folhaId = [[${folha.id}]]` `src/main/resources/templates/rh/folha-pagamento/visualizar.html:100`.
  - Busca holerites via `fetch` `src/main/resources/templates/rh/folha-pagamento/visualizar.html:153` e renderiza tabela.

## Hipóteses para “página vazia”
- Folha ID 3 não existe → controller redireciona; se houver acesso direto, `window.folhaId` pode ficar indefinido e a lista fica vazia.
- Folha existe, mas sem holerites (colaborador não ativo, ou geração não criou registros) → tabela renderiza “Nenhum registro”.
- Erro 403/500 na API `/api/folha/{id}/holerites` → catch do JS desenha tabela vazia, aparentando “página vazia”.

## Testes End-to-End (executar localmente)
1. Iniciar o app e acessar `http://localhost:8080/login`; autenticar com `master@sistema.com` / `master123`.
2. Confirmar conexão MySQL (`painelceo`, `root`/`12345`) e que o usuário master e 1 colaborador ativo (salário 5000) existem.
3. Verificar se existe a folha `id=3`:
   - Se existir, abrir `/rh/folha-pagamento/visualizar/3` e inspecionar a chamada `GET /rh/folha-pagamento/api/folha/3/holerites` (Status/JSON).
   - Se não existir, gerar nova folha em `/rh/folha-pagamento/gerar` e seguir o redirect para visualizar.
4. Conferir no DevTools se `window.folhaId` tem valor e se o `fetch` retorna `{content, totalElements}` > 0.

## Diagnósticos no Backend
- Validar `FolhaPagamentoController.visualizar` `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:279` retorna corretamente o template com `folha`.
- Checar `listarHoleritesFolha` `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:295` e sua projeção `HoleriteRepository.findListByFolhaPaginado` `src/main/java/com/jaasielsilva/portalceo/repository/HoleriteRepository.java:79`.
- Confirmar que a geração cria holerites para colaboradores ativos: `FolhaPagamentoService.gerarFolhaPagamento` `src/main/java/com/jaasielsilva/portalceo/service/FolhaPagamentoService.java:286` e uso de `ColaboradorRepository.findByAtivoTrue` `src/main/java/com/jaasielsilva/portalceo/repository/ColaboradorRepository.java:29`.

## Diagnósticos no Frontend
- Ver se os cards de resumo mostram valores (> 0) — vêm de `HoleriteService.calcularResumoFolha` `src/main/java/com/jaasielsilva/portalceo/service/HoleriteService.java:150`.
- Inspecionar o `fetch` e mensagens de erro na aba Network/Console.

## Correções Propostas (se confirmada a causa)
- Se `window.folhaId` estiver indefinido, exibir aviso claro e evitar a chamada.
- Se a API retorna erro, ajustar tratamentos e logs para refletir o problema ao usuário.
- Se não há holerites por falta de colaborador ativo, orientar a ativação/atributos necessários e regenerar.

## Entregáveis
- Execução dos testes e relatório com: status das APIs, existência/quantidade de holerites, prints de DevTools, e diagnóstico final com a correção aplicada (frontend/backend conforme a causa).