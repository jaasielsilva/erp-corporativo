# Documentação do Módulo `exception`

O módulo `exception` centraliza o tratamento de exceções personalizadas e globais da aplicação. Ele define classes de exceção específicas para cenários de negócio e manipuladores globais para garantir uma resposta consistente e amigável ao usuário em caso de erros.

## Arquivos e suas Finalidades:

*   **`ApiExceptionHandler.java`**: Um manipulador de exceções específico para APIs, que pode formatar as respostas de erro de forma padronizada (e.g., JSON) para clientes RESTful.
*   **`BusinessValidationException.java`**: Uma exceção personalizada para erros de validação de regras de negócio. É lançada quando uma operação não atende aos requisitos de negócio da aplicação.
*   **`CargoNotFoundException.java`**: Exceção personalizada lançada quando um `Cargo` específico não é encontrado no sistema, geralmente em operações de busca ou atualização.
*   **`ColaboradorNotFoundException.java`**: Exceção personalizada lançada quando um `Colaborador` específico não é encontrado no sistema.
*   **`DepartamentoNotFoundException.java`**: Exceção personalizada lançada quando um `Departamento` específico não é encontrado no sistema.
*   **`GlobalExceptionHandler.java`**: Um manipulador de exceções global que captura e processa exceções não tratadas em toda a aplicação, fornecendo uma camada de tratamento de erros centralizada e fallback.