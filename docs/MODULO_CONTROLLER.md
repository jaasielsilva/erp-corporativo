# Documentação do Módulo `controller`

O módulo `controller` é responsável por gerenciar as requisições HTTP, atuar como a camada de apresentação da aplicação e orquestrar as interações entre a interface do usuário e a lógica de negócios (serviços). Ele contém diversos controladores que lidam com diferentes aspectos da aplicação, desde a gestão de estoque até o controle de usuários e finanças.

## Arquivos e suas Finalidades:

### Controladores Principais:

*   **`AuditoriaEstoqueController.java`**: Gerencia as requisições relacionadas à auditoria de estoque.
*   **`AvaliacaoFornecedorController.java`**: Lida com as operações de avaliação de fornecedores.
*   **`CategoriaPageController.java`**: Controla as páginas e requisições relacionadas a categorias.
*   **`ChamadoRestController.java`**: Fornece uma API RESTful para a gestão de chamados.
*   **`ChatController.java`**: Gerencia as funcionalidades de chat.
*   **`ChatRestController.java`**: Oferece uma API RESTful para o chat.
*   **`ChatWebController.java`**: Controla a interface web do chat.
*   **`ChatWebSocketController.java`**: Lida com as funcionalidades de WebSocket para o chat.
*   **`ConfiguracoesController.java`**: Gerencia as requisições de configurações da aplicação.
*   **`ContaReceberController.java`**: Controla as operações de contas a receber.
*   **`ContratosController.java`**: Lida com as requisições relacionadas a contratos.
*   **`CustomErrorController.JAVA`**: Gerencia a exibição de páginas de erro personalizadas.
*   **`DashboardController.java`**: Controla a exibição do dashboard principal da aplicação.
*   **`DevolucaoController.java`**: Gerencia as requisições de devolução de produtos.
*   **`DocumentosController.java`**: Lida com as operações de documentos.
*   **`EstoqueController.java`**: Controla as funcionalidades gerais de estoque.
*   **`FavoritosController.java`**: Gerencia as requisições de itens favoritos.
*   **`FornecedorController.java`**: Lida com as operações de fornecedores.
*   **`InventarioController.java`**: Controla as funcionalidades de inventário.
*   **`JuridicoController.java`**: Gerencia as requisições do módulo jurídico.
*   **`LoginController.java`**: Lida com as operações de login e autenticação.
*   **`MetasController.java`**: Controla as funcionalidades de metas.
*   **`MeusPedidosController.java`**: Gerencia as requisições de pedidos do usuário.
*   **`MeusServicosController.java`**: Lida com as operações de serviços do usuário.
*   **`MovimentacaoEstoqueController.java`**: Controla as requisições de movimentação de estoque.
*   **`NotificationRestController.java`**: Fornece uma API RESTful para notificações.
*   **`PerfilController.java`**: Gerencia as requisições de perfil do usuário.
*   **`PermissaoController.java`**: Lida com as operações de permissões.
*   **`ProcessoAdesaoController.java`**: Controla as requisições de processo de adesão.
*   **`ProdutoController.java`**: Gerencia as operações de produtos.
*   **`ProdutoRestController.java`**: Fornece uma API RESTful para produtos.
*   **`ReacaoMensagemController.java`**: Lida com as requisições de reações a mensagens.
*   **`RecomendacoesController.java`**: Controla as funcionalidades de recomendações.
*   **`RelatoriosEstoqueController.java`**: Gerencia as requisições de relatórios de estoque.
*   **`SenhaController.java`**: Lida com as operações de recuperação e alteração de senha.
*   **`ServicosController.java`**: Controla as funcionalidades de serviços.
*   **`SolicitacoesController.java`**: Gerencia as requisições de solicitações.
*   **`SuporteApiController.java`**: Fornece uma API para o módulo de suporte.
*   **`SuporteController.java`**: Lida com as requisições do módulo de suporte.
*   **`TermosController.java`**: Controla a exibição de termos e condições.
*   **`TestController.java`**: Controlador para fins de teste.
*   **`TestNotificationController.java`**: Controlador para testar notificações.
*   **`TiController.java`**: Gerencia as requisições do módulo de TI.
*   **`TransferenciaEstoqueController.java`**: Lida com as operações de transferência de estoque.
*   **`UserController.java`**: Controla as operações de usuários.
*   **`UsuarioController.java`**: Gerencia as requisições de usuários.
*   **`VendasController.java`**: Lida com as operações de vendas.

### Subdiretórios de Controladores:

*   **`agenda/AgendaController.java`**: Controla as funcionalidades da agenda.
*   **`ajuda/AjudaController.java`**: Gerencia as requisições do módulo de ajuda.
*   **`api/ContaPagarApiController.java`**: Fornece uma API para contas a pagar.
*   **`cliente/ClienteController.java`**: Lida com as operações de clientes.
*   **`estoque/AlertaEstoqueController.java`**: Controla os alertas de estoque.
*   **`financeiro/ContaPagarController.java`**: Gerencia as requisições de contas a pagar.
*   **`financeiro/FinanceiroController.java`**: Lida com as operações financeiras gerais.
*   **`marketing/CampanhaController.java`**: Controla as campanhas de marketing.
*   **`marketing/MarketingDashboardController.java`**: Gerencia o dashboard de marketing.
*   **`marketing/PublicoAlvoController.java`**: Lida com as operações de público-alvo.
*   **`marketing/RelatorioMarketingController.java`**: Controla os relatórios de marketing.
*   **`projetos/CronogramaController.java`**: Gerencia os cronogramas de projetos.
*   **`projetos/EquipesProjetoController.java`**: Lida com as equipes de projeto.
*   **`projetos/GeralProjetoController.java`**: Controla as funcionalidades gerais de projetos.
*   **`projetos/ProjetoAjaxController.java`**: Fornece funcionalidades AJAX para projetos.
*   **`projetos/RelatoriosController.java`**: Gerencia os relatórios de projetos.
*   **`projetos/TarefaController.java`**: Lida com as operações de tarefas de projeto.
*   **`rh/RhController.java`**: Controla as funcionalidades gerais de RH.
*   **`rh/WorkflowAdesaoController.java`**: Gerencia o workflow de adesão de RH.
*   **`rh/beneficios/AdesaoPlanoSaudeController.java`**: Lida com a adesão a planos de saúde.
*   **`rh/beneficios/PlanoSaudeController.java`**: Controla as operações de planos de saúde.
*   **`rh/beneficios/ValeRefeicaoController.java`**: Gerencia as requisições de vale-refeição.
*   **`rh/beneficios/ValeTransporteController.java`**: Lida com as operações de vale-transporte.
*   **`rh/colaborador/AdesaoColaboradorController.java`**: Controla a adesão de colaboradores.
*   **`rh/colaborador/BeneficioAdesaoController.java`**: Gerencia a adesão a benefícios de colaboradores.
*   **`rh/colaborador/ColaboradorController.java`**: Lida com as operações de colaboradores.
*   **`rh/colaborador/DocumentoAdesaoController.java`**: Controla os documentos de adesão.
*   **`rh/folha/FolhaPagamentoController.java`**: Gerencia as requisições de folha de pagamento.
*   **`rh/ponto/CorrecaoPontoApiController.java`**: Fornece uma API para correção de ponto.
*   **`rh/ponto/PontoEscalaController.java`**: Lida com as operações de ponto e escala.