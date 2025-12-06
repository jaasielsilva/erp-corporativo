## Objetivo
Eliminar duplicatas e itens inválidos na central de notificações, corrigindo "De: undefined" e "Invalid Date" sem perder alertas úteis.

## Abordagem
- Normalizar todas as notificações (system e legacy) para um formato único com campos seguros: id, title, message, type, timestamp (Date válido), remetenteNome, actionUrl, metadata, read, priority.
- Dedupe por protocolo quando existir (chave: `${type}:${protocol}`) mantendo a mais recente.
- Dedupe por id para itens sem protocolo; fallback por combinação `title+message` se id ausente.
- Ordenar por timestamp válido; exibir data apenas quando válida.
- Preservar contadores de não lidas.

## Escopo
- Atualizar `src/main/resources/static/js/notifications.js` dentro da função `loadNotifications()`.
- Sem mudanças obrigatórias no backend; o frontend passa a condensar notificações duplicadas (user vs global e início vs conclusão por protocolo).

Confirma prosseguir com esta correção no frontend?