## Visão Geral
Implementar chat interno em tempo real usando Vue 3 (Vite) no frontend e Spring WebSocket (STOMP) no backend, com presença, notificações, histórico opcional e criptografia ponta‑a‑ponta.

## Backend (Spring Boot)
### Configuração WebSocket
- Criar `WebSocketConfig`:
  - Endpoint `"/ws"` com STOMP e SockJS opcional.
  - Prefixos: `"/app"` (envio) e `"/topic"`, `"/queue"` (assinar).
  - Interceptor de autenticação: extrai `Principal` da sessão e injeta no `StompHeaderAccessor`.

### Handlers STOMP
- `ChatWsController`:
  - `@MessageMapping("/chat/send")` → valida participantes da conversa e publica em `"/topic/chat/{conversationId}"`.
  - `@MessageMapping("/presence/join")` / `@MessageMapping("/presence/leave")` → atualiza presença e publica em `"/topic/presence"`.

### REST
- `ChatController`:
  - `GET /api/chat/history?conversationId={id}&page={n}` → retorna ciphertext, iv e metadados.
  - `GET /api/chat/keys/public/{userId}` → retorna chave pública assinada para E2E.

### Modelo/Persistência
- Entidades:
  - `ChatMessage(id, conversationId, senderId, ciphertext, iv, aad, sentAt)`.
  - `ChatConversation(id, type, createdAt)` + tabela associativa `chat_conversation_participants`.
  - `ChatPresence(userId, status, lastHeartbeat)`.
- Repositórios JPA básicos e serviços (`ChatMessageService`, `ChatSessionService`).
- DDL:
  - Tabelas `chat_messages`, `chat_conversations`, `chat_conversation_participants`, `chat_presence` com índices por `conversationId` e `sentAt`.

### Segurança
- Autenticação via sessão existente; restrição de envio/assinatura por conversa.
- E2E: servidor não acessa plaintext; armazena somente ciphertext + metadados.

### Propriedades
- `spring.websocket.enabled=true` e tuning de thread‑pools.

### Testes
- Unitários: presença, histórico.
- Integração: MockMvc + StompClient (envio/recebimento, auth); REST histórico.

## Frontend (Vue 3 + Vite)
### Setup
- Projeto em `src/main/resources/static/chat/` ou build externo com assets servidos via Thymeleaf.
- Pinia para estado; Vue Router (opcional) para navegação interna.

### Conexão STOMP
- `useStomp.ts`: conectar ao `"/ws"`, subscribes `"/topic/chat/{id}"` e `"/topic/presence"`, reconexão com backoff.

### E2E Criptografia
- `useE2E.ts`: chaves X25519 por usuário; derivação HKDF para `aesKey`; cifragem AES‑GCM; consulta de chaves públicas.

### Componentes
- `ChatApp.vue`: layout geral.
- `ConversationList.vue`: lista conversas, busca, contadores de não lidas.
- `ChatWindow.vue`: janela de mensagens, input, anexos (opcional).
- `PresenceBadge.vue`: mostra online/offline.
- `SearchBar.vue`: filtro por participante/termo.

### Integração UI
- Montar SPA em container Thymeleaf; link na sidebar para “Chat”.
- Notificações: toasts ao receber mensagens fora da conversa ativa.

### Testes
- Unit: stores, E2E utilitários.
- Integração: simulação de envio/recebimento com servidor dev.

## Histórico (Opcional)
- Paginação por data/quantidade; REST retorna ciphertext; cliente decifra.

## Desempenho & Resiliência
- Latência alvo < 500ms; compressão opcional.
- Reconexão STOMP, heartbeats; presença consistente.

## Entregáveis
- Diagrama de arquitetura (cliente ↔ `/ws` ↔ handlers ↔ broker ↔ assinantes; REST histórico/keys).
- Protótipo funcional (conversa 1‑a‑1, presença, notificações, histórico opcional).
- Documentação técnica (endpoints STOMP/REST, modelos, segurança E2E, eventos).
- Guia de implantação (propriedades, DDL, build, scaling e observabilidade).

## Passos de Implementação
1) Criar `WebSocketConfig` e `ChatWsController` com destinos e validação de auth.
2) Implementar entidades, repositórios e serviços (mensagens/presença).
3) Subir `ChatController` REST (histórico, chaves públicas).
4) Criar SPA Vue: `useStomp.ts`, `useE2E.ts`, componentes e store.
5) Integrar UI: página “Chat” e link na sidebar; toasts.
6) Escrever testes unitários e de integração; medir latência.
7) Entregar diagrama, docs e guia de deploy.

Confirma prosseguir com a implementação conforme os passos acima (criando classes, endpoints e SPA), mantendo compatibilidade com a segurança e infraestrutura existentes?