## Problema e Causas Prováveis
- Toast global só aparece ao abrir o painel de notificações; provável dependência de jQuery/DOM ou cliente WebSocket não carregado em todas as páginas.
- Itens “De: undefined”, “Invalid Date” sugerem metadados incompletos (user nulo, timestamps não formatados) e renderização sem fallback.

## Correções (Notificações Globais)
1. Garantir cliente WS em todas as páginas:
   - Confirmar que `topbar` é incluído em todas as páginas; caso alguma não use, mover `<script src="/js/websocket-notifications.js">` para layout base comum.
   - Arquivo: `templates/components/topbar.html` (já injeta `data-user-email`), validar presença em todas páginas.
2. Exibir toast automaticamente ao receber WS:
   - Verificar `handleNewNotification` em `static/js/websocket-notifications.js` chama `showToast(notification)` sempre.
   - Eliminar qualquer dependência de painel/ícone; usar DOM nativo para inserir toasts.
3. CSP/Conexão:
   - Confirmar `WebSocketConfig` e CSP permitem `ws:`/`wss:` e `/ws` endpoint (`config/WebSocketConfig.java`, `SecurityInterceptor.addSecurityHeaders`).

## Correções (Metadados e Renderização)
1. Backend: preencher metadados em `NotificationService` ao criar notificação:
   - `title`, `message`, `priority` já definidos; garantir `user` para notificações direcionadas.
   - Para globais, esconder “De:” ou usar placeholder “Sistema”.
   - Preencher `actionUrl` com `/utilidades/processar-cnpj` para abrir a tela.
   - Opcional: `metadata` JSON com `{ processedCount, startedAt, finishedAt, durationMs }` nas de conclusão.
2. Frontend (painel de notificações):
   - Formatar `timestamp` com `toLocaleString('pt-BR')` e, se for inválido, ocultar.
   - Se `user` for nulo, exibir “Sistema” em vez de `undefined`.
   - Garantir que “Marcar como lida” e “Silenciar” não dependam da abertura do sino, mas do item em si.

## Polimento Profissional (Utilidades)
1. Página `/utilidades/processar-cnpj`:
   - Barra de progresso com total processado e estimativa.
   - Botão “Cancelar” e “Pausar”, controle de concorrência (slider de threads), e resumo do último run.
   - Log compacto dos últimos erros (ex.: CNPJs inválidos) e link “Ver detalhes”.
2. Serviço:
   - Status enriquecido: `{ running, processed, startedAt, finishedAt, durationMs }` em `GET /utilidades/processamento-status`.
   - Persistir “última execução” para exibir no front.
   - Enviar notificação de conclusão com `processedCount` e `durationMs`.
3. Notificações:
   - Tipos distintos: `cnpj_processing_start`, `cnpj_processing_complete`.
   - Ícones dedicados, prioridade configurável, `actionUrl` para abrir a página.
   - Agrupar/descartar duplicatas antigas (auto-expiração) e evitar spam de progresso parcial.

## Segurança e Regras de Acesso
- Restringir iniciar processamento a perfis (`ADMIN`/`GERENTE`) via `@PreAuthorize`.
- Auditar quem iniciou, quando e quantos foram processados.

## Verificação
- Iniciar processamento; ver toast de início na página atual.
- Navegar pelo ERP; ao concluir, toast aparece sem abrir o sino.
- Painel de notificações mostra “Processamento de CNPJs” com “De: Sistema” (ou usuário correto), data formatada, ação para abrir a tela.

## Implementação Pontos-Chave
- `static/js/websocket-notifications.js`: garantir `showToast` nativo e fallback seguro.
- `service/NotificationService.java`: preencher `actionUrl` e `metadata`; usar `sendGlobalNotification`/`sendNotificationToUser` conforme o caso.
- `controller/utilidades/ProcessamentoCnpjController.java`: manter email do usuário iniciador.
- `service/cnpj/ProcessamentoCnpjService.java`: enriquecer status e metadados para notificação.

## Próximos Passos
- Aplicar as correções e polimentos acima.
- Testar em páginas diferentes do ERP, com e sem painel de notificações aberto.
- Ajustar UI/UX conforme seu feedback (cores, animações, sons, tempos).