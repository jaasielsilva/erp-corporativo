# Mapeamento de Controladores e Páginas HTML

- Data da análise: 2025-12-14
- Versão do sistema: 0.0.1-SNAPSHOT
- Framework: Spring Boot + Thymeleaf

## Objetivo

- Identificar todos os controladores e páginas HTML
- Mapear dependências entre controladores e templates
- Classificar acoplamento (alto/baixo) e dependências cruzadas
- Fornecer um documento autoexplicativo e fácil de atualizar

## Critérios de Classificação

- Alto acoplamento: `@Controller` que retorna múltiplos templates específicos (ex.: `return "modulo/pagina"`) e lógica de apresentação acoplada
- Baixo acoplamento: `@RestController` (APIs JSON) ou controladores sem retorno de template
- Médio acoplamento: controladores com poucos templates ou páginas auxiliares

## Lista de Controladores

- ajuda/AjudaController (MVC)
- ajuda/AjudaConteudoController (MVC)
- ajuda/AjudaRestController (REST)
- api/ContaPagarApiController (REST)
- AssinaturasController (MVC)
- AuditoriaEstoqueController (MVC)
- AuditoriaGlobalApiController (REST)
- cadastros/ConsultaCnpjController (MVC)
- categoria/CategoriaPageController (MVC)
- chat/ChatApiController (REST)
- chat/ChatController (MVC)
- chat/ChatPageController (MVC)
- chat/ChatWsController (REST)
- ChamadoRestController (REST)
- ConfiguracoesController (MVC)
- ContratosController (MVC)
- DashboardController (MVC)
- DocumentosController (MVC)
- EstoqueController (MVC)
- FaviconController (MVC)
- FavoritosController (MVC)
- financeiro/ContaPagarController (MVC)
- FinanceiroController (MVC)
- FornecedorController (MVC)
- InventarioController (MVC)
- JuridicoController (MVC)
- LoginController (MVC)
- Marketing/CampanhaController (MVC)
- Marketing/MarketingDashboardController (MVC)
- Marketing/PublicoAlvoController (MVC)
- Marketing/RelatorioMarketingController (MVC)
- MeusPedidosController (MVC)
- MeusServicosController (MVC)
- MetasController (MVC)
- NavigationController (REST)
- PerfilController (MVC)
- PermissaoController (MVC)
- ProdutoController (MVC)
- ProdutoRestController (REST)
- RelatoriosEstoqueController (MVC)
- RecomendacoesController (MVC)
- rh/AvaliacaoApiController (REST)
- rh/AvaliacaoController (MVC)
- rh/FeriasApiController (REST)
- rh/FeriasController (MVC)
- rh/folha/FolhaPagamentoController (MVC)
- rh/PontoEscalaController (MVC)
- rh/PontoEscalasController (MVC)
- rh/RhAuditoriaApiController (REST)
- rh/RhAuditoriaController (MVC)
- rh/RhConfiguracoesController (MVC)
- rh/RhController (MVC)
- rh/RhRelatoriosApiController (REST)
- rh/RhRelatoriosController (MVC)
- rh/RecrutamentoApiController (REST)
- rh/RecrutamentoController (MVC)
- rh/TreinamentosApiController (REST)
- rh/TreinamentosController (MVC)
- rh/WorkflowAdesaoController (MVC)
- rh/beneficios/AdesaoPlanoSaudeController (MVC)
- rh/beneficios/PlanoSaudeController (MVC)
- rh/beneficios/ValeRefeicaoController (MVC)
- rh/beneficios/ValeTransporteController (MVC)
- rh/colaborador/AdesaoColaboradorController (MVC)
- rh/colaborador/BeneficioAdesaoController (MVC)
- rh/colaborador/ColaboradorController (MVC)
- rh/colaborador/DocumentoAdesaoController (MVC)
- rh/escala/EscalaTrabalhoController (MVC)
- senha/SenhaController (MVC)
- ServicosController (MVC)
- SolicitacoesController (MVC)
- SuporteApiController (REST)
- SuporteController (MVC)
- TermosController (MVC)
- TestController (MVC)
- TestNotificationController (MVC)
- TiController (MVC)
- TransferenciaEstoqueController (MVC)
- UsuarioController (MVC)
- UsuarioRestController (REST)
- UserController (MVC)
- VendasController (MVC)
- vendas/CheckoutController (MVC)

## Lista de Páginas HTML (templates)

