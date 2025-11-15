## Visão e Objetivo
- Ativar e finalizar o Chat Interno entre colaboradores autenticados, aproveitando sua base já existente (REST + WebSocket + Templates), e publicar documentação empresarial de uso e operação.

## Diagnóstico da Base Atual
- Back-end: REST e WebSocket já implementados
  - REST: `ChatRestController` com conversas, mensagens, anexos, participantes, departamentos (`src/main/java/com/jaasielsilva/portalceo/controller/ChatRestController.java`).
  - WebSocket: STOMP endpoint `/ws` e broker `/topic` e `/queue` (`src/main/java/com/jaasielsilva/portalceo/config/WebSocketConfig.java`).
  - WS Controller: `ChatWebSocketController` com enviar mensagem, digitação, presença e eventos de conversa (`src/main/java/com/jaasielsilva/portalceo/controller/ChatWebSocketController.java`).
  - Service: `ChatService` com lógica completa (notificações, anexos, participantes, leitura) (`src/main/java/com/jaasielsilva/portalceo/service/ChatService.java`).
  - Reações: `ReacaoMensagemController` + `ReacaoMensagemService` funcionando (`src/main/java/...`).
- Front-end:
  - Templates: `templates/chat/index.html` e `templates/chat/departamentos.html`.
  - JS/CSS: `static/js/chat.js` e `static/js/departamentos-chat.js`, `static/css/chat.css`.
- Pontos a ajustar:
  - `chat/index.html` injeta `window.CHAT_CONTEXT` com `usuarioId`/`usuarioNome`, mas o `ChatController` não popula o `Model` (faltam atributos).
  - `chat.js` usa `messagesContainer` não definido (deve usar `messagesArea`), e endpoint de busca de usuários `/api/usuarios/busca` está correto; o de buscar usuários via chat também existe em `/api/chat/usuarios/buscar` (vamos unificar o uso).
  - Padronização visual (Bootstrap/FA local) para seguir padrão corporativo.

## Escopo Funcional do Chat
- Conversas:
  - Individual, Grupo e por Departamento, com criação, listagem e busca.
- Mensagens:
  - Texto, imagem e arquivo, com reações, leitura e indicadores de digitação.
- Presença:
  - Usuários online/offline com broadcast de status.
- Segurança:
  - Apenas usuários logados; acesso a conversa restrito a participantes; anexos em `uploads/chat/` com sanitização básica.

## Implementações Planejadas
1. Conectar contexto de usuário ao template
- Atualizar `ChatController` para adicionar `usuarioId` e `usuarioNome` no `Model` ao renderizar `chat/index`.
- Garantir proteção de rota `/chat` via security já existente.

2. Padronizar dependências front-end
- Atualizar `chat/index.html` para usar `/css/bootstrap.min.css` local e `Font Awesome 6.4.0`, mantendo o padrão corporativo.

3. Correções e melhorias em `chat.js`
- Corrigir variável `messagesContainer` para `messagesArea` no upload.
- Unificar busca de usuários: manter `/api/usuarios/busca?q=` para nova conversa individual e `/api/chat/usuarios/buscar?termo=` opcional; ajustaremos para usar apenas um deles.
- Assinar/desassinar tópicos corretamente ao trocar de conversa; publicar `online/offline` no connect/disconnect (`/app/chat.online`, `/app/chat.offline`).
- Melhorar UX: feedback de erro em `queue/errors`, indicador de conexão, e contador de não lidas na lista.

4. Departamentos
- Validar `departamentos.html` e `departamentos-chat.js` com endpoints de `ChatRestController` (`/api/chat/departamentos` e `/api/chat/departamentos/{id}/conversa`).

5. Segurança e limites
- Validar size-limit de upload, extensões permitidas, sanitização de nomes; manter uploads em `uploads/chat/` com controle de acesso.
- Garantir que apenas participantes consumam `/topic/conversa.{id}` (já há checagem no servidor).

6. Documentação Empresarial
- Criar `docs/DOCUMENTACAO_CHAT_INTERNO.md` com:
  - Visão geral e objetivos; papéis e permissões.
  - Fluxos: individual, grupo e departamento; anexos; reações; digitação; presença.
  - APIs REST e rotas WebSocket: exemplos de uso.
  - Operação: limites de tamanho, retenção, privacidade, auditoria básica.
  - Uso das telas: como iniciar conversa, criar grupo, anexar, reagir, marcar lidas.

## Entregáveis
- Chat funcional e integrado ao usuário logado.
- Templates padronizados e UX consistente.
- Documentação completa de uso e operação.

## Validação
- Build (`mvn -DskipTests package`), navegação `/chat`, testes de fluxo: criar conversa, enviar texto, anexar imagem/arquivo, digitação visível, presença e reações.

## Timeline
- Dia 1: ajustes back/front mínimos (controller, template, JS correções) + validação.
- Dia 2: documentação e refinamentos (presença e departamentos).

## Observações
- Não vamos alterar a modelagem de bancos neste passo; anexos permanecem no filesystem.
- Se desejar políticas de retenção/privacidade específicas, incluímos seções de configuração no documento e, em etapa posterior, criamos jobs de limpeza e flags de confidencialidade.

Confirma a execução do plano para eu implementar e publicar a documentação?