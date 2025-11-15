## Objetivo

Remover completamente o módulo de Chat atual e implantar um Chat novo, padronizado e estável, com mensagens em tempo real, anexos e grupos.

## Remoção Segura

* Apagar back-end atual: controllers (ChatController, ChatRestController, ChatWebSocketController, ReacaoMensagemController), services (ChatService, ReacaoMensagemService), models (Conversa, Mensagem, ParticipanteConversa, ReacaoMensagem), repositories correlatos e DTOs.

* Apagar front-end: templates em `templates/chat/*`, JS `static/js/chat.js`, `static/js/departamentos-chat.js`, CSS relacionados.

* Garantir build após remoção.

## Novo Chat Padronizado

* Modelos JPA:

  * ChatRoom: id, nome, tipo (DIRECT, GROUP), criadoPor, criadoEm, ativo

  * ChatMembership: id, usuario, room, ativo, ultimaVisualizacao, adicionadoEm

  * ChatMessage: id, room, sender, conteudo, tipo (TEXT, IMAGE, FILE), enviadoEm, lida, arquivoUrl/nome/tamanho

* Repositórios:

  * ChatRoomRepository, ChatMembershipRepository, ChatMessageRepository com consultas para listar salas do usuário e mensagens ordenadas

* Serviço:

  * ChatNewService: criar sala direta/grupo; listar salas/mensagens; enviar mensagem texto/anexo; marcar lidas; broadcast para `/topic/chat.room.{id}` e fila privada do usuário

* Controllers:

  * ChatPageController: render `/chat` com contexto do usuário e título

  * ChatApiController: `GET /api/chat/rooms`, `POST /api/chat/rooms/direct`, `POST /api/chat/rooms/group`, `GET /api/chat/rooms/{id}/messages`, `POST /api/chat/rooms/{id}/messages`, `POST /api/chat/rooms/{id}/messages/upload`, `PUT /api/chat/rooms/{id}/read`

* WebSocketConfig: reutilizar existente `/ws`; sem mudanças

## Front-end

* Templates:

  * `templates/chat/index.html` novo, usando Bootstrap CDN e padrão corporativo, Sidebar/Topbar

* JS:    

  * `static/js/chat-new.js`: conexão STOMP, subscrição em `/topic/chat.room.{id}`, lista salas via REST, envio por REST, renderização live

## Validação

* `mvn -DskipTests package` e `spring-boot:run`

* Testes: criar sala direta, enviar texto/anexo, receber em tempo real; grupos; marcar lidas; sem erros de console

## Observações

* Tabelas antigas podem permanecer; se desejar, preparo migração Flyway posterior

Confirmando, inicio a remoção e criação do Chat novo padronizado agora.
