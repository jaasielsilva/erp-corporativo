# Documentação do Módulo `dto`

O módulo `dto` (Data Transfer Object) contém classes utilizadas para transferir dados entre diferentes camadas da aplicação, como entre a camada de serviço e a camada de apresentação (controladores/APIs), ou entre a aplicação e clientes externos. Essas classes são projetadas para encapsular dados de forma eficiente e específica para cada caso de uso, evitando a exposição de entidades de domínio completas e simplificando a comunicação.

## Arquivos e suas Finalidades:

*   **`AcaoUsuarioDTO.java`**: DTO para ações de usuário, possivelmente para registrar ou exibir atividades de usuários.
*   **`AdesaoColaboradorDTO.java`**: DTO para dados de adesão de colaboradores, usado no processo de integração de novos funcionários.
*   **`AdesaoDTO.java`**: DTO genérico para processos de adesão, pode ser uma base ou um DTO para um tipo específico de adesão.
*   **`AtualizarStatusRequest.java`**: DTO para requisições de atualização de status, comum em módulos como chamados ou tarefas.
*   **`CategoriaChamadoDTO.java`**: DTO para categorias de chamados, usado para classificar e organizar os tipos de solicitações de suporte.
*   **`ChamadoDTO.java`**: DTO para dados de chamados, contendo informações detalhadas sobre uma solicitação de suporte.
*   **`ChamadoStatusResponse.java`**: DTO para a resposta de status de um chamado, indicando o estado atual de uma solicitação.
*   **`ClienteDTO.java`**: DTO para dados de clientes, usado para representar informações de clientes em operações de CRUD ou exibição.
*   **`ColaboradorSimpleDTO.java`**: DTO simplificado para colaboradores, contendo apenas informações essenciais para listagens ou seleções.
*   **`ConfiguracaoValeTransporteDTO.java`**: DTO para configurações relacionadas a vale-transporte, como valores e regras.
*   **`ContaPagarDto.java`**: DTO para contas a pagar, usado no módulo financeiro para gerenciar despesas.
*   **`ConversaDTO.java`**: DTO para dados de conversas, utilizado no módulo de chat para representar informações de uma conversa.
*   **`CorrecaoPontoCreateDTO.java`**: DTO para a criação de solicitações de correção de ponto, no módulo de RH.
*   **`CorrecaoPontoListDTO.java`**: DTO para listar solicitações de correção de ponto.
*   **`CorrecaoPontoResumoDTO.java`**: DTO para um resumo de solicitações de correção de ponto.
*   **`DependenteBeneficioDTO.java`**: DTO para dependentes de benefícios, usado para gerenciar informações de dependentes em planos de benefícios.
*   **`DigitacaoNotificationDTO.java`**: DTO para notificações de digitação em tempo real, comum em módulos de chat.
*   **`DigitandoEventoDTO.java`**: DTO para eventos de digitação, indicando que um usuário está digitando em uma conversa.
*   **`EstatisticasDashboard.java`**: DTO para dados estatísticos exibidos em dashboards, agregando informações de diversos módulos.
*   **`EstatisticasUsuariosDTO.java`**: DTO para estatísticas relacionadas a usuários, como número de usuários ativos, etc.
*   **`FornecedorDTO.java`**: DTO para dados de fornecedores, usado para representar informações de fornecedores.
*   **`MemberDTO.java`**: DTO para membros, possivelmente em contextos de equipe ou grupos.
*   **`MensagemDTO.java`**: DTO para dados de mensagens, contendo o conteúdo e metadados de uma mensagem de chat.
*   **`MovimentacaoEstoqueDTO.java`**: DTO para movimentações de estoque, registrando entradas, saídas e transferências de produtos.
*   **`NotificacaoChatDTO.java`**: DTO para notificações de chat, informando sobre novas mensagens ou eventos no chat.
*   **`ProdutoDTO.java`**: DTO para dados de produtos, usado para representar informações de produtos.
*   **`ReacaoNotificationDTO.java`**: DTO para notificações de reações a mensagens, como curtidas ou outros emojis.
*   **`ResumoBeneficiariosDTO.java`**: DTO para um resumo de beneficiários, possivelmente em planos de saúde ou outros benefícios.
*   **`ResumoValeTransporteDTO.java`**: DTO para um resumo de vale-transporte.
*   **`SolicitacaoSimpleDTO.java`**: DTO simplificado para solicitações, usado para representar informações básicas de uma solicitação.
*   **`TicketMedioDTO.java`**: DTO para o cálculo ou exibição do ticket médio de vendas.
*   **`UsuarioDTO.java`**: DTO para dados de usuários, contendo informações gerais de um usuário.
*   **`UsuarioFormDTO.java`**: DTO para formulários de usuário, usado para coletar dados de entrada do usuário.
*   **`UsuarioPDFDTO.java`**: DTO para dados de usuário formatados para geração de PDF.
*   **`ValeTransporteListDTO.java`**: DTO para listar informações de vale-transporte.
*   **`VendaCategoriaDTO.java`**: DTO para vendas por categoria, usado para análises de vendas.
*   **`VendaDTO.java`**: DTO para dados de vendas, contendo informações detalhadas sobre uma transação de venda.
*   **`VendaFormDTO.java`**: DTO para formulários de venda, usado para coletar dados de entrada para uma nova venda.
*   **`VendaItemDTO.java`**: DTO para itens de venda, representando um produto dentro de uma venda.
*   **`VendaItemFormDTO.java`**: DTO para formulários de itens de venda.
*   **`VendaPdvRequest.java`**: DTO para requisições de venda no Ponto de Venda (PDV).
*   **`VendaPdvResponse.java`**: DTO para respostas de venda no Ponto de Venda (PDV).