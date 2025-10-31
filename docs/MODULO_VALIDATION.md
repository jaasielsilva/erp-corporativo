# Documentação do Módulo `validation`

O módulo `validation` contém classes e anotações personalizadas para validação de dados na aplicação. Ele garante que os dados de entrada atendam a critérios específicos antes de serem processados, contribuindo para a integridade e consistência das informações.

## Arquivos e suas Finalidades:

*   **`AtualizacaoStatusValidator.java`**: Implementa a lógica de validação para a atualização de status. Garante que as transições de status sejam válidas de acordo com as regras de negócio definidas.
*   **`ValidAtualizacaoStatus.java`**: Anotação personalizada que pode ser usada para aplicar a validação `AtualizacaoStatusValidator` a campos ou métodos específicos. Facilita a aplicação de regras de validação de forma declarativa.