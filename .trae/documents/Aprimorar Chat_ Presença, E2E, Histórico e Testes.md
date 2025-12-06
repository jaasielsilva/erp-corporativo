## Objetivo
Entregar presença visual (online/offline), criptografia ponta‑a‑ponta completa no cliente, histórico com paginação e busca, e testes unitários/integrados para STOMP e REST.

## Presença na UI
- Backend:
  - Publicar eventos ricos em `/topic/presence`: `{userId, nome, status, lastHeartbeat}`.
  - REST `GET /api/chat/presence/online` para bootstrap inicial.
- Frontend:
  - `PresenceBadge.vue` e `PresenceList.vue` mostram usuários online com atualização em tempo real.
  - Store Pinia mantém mapa `{userId -> status}` e tempo.
- Aceitação:
  - Alterações de presença refletem em ≤500ms; lista atualiza automaticamente.

## E2E no Cliente (X25519 + AES‑GCM)
- Backend:
  - `GET /api/chat/keys/public/{userId}` retorna chave pública assinada.
  - `POST /api/chat/keys/public` publica/atualiza a chave pública do cliente autenticado.
- Frontend:
  - `useE2E.ts`: gerar par X25519, publicar chave pública; obter chave do interlocutor; derivar `sharedKey` (HKDF) e `aesKey` por conversa.
  - Cifrar `plaintext` com `AES‑GCM(iv aleatório, aad metadados)` antes de enviar; decifrar ao receber.
  - Persistir apenas ciphertext no servidor; o cliente mantém cache efêmero das chaves derivadas.
- Aceitação:
  - Mensagens trafegam e são armazenadas somente como ciphertext; decifragem ocorre em cliente sem falhas.

## Histórico com Paginação e Busca
- Backend:
  - `GET /api/chat/history?conversationId&Page&size` retorna página com `{ciphertext, iv, aad, senderId, sentAt}`.
  - `GET /api/chat/conversations/{id}/participants` e `/api/chat/search?term` (metadados) opcional.
- Frontend:
  - `HistoryPane.vue` lista mensagens com paginação infinita; decifra ao carregar.
  - Busca por termo: executa client‑side nos plaintexts já decifrados e/ou server‑side por metadados.
- Aceitação:
  - Scroll infinito carrega páginas sem bloquear; busca retorna resultados coerentes.

## Testes
- Unitários Backend:
  - `ChatMessageService` paginação e persistência.
  - `ChatSessionService` presença e heartbeats.
- Integração Backend:
  - STOMP com `StompClient`: envio/recebimento, autorização por conversa, presença.
  - REST histórico e chaves públicas.
- Unitários Frontend:
  - `useE2E.ts` cifragem/decifragem com vetores de teste.
  - Stores Pinia: presença e mensagens.
- Integração Frontend:
  - Simular conexão STOMP e fluxo básico de conversa com cifragem.
- Aceitação:
  - Round‑trip médio <500ms em ambiente dev; testes passam.

## Entregáveis
- Atualizações de código backend (controllers/serviços/REST) e frontend (Vue components/hooks).
- Documentação de segurança E2E e fluxos de presença.
- Guia de uso de histórico e busca.

## Passos
1. Expor REST de presença e chave pública; enriquecer eventos STOMP de presença.
2. Implementar `useE2E.ts` completo com X25519, HKDF e AES‑GCM.
3. Adaptar envio/recebimento para ciphertext; ajustar modelo `ChatMessage` se necessário (tipos binários base64).
4. Implementar histórico com scroll, paginação e busca.
5. Escrever testes unitários e de integração; medir latência.
6. Atualizar documentação e guias.

Confirma proceder com estas implementações e publicar as atualizações para teste?