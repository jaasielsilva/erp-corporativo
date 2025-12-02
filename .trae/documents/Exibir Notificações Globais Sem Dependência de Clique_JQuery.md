## Objetivo
Fazer as notificações globais aparecerem automaticamente em qualquer página, sem depender de clique no sino/menu e sem depender de carregamento prévio do jQuery.

## Estratégia
- Alterar `showToast` em `websocket-notifications.js` para usar DOM nativo ao invés de jQuery, garantindo exibição imediata.
- Manter a assinatura atual e integração com o WebSocket existente; nenhuma mudança nos endpoints.

## Implementação
- Substituir uso de `$` e `$('body').append(...)` por `document.createElement`, `appendChild`, `classList.add/remove` e `setTimeout`.
- Preservar classes atuais (priority, ícone, auto fechamento) para manter o visual.

## Verificação
- Enviar notificação global: aparece automaticamente sem clique.
- Enviar notificação por usuário: idem.
- Testar em páginas sem jQuery: a toast aparece normalmente.

## Observações
- Não altera fluxo de negócio nem segurança; apenas elimina dependência de jQuery para exibir toasts.