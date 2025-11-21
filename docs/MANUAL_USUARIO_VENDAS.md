# Módulo Vendas — Manual de Uso

## Acesso e Permissões
- Acesso visível no menu quando `podeAcessarVendas` for verdadeiro.
- Quem acessa: usuários de Vendas/Comercial, níveis Gerenciais, `ADMIN` e `MASTER`.
- Quem gerencia (criar/editar operações avançadas): `podeGerenciarVendas` (Gerencial, `ADMIN`, `MASTER`).
- `MASTER` possui acesso total.

## Navegação
- Sidebar → Comercial → Vendas
  - `Vendas → Dashboard` (`/vendas`)
  - `Vendas → PDV` (`/vendas/pdv`)
  - `Vendas → Pedidos` (`/vendas/pedidos`)
  - `Vendas → Relatórios` (`/vendas/relatorios`)

## Dashboard de Vendas (`/vendas`)
- Cartões:
  - `Faturamento do Dia`: exibe valor de faturamento (aproximação via indicadores); atualizado via API.
  - `Pedidos Hoje`: quantidade de vendas/recebimentos recentes.
  - `Ticket Médio`: valor médio por venda.
  - `Itens Vendidos`: contagem de itens (placeholder até integração com itens do pedido).
- Ações:
  - `Abrir PDV`: direciona para registro rápido de vendas.
  - `Ver Pedidos`: navega para listagem de pedidos.
  - `Relatórios`: navega para geração de relatórios.

## PDV (`/vendas/pdv`)
- Campos:
  - `Produto`: busca por nome/código.
  - `Quantidade`: número de unidades.
  - Botão `Adicionar`: inclui o item na venda atual.
- Tabela de Itens:
  - Colunas `Item`, `Qtd`, `Preço`, `Total` e `Ações`.
- Ações de Fechamento:
  - `Cancelar`: descarta venda em andamento.
  - `Finalizar`: confirma venda (integração futura com pagamentos/estoque).

## Pedidos (`/vendas/pedidos`)
- Filtros:
  - `Busca`: por cliente ou número do pedido.
  - `Status`: `Aguardando`, `Pago`, `Cancelado`.
- Tabela:
  - `Nº Pedido`, `Cliente`, `Data`, `Status`, `Total`, `Ações`.
  - Botão `Detalhes` para visualizar informações do pedido.

## Relatórios (`/vendas/relatorios`)
- Filtros:
  - `Período`: `Últimos 30 dias`, `3 meses`, `12 meses`.
  - `Categoria`: filtra por categoria de produto/receita.
  - `Canal`: `Loja`, `Online`, `Representante`.
- Ação:
  - `Gerar Relatório`: aplica filtros e exibe resultados.

## Dicas de Uso
- `Dashboard` utiliza dados consolidados por período; para visão detalhada use `Relatórios`.
- Para registrar vendas rápidas do balcão, use o `PDV`.
- `Pedidos` permite acompanhar o ciclo de vida de cada venda.

## Futuras Integrações
- Itens do pedido e estoque para cálculo real de `Itens Vendidos`.
- Integração com pagamentos para status financeiro dos pedidos.