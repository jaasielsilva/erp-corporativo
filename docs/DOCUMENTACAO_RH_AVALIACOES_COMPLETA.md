# 📋 Documentação Completa — Módulo RH/Avaliações

Versão: 1.0 • Ambiente: `http://localhost:8080/`

---

## 🎯 Objetivo do Módulo

- Finalidade: conduzir ciclos de avaliação de desempenho de colaboradores com registro de nota, feedback e decisão (aprovação/reprovação) pelo RH/gestores.
- Público‑alvo: avaliadores (gestores, RH) e avaliados (colaboradores). Perfis habilitados por RBAC.
- Benefícios esperados:
  - Padronização do processo de avaliação por período
  - Transparência de status e decisões
  - Histórico rastreável e auditoria de ações
  - Base para relatórios e decisões de gestão (promoções, PDI)

Referências técnicas:
- API principal: `src/main/java/com/jaasielsilva/portalceo/controller/rh/AvaliacaoApiController.java:33-118`
- Serviço: `src/main/java/com/jaasielsilva/portalceo/service/rh/AvaliacaoDesempenhoService.java:20-92`
- Entidade: `src/main/java/com/jaasielsilva/portalceo/model/AvaliacaoDesempenho.java:10-59`
- Auditoria RH: `src/main/java/com/jaasielsilva/portalceo/service/AuditoriaRhLogService.java:19-29`

---

## 🔄 Fluxo Completo

- Cadastro de avaliações (abertura de ciclo)
  - Página: `http://localhost:8080/rh/avaliacao/periodicidade`
  - Template: `src/main/resources/templates/rh/avaliacao/periodicidade.html`
  - Endpoint: `POST /api/rh/avaliacao/ciclos` em `AvaliacaoApiController.java:33-48`
  - Resultado: cria `AvaliacaoDesempenho` (status `ABERTA`) com período definido.

- Definição de critérios e pesos
  - Estado atual: avaliação coleta `nota` (0–10) e `feedback` livre (`feedbacks.html`).
  - Modelos com critérios/pesos por competência: planejado para versão futura; hoje, a métrica é agregada pela `nota` do ciclo.

- Períodos de avaliação
  - Definidos na abertura de ciclo (`inicio`, `fim`).
  - Validações: data fim não pode ser anterior à data início; períodos em conflito são bloqueados no serviço (`SolicitacaoFeriasService` usa padrão semelhante de validação de conflito).

- Notificações e prazos
  - Interface orienta abertura e submissão. Notificações automáticas podem ser integradas via serviço de notificações (como em workflow de adesão); atualmente não exclusivo para avaliações.

- Processo de preenchimento (submissão)
  - Página: `http://localhost:8080/rh/avaliacao/feedbacks`
  - Template: `src/main/resources/templates/rh/avaliacao/feedbacks.html`
  - Endpoint: `POST /api/rh/avaliacao/{id}/submeter` em `AvaliacaoApiController.java:72-87`
  - Entrada: `nota`, `feedback`; transição para `SUBMETIDA`.

- Aprovações hierárquicas
  - Página: `http://localhost:8080/rh/avaliacao/relatorios`
  - Template: `src/main/resources/templates/rh/avaliacao/relatorios.html`
  - Endpoints: aprovar/reprovar em `AvaliacaoApiController.java:89-119`
  - Perfis: `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER` (aprovação); `ROLE_GERENCIAL` (submissão).

- Geração de relatórios
  - Página: `http://localhost:8080/rh/avaliacao/relatorios`
  - Filtros: status, período (início/fim)
  - Fonte: `GET /api/rh/avaliacao/ciclos` em `AvaliacaoApiController.java:50-70`
  - Visualização: tabela paginada com colunas-chave do ciclo.

---

## 🧭 Casos de Uso

- Avaliação de desempenho anual
  - Abrir ciclo com período do ano corrente em `periodicidade`
  - Submeter nota/feedback em `feedbacks`
  - Aprovar ou reprovar em `relatorios`
  - Resultado: ciclo fechado com decisão, disponível para consulta.

- Avaliação de período probatório (90 dias)
  - Abrir ciclo com 90 dias a partir da admissão
  - Submeter no fim do período; aprovar no `relatorios`
  - Observações devem registrar pontos críticos e recomendação.

- Avaliação para promoção
  - Abrir ciclo vinculado ao colaborador e período de análise
  - Submeter com justificativa; aprovação pelo RH
  - Decisão embasa atualização de cargo/salário fora deste módulo.

- Autoavaliação
  - Submissão de nota/feedback pelo avaliador habilitado; para autoavaliação, permitir que o colaborador com perfil gerencial submeta seu próprio ciclo.
  - Endpoint de submissão requer `ROLE_GERENCIAL` (`AvaliacaoApiController.java:72-77`).

- Avaliação 360 graus
  - Planejado: múltiplos avaliadores por ciclo e consolidação de notas/pesos.
  - Estado atual: um avaliador por ciclo; extensão futura via “modelos de avaliação”.

---

## ⚙️ Funcionalidades

- Configuração de modelos de avaliação
  - Planejado: catálogo de modelos com critérios (competências) e pesos; associação por cargo/departamento.
  - Estado atual: nota agregada e feedback textual.

