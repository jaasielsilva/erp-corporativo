## Objetivo
Descrever e padronizar a UI do Chat Interno, com ênfase em seções claras, ajuda contextual (tooltips e painel de ajuda), cópias de placeholder e mensagens de sistema, acessibilidade, i18n, performance, analytics e critérios de validação.

## Escopo
- Página `Chat Interno` acessada em `/chat`.
- Recursos front-end em `src/main/resources/static/js/chat-app.js` e `src/main/resources/templates/chat/index.html`.
- Não aborda protocolo de criptografia/segurança de mensagens do back-end; foca experiência do usuário e instrumentação.

## Princípios de UX
- Clareza: rótulos diretos, textos de apoio curtos e objetivos.
- Consistência: padrões visuais e de comportamento repetíveis.
- Descoberta: ajuda contextual sempre disponível e tooltips discretos.
- Acessibilidade: navegação por teclado, ARIA onde aplicável, contraste adequado.
- Resiliência: estados vazios e erros tratados com cópias úteis.

## Arquitetura de Página
- `index.html` (cabeçalho e copy introdutória): `src/main/resources/templates/chat/index.html`.
- `chat-app.js` (componentização Vue 3 + Pinia): `src/main/resources/static/js/chat-app.js`.
  - `App` (container, painel de ajuda, layout em colunas).
  - `ConversationList` (lista, busca, ações "Nova").
  - `ChatWindow` (mensagens, input e placeholder quando nada selecionado).

## Seções
- Cabeçalho e copy
  - Título: `Chat Interno` com ícone `fa-comments`.
  - Parágrafo introdutório: “Esta é a área de Chat Interno para comunicação entre membros da equipe. Selecione uma conversa existente ou inicie uma nova.”
  - Local: `templates/chat/index.html` linhas 17–19.
- Lista de Conversas
  - Título: `Conversas` com tooltip `Lista de diálogos anteriores que você pode retomar`.
  - Ação: botão `Nova` com tooltip `Iniciar uma nova conversa com um ou mais colegas`.
  - Busca: input com placeholder `Buscar conversas específicas` e filtro por termo.
  - Item: `Conversa #<id>` com seta indicativa, clique abre a conversa e assina o tópico.
  - Local: `static/js/chat-app.js` linhas 71–102.
- Janela de Chat
  - Placeholder quando não há conversa ativa: `Selecione uma conversa para visualizar as mensagens ou inicie uma nova conversa`.
  - Lista de mensagens com rolagem (`max-height:360px;overflow:auto`).
  - Input com placeholder `Digite sua mensagem` e botão `Enviar` com ícone `fa-paper-plane`.
  - Local: `static/js/chat-app.js` linhas 43–69.
- Ajuda Contextual
  - Botão no topo direito com ícone `fa-question` que alterna painel.
  - Painel exibe guia rápido com 3 passos: selecionar conversa, criar nova, usar busca.
  - Local: `static/js/chat-app.js` linhas 104–130.

## Tooltips
- Uso de atributo `title` para explicar elementos-chave.
- Diretrizes:
  - Deve completar a intenção do elemento sem redundância com o rótulo.
  - Frases curtas, em voz ativa, até ~80 caracteres.
  - Evitar jargões; usar termos consistentes com cópia da interface.
- Elementos com tooltip previstos:
  - Título `Conversas`.
  - Botão `Nova`.
  - Ícone `?` (quando necessário, preferir painel de ajuda como explicação mais longa).

## Placeholders e Mensagens
- Placeholders:
  - Busca: `Buscar conversas específicas`.
  - Input de mensagem: `Digite sua mensagem`.
  - Janela sem conversa: `Selecione uma conversa para visualizar as mensagens ou inicie uma nova conversa`.
- Mensagens de sistema (próximos passos):
  - Envio falhou: `Não foi possível enviar sua mensagem. Tente novamente.`
  - Conexão perdida: `Conexão com o chat foi interrompida. Tentando reconectar...`.
  - Nenhum resultado de busca: `Nenhuma conversa corresponde ao termo digitado`.

## Acessibilidade
- Navegação por teclado:
  - Foco visível em botões, inputs e itens de lista.
  - `Enter` para enviar mensagem; `Esc` para fechar painel de ajuda.
