# Documentação do Módulo `mapper`

O módulo `mapper` contém classes responsáveis por mapear objetos entre diferentes camadas da aplicação, geralmente entre entidades de domínio (models) e objetos de transferência de dados (DTOs). Esses mappers facilitam a conversão de dados, garantindo que as informações sejam transferidas de forma correta e eficiente, além de desacoplar as camadas da aplicação.

## Arquivos e suas Finalidades:

*   **`ContaPagarMapper.java`**: Responsável por mapear objetos `ContaPagar` (entidade de domínio) para `ContaPagarDto` (DTO) e vice-versa. Isso é útil para apresentar dados de contas a pagar na interface do usuário ou para receber dados de entrada para criar/atualizar contas.
*   **`VendaMapper.java`**: Responsável por mapear objetos `Venda` (entidade de domínio) para `VendaDTO` (DTO) e vice-versa. Este mapper é utilizado para converter dados de vendas para exibição ou para processar informações de vendas recebidas de requisições.