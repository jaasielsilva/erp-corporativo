# Documentação do Módulo `estoque`

O módulo `estoque` é responsável por gerenciar todas as operações relacionadas ao inventário e movimentação de produtos. Ele inclui entidades para alertas, auditorias, inventários, movimentações e transferências de estoque, garantindo o controle e a rastreabilidade dos itens.

## Arquivos e suas Finalidades:

*   **`AlertaEstoque.java`**: Entidade que representa um alerta gerado quando o estoque de um produto atinge um nível crítico (mínimo ou máximo).
*   **`AuditoriaEstoque.java`**: Entidade para registrar auditorias de estoque, incluindo informações sobre quem realizou a auditoria, a data e os resultados.
*   **`Inventario.java`**: Entidade que representa um inventário de estoque, registrando a contagem física dos produtos em um determinado momento.
*   **`MovimentacaoEstoque.java`**: Entidade para registrar todas as movimentações de estoque, como entradas (compras, devoluções) e saídas (vendas, perdas).
*   **`TransferenciaEstoque.java`**: Entidade para registrar transferências de produtos entre diferentes locais de armazenamento ou depósitos.