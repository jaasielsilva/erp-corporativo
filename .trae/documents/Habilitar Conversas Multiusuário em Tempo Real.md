## Objetivo
Permitir conversas em tempo real entre usuários logados (1‑a‑1 inicialmente), com criação/listagem de conversas, assinatura automática do tópico e envio autenticado.

## Backend
- **Endpoints REST**
  - `POST /api/chat/conversations` → body `{ participants: [userIdA, userIdB] }` cria conversa e participantes; retorna `{ id }`.
  - `GET /api/chat/my-conversations` → lista conversas do usuário atual com últimos metadados (id, lastMessageAt, otherUserName).
  - `GET /api/chat/conversations/{id}/participants` → participantes para UI (nome, id).
- **STOMP segurança/autorizações**
  - No `ChatWsController.send`: usar `Authentication` para obter `senderId` (ignorar qualquer `senderId` do cliente); validar se `senderId` pertence à conversa.
  - Interceptar assinaturas (subscription) e validar se o usuário é participante antes de permitir `/topic/chat/{id}` (ChannelInterceptor ou checagem no controller).
- **Persistência**
  - Repositório de `ChatConversationParticipant` já criado: usar para validação e listagem.
  - Ajustar `ChatMessage` para preencher `senderId` com base no usuário autenticado.

## Frontend (Vue)
- **Lista de Conversas**
  - Popular via `GET /api/chat/my-conversations` ao carregar.
  - Ao abrir uma conversa, assinar `/topic/chat/{id}` e renderizar mensagens.
- **Nova Conversa**
  - Botão “Nova” abre modal de seleção de colega; após escolher, chama `POST /api/chat/conversations` e adiciona a conversa na lista.
  - Simples: campo de busca por nome/parte do email (REST auxiliar opcional) ou seleção por lista.
- **Envio de Mensagens**
  - Payload mantém `ciphertext/iv/aad`; servidor define `senderId` e valida.
- **Presença**
  - Aproveitar `/topic/presence`; exibir online/offline ao lado do colega na conversa.

## Testes
- **Integração Backend**
  - Criar conversa com dois usuários; enviar mensagem autenticada; listar conversas; validar autorização de subscription.
- **Integração Frontend**
  - Dois navegadores: criar conversa, abrir em ambos, enviar mensagens e observar recepção em tempo real.

## Entrega
- Endpoints REST e validação STOMP prontos.
- UI com lista “Conversas”, fluxo “Nova”, assinatura automática e troca em tempo real.
- Documentação breve de uso.

Confirma que eu implemente conforme acima e disponibilize para teste imediato?