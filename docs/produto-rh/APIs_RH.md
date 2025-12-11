# Documentação de APIs — Módulo RH

## Colaboradores
- `POST /rh/colaboradores/novo` — cria colaborador (REST)
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/colaborador/ColaboradorController.java:159-213`
- `GET /rh/colaboradores/api/listar?page&size&q` — listagem paginada simples
  - Controller: `.../ColaboradorController.java:78-104`

## Folha de Pagamento / Holerites
- `GET /rh/folha-pagamento/api/folha/{id}/holerites?page&size&q`
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:294-322`
- `GET /rh/folha-pagamento/api/colaborador/{colaboradorId}/holerites?page&size&ano&mes`
  - Controller: `.../FolhaPagamentoController.java:324-343`
- `GET /rh/folha-pagamento/api/holerite/{id}/colaborador`
  - Controller: `.../FolhaPagamentoController.java:355-371`

## Ponto e Escalas
- MVC páginas: registros, correções, escalas, vigências, exceções
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/PontoEscalasController.java`

## Recrutamento (API)
- Base: `/api/rh/recrutamento/*` — candidatos, experiências, formações, habilidades, vagas, candidaturas, entrevistas, avaliação, etapas
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/RecrutamentoApiController.java`
- Métricas: `GET /api/rh/recrutamento/relatorios/metrics`
  - Controller: `.../RecrutamentoApiController.java:238-242`

## Treinamentos (API)
- Cursos: `GET/POST /api/rh/treinamentos/cursos`
- Instrutores: `GET /api/rh/treinamentos/instrutores`
- Turmas: `POST /api/rh/treinamentos/turmas`, `GET /api/rh/treinamentos/turmas`
- Matrículas: `POST /api/rh/treinamentos/turmas/{turmaId}/matriculas`
- Frequência: `POST /api/rh/treinamentos/matriculas/{matriculaId}/frequencia`
- Avaliação: `POST /api/rh/treinamentos/matriculas/{matriculaId}/avaliacao`
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/TreinamentosApiController.java`

## Workflow de Aprovação (Adesão)
- Listar processos: `GET /rh/workflow/api/processos?status&page&size&search`
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/WorkflowAdesaoController.java:51-79`
- Detalhes: `GET /rh/workflow/api/processo/{id}` — `.../WorkflowAdesaoController.java:92-119`
- Aprovar: `POST /rh/workflow/api/processo/{id}/aprovar` — `.../WorkflowAdesaoController.java:151-215`
- Relatórios: `GET /rh/workflow/api/relatorios/processos` — `.../WorkflowAdesaoController.java:364-392`
- Métricas: `GET /rh/workflow/api/relatorios/metricas` — `.../WorkflowAdesaoController.java:405-432`

## Relatórios RH
- `GET /api/rh/relatorios/turnover?inicio&fim`
- `GET /api/rh/relatorios/admissoes-demissoes?inicio&fim`
- `GET /api/rh/relatorios/ferias-beneficios?inicio&fim`
- `GET /api/rh/relatorios/indicadores?inicio&fim`
  - Controller técnico: `docs/DOCUMENTACAO_TECNICA_RH_RELATORIOS.md`

## Segurança
- Perfis exigidos em cada endpoint conforme `@PreAuthorize` nos controllers.
- Respostas padronizadas com `ResponseEntity`, JSON com objetos e listas.

