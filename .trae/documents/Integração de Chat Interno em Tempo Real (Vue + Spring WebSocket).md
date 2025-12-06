## Objetivo
Entregar um chat interno em tempo real, responsivo e profissional, com presença online/offline, notificações e histórico (opcional), mantendo baixa latência (<500ms), segurança (E2E) e testes.

## Arquitetura
- **Frontend (Vue 3 + Vite)**:
  - SPA de chat embutida em página existente (montada em um container Thymeleaf).
  - Componentes: Lista de conversas, janela de chat, presença, busca, notificações.
  - WebSocket via STOMP para canalização de mensagens e presença.
- **Backend (Spring Boot)**:
  - WebSocketConfig (STOMP) com endpoint `/ws` e broker simples (`/topic`, `/queue`).
  - Serviços: `ChatSessionService` (presença e sessões), `ChatMessageService` (envio, recebimento, histórico), `ChatEncryptionService` (chaves e E2E helpers).
  - Controllers: REST (`/api/chat/*`) para histórico/pesquisa e Handlers STOMP (`/app/chat/*`) para mensagem/presença.
  - Persistência (opcional): tabela `chat_messages` (ciphertext + metadados), `chat_conversations`, `chat_presence`.
- **Segurança**:
  - Autenticação via sessão (principal do Spring Security) com interceptors WebSocket.
  - E2E: troca de chaves públicas (Curve25519/X25519) e cifragem simétrica (AES‑GCM) em cliente; servidor armazena apenas ciphertext.

## Fluxo de Trabalho
1. **Login**: usuário autenticado → carrega SPA e inicializa STOMP.
2. **Presença**: cliente envia `JOIN`, servidor publica `/topic/presence` com usuários online; heartbeats mantêm sessão.
3. **Mensagens**:
   - Cliente cifra (AES‑GCM) com chave derivada E2E e envia para `/app/chat/send`.
   - Servidor repassa (`/topic/chat/{conversationId}`) sem acesso ao plaintext.
   - Notificações locais e badges ao receber.
4. **Histórico (opcional)**:
   - Cliente requisita `/api/chat/history?conversationId=...&page=...` → retorna ciphertext + IV + AAD; cliente decifra.

## Componentes Técnicos
- **WebSockets**: STOMP (SockJS opcional) para fallback; canais:
  - `/app/chat/send` (envio), `/topic/chat/{conversationId}` (recebimento).
  - `/app/presence/join|leave`, `/topic/presence` (presença).
- **Backend**:
  - `ChatMessage` (id, conversationId, senderId, ciphertext, iv, aad, ts).
  - `ChatConversation` (id, participants[], lastMessageAt, tipo: direct/group).
  - `ChatPresence` (userId, status: online/offline, lastHeartbeat).
  - `ChatController` (REST histórico/busca), `ChatWsController` (STOMP handlers).
- **Frontend (Vue)**:
  - `ChatApp.vue` (layout geral), `ConversationList.vue`, `ChatWindow.vue`, `PresenceBadge.vue`, `SearchBar.vue`.
  - `useStomp.ts` (conexão), `useE2E.ts` (chaves, cifragem AES‑GCM), `useStore.ts` (Pinia para estado).

## E2E Criptografia
- **Geração de chaves**: cada usuário possui par de chaves (X25519) gerados pelo cliente; publica a chave pública assinada (verificável via servidor com certificado do usuário).
- **Acordo de chaves**: para conversa, derivar `sharedKey = DH(privateSelf, publicPeer)` e aplicar HKDF para `aesKey` e `nonce base`.
- **Cifragem**: `AES‑GCM(plaintext, iv, aad)` por mensagem; armazenar `ciphertext`, `iv`, `aad`.
- **Rotação**: opcionalmente implementar Double Ratchet para maior forward secrecy.

## Qualidade e Desempenho
- **Baixa Latência**: STOMP/WebSocket, serialização leve (JSON), compressão opcional; thread pool ajustado e Hikari.
- **Testes**:
  - Unitários: serviços de presença, histórico, cifragem (Web Crypto simulado); validação de chave pública.
  - Integração: MockMvc + StompClient para envio/recebimento, autenticação de sessão e tópicos.

## Entregáveis
- **Diagrama de Arquitetura**:
  - Descreve cliente Vue ↔ STOMP `/ws` ↔ Handlers STOMP ↔ broker ↔ assinantes, e REST histórico.
- **Protótipo Funcional**:
  - Conversa 1‑a‑1 com presença, envio/recebimento em tempo real, notificações locais, histórico (opcional) cifrado.
- **Documentação Técnica**:
  - Endpoints STOMP e REST, modelo de dados, segurança E2E, fluxos de presença, eventos.
- **Guia de Implantação**:
  - Build (Vite + Maven), propriedades (`server`, `ws`, segurança), migrações de banco (tabelas de chat), scaling (broker, sticky sessions), observabilidade.

## Passos de Implementação
1. **Backend WebSocket**: configurar STOMP (`/ws`), destinos (`/app`, `/topic`, `/queue`), interceptors de autenticação, handlers para `send`, `presence`.
2. **Modelos e Repositórios**: entidades `ChatMessage`, `ChatConversation`, `ChatPresence`; serviços para persistência e presença.
3. **Frontend Vue**: setup Vite, componentes principais e estados; conexão STOMP; UI responsiva.
4. **Criptografia**: módulo E2E no cliente, publicação de chaves, derivação e cifragem; REST para recuperar chaves públicas.
5. **Histórico/Busca**: REST de histórico (paginado) e busca por participante/termos (metadados); cliente decifra após carregar.
6. **Notificações**: integração com toasts globais e badges; eventos de nova mensagem/presença.
7. **Testes**: unitários e integração; cenários de latência e reconexão.
8. **Implantação**: pipeline de build; properties; migrações; instruções de scaling e monitoramento.

## Observações
- Evitar acesso do servidor ao plaintext; todas mensagens cifradas no cliente.
- Tratar reconexões e perda de sessões com retentativas e re‑subscription.
- Opcional: grupos, threads, anexos com criptografia (upload cifrado, metadados seguros).

Confirma seguir com essa implementação (Vue + STOMP), iniciando pela configuração WebSocket e protótipo de UI com presença e troca de mensagens cifradas?