- Controle de acesso por perfil (RBAC)
  - Abrir/listar ciclos: `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_GERENCIAL` (`AvaliacaoApiController.java:33-57`)
  - Submeter: `ROLE_GERENCIAL`, `ROLE_ADMIN`, `ROLE_MASTER` (`AvaliacaoApiController.java:72-74`)
  - Aprovar/Reprovar: `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER` (`AvaliacaoApiController.java:89-107`)

- Dashboard de acompanhamento
  - `relatorios.html` filtra e mostra status dos ciclos com ações rápidas de decisão.

- Histórico de avaliações
  - Consulta via listagem de ciclos e auditoria RH.
  - Auditoria detalha ações (abrir, submeter, aprovar, reprovar): `service/rh/AvaliacaoDesempenhoService.java:46-90`

- Exportação de dados
  - Estado atual: consulta paginada via API (`/api/rh/avaliacao/ciclos`) e consumo externo.
  - Planejado: exportação CSV/Excel/PDF a partir da página de relatórios.

---

## 📈 Relatórios (`/rh/avaliacao/relatorios`)

- Tipos de relatórios gerados
  - Listagem dos ciclos por filtros: status e período; visão operacional para decisão.

- Parâmetros de filtragem
  - `status`: `ABERTA`, `SUBMETIDA`, `APROVADA`, `REPROVADA`
  - `inicio` e `fim`: ISO Date (`yyyy-MM-dd`)

- Formatos de exportação
  - Planejado: PDF/Excel; atual: tabela e API para integração.

- Visualizações gráficas
  - Base com `Chart.js` preparada no template; gráficos por status e período podem ser adicionados (dados via `/api/rh/avaliacao/ciclos`).

Referência da página:
- `src/main/resources/templates/rh/avaliacao/relatorios.html:23-86`

---

## 🧪 Exemplos Práticos com Telas

- Abrir ciclo (Periodicidade)
  - Acessar: `http://localhost:8080/rh/avaliacao/periodicidade`
  - Buscar colaborador, definir `Início`/`Fim`, clicar “Abrir Ciclo”
  - Resultado visível na tabela de ciclos
  - Tela: `templates/rh/avaliacao/periodicidade.html:23-66`

- Submeter nota/feedback
  - Acessar: `http://localhost:8080/rh/avaliacao/feedbacks`
  - Selecionar avaliação `ABERTA`, informar `Nota` e `Feedback`, clicar “Submeter”
  - Tela: `templates/rh/avaliacao/feedbacks.html:23-49`

- Aprovar/Reprovar avaliação
  - Acessar: `http://localhost:8080/rh/avaliacao/relatorios`
  - Filtrar por `SUBMETIDA`, clicar “Aprovar” ou “Reprovar” na linha
  - Tela: `templates/rh/avaliacao/relatorios.html:49-84`

Observação sobre prints: as páginas acima são as telas reais do sistema; para registrar as imagens, acesse os caminhos, use a função de captura do navegador e anexe ao repositório em `docs/img/rh-avaliacoes/` conforme necessidade.

---

## 🔐 Auditoria e Rastreamento

- Ações registradas em `rh_auditoria_logs` com “categoria”, “ação”, “recurso”, “usuário”, “ip”, “detalhes”, “sucesso”.
- Registros gerados no serviço de avaliações: `src/main/java/com/jaasielsilva/portalceo/service/rh/AvaliacaoDesempenhoService.java:46-90`
- Filtros e listagem: `GET /api/rh/auditoria/logs` em `src/main/java/com/jaasielsilva/portalceo/controller/rh/RhAuditoriaApiController.java:26-37`

---

## 📚 Referências de Código

- DTO: `src/main/java/com/jaasielsilva/portalceo/dto/AvaliacaoDesempenhoDTO.java:5-46`
- Entidade: `src/main/java/com/jaasielsilva/portalceo/model/AvaliacaoDesempenho.java:23-59`
- API: `src/main/java/com/jaasielsilva/portalceo/controller/rh/AvaliacaoApiController.java:33-119`
- Templates:
  - `periodicidade.html`: `src/main/resources/templates/rh/avaliacao/periodicidade.html:23-66`
  - `feedbacks.html`: `src/main/resources/templates/rh/avaliacao/feedbacks.html:23-75`
  - `relatorios.html`: `src/main/resources/templates/rh/avaliacao/relatorios.html:23-86`

---

## 🗺️ Roadmap (Resumo)

- Modelos de avaliação com critérios/pesos
- Exportação (CSV/Excel/PDF) e gráficos por período/status
- Notificações automáticas e SLA de ciclos
- 360° com múltiplos avaliadores e consolidação

---

## ✅ RBAC e Segurança

- Submissão (`submeter`): `ROLE_GERENCIAL`, `ROLE_ADMIN`, `ROLE_MASTER`
- Aprovação/Reprovação: `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`
- Listagem/Abertura: `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_GERENCIAL`

Veja: `AvaliacaoApiController.java:33-74, 89-107`

---

## 🧭 Como os Dados Fluem

- Abertura: usuário autorizado cria ciclo (`ABERTA`) com período → persiste em `AvaliacaoDesempenho`
- Submissão: avaliador envia `nota`/`feedback` → transita para `SUBMETIDA`
- Decisão: RH aprova/reprova → `APROVADA` ou `REPROVADA`
- Consulta: relatórios filtram ciclos via API; auditoria registra ações com “quem/onde/resultado”

---

## 🛠️ Suporte

- Em caso de dúvidas, verifique logs de aplicação e o endpoint de auditoria.
- Erros comuns: falta de permissão (RBAC), período inválido, ciclo inexistente.

