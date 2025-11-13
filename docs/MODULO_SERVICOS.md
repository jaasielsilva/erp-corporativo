# Módulo de Serviços — Documentação de Implementação Completa

Esta documentação descreve, de ponta a ponta, como implementar o módulo de Serviços: páginas, botões, rotas, modelos de dados, validações, segurança, fluxo de aprovação, SLA, custos, avaliações e relatórios. O objetivo é permitir que você construa o módulo com consistência, previsibilidade e alta qualidade.

## Visão Geral

- Propósito: disponibilizar um catálogo de serviços e gerenciar solicitações com aprovação, execução, SLA e avaliações.
- Público: colaboradores, gestores, equipe de suporte/execução e administradores.
- Stack: Spring Boot (MVC com Thymeleaf), Spring Security, JPA/Hibernate, Validation, i18n.
- Controller inicial: `com.jaasielsilva.portalceo.controller.servicos.ServicosController` com `@RequestMapping("/servicos")` e view `servicos/index`.

## Personas e Perfis de Acesso

- `ROLE_USER` (colaborador):
  - Visualiza catálogo
  - Cria solicitações
  - Acompanha status e histórico
  - Avalia serviços após conclusão

- `ROLE_GESTOR` (aprovador):
  - Aprova/recusa solicitações (quando exigido)
  - Visualiza cargas, prazos e custos estimados

- `ROLE_SUPORTE` (executor/atendente):
  - Assumir execução, alterar status, registrar histórico e anexos
  - Gerir prazos (SLA) e comunicar bloqueios

- `ROLE_ADMIN` (administrador):
  - Administra catálogo, categorias, SLAs, custos, equipes
  - Acessa relatórios e define configurações globais

## Modelo de Domínio

- Entidades principais:
  - `Servico`: item do catálogo; possui categoria, descrição, SLA padrão, custo base e flags (exige aprovação, permite anexos, etc.)
  - `ServicoCategoria`: organização do catálogo; filtros e ordenações
  - `SolicitacaoServico`: pedido criado por um usuário para um `Servico`
  - `SolicitacaoHistorico`: eventos da solicitação (mudanças de status, comentários, alteração de executor)
  - `AnexoSolicitacao`: arquivos anexados à solicitação
  - `SlaAcordo`: regras e tempos por tipo de serviço (ex.: resposta em 4h, solução em 24h)
  - `AvaliacaoServico`: avaliação pós-conclusão (nota, comentário)
  - `CustoServico`: custos associados (fixo, hora, materiais)

## Status e Workflow da Solicitação

- Possíveis status:
  - `CRIADA` → `EM_APROVACAO` (se exigir aprovação) → `APROVADA` → `EM_EXECUCAO` → `CONCLUIDA`
  - Alternativas: `REJEITADA`, `PENDENTE_INFO`, `CANCELADA`
- Regras:
  - `CRIADA`: criada pelo usuário; se não exigir aprovação, vai direto para `EM_EXECUCAO`
  - `EM_APROVACAO`: gestor avalia; `APROVADA` ou `REJEITADA`
  - `EM_EXECUCAO`: equipe de suporte executa; registra histórico e anexos
  - `PENDENTE_INFO`: solicitante deve complementar informações
  - `CONCLUIDA`: permite avaliação e gera fechamento de SLA/custos
  - `CANCELADA`: somente por solicitante enquanto não em execução, ou admin

## Páginas, Componentes e Botões

1) Catálogo de Serviços — `GET /servicos` ou `/servicos/catalogo`
   - Elementos:
     - Barra de busca (texto)
     - Filtros: categoria, SLA, custo estimado, tags
     - Lista em cards/tabela com nome, descrição breve, SLA, custo base
     - Botões:
       - `Detalhes`: vai para `GET /servicos/{id}`
       - `Solicitar`: vai para `GET /servicos/{id}/solicitar`

2) Detalhe do Serviço — `GET /servicos/{id}`
   - Elementos:
     - Descrição completa, pré-requisitos, anexos exigidos
     - SLA padrão, custo base, equipe responsável
     - Botões:
       - `Solicitar Serviço`: `GET /servicos/{id}/solicitar`
       - `Voltar ao Catálogo`

