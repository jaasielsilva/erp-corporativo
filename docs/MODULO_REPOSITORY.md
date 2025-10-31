# Documentação do Módulo `repository`

O módulo `repository` contém interfaces que estendem as funcionalidades do Spring Data JPA, fornecendo métodos para operações de persistência e recuperação de dados para as entidades da aplicação. Cada interface de repositório é responsável por gerenciar o acesso aos dados de uma entidade específica, abstraindo a complexidade das operações de banco de dados.

## Arquivos e suas Finalidades:

*   **`AcaoUsuarioRepository.java`**: Repositório para a entidade `AcaoUsuario`, responsável por operações de CRUD e consultas relacionadas às ações dos usuários.
*   **`AdesaoPlanoSaudeRepository.java`**: Repositório para a entidade `AdesaoPlanoSaude`, gerenciando o acesso aos dados de adesões a planos de saúde.
*   **`AlertaEstoqueRepository.java`**: Repositório para a entidade `AlertaEstoque`, utilizado para gerenciar alertas relacionados ao estoque.
*   **`AuditoriaEstoqueRepository.java`**: Repositório para a entidade `AuditoriaEstoque`, responsável por registrar e consultar auditorias de estoque.
*   **`AvaliacaoFornecedorRepository.java`**: Repositório para a entidade `AvaliacaoFornecedor`, gerenciando avaliações de fornecedores.
*   **`BacklogChamadoRepository.java`**: Repositório para a entidade `BacklogChamado`, utilizado para gerenciar o backlog de chamados.
*   **`BeneficioRepository.java`**: Repositório para a entidade `Beneficio`, responsável por operações de CRUD e consultas de benefícios.
*   **`CaixaRepository.java`**: Repositório para a entidade `Caixa`, gerenciando o fluxo de caixa.
*   **`CampanhaMarketingRepository.java`**: Repositório para a entidade `CampanhaMarketing`, utilizado para gerenciar campanhas de marketing.
*   **`CargoDepartamentoAssociacaoRepository.java`**: Repositório para a entidade `CargoDepartamentoAssociacao`, gerenciando associações entre cargos e departamentos.
*   **`CargoHierarquiaRepository.java`**: Repositório para a entidade `CargoHierarquia`, responsável pela hierarquia de cargos.
*   **`CargoRepository.java`**: Repositório para a entidade `Cargo`, gerenciando cargos.
*   **`CategoriaRepository.java`**: Repositório para a entidade `Categoria`, responsável por categorias.
*   **`ChamadoAnexoRepository.java`**: Repositório para a entidade `ChamadoAnexo`, gerenciando anexos de chamados.
*   **`ChamadoRepository.java`**: Repositório para a entidade `Chamado`, responsável por chamados.
*   **`ChatMessageRepository.java`**: Repositório para a entidade `ChatMessage`, gerenciando mensagens de chat.
*   **`ClienteRepository.java`**: Repositório para a entidade `Cliente`, responsável por clientes.
*   **`ColaboradorBeneficioRepository.java`**: Repositório para a entidade `ColaboradorBeneficio`, gerenciando benefícios de colaboradores.
*   **`ColaboradorRepository.java`**: Repositório para a entidade `Colaborador`, responsável por colaboradores.
*   **`ContaPagarRepository.java`**: Repositório para a entidade `ContaPagar`, gerenciando contas a pagar.
*   **`ContaReceberRepository.java`**: Repositório para a entidade `ContaReceber`, responsável por contas a receber.
*   **`ContratoAditivoRepository.java`**: Repositório para a entidade `ContratoAditivo`, gerenciando aditivos de contrato.
*   **`ContratoAlertaRepository.java`**: Repositório para a entidade `ContratoAlerta`, responsável por alertas de contrato.
*   **`ContratoFornecedorRepository.java`**: Repositório para a entidade `ContratoFornecedor`, gerenciando contratos de fornecedores.
*   **`ContratoLegalRepository.java`**: Repositório para a entidade `ContratoLegal`, responsável por contratos legais.
*   **`ContratoRepository.java`**: Repositório para a entidade `Contrato`, gerenciando contratos.
*   **`ConversaRepository.java`**: Repositório para a entidade `Conversa`, responsável por conversas.
*   **`CorrecaoPontoRepository.java`**: Repositório para a entidade `CorrecaoPonto`, gerenciando correções de ponto.
*   **`DepartamentoRepository.java`**: Repositório para a entidade `Departamento`, responsável por departamentos.
*   **`DevolucaoRepository.java`**: Repositório para a entidade `Devolucao`, gerenciando devoluções.
*   **`EscalaTrabalhoRepository.java`**: Repositório para a entidade `EscalaTrabalho`, responsável por escalas de trabalho.
*   **`FluxoCaixaRepository.java`**: Repositório para a entidade `FluxoCaixa`, gerenciando o fluxo de caixa.
*   **`FolhaPagamentoRepository.java`**: Repositório para a entidade `FolhaPagamento`, responsável por folhas de pagamento.
*   **`FormaPagamentoRepository.java`**: Repositório para a entidade `FormaPagamento`, gerenciando formas de pagamento.
*   **`FornecedorRepository.java`**: Repositório para a entidade `Fornecedor`, responsável por fornecedores.
*   **`HistoricoColaboradorRepository.java`**: Repositório para a entidade `HistoricoColaborador`, gerenciando histórico de colaboradores.
*   **`HistoricoContaPagarRepository.java`**: Repositório para a entidade `HistoricoContaPagar`, responsável por histórico de contas a pagar.
*   **`HistoricoContaReceberRepository.java`**: Repositório para a entidade `HistoricoContaReceber`, gerenciando histórico de contas a receber.
*   **`HistoricoProcessoAdesaoRepository.java`**: Repositório para a entidade `HistoricoProcessoAdesao`, responsável por histórico de processo de adesão.
*   **`HoleriteRepository.java`**: Repositório para a entidade `Holerite`, gerenciando holerites.
*   **`InventarioRepository.java`**: Repositório para a entidade `Inventario`, responsável por inventários.
*   **`MensagemRepository.java`**: Repositório para a entidade `Mensagem`, gerenciando mensagens.
*   **`MovimentacaoEstoqueRepository.java`**: Repositório para a entidade `MovimentacaoEstoque`, responsável por movimentações de estoque.
*   **`NotificacaoChatRepository.java`**: Repositório para a entidade `NotificacaoChat`, gerenciando notificações de chat.
*   **`NotificationRepository.java`**: Repositório para a entidade `Notification`, responsável por notificações.
*   **`ParticipanteConversaRepository.java`**: Repositório para a entidade `ParticipanteConversa`, gerenciando participantes de conversa.
*   **`PasswordResetTokenRepository.java`**: Repositório para a entidade `PasswordResetToken`, responsável por tokens de reset de senha.
*   **`PerfilRepository.java`**: Repositório para a entidade `Perfil`, gerenciando perfis.
*   **`PermissaoRepository.java`**: Repositório para a entidade `Permissao`, responsável por permissões.
*   **`PlanoSaudeRepository.java`**: Repositório para a entidade `PlanoSaude`, gerenciando planos de saúde.
*   **`PrestadorServicoRepository.java`**: Repositório para a entidade `PrestadorServico`, responsável por prestadores de serviço.
*   **`ProcessoAdesaoRepository.java`**: Repositório para a entidade `ProcessoAdesao`, gerenciando processos de adesão.
*   **`ProdutoRepository.java`**: Repositório para a entidade `Produto`, responsável por produtos.
*   **`ReacaoMensagemRepository.java`**: Repositório para a entidade `ReacaoMensagem`, gerenciando reações a mensagens.
*   **`RegistroPontoRepository.java`**: Repositório para a entidade `RegistroPonto`, responsável por registros de ponto.
*   **`SolicitacaoAcessoRepository.java`**: Repositório para a entidade `SolicitacaoAcesso`, gerenciando solicitações de acesso.
*   **`TicketMedioRepository.java`**: Repositório para a entidade `TicketMedio`, responsável por ticket médio.
*   **`TransferenciaEstoqueRepository.java`**: Repositório para a entidade `TransferenciaEstoque`, gerenciando transferências de estoque.
*   **`UsuarioRepository.java`**: Repositório para a entidade `Usuario`, responsável por usuários.
*   **`ValeRefeicaoRepository.java`**: Repositório para a entidade `ValeRefeicao`, gerenciando vales-refeição.
*   **`ValeTransporteRepository.java`**: Repositório para a entidade `ValeTransporte`, responsável por vales-transporte.
*   **`VendaItemRepository.java`**: Repositório para a entidade `VendaItem`, gerenciando itens de venda.
*   **`VendaRepository.java`**: Repositório para a entidade `Venda`, responsável por vendas.
*   **`agenda/EventoRepository.java`**: Repositório para a entidade `Evento` dentro do módulo de agenda.
*   **`indicadores/ViewInadimplenciaRepository.java`**: Repositório para a view `ViewInadimplencia` dentro do módulo de indicadores.
*   **`indicadores/ViewMargemLucroRepository.java`**: Repositório para a view `ViewMargemLucro` dentro do módulo de indicadores.
*   **`indicadores/ViewRoiMensalRepository.java`**: Repositório para a view `ViewRoiMensal` dentro do módulo de indicadores.
*   **`projetos/EquipeProjetoRepository.java`**: Repositório para a entidade `EquipeProjeto` dentro do módulo de projetos.
*   **`projetos/ProjetoRepository.java`**: Repositório para a entidade `Projeto` dentro do módulo de projetos.
*   **`projetos/TarefaProjetoRepository.java`**: Repositório para a entidade `TarefaProjeto` dentro do módulo de projetos.