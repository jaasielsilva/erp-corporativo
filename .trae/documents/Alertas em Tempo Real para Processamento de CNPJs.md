## Objetivo
Mostrar alertas enquanto o usuário está na tela de processamento (via badge/toast local) e um alerta de conclusão em qualquer tela onde o usuário estiver logado (via WebSocket notificação direcionada).

## Estratégia
- Manter progresso na página via polling (já implementado) e toasts locais.
- Enviar notificação em tempo real (WebSocket) ao usuário que iniciou o processo assim que concluir — aparece como toast em qualquer página do ERP (topbar carrega o cliente WS global).

## Implementação
1. Controller: capturar o email do usuário autenticado (SecurityContext) e passar para o service.
2. Service: aceitar o email do usuário; usar `NotificationService` para:
   - Notificar início (opcional, prioridade baixa).
   - Notificar conclusão com contagem processada.
3. Payload: usar `NotificationService.createNotification(type, title, message, priority, user)` que já integra com `RealtimeNotificationService` para enviar via `/queue/notifications/{email}`.

## Verificação
- Iniciar processamento na tela; status local mostra progresso.
- Navegar para outras páginas enquanto processa; ao finalizar, o toast “Processamento de CNPJs concluído (N)” aparece automaticamente onde o usuário estiver.

## Observações
- Evitar spam de notificação de progresso (sem push por item); usar só a notificação de conclusão para o usuário iniciador.
- Posteriormente podemos adicionar perfil/segurança para restringir quem inicia o processamento, se necessário.