3) Solicitar Serviço (Formulário) — `GET /servicos/{id}/solicitar` / `POST /servicos/{id}/solicitar`
   - Campos:
     - Título da solicitação (auto ou manual)
     - Descrição detalhada da necessidade
     - Prioridade (baixa/média/alta)
     - Anexos (opcional/obrigatório conforme serviço)
   - Botões:
     - `Enviar Solicitação` (POST)
     - `Cancelar`
   - Validar: campos obrigatórios, tamanho, tipos de anexo

4) Minhas Solicitações — `GET /servicos/solicitacoes`
   - Elementos:
     - Tabela: ID, Serviço, Status, Prioridade, SLA, Criado em, Atualizado em
     - Ações por linha:
       - `Ver`: `GET /servicos/solicitacoes/{id}`
       - `Editar` (somente em `CRIADA` ou `PENDENTE_INFO`)
       - `Cancelar` (restrito a status iniciais)

5) Detalhe da Solicitação — `GET /servicos/solicitacoes/{id}`
   - Elementos:
     - Timeline de histórico (quem alterou, quando, comentário)
     - Anexos e comentários
     - SLA e prazo estimado
     - Botões contextuais:
       - `Adicionar Comentário`
       - `Enviar Informação` (quando `PENDENTE_INFO`)
       - `Anexar Arquivo`
       - `Cancelar Solicitação` (conforme regra)

6) Painel de Aprovação — `GET /servicos/aprovacoes` (gestores)
   - Elementos:
     - Tabela: Solicitação, Solicitante, Serviço, Justificativa, Custo Estimado
     - Botões:
       - `Aprovar` (define status `APROVADA`)
       - `Rejeitar` (status `REJEITADA`)
       - `Ver Detalhes`

7) Execução/Suporte — `GET /servicos/suporte`
   - Elementos:
     - Fila de solicitações em execução
     - Botões:
       - `Assumir`/`Reatribuir`
       - `Mover para Pendente Info`
       - `Concluir`
       - `Adicionar Anotação/Anexo`

8) Avaliações — `GET /servicos/avaliacoes`
   - Elementos:
     - Lista de solicitações concluídas aguardando avaliação
     - Formulário de avaliação: nota (1–5), comentário
   - Botões:
     - `Enviar Avaliação`

9) Administração do Catálogo — `GET /servicos/admin/catalogo`
   - Elementos:
     - CRUD de `Servico` e `ServicoCategoria`
     - Configurações: `SlaAcordo` por categoria/serviço, custos base
   - Botões:
     - `Novo Serviço`
     - `Editar`
     - `Arquivar/Ativar`

10) Relatórios — `GET /servicos/relatorios`
   - Elementos:
     - Indicadores: tempo médio de resposta/solução, taxa de aprovação, custos, NPS
     - Filtros: período, categoria, equipe, status
   - Botões:
     - `Exportar CSV`
     - `Exportar PDF`

## URLs, Controllers e Views

- Base: `@RequestMapping("/servicos")` no `ServicosController`
- Rotas principais:
  - `GET /servicos` → `servicos/index`
  - `GET /servicos/catalogo` → `servicos/catalogo`
  - `GET /servicos/{id}` → `servicos/detalhe`
  - `GET /servicos/{id}/solicitar` → `servicos/solicitar`
  - `POST /servicos/{id}/solicitar` → redireciona para `solicitacoes`
  - `GET /servicos/solicitacoes` → `servicos/solicitacoes`
  - `GET /servicos/solicitacoes/{id}` → `servicos/solicitacao-detalhe`
  - `POST /servicos/solicitacoes/{id}/cancelar`
  - `POST /servicos/solicitacoes/{id}/informacao`
  - `GET /servicos/aprovacoes` → `servicos/aprovacoes` (gestor)
  - `POST /servicos/aprovacoes/{id}/aprovar`
  - `POST /servicos/aprovacoes/{id}/rejeitar`
  - `GET /servicos/suporte` → `servicos/suporte` (suporte)
  - `POST /servicos/suporte/{id}/assumir`
  - `POST /servicos/suporte/{id}/concluir`
  - `GET /servicos/avaliacoes` → `servicos/avaliacoes`
  - `POST /servicos/avaliacoes/{id}`
  - `GET /servicos/admin/catalogo` → `servicos/admin/catalogo`
  - `GET /servicos/relatorios` → `servicos/relatorios`

