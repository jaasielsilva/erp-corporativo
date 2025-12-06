## Objetivo
Adicionar controles opcionais de pausa, retomada e cancelamento para o job assíncrono de Folha de Pagamento, alinhados ao módulo de CNPJs, e incluir barras de toasts visuais no cabeçalho.

## Backend (Controles do Job)
- Serviço: `src/main/java/com/jaasielsilva/portalceo/service/FolhaPagamentoService.java`
  - Criar flags por `jobId`: `pauseRequested[jobId]`, `cancelRequested[jobId]` (Map/ConcurrentHashMap).
  - Métodos: `pause(jobId)`, `resume(jobId)`, `cancel(jobId)` atualizam flags e registram logs.
  - Inserir checagens em loops de processamento (departamento e página):
    - `if (cancelRequested(jobId)) break` interrompe imediatamente o processamento restante.
    - `while (pauseRequested(jobId) && !cancelRequested(jobId)) Thread.sleep(200)` para pausar.
  - Status: estender map `processamentoJobs[jobId]` com `paused=true|false`, `canceled=true|false` e mensagens.

- Controller: `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java`
  - Endpoints autenticados (mesmo padrão CNPJ):
    - `POST /rh/folha-pagamento/processar/pause?jobId=...`
    - `POST /rh/folha-pagamento/processar/resume?jobId=...`
    - `POST /rh/folha-pagamento/processar/cancel?jobId=...`
  - Cada endpoint chama `folhaPagamentoService.pause|resume|cancel(jobId)` e retorna `200 OK` com JSON `{status:"paused|resumed|canceled"}`.

## Backend (Notificações em Ações)
- Em `FolhaPagamentoService` emitir notificações em ações:
  - `payroll_processing_paused` / `payroll_processing_resumed` / `payroll_processing_canceled` (para usuário logado e global), com `actionUrl` para `/rh/folha-pagamento/gerar` e `metadata` com `jobId`.

## Frontend (UI da Folha)
- Página: `src/main/resources/templates/rh/folha-pagamento/gerar.html`
  - Adicionar botões ao lado de “Processar Folha”: `Pausar`, `Retomar`, `Cancelar`.
  - JS inline:
    - Manter `processamentoJobId` ao iniciar; implementar `pauseJob()`, `resumeJob()`, `cancelJob()` que fazem `fetch` aos novos endpoints.
    - Atualizar cartão de status: exibir `paused` (alerta amarelo), `canceled` (vermelho) e congelar barra.
    - Integrar com toasts globais: ao iniciar/pausar/retomar/cancelar, chamar `window.globalToast(...)`.

## Toasts Globais no Cabeçalho
- Componente: `src/main/resources/templates/components/topbar.html`
  - Inserir container Bootstrap Toast (posição top-right) com `id="global-toast"`.
  - Script curto: `window.globalToast = (msg) => { /* atualiza corpo e mostra toast */ }`.
  - Reutilizar em páginas (inclui `topbar` em todas as telas).

## Logs & Status
- Registrar logs em cada transição (`pause/resume/cancel`) e no status push via WebSocket (`/topic/folha-status/{jobId}`), incluindo `paused`/`canceled`.

## Segurança
- Endpoints protegidos com `@PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")`.

## Testes
- Fluxo:
  - Iniciar processamento (gera `jobId`).
  - Pausar: status `paused=true`, barra congela, notificação de pausa e toast.
  - Retomar: volta a processar, status atualiza, notificação de retomada e toast.
  - Cancelar: encerra laços, status final como `ERRO` ou `CANCELADO` (mensagem de cancelamento), notificação de cancelamento e toast.

## Entregáveis
- Serviço e Controller com novos endpoints e flags de controle.
- UI da Folha com botões e handlers.
- Toasts globais no cabeçalho disponíveis em todo o sistema.

Confirma prosseguir com esta implementação? 