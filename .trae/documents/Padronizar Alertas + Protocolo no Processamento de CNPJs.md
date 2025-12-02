## Objetivo
Padronizar os alertas em todo o ERP e aplicar o mesmo estilo de sucesso com número de protocolo (como em Solicitações) para o processamento de CNPJs.

## Melhorias de Notificações
- Unificar o componente de toast (DOM nativo, sem jQuery), com visual consistente usando `notifications.css`.
- Corrigir “De: undefined” e “Invalid Date”: fallback ‘Sistema’ quando global e formatação `pt-BR` para timestamps.
- Incluir `actionUrl` e `metadata` (ex.: `{ protocol, processedCount, durationMs }`) em notificações de início e conclusão.

## Protocolo no Processamento
- Gerar um protocolo único por execução (ex.: `PROC` + epoch) no `ProcessamentoCnpjService`.
- Notificar início: “Processamento de CNPJs — Iniciado! Protocolo: PROC…”.
- Notificar conclusão: “Concluído (N) — Protocolo: PROC…”.
- Retornar JSON no `POST /utilidades/processar` com o `protocol` para exibir alert imediato na página.

## UI da Página de Utilidades
- Exibir o alert padronizado de sucesso com o protocolo ao iniciar.
- Manter barra de progresso, detalhes (início/fim/duração) e controles (concorrência, pausar/retomar/cancelar).

## Implementação
- Backend: gerar protocolo, enriquecer `Notification`, alterar resposta do `POST /utilidades/processar` para JSON.
- Frontend: atualizar `websocket-notifications.js` para padronizar toasts e `cnpj-processar.js` para consumir o `protocol` e exibir o alert.

## Verificação
- Iniciar processamento: exibe “Iniciado — Protocolo: …” sem abrir o sino.
- Concluir: aparece “Concluído (N) — Protocolo: …” em qualquer página.
- Painel de notificações mostra remetente correto, datas formatadas e link “Abrir”.

## Observações
- Sem alterar regras de negócio; foco em UX e consistência de alertas.
- Depois, opcional: centralizar `showToast` em `notifications.js` e aplicar gradualmente em outras páginas.