## Templates Thymeleaf

- Local: `src/main/resources/templates/servicos/`
- Arquivos sugeridos:
  - `index.html`, `catalogo.html`, `detalhe.html`, `solicitar.html`
  - `solicitacoes.html`, `solicitacao-detalhe.html`
  - `aprovacoes.html`, `suporte.html`, `avaliacoes.html`
  - `admin/catalogo.html`, `relatorios.html`
- Partials: `fragments/_filtros.html`, `fragments/_tabela.html`, `fragments/_timeline.html`
- i18n: `src/main/resources/messages.properties` com chaves `servicos.*`

## DTOs e Mappers

- DTOs:
  - `ServicoDTO`: id, nome, descricaoBreve, categoria, slaPadrao, custoBase, ativo
  - `SolicitacaoServicoDTO`: id, servicoId, titulo, descricao, prioridade, status, prazos, solicitante, executor
  - `AvaliacaoDTO`: solicitacaoId, nota, comentario, data
  - `SlaDTO`: servicoId, tempoRespostaHoras, tempoSolucaoHoras, metas
  - `CustoDTO`: servicoId, tipo (fixo/hora/material), valor, moeda
- Mapper: `ServicoMapper`, `SolicitacaoMapper` (MapStruct recomendado)

## Serviços (Service Layer)

- `CatalogoService`:
  - Listar catálogo, filtrar e buscar
  - Gerir categorias e serviços (admin)

- `SolicitacaoService`:
  - Criar, editar, cancelar
  - Transições de status e histórico
  - Adicionar anexos e comentários
  - Calcular SLA (prazos) e custos estimados

- `AprovacaoService`:
  - Aprovar/rejeitar com justificativa

- `SuporteService`:
  - Assumir execução, reatribuir, concluir
  - Gerir pendências de informação

- `AvaliacaoService`:
  - Registrar avaliações e calcular métricas

- `RelatorioService`:
  - KPIs, exportações (`CSV`/`PDF`)

## Repositórios (JPA)

- `ServicoRepository`, `ServicoCategoriaRepository`
- `SolicitacaoServicoRepository`, `SolicitacaoHistoricoRepository`
- `AnexoSolicitacaoRepository`
- `SlaAcordoRepository`, `AvaliacaoServicoRepository`, `CustoServicoRepository`

## Validações

- Formulários:
  - `titulo`: obrigatório, 5–120 caracteres
  - `descricao`: obrigatório, até 2000 caracteres
  - `prioridade`: obrigatório (`BAIXA`, `MEDIA`, `ALTA`)
  - `anexos`: tamanho máximo por arquivo (ex.: 10MB), tipos permitidos
- Domínio:
  - Solicitação não pode ser cancelada em `EM_EXECUCAO` sem justificativa admin
  - Aprovação obrigatória para serviços marcados com `exigeAprovacao = true`
  - Avaliação só permitida em `CONCLUIDA` e apenas pelo solicitante

## Exceções e Tratamento de Erros

- Exceções customizadas:
  - `SolicitacaoNaoEncontradaException`
  - `TransicaoStatusInvalidaException`
  - `AcessoNegadoException`
  - `ValidacaoNegocioException`
- Controller Advice:
  - Converter exceções em respostas amigáveis e mensagens i18n

## Segurança e Regras de Autorização

- Rotas públicas: catálogo e detalhe do serviço
- Autenticadas: solicitar, ver próprias solicitações, avaliar
- Restringidas:
  - Aprovações (`ROLE_GESTOR`)
  - Suporte (`ROLE_SUPORTE`)
  - Administração (`ROLE_ADMIN`)
- Política:
  - `@PreAuthorize` em métodos de service/controller
  - Filtrar por `solicitanteId` quando listar solicitações do usuário

## SLA e Custos

- SLA:
  - `tempoRespostaHoras` e `tempoSolucaoHoras` por serviço
  - Calculado na criação da solicitação; prazos ajustados por prioridade
