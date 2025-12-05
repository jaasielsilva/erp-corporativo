# Manual do Módulo de Ajuda

## Visão Geral
- O módulo de Ajuda centraliza conteúdos de suporte (artigos, FAQs e vídeos) e integra busca inteligente, IA assistente e abertura rápida de chamados.
- A página principal está em `GET /ajuda` e carrega dados via APIs `GET /api/ajuda/*`.

## Acesso
- URL: `http://localhost:8080/ajuda`
- Requer login para enviar feedback, usar IA e abrir chamados.

## Layout e Navegação
- Cabeçalho com título e subtítulo: “Busque conteúdos, FAQs e vídeos. Se precisar, acione a IA.”
- Colunas:
  - Esquerda: lista de categorias.
  - Direita: formulário de busca, resultados e ações (IA, abrir chamado). 
- Componentes reutilizados: sidebar e topbar.

## Funcionalidades
- Busca por texto e por categoria com paginação.
- Ordenação por relevância (upvotes + visualizações).
- Feedback em conteúdos (positivo/negativo).
- Sugestões da IA quando não há resultados ou sob demanda.
- Abertura de chamado diretamente do módulo.

## Fluxo de Uso
1. Acesse `Ajuda` pelo menu lateral ou diretamente pela URL.
2. Digite sua dúvida no campo “Digite sua dúvida” e selecione uma categoria opcional.
3. Clique em “Buscar”.
4. Se houver resultados, abra o conteúdo desejado ou envie feedback. 
5. Se não houver resultados:
   - Clique em “IA” para obter sugestões automáticas.
   - Clique em “Abrir Chamado” para registrar sua solicitação.

## Categorias
- Exemplos disponíveis: Suporte, Impressoras, Chamados, TI, Sistemas, Financeiro, RH, Comercial, Estoque, Segurança, Relatórios, Integrações, Usuários & Acesso, Documentação.
- As categorias são semeadas automaticamente em ambiente novo.

## IA Assistente
- Botão “IA” gera sugestões com base na dúvida.
- Exibe resposta resumida e cartões de sugestões por categoria.
- Requer usuário autenticado.

## Abrir Chamado pelo Módulo
- A ação “Abrir Chamado” preenche automaticamente assunto, descrição, categoria e prioridade.
- Encaminha ao endpoint de criação de chamados do sistema.
- Exibe confirmação com número do chamado quando bem-sucedido.

## APIs do Módulo
- Base: `/api/ajuda`
- `GET /api/ajuda/categorias`
  - Retorna lista de categorias.
- `GET /api/ajuda/busca?q={texto}&categoria={slug}&page={n}&size={n}`
  - Retorna página com itens publicados (artigo/faq/vídeo) ordenados por relevância.
- `GET /api/ajuda/conteudo/{id}`
  - Retorna conteúdo e incrementa visualizações.
- `POST /api/ajuda/feedback?conteudoId={id}&upvote={true|false}&comentario={texto}`
  - Registra feedback do usuário autenticado.
- `POST /api/ajuda/ia/generate?query={texto}`
  - Gera resposta e sugestões com IA para o usuário autenticado.

## Modelos de Dados
- Conteúdo (`AjudaConteudo`)
  - Campos: categoria, título, tipo (`ARTIGO|FAQ|VIDEO`), corpo, tags, publicado, criadoAt, visualizacoes.
- Categoria (`AjudaCategoria`)
  - Campos: slug, nome, parent.
- Feedback (`AjudaFeedback`)
  - Campos: conteudo, usuario, upvote, comentario.
- Log de busca (`AjudaBuscaLog`)
  - Campos: usuario, query, resultados, resolveu.

## Relevância e Ordenação
- A busca e as listagens retornam itens publicados ordenados por: `upvotes * 2 + visualizacoes`.
- O objetivo é priorizar conteúdos úteis, atualizados e mais vistos.

## Segurança e Permissões
- Endpoints de leitura (categorias, busca, conteúdo): acesso geral.
- Endpoints que exigem autenticação: feedback e IA.
- Proteção CSRF aplicada às requisições POST.

## Boas Práticas de Consulta
- Use palavras-chave existentes (ex.: “impressora”, “chamado”, “status”).
- Combine termo + categoria quando o tema estiver claro.
- Em ausência de resultados, acione a IA ou abra um chamado.

## Solução de Problemas
- “Nenhum resultado encontrado.”
  - O termo pesquisado não corresponde a conteúdos publicados.
  - Ações: tente sinônimos, selecione outra categoria, acione a IA ou abra chamado.
- Erro 401 ao enviar feedback/usar IA/abrir chamado
  - Faça login e tente novamente.

## Referências Técnicas
- Rota da página: `src/main/java/com/jaasielsilva/portalceo/controller/ajuda/AjudaController.java:8`
- APIs: `src/main/java/com/jaasielsilva/portalceo/controller/ajuda/AjudaRestController.java:24-76`
- Serviço de busca: `src/main/java/com/jaasielsilva/portalceo/service/ajuda/HelpService.java:25-44`
- Repositórios de ajuda: `src/main/java/com/jaasielsilva/portalceo/repository/ajuda/`
- Template: `src/main/resources/templates/ajuda/index.html`

## Expansões Futuras
- Conteúdos adicionais (ex.: “mouse com problema”, “periféricos”).
- Busca com sinônimos e stemming.
- Métricas de uso do módulo na dashboard.