- agenda/*
- ajuda/*
- cadastros/*
- categoria/*
- chat/*
- clientes/*
- components/* (parciais reutilizáveis)
- configuracoes/*
- contrato/*
- dashboard/*
- documentos/*
- email/*
- error/* e `error.html`
- estoque/*
- favoritos/*
- financeiro/*
- fornecedor/*
- juridico/*
- marketing/*
- metas/*
- meus-pedidos/*
- meus-servicos/*
- perfis/*
- permissoes/*
- produto/*
- projetos/*
- recomendacoes/*
- relatorios/*
- rh/* (submódulos variados)
- senha/*
- servicos/*
- solicitacoes/*
- suporte/*
- termos/*
- ti/*
- login.html

> Estrutura completa listada em `src/main/resources/templates`.

## Matriz de Relacionamento (Controlador → Templates)

- TiController → `ti/index`, `ti/sistemas`, `ti/backup`, `ti/seguranca`, `ti/auditoria` (ex.: TiController.java:82, 98, 142, 175, 456)
- TermosController → `termos/index`, `termos/gerenciar`, `termos/uso`, `termos/privacidade`, `termos/historico`, `termos/aceites`, `termos/meusAceites` (TermosController.java:174, 184, 209, 225, 239, 253, 276)
- JuridicoController → `juridico/index`, `juridico/contratos`, `juridico/contrato-detalhe`, `juridico/processos`, `juridico/compliance`, `juridico/documentos` (JuridicoController.java:90, 152, 160, 249, 298, 385)
- RhAuditoriaController → `rh/auditoria/index`, `acessos`, `alteracoes`, `exportacoes`, `revisoes` (RhAuditoriaController.java:33, 49, 65, 81, 97)
- TreinamentosController → `rh/treinamentos/cadastro`, `certificado`, `inscricao`, `turma-detalhe`, `cursos`, `instrutores`, `turmas`, `relatorios` (TreinamentosController.java:37, 46, 55, 65, 73, 82, 91, 99)
- RhConfiguracoesController → `rh/configuracoes/index`, `politicas-ferias`, `ponto`, `integracoes` (RhConfiguracoesController.java:51, 61, 71, 81, 98, 115, 132)
- AdesaoColaboradorController → `rh/colaboradores/adesao/inicio`, `status`, `beneficios`, `documentos`, `revisao`, `em-andamento` (AdesaoColaboradorController.java:125, 384, 458, 490, 683, 804)
- ColaboradorController → `rh/colaboradores/listar`, `novo`, `editar`, `ficha`, `historico`, `relatorios`, `corrigir-cpf` (ColaboradorController.java:77, 124, 268, 454, 470, 526, 534)
- FeriasController → `rh/ferias/solicitar`, `aprovar`, `planejamento`, `calendario` (FeriasController.java:17, 25, 33, 41)
- PontoEscalasController → `rh/ponto-escalas/registros`, `escalas`, `atribuir-massa`, `nova`, `correcoes`, `relatorios` (PontoEscalasController.java:68, 80, 90, 280, 454, 515)
- RhController → `rh/dashboard`, `processo-detalhes` (RhController.java:112, 155)
- RhRelatoriosController → `rh/relatorios/turnover`, `absenteismo`, `headcount`, `indicadores` (RhRelatoriosController.java:30, 46, 59, 67)
- RecrutamentoController → `rh/recrutamento/vagas`, `triagem`, `entrevistas`, `historico`, `pipeline`, `relatorios` (ver `rh/recrutamento/*`)
- AvaliacaoController → `rh/avaliacao/periodicidade`, `feedbacks`, `relatorios`
- FolhaPagamentoController → `rh/folha-pagamento/index`, `gerar`, `holerite`, `visualizar`, `descontos`, `relatorios`, `listar`, `holerite-index`, `holerites-colaborador`
- PontoEscalaController → páginas de `rh/ponto-escalas/*` correlatas
- ValeTransporteController → `rh/beneficios/vale-transporte/form`, `listar`
- ValeRefeicaoController → `rh/beneficios/vale-refeicao/form`, `listar`
- PlanoSaudeController → `rh/beneficios/plano-de-saude/plano-saude`, `adesoes`
- WorkflowAdesaoController → `rh/workflow-aprovacao`, `rh/workflow-relatorios`
- UsuarioController → `usuarios/listar`, `editar`, `detalhes`, `cadastro`, `index`, `solicitar-acesso`, `relatorio-usuarios`, `comparativo-perfil`, `enviar-email`, `filtros-avancados` (UsuarioController.java:390-403)
- DashboardController → `dashboard/notificacoes`, `estatisticas`, `alertas` (DashboardController.java:229-242)
- ChatWebController → `chat/index`, `chat/departamentos` (ChatWebController.java:16-22)
- AjudaController → `ajuda/index` (AjudaController.java:7-11)
- AjudaConteudoController → `ajuda/conteudo`
- LoginController → `login`
- SolicitacoesController → `solicitacoes/listar`, `detalhes`, `nova`, `dashboard`, `minhas`, `pendentes`, `todas`, `aprovar`, `detalhes-usuario`
- SuporteController → `suporte/index`, `chamados`, `atribuir`, `avaliar`, `status`, `visualizar`, `novo` (SuporteController.java:498-541, e demais)
- VendasController → `vendas/index`, `pedidos`, `pdv`, `relatorios`
- FornecedorController → `fornecedor/listar`, `form`, `contratos-listar`, `contrato-form`, `avaliacoes`, `avaliacao-form`, `pagamentos`
- ProdutoController → `produto/lista`, `produto/form`, e dependências em `categoria/*` e `fornecedor/*`
- EstoqueController → `estoque/lista`, `detalhes`, `entrada`, `saida`, `movimentacoes`, `transferencias`, `inventario`, `alertas`, `ajuste`, `relatorios`, `historico`
- FinanceiroController → `financeiro/index`, `fluxo-caixa`, `transferencias`, `relatorios` e submódulos de `contas-pagar/*`, `contas-receber/*`
- ContaPagarController → `financeiro/contas-pagar/*`
- ContaReceberController → `financeiro/contas-receber/*`
- ContratosController → `contrato/lista`, `contrato/listar`, `contrato/detalhes`, `contrato/contrato-form`
- CategoriaPageController → `categoria/lista`
- PerfilController → `perfis/listar`, `perfis/form`, `perfis/detalhes`
- PermissaoController → `permissoes/listar`, `permissoes/form`, `permissoes/detalhes`
- ServicosController → `servicos/index`
- AgendaController → `agenda/index` e subpáginas
- MeusPedidosController → `meus-pedidos/index`
- MeusServicosController → `meus-servicos/index`
- MetasController → `metas/index`
- DocumentosController → `documentos/index`
- FavoritosController → `favoritos/index`
- RelatoriosEstoqueController → `estoque/relatorios`
- TransferenciaEstoqueController → `estoque/transferencias`
- RecomendacoesController → `recomendacoes/index`
- UserController → páginas correlatas de usuário (se aplicável)
- TestController → `test`

> Controladores REST (baixo acoplamento): ChatApiController, AjudaRestController, ChamadoRestController, NavigationController, UsuarioRestController, ProdutoRestController, ContaPagarApiController, RhRelatoriosApiController, RhAuditoriaApiController, RecrutamentoApiController, TreinamentosApiController, AvaliacaoApiController, FeriasApiController, SuporteApiController, CorrecaoPontoApiController, ChatWsController.

## Dependências Cruzadas e Observações

- Templates compartilhados: múltiplos controladores usam parciais `components/topbar.html`, `components/sidebar.html`, `components/footer.html` (Thymeleaf `th:insert`)
- Módulo Suporte: `SuporteController` (páginas) e `ChamadoRestController` (APIs) compartilham serviços e modelos
- Módulo Usuários: `UsuarioController` (páginas) integra com `UsuarioRestController` para operações assíncronas
- Módulo Financeiro: `FinanceiroController` coordena submódulos `contas-pagar` e `contas-receber` via controladores específicos e templates
- Redirecionamentos: vários métodos retornam `redirect:/...` para acionar rotas de outros controladores (ex.: TreinamentosController.java:28)
- Inconsistências detectadas: `rh/relatorios/turnover-analytics` citado em controlador não existe em `templates` (verificar criação ou ajuste de rota)

## Classificação de Acoplamento

- Alto: TiController, TermosController, JuridicoController, ColaboradorController, SuporteController, FinanceiroController, ProdutoController, EstoqueController, UsuarioController, RhRelatoriosController, RhConfiguracoesController, TreinamentosController, RecrutamentoController, FolhaPagamentoController
- Médio: AgendaController, ContratosController, PerfilController, PermissaoController, VendasController, ContaPagarController, ContaReceberController, MeusPedidosController, MeusServicosController, RecomendacoesController
- Baixo: Todos `@RestController` listados; ChatWsController

## Exemplos de Código

- `src/main/java/com/jaasielsilva/portalceo/controller/TiController.java:82` retorna `"ti/index"`
- `src/main/java/com/jaasielsilva/portalceo/controller/TermosController.java:174` retorna `"termos/index"`
- `src/main/java/com/jaasielsilva/portalceo/controller/JuridicoController.java:152` retorna `"juridico/contratos"`
- `src/main/java/com/jaasielsilva/portalceo/controller/rh/colaborador/ColaboradorController.java:454` retorna `"rh/colaboradores/ficha"`
- `src/main/java/com/jaasielsilva/portalceo/controller/SuporteController.java:498` retorna `"suporte/chamados"`

## Como Atualizar Futuramente

- Adicionar novos controladores: incluir na lista e na matriz (Controller → Templates)
- Novos templates: listar em `templates/` e vincular aos controladores
- Conferir vínculos: buscar por `return "..."` e `ModelAndView("...")` nos controladores
- Verificar inconsistências: garantir que cada view retornada existe em `templates`

## Metadados

- Origem: `src/main/java/com/jaasielsilva/portalceo/controller` e `src/main/resources/templates`
- Geração: análise estática do código e estrutura de templates