- ARIA sugerido:
  - Lista de conversas: `role="list"`; itens: `role="listitem"`.
  - Painel de ajuda: `aria-live="polite"` ao abrir; botão de ajuda com `aria-expanded`.
- Contraste e tamanhos:
  - Manter contraste mínimo WCAG AA; fonte legível e espaçamento adequado.

## Internacionalização (i18n)
- Centralizar cópias em constantes futuras para facilitar tradução.
- Evitar concatenar IDs com texto em cópias visíveis.
- Confirmar formatação local (`pt-BR`) para `title`, datas (se introduzidas) e pluralização.

## Performance
- Renderização eficiente:
  - Computed para mensagens e filtro de conversas (já presente).
  - Limitar DOM em listas grandes com altura máxima e rolagem.
- Conexões:
  - Conectar após `load` e evitar debug verboso (`client.debug = () => {}` já aplicado).
- Assets:
  - Usar CDNs aprovados conforme CSP; evitar bibliotecas extras para tooltips simples.

## Analytics (opcional)
- Eventos sugeridos:
  - `chat_help_opened` ao abrir painel de ajuda.
  - `chat_conversation_opened` com `conversationId`.
  - `chat_message_sent` com tamanho da mensagem (sem conteúdo).
- Implementação mínima:
  - Enviar via `fetch('/analytics', { method: 'POST', body: JSON.stringify(evt) })` quando disponível.

## Segurança
- Não logar conteúdo de mensagens no cliente.
- Respeitar CSP vigente; manter dependências via CDN aprovadas.
- Evitar expor IDs sensíveis em atributos visíveis; usar apenas `conversationId` necessário.

## Validação e Aceite
- A página exibe:
  - Cabeçalho e texto explicativo sob o título.
  - Lista com título, botão `Nova`, busca e tooltips.
  - Painel de ajuda alternável pelo ícone `?` com guia em 3 passos.
  - Placeholder na janela quando nenhuma conversa está ativa.
  - Filtro de busca funcionando sobre a lista de conversas.
- Verificar manualmente em `/chat` nos principais navegadores suportados.

## Checklist de QA
- Layout
  - Título e parágrafo introdutório presentes.
  - Colunas `Conversas` (esquerda) e `Chat` (direita) renderizam corretamente.
- Comportamento
  - Tooltip aparece ao focar/hover nos elementos com `title`.
  - Painel de ajuda abre/fecha e é navegável por teclado.
  - Busca filtra itens conforme digitado, incluindo acentuação básica.
  - Botão `Nova` cria uma conversa com ID único e selecionável.
  - Envio de mensagem limpa o input e adiciona ao histórico.
- Acessibilidade
  - Foco visível; leitura de painel de ajuda por leitores de tela.
- Resiliência
  - Placeholder exibido sem conversa ativa.
  - Nenhum erro JS em console nas ações principais.

## Plano de Rollout
- Fase 1: validar em ambiente de testes e coletar feedback interno.
- Fase 2: publicar em produção com monitoramento de erros no console.
- Fase 3: instrumentar eventos mínimos de uso e ajustar cópias conforme adoção.

## Métricas de Sucesso
- Abertura do painel de ajuda em primeira visita.
- Taxa de criação de novas conversas por sessão.
- Redução de cliques equivocados/erros após introdução de tooltips.

## Manutenção
- Consolidar cópias em módulo dedicado quando introduzir i18n.
- Revisar a cada trimestre textos de ajuda e placeholders.
- Atualizar referências caso estrutura de componentes mude.

## Mapa de Arquivos (referência)
- `src/main/resources/templates/chat/index.html`: cabeçalho e copy introdutória.
- `src/main/resources/static/js/chat-app.js`: componentes `App`, `ConversationList`, `ChatWindow`.

## Conteúdo de UI (cópias)
- Título: `Chat Interno`.
- Parágrafo introdutório: “Esta é a área de Chat Interno para comunicação entre membros da equipe. Selecione uma conversa existente ou inicie uma nova.”
- Tooltips:
  - `Conversas`: “Lista de diálogos anteriores que você pode retomar”.
  - `Nova`: “Iniciar uma nova conversa com um ou mais colegas”.
- Placeholders:
  - Busca: “Buscar conversas específicas”.
  - Mensagem: “Digite sua mensagem”.
  - Janela sem conversa: “Selecione uma conversa para visualizar as mensagens ou inicie uma nova conversa”.
