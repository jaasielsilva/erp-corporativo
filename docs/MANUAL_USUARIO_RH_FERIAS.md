# Manual do Usuário — RH/Férias

## Objetivo
- Orientar o preenchimento correto dos templates de Aprovação/Reprovação de Férias e os passos para testar o fluxo completo.

## Acesso
- Menu: `Recursos Humanos` → `Férias`
- Páginas:
  - `Solicitar`: `src/main/resources/templates/rh/ferias/solicitar.html`
  - `Aprovar`: `src/main/resources/templates/rh/ferias/aprovar.html`
  - `Planejamento`: `src/main/resources/templates/rh/ferias/planejamento.html`
  - `Calendário`: `src/main/resources/templates/rh/ferias/calendario.html`

## Perfis Necessários
- `GERENCIAL`, `ADMIN`, `MASTER` para Aprovar/Reprovar.
- `RH_GERENTE`/`RH_ANALISTA` para visão e validação.
- `COLABORADOR` para solicitar.

## Campos dos Templates
- Solicitar:
  - `Colaborador` (seleção ou id interno)
  - `Início` (formato `YYYY-MM-DD`)
  - `Fim` (formato `YYYY-MM-DD`)
  - `Observações` (opcional)
- Aprovar/Reprovar:
  - `ID da Solicitação` (tabela/lista da página)
  - `Observações` (opcional, recomendado)
  - Ações: `Aprovar` ou `Reprovar`

## Validações de Negócio
- Conflitos: não permite sobreposição com solicitações `SOLICITADA` ou `APROVADA` (`SolicitacaoFeriasService.java:53`).
- Blackout: bloqueia intervalo em períodos configurados (`Configurações RH → Políticas de Férias`), ex.: `12-15;12-25` (`SolicitacaoFeriasService.java:65`).
- Limite anual: valida `diasPorAno` (`SolicitacaoFeriasService.java:60`).
- Aprovação automática: quando `exigeAprovacaoGerente=false` (`SolicitacaoFeriasService.java:79`).

## Endpoints (para testes)
- Criar: `POST /api/rh/ferias/solicitacoes`
  - Params: `colaboradorId`, `inicio`, `fim`, `observacoes`
- Aprovar: `POST /api/rh/ferias/{id}/aprovar`
  - Params: `observacoes`
- Reprovar: `POST /api/rh/ferias/{id}/reprovar`
  - Params: `observacoes`
- Listar: `GET /api/rh/ferias/solicitacoes?status=APROVADA&inicio=YYYY-MM-DD&fim=YYYY-MM-DD`

## Exemplo de Teste Rápido (via formulário/login)
1. Faça login: usuário `master@sistema.com`, senha `master123`.
2. Acesse `Férias → Solicitar`, selecione colaborador e período, salve.
3. Acesse `Férias → Aprovar`, localize a solicitação, preencha `Observações` e clique `Aprovar`.
4. Para reprovar, repita o passo 3 usando `Reprovar`.
5. Consulte em `Relatórios RH` ou via API `GET /api/rh/ferias/solicitacoes`.
6. Verifique no MySQL (root/12345, banco `painelceo`):
   - `SELECT id, status, periodo_inicio, periodo_fim FROM rh_solicitacoes_ferias ORDER BY id DESC LIMIT 5;`

## Referências
- Controller páginas: `src/main/java/com/jaasielsilva/portalceo/controller/rh/FeriasController.java`
- API: `src/main/java/com/jaasielsilva/portalceo/controller/rh/FeriasApiController.java`
- Serviço e regras: `src/main/java/com/jaasielsilva/portalceo/service/rh/SolicitacaoFeriasService.java`
- Políticas: `src/main/resources/templates/rh/configuracoes/politicas-ferias.html`
