## Objetivo
Executar testes ponta a ponta do Chat Interno (login, conversas, mensagens, anexos, reações, presença e departamentos) e validar persistência no MySQL, usando a conta master@sistema.com.

## Pré-requisitos
- Servidor iniciado com perfil `dev` e conexão MySQL (configuração em `src/main/resources/application.properties`).
- Usuário `master@sistema.com` com senha `master123` existente.

## Fluxo de Testes
1. Autenticação
- Acessar `/login` (ou a raiz `/`) e autenticar com `master@sistema.com` / `master123`.
- Verificar que os interceptors não bloqueiam recursos do chat (`WebSecurityConfig` permite `/ws` e está atrás de validação de sessão apenas para outras rotas).

2. Chat Principal (`/chat`)
- Navegar para `/chat` e confirmar que o cabeçalho mostra “Chat Interno” e a UI carrega.
- Checar que `window.CHAT_CONTEXT` está presente com `usuarioId` e `usuarioNome` (renderizados pelo `ChatController`).
- Validar a conexão WebSocket (`/ws` via SockJS/STOMP): status muda para “Online” e há subscrições em `/user/queue/mensagens`, `/user/queue/errors`, `/topic/usuarios.status`.

3. Conversa Individual
- Usar “Nova Conversa” e pesquisar usuários em `GET /api/usuarios/busca?q=`.
- Iniciar uma conversa com um usuário retornado via `POST /api/chat/conversas/individual?destinatarioId={id}`.
- Enviar mensagem de texto: `POST /api/chat/conversas/{id}/mensagens` com `conteudo`.
- Validar recebimento na UI e via REST: `GET /api/chat/conversas/{id}/mensagens`.
- Verificar não lidas: `PUT /api/chat/conversas/{id}/marcar-lidas` (marca como lida).

4. Anexos e Imagens
- Enviar arquivo/imagem via `POST /api/chat/conversas/{id}/mensagens/upload` (multipart `arquivo` + `conteudo` opcional).
- Confirmar que o arquivo foi salvo em `uploads/chat/` e que a UI mostra preview.

5. Reações
- Adicionar/remover reação em uma mensagem: `POST /api/chat/reacoes/{mensagemId}` com `{ emoji }`.
- Verificar listagem de reações: `GET /api/chat/reacoes/{mensagemId}`.

6. Presença e Digitação
- Checar eventos em `/topic/conversa.{id}` e `/topic/chat.conversa.{id}.digitando` ao digitar/parar digitar.
- Confirmar que o cliente publica `/app/chat.online` ao conectar e `/app/chat.offline` ao sair.

7. Conversa em Grupo
- Criar grupo: `POST /api/chat/conversas/grupo` com `{ titulo, participantes:[ids] }`.
- Enviar mensagens e validar reações e não lidas.

8. Conversa por Departamento (`/chat/departamentos`)
- Abrir `/chat/departamentos` e listar departamentos via `GET /api/chat/departamentos`.
- Criar/abrir conversa do departamento: `POST /api/chat/departamentos/{id}/conversa`.
- Enviar mensagem, verificar mensagens e participantes ativos.

## Validação no MySQL (apenas leitura)
- Conferir persistência (após enviar mensagens):
  - `SELECT * FROM conversas ORDER BY id DESC;`
  - `SELECT * FROM participantes_conversa WHERE conversa_id = {id};`
  - `SELECT * FROM mensagens WHERE conversa_id = {id} ORDER BY data_envio;`
- Checar colunas de anexos em `mensagens` (`arquivo_url`, `arquivo_nome`, `arquivo_tamanho`).
- Confirmar atualização de `ultima_atividade` em `conversas`.

## Critérios de Aceite
- Login funciona com a conta informada.
- Conversas (individual/grupo/departamento) criam registros e listam corretamente.
- Mensagens de texto e anexos são exibidos e persistidos.
- Reações funcionam e são refletidas na UI.
- Presença e digitação aparecem nos tópicos corretos.
- Sem erros em `/user/queue/errors` durante o fluxo.

## Observações de Segurança
- Acesso ao chat é restrito a usuários autenticados; mensagens só para participantes.
- uploads em `uploads/chat/` (sanitização básica); manter limites de upload.

Aprova que eu execute esses testes com login `master@sistema.com` e valide no MySQL conforme o plano?