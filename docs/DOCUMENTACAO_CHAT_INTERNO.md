# Chat Interno Empresarial – Manual de Uso e Operação

## Objetivo
- Facilitar a comunicação entre colaboradores autenticados, com conversas individuais, em grupo e por departamento, em tempo real, com segurança corporativa.

## Acesso
- Rota: `/chat` (Chat principal) e `/chat/departamentos` (Chat por departamentos)
- Requer usuário logado. O acesso às conversas é restrito aos participantes.

## Principais Funcionalidades
- Conversas:
  - Individual: entre dois usuários.
  - Grupo: múltiplos participantes com criador/admin.
  - Departamento: conversa associada ao departamento (participantes do departamento).
- Mensagens:
  - Texto, Imagem e Arquivo (PDF, Doc, Planilha, etc.).
  - Reações (👍 ❤️ 🎉 🔥) por mensagem.
  - Marcação de lida automática ao abrir a conversa.
- Tempo real:
  - Indicador de digitação.
  - Presença (online/offline) dos usuários.
  - Notificações push para participantes via WebSocket.

## Como Usar (Telas)
- `/chat`:
  - Nova conversa: botão “Nova Conversa”. Pesquise um usuário e clique em “Iniciar Conversa”.
  - Novo grupo: “Novo Grupo”. Informe título, selecione participantes e crie.
  - Enviar mensagem: digite e pressione `Enter` (ou use o botão enviar). `Shift+Enter` quebra linha.
  - Anexos: clique no clip e selecione o arquivo. Imagens têm visualização ampliada.
  - Reações: clique em um emoji na mensagem para adicionar/remover sua reação.
- `/chat/departamentos`:
  - Selecione um departamento na lista para entrar na conversa.

## APIs REST (principais)
- `GET /api/chat/conversas`: lista conversas do usuário.
- `POST /api/chat/conversas/individual?destinatarioId={id}`: cria conversa individual.
- `POST /api/chat/conversas/grupo`: `{ titulo, participantes: [ids] }` cria grupo.
- `GET /api/chat/conversas/{id}`: detalhes da conversa.
- `GET /api/chat/conversas/{id}/mensagens`: mensagens da conversa.
- `POST /api/chat/conversas/{id}/mensagens`: `conteudo` para enviar texto.
- `POST /api/chat/conversas/{id}/mensagens/upload`: multipart `arquivo` (+ `conteudo` opcional).
- `PUT /api/chat/conversas/{id}/marcar-lidas`: marca mensagens como lidas.
- Reações:
  - `POST /api/chat/reacoes/{mensagemId}` body `{ emoji }` adiciona/remove.
  - `GET /api/chat/reacoes/{mensagemId}`: lista reações.
- Usuários:
  - `GET /api/usuarios/busca?q=termo`: busca usuários ativos (exclui o logado).
  - `GET /api/chat/usuarios/online`: usuários online.
- Departamentos:
  - `GET /api/chat/departamentos`: lista com estatísticas.
  - `POST /api/chat/departamentos/{id}/conversa`: cria ou retorna conversa do departamento.

## WebSocket (STOMP)
- Endpoint: `/ws` (SockJS)
- Subscrições:
  - `/user/queue/mensagens`: mensagens específicas ao usuário.
  - `/user/queue/errors`: erros direcionados.
  - `/topic/usuarios.status`: broadcast de presença.
  - `/topic/conversa.{id}`: eventos da conversa (digitação, lida, notificações).
  - `/topic/chat.conversa.{id}.digitando`: indicador de digitação.
- Publicações (cliente):
  - `/app/chat.enviarMensagem` `{ conversaId, conteudo }`
  - `/app/chat.digitando` `{ conversaId }`
  - `/app/chat.pararDigitar` `{ conversaId }`
  - `/app/chat.online` e `/app/chat.offline` `{}`

## Segurança e Privacidade
- Autenticação obrigatória.
- Autorização: apenas participantes podem acessar mensagens/conversa.
- Anexos salvos em `uploads/chat/` com nome sanitizado; evitar dados sensíveis.
- Logs de eventos críticos (erros WS, upload) são feitos no servidor.

## Limites e Boas Práticas
- Tamanho máximo de arquivo recomendado: 10MB (configurável).
- Extensões permitidas: imagens comuns, PDF, documentos Office e texto.
- Uso responsável: evite enviar informações confidenciais sem criptografia.

## Operação
- Presença: o cliente anuncia online ao conectar e offline ao fechar a janela.
- Retenção (opcional): politica de limpeza pode ser adicionada futuramente.
- Monitoramento: health em `GET /api/chat/health`.

## Suporte
- Em caso de falhas, consulte os logs do servidor e o canal `/user/queue/errors` no cliente para entender erros de WS/REST.