- Custos:
  - `fixo` + `hora` + `materiais` (opcional)
  - Estimativa exibida antes de solicitar; fechamento na conclusão

## Notificações e Comunicação

- Eventos:
  - `SolicitacaoCriadaEvent`, `SolicitacaoAprovadaEvent`, `SolicitacaoConcluidaEvent`
  - Listeners: enviar email/alerta interno (ex.: chat corporativo) com templates i18n

## Auditoria

- `SolicitacaoHistorico` registra quem, quando, o que (status/comentário)
- Log de alterações críticas (aprovação, cancelamento, conclusão)

## Relatórios e KPIs

- Indicadores:
  - Tempo médio de resposta/solução (por categoria/equipe)
  - Taxa de aprovação/rejeição
  - Custos por serviço/solicitação
  - Satisfação (média de notas e NPS)
- Exportações:
  - `CSV`: colunas simples
  - `PDF`: templates com branding corporativo

## Estrutura de Templates e Navegação

- Navegação padrão:
  - Barra lateral: Catálogo, Minhas Solicitações, Aprovações, Execução, Avaliações, Administração, Relatórios
  - Breadcrumbs em páginas de detalhe
- Acessibilidade:
  - Teclas de atalho para salvar/cancelar
  - Labels e `aria-*` nos formulários

## Checklist de Implementação

1. Criar entidades e repositórios JPA
2. Implementar services com regras de negócio e validação
3. Construir controllers e rotas conforme mapa
4. Criar templates Thymeleaf e fragments comuns
5. Configurar i18n e mensagens de erro
6. Implementar segurança (roles, `@PreAuthorize`)
7. Adicionar eventos e notificações
8. Construir relatórios e exportação
9. Escrever testes (services e controllers)
10. Revisar UX, acessibilidade e consistência visual

## Plano de Testes

- Unitários: services (mudança de status, cálculo de SLA, validações)
- Integração: controllers (rotas e redirects), repositórios (queries)
- UI: navegação e formulários (campos obrigatórios, mensagens)
- Segurança: acesso por role, filtros por `solicitanteId`

## Nomes de Arquivos e Paths (Sugeridos)

- Controller:
  - `src/main/java/com/jaasielsilva/portalceo/controller/servicos/ServicosController.java`
- Services:
  - `src/main/java/com/jaasielsilva/portalceo/service/servicos/*.java`
- Repositórios:
  - `src/main/java/com/jaasielsilva/portalceo/repository/servicos/*.java`
- Modelos:
  - `src/main/java/com/jaasielsilva/portalceo/model/servicos/*.java`
- DTOs/Mappers:
  - `src/main/java/com/jaasielsilva/portalceo/dto/servicos/*.java`
  - `src/main/java/com/jaasielsilva/portalceo/mapper/servicos/*.java`
- Templates:
  - `src/main/resources/templates/servicos/*.html`
- i18n:
  - `src/main/resources/messages.properties`

## Exemplos de Endpoints (Assinaturas)

- `GET /servicos` → lista catálogo
- `GET /servicos/{id}` → detalhe
- `GET /servicos/{id}/solicitar` → form
- `POST /servicos/{id}/solicitar` → cria `SolicitacaoServico`
- `GET /servicos/solicitacoes` → lista do usuário
- `GET /servicos/solicitacoes/{id}` → detalhe da solicitação
- `POST /servicos/solicitacoes/{id}/cancelar` → cancela
- `POST /servicos/aprovacoes/{id}/aprovar` → aprova
- `POST /servicos/aprovacoes/{id}/rejeitar` → rejeita
- `POST /servicos/suporte/{id}/assumir` → assume execução
- `POST /servicos/suporte/{id}/concluir` → conclui execução
- `POST /servicos/avaliacoes/{id}` → registra avaliação

## Observações de UX

- Feedback imediato após ações (toasts/sucesso/erro)
- Estados vazios com instruções claras
- Confirm dialogs para ações destrutivas (cancelar/rejeitar)

## Próximos Passos

- Gerar os esboços das entidades e repositórios.
- Criar os templates base e navegação.
- Iterar nas regras de negócio de acordo com feedback do time.