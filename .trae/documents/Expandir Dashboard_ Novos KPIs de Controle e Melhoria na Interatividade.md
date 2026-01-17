Vou expandir o Dashboard Gerencial para oferecer o "Panorama Geral de Tudo" que você precisa para o controle total do escritório.

**1. Interatividade dos Cards (Toggle):**
*   Ajustarei o comportamento do clique: clicar no card abre os detalhes; clicar novamente fecha (toggle).
*   Adicionarei um indicador visual (setinha ou sombra) para mostrar qual card está ativo.

**2. Novos Cards de Controle (KPIs Expandidos):**
Vou adicionar uma nova linha de cards focada em **Produtividade e RH**, essencial para tomada de decisão:

*   **🟡 Prazos Críticos**: Processos próximos de expirar ou com prazos judiciais fatais.
*   **🔵 Taxa de Conversão**: % de Leads que viraram Clientes (Funil de Vendas).
*   **🟣 Produtividade Equipe**: Média de processos movimentados por funcionário hoje.
*   **🔴 Custo Operacional**: Gastos do mês (aluguel, sistemas, fixos) vs Faturamento.

**3. Implementação Técnica:**
*   Atualizarei o objeto `mockData` com dados fictícios para esses novos cards.
*   Ajustarei o CSS Grid para acomodar mais cards de forma responsiva.
*   Melhorarei a função `showDetails` para suportar a lógica de abrir/fechar.

O objetivo é transformar essa tela em um **Centro de Comando** onde você bate o olho e sabe a saúde financeira, operacional e humana do escritório.