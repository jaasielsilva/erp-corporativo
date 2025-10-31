# Documentação do Módulo `formatter`

O módulo `formatter` contém classes responsáveis por formatar dados para exibição ou para processamento específico. Isso inclui a conversão de tipos de dados para representações de string e vice-versa, garantindo a consistência e a apresentação adequada das informações na aplicação.

## Arquivos e suas Finalidades:

*   **`BigDecimalFormatter.java`**: Um formatador personalizado para objetos `BigDecimal`, garantindo que valores monetários ou numéricos de alta precisão sejam exibidos e processados corretamente, com a formatação adequada (e.g., separadores de milhar, casas decimais).
*   **`WebConfig.java`**: Uma classe de configuração web que pode incluir a adição de formatadores personalizados (como o `BigDecimalFormatter`) ao contexto da aplicação Spring, garantindo que eles sejam aplicados automaticamente onde necessário.