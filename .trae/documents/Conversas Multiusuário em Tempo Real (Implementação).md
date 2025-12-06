## Objetivo
Habilitar conversas 1‑a‑1 em tempo real entre usuários logados, com criação/listagem de conversas, assinatura segura dos tópicos e envio autenticado, integradas à UI atual do chat.

## Backend
- Endpoints REST:
  - POST `/api/chat/conversations` → body `{ participants: [userIdA, userIdB] }` cria conversa e participantes; retorna `{ id }`.
  - GET `/api/chat/my-conversations` → lista conversas do usuário atual com `{ id, lastMessageAt, otherUser }`.
  - GET `/api/chat/conversations/{id}/participants` → retorna lista de participantes com `{ userId, nome }`.
- STOMP segurança:
  - Envio `/app/chat/send`: obter `senderId` de `Authentication` (ignorar o fornecido pelo cliente); validar que `senderId` participa da conversa antes de publicar em `/topic/chat/{id}`.
  - Assinatura `/topic/chat/{id}`: validar participação do usuário antes de permitir subscribe (ChannelInterceptor ou verificação no controller).
- Persistência:
  - Usar `ChatConversation`, `ChatConversationParticipant`, `ChatMessage`; preencher `senderId` no servidor.
  - Atualizar serviços para listar conversas por participante e último evento.

## Frontend (Vue)
- Lista de conversas:
  - Carregar via GET `/api/chat/my-conversations` ao iniciar; renderizar “Conversas” com usuário/última atividade.
  - Ao abrir, assinar `/topic/chat/{id}`, carregar histórico via GET `/api/chat/history?conversationId=...` e renderizar.
- Nova conversa:
  - Botão “Nova” abre modal de seleção de colega (busca por nome/email); ao confirmar, POST `/api/chat/conversations`, adicionar à lista e abrir.
- Envio:
  - Manter payload com `ciphertext/iv/aad`; servidor define `senderId` autenticado e valida participação.
- Presença:
  - Exibir online/offline ao lado do colega usando `/topic/presence`; opcional REST para bootstrap inicial.

## Testes
- Integração backend:
  - Criação de conversa; envio autenticado; assinatura autorizada; histórico.
- Integração frontend:
  - Dois usuários em navegadores distintos trocando mensagens em tempo real na mesma conversa.
- Critérios:
  - Latência média < 500ms; conversas acessíveis somente aos participantes; lista atualiza com novas conversas e última atividade.

## Entregáveis
- Código de endpoints e validações STOMP.
- UI com lista “Conversas” e fluxo “Nova” funcional.
- Documentação curta de uso e endpoints.

## Passos
1) Implementar endpoints REST de conversas e participantes.
2) Validar participação em envio/assinatura STOMP; usar `Authentication` para `senderId`.
3) Atualizar SPA para carregar conversas via REST, abrir e assinar tópico, além de fluxo “Nova”.
4) Criar testes end‑to‑end básicos e validar latência.

Confirma prosseguir com esta implementação agora?