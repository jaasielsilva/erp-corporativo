# Documentação do Módulo `model`

O módulo `model` contém as classes que representam as entidades de dados da aplicação. Essas classes são a base para a persistência de dados, definindo a estrutura das informações que são armazenadas e manipuladas pelo sistema. Elas incluem desde entidades básicas de usuário e produto até estruturas mais complexas para controle de estoque, finanças e recursos humanos.

## Arquivos e suas Finalidades:

### Entidades Principais:

*   **`AcaoUsuario.java`**: Representa uma ação realizada por um usuário no sistema, para fins de auditoria ou rastreabilidade.
*   **`AdesaoPlanoSaude.java`**: Entidade que registra a adesão de um colaborador a um plano de saúde.
*   **`AlertaEstoque.java`**: Entidade que representa um alerta gerado quando o estoque de um produto atinge um nível crítico (mínimo ou máximo).
*   **`AuditoriaEstoque.java`**: Entidade para registrar auditorias de estoque, incluindo informações sobre quem realizou a auditoria, a data e os resultados.
*   **`AvaliacaoFornecedor.java`**: Entidade para registrar a avaliação de um fornecedor.
*   **`BacklogChamado.java`**: Representa um item no backlog de chamados, para gerenciamento de tarefas de suporte.
*   **`BaseEntity.java`**: Classe base abstrata para outras entidades, fornecendo campos comuns como ID e datas de criação/atualização.
*   **`Beneficio.java`**: Entidade que representa um benefício oferecido aos colaboradores.
*   **`Caixa.java`**: Entidade para controle de caixa, registrando entradas e saídas de dinheiro.
*   **`CampanhaCliente.java`**: Representa a associação de um cliente a uma campanha de marketing.
*   **`CampanhaMarketing.java`**: Entidade que define uma campanha de marketing.
*   **`CampanhaMetrica.java`**: Entidade para registrar métricas de desempenho de campanhas de marketing.
*   **`Cargo.java`**: Entidade que representa um cargo dentro da organização.
*   **`CargoDepartamentoAssociacao.java`**: Entidade para associar cargos a departamentos.
*   **`CargoHierarquia.java`**: Entidade para definir a hierarquia entre cargos.
*   **`Categoria.java`**: Entidade para categorizar produtos ou serviços.
*   **`Chamado.java`**: Entidade que representa um chamado de suporte ou serviço.
*   **`ChatMessage.java`**: Entidade que representa uma mensagem em um chat.
*   **`Cliente.java`**: Entidade que representa um cliente da empresa.
*   **`Colaborador.java`**: Entidade que representa um colaborador da empresa.
*   **`ColaboradorBeneficio.java`**: Entidade para associar colaboradores a benefícios.
*   **`ColaboradorEscala.java`**: Entidade para definir a escala de trabalho de um colaborador.
*   **`ContaPagar.java`**: Entidade que representa uma conta a pagar.
*   **`ContaReceber.java`**: Entidade que representa uma conta a receber.
*   **`Contrato.java`**: Entidade que representa um contrato.
*   **`ContratoAditivo.java`**: Entidade para registrar aditivos a um contrato.
*   **`ContratoAlerta.java`**: Entidade para alertas relacionados a contratos.
*   **`ContratoFornecedor.java`**: Entidade para associar contratos a fornecedores.
*   **`ContratoLegal.java`**: Entidade para contratos com aspectos legais.
*   **`Conversa.java`**: Entidade que representa uma conversa de chat.
*   **`CorrecaoPonto.java`**: Entidade para registrar solicitações de correção de ponto.
*   **`Departamento.java`**: Entidade que representa um departamento da organização.
*   **`DependentePlanoSaude.java`**: Entidade para registrar dependentes em planos de saúde.
*   **`DescontoFolha.java`**: Entidade para registrar descontos na folha de pagamento.
*   **`Devolucao.java`**: Entidade que representa uma devolução de produto.
*   **`DevolucaoItem.java`**: Entidade para registrar itens de uma devolução.
*   **`EscalaTrabalho.java`**: Entidade que define uma escala de trabalho.
*   **`FluxoCaixa.java`**: Entidade para registrar o fluxo de caixa.
*   **`FolhaPagamento.java`**: Entidade que representa uma folha de pagamento.
*   **`FormaPagamento.java`**: Entidade para definir formas de pagamento.
*   **`Fornecedor.java`**: Entidade que representa um fornecedor.
*   **`Genero.java`**: Enumeração para representar o gênero.
*   **`HistoricoColaborador.java`**: Entidade para registrar o histórico de um colaborador.
*   **`HistoricoContaPagar.java`**: Entidade para registrar o histórico de uma conta a pagar.
*   **`HistoricoContaReceber.java`**: Entidade para registrar o histórico de uma conta a receber.
*   **`HistoricoProcessoAdesao.java`**: Entidade para registrar o histórico de um processo de adesão.
*   **`Holerite.java`**: Entidade que representa um holerite.
*   **`Inventario.java`**: Entidade que representa um inventário de estoque.
*   **`Mensagem.java`**: Entidade que representa uma mensagem.
*   **`MovimentacaoEstoque.java`**: Entidade para registrar movimentações de estoque.
*   **`NivelAcesso.java`**: Entidade para definir níveis de acesso.
*   **`NotificacaoChat.java`**: Entidade para notificações de chat.
*   **`Notification.java`**: Entidade genérica para notificações.
*   **`ParteEnvolvida.java`**: Entidade para representar uma parte envolvida em um processo ou contrato.
*   **`ParticipanteConversa.java`**: Entidade para associar participantes a uma conversa.
*   **`PasswordResetToken.java`**: Entidade para tokens de redefinição de senha.
*   **`Perfil.java`**: Entidade que representa um perfil de usuário.
*   **`Permissao.java`**: Entidade que representa uma permissão de acesso.
*   **`PlanoSaude.java`**: Entidade que representa um plano de saúde.
*   **`PrestadorServico.java`**: Entidade que representa um prestador de serviços.
*   **`ProcessoAdesao.java`**: Entidade que representa um processo de adesão.
*   **`Produto.java`**: Entidade que representa um produto.
*   **`ReacaoMensagem.java`**: Entidade para registrar reações a mensagens.
*   **`RegistroPonto.java`**: Entidade para registrar o ponto de um colaborador.
*   **`SolicitacaoAcesso.java`**: Entidade para solicitações de acesso.
*   **`Status.java`**: Enumeração para representar status genéricos.
*   **`StatusCaixa.java`**: Enumeração para representar o status do caixa.
*   **`StatusContrato.java`**: Enumeração para representar o status de um contrato.
*   **`StatusVenda.java`**: Enumeração para representar o status de uma venda.
*   **`TipoContrato.java`**: Enumeração para representar tipos de contrato.
*   **`TipoMovimentacao.java`**: Enumeração para representar tipos de movimentação.
*   **`TransferenciaEstoque.java`**: Entidade para registrar transferências de estoque.
*   **`Usuario.java`**: Entidade que representa um usuário do sistema.
*   **`ValeRefeicao.java`**: Entidade que representa um vale-refeição.
*   **`ValeTransporte.java`**: Entidade que representa um vale-transporte.
*   **`Venda.java`**: Entidade que representa uma venda.
*   **`VendaItem.java`**: Entidade para registrar itens de uma venda.

### Subdiretórios de Modelos:

*   **`agenda/`**: Contém modelos relacionados à agenda.
*   **`estoque/`**: Contém modelos relacionados ao estoque (já documentados em `MODULO_ESTOQUE.md`).
*   **`indicadores/`**: Contém modelos relacionados a indicadores (já documentados em `MODULO_INDICADORES.md`).
*   **`projetos/`**: Contém modelos relacionados a projetos.
*   **`suporte/`**: Contém modelos relacionados a suporte.
*   **`ti/`**: Contém modelos relacionados a TI (já documentados em `MODULO_TI.md`).
*   **`vendas/`**: Contém modelos relacionados a vendas.