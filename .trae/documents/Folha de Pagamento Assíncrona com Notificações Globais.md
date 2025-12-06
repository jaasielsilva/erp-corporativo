## Objetivo
Transformar o processamento da Folha de Pagamento em operação totalmente assíncrona, com notificações globais de início, conclusão e erro, mantendo as funcionalidades atuais e adicionando logs e tratamento robusto de falhas — espelhando o módulo "Processar CNPJs (Assíncrono)".

## Contexto Atual (mapeado)
- Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java` (endpoints síncronos e assíncronos já presentes: `POST /rh/folha-pagamento/processar-async`, `GET /rh/folha-pagamento/status-processamento`).
- Serviço: `src/main/java/com/jaasielsilva/portalceo/service/FolhaPagamentoService.java` (métodos `iniciarProcessamentoAsync`, `gerarFolhaPagamentoAsync`, status por job). Cálculos distribuídos por `HoleriteService`, `HoleriteCalculoService`, `ResumoFolhaService`.
- Templates principais: `templates/rh/folha-pagamento/gerar.html`, `visualizar.html`, `index.html` já possuem JS inline para polling de status.
- Notificações globais existentes no módulo CNPJ: padrão de criação em `ProcessamentoCnpjService` via `NotificationService.createNotification(...)` e `createGlobalNotification(...)`, com `actionUrl` e `metadata`.

## Alterações de Backend
- Padronizar job assíncrono:
  - Consolidar `iniciarProcessamentoAsync(mes, ano, usuario, ...)` para marcar `emExecucao` (AtomicBoolean), registrar `startedAt`, `finishedAt`, contadores (processados, erros), amostras de erro — igual ao `ProcessamentoCnpjService`.
  - Controlar concorrência com `ThreadPoolTaskExecutor` + `Semaphore` quando houver processamento por lotes de colaboradores.
- Endpoints de controle (se necessários/ausentes):
  - `POST /rh/folha-pagamento/processar-async` (start, já existe).
  - `GET /rh/folha-pagamento/status-processamento` (status, já existe — revisar DTO para incluir `durationMs`, amostras, mensagens).
  - Opcional: `POST /rh/folha-pagamento/pause|resume|cancel` para alinhamento com CNPJ quando aplicável.
- DTO de Status:
  - Introduzir/estender `StatusFolhaProcessamentoDto` com: `running`, `processed`, `startedAt`, `finishedAt`, `durationMs`, `errorCount`, `errorSamples`, `folhaId`/`jobId`.
- Notificações Globais:
  - Integrar `NotificationService` no serviço de folha para eventos:
    - `payroll_processing_start` → título "Processamento de Folha", `actionUrl: /rh/folha-pagamento` ou `/rh/folha-pagamento/visualizar/{folhaId}` quando disponível; `metadata` com `jobId`, `mes`, `ano`.
    - `payroll_processing_complete` → resumo com totais e contadores.
    - `payroll_processing_error` → mensagem com amostras/primeiro erro.
  - Manter também notificação por usuário (não só global), como feito em CNPJ.
- Logs (SLF4J + MDC):
  - Adicionar entradas de log para início/fim, cada etapa crítica de cálculo (proventos, descontos, tributos), agregações, persistência e erros (incluindo stacktrace e contexto: `jobId`, `folhaId`, `mes/ano`).

## Alterações de Frontend
- `templates/rh/folha-pagamento/gerar.html`:
  - Incluir `notifications.js` (se não incluso) e chamar toasts visuais ao receber status de início/conclusão/erro.
  - Alinhar JS inline ao padrão de `cnpj-processar.js`: funções `iniciar`, `consultarStatus`, `iniciarPolling`, `pararPolling`, atualização de badges e barra de progresso.
  - Exibir painel de pré‑relatório: amostras de erros (similar a inválidos/erros do CNPJ), tempo de processamento e contadores.
- `visualizar.html`:
  - Adicionar banner de conclusão quando navegado via `actionUrl` clicado na notificação.

## Tratamento de Erros
- Envolver laços e tarefas assíncronas em `try/catch` com contabilização (`errorCount`) e coleta limitada em `errorSamples`.
- Propagar erro final para status e emitir `payroll_processing_error`.
- Cancelamento/pausa/retomada: respeitar flags como em CNPJ e garantir consistência transacional (não deixar folha parcialmente persistida sem marcação de status apropriado).

## Segurança & Permissões
- Reutilizar guardas de segurança do módulo RH já existentes: endpoints sob autenticação e perfis adequados.
- Sem dados sensíveis em logs/metadata da notificação; usar IDs e referências seguras.

## Manutenção de Funcionalidades
- Preservar fluxos atuais de geração, fechamento, cancelamento e visualização.
- O async não altera o resultado funcional; apenas desloca para segundo plano com feedback.

## Observabilidade
- Métricas de processamento: tempo total (`durationMs`), itens processados, erros, throughputs por etapa.
- Opcional: publicar métricas em Actuator/Micrometer com tags `mes`, `ano`, `tipoFolha`.

## Verificação
- Testes de integração (MockMvc + `@WithMockUser`) cobrindo:
  - início de processamento, polling de status, conclusão.
  - emissão de notificações (validação via endpoints `/api/notifications`).
  - simulação de erro e emissão de `payroll_processing_error`.
- Testes de UI manuais: gerar folha, observar toasts e painel de status enquanto continua navegando em outras páginas.

## Entregáveis
- Atualização de serviço `FolhaPagamentoService` com execução assíncrona robusta, status detalhado e notificações globais.
- Ajustes nos templates JS/HTML para toasts, barra de progresso e pré‑relatório.
- Logs estruturados e (opcional) métricas.

Confirma prosseguir com esta implementação? 