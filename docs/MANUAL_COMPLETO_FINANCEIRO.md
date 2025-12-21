# Manual Completo do Módulo Financeiro - ERP Corporativo

## 1. Visão Geral
O Módulo Financeiro do ERP Corporativo é responsável pela gestão integral dos recursos financeiros da empresa, garantindo controle, rastreabilidade e visão estratégica. Ele opera de forma integrada com outros módulos (RH, Vendas) e oferece ferramentas para gestão de contas, fluxo de caixa e relatórios gerenciais.

### 1.1 Público Alvo
- **Gestores Financeiros**: Acesso total a configurações, aprovações e relatórios.
- **Analistas Financeiros**: Operação diária de contas a pagar/receber e conciliação.
- **Diretoria (CEO/Master)**: Visualização de Dashboards e Relatórios Estratégicos (DRE).

---

## 2. Ciclo de Vida Financeiro (Fluxograma)

O diagrama abaixo ilustra o fluxo principal dos dados financeiros dentro do sistema:

```mermaid
graph TD
    A[Origem dos Dados] --> B{Tipo de Operação}
    
    B -- Compra/Despesa --> C[Contas a Pagar]
    B -- Venda/Serviço --> D[Contas a Receber]
    B -- Folha RH --> C
    B -- Transferência Interna --> E[Transferências]
    
    C -- Pagamento --> F[Fluxo de Caixa (Saída)]
    D -- Recebimento --> F[Fluxo de Caixa (Entrada)]
    E -- Movimentação --> F[Fluxo de Caixa (Entrada/Saída)]
    
    F --> G[Dashboards e KPIs]
    F --> H[Relatórios Gerenciais]
    H --> I[DRE (Regime de Caixa)]
    H --> J[Fluxo Diário]
```

---

## 3. Funcionalidades e Módulos

### 3.1 Dashboard Financeiro
Painel central para visualização rápida da saúde financeira.
- **KPIs**: Saldo Atual, Total a Receber, Contas Vencidas.
- **Gráficos**: Evolução do Fluxo de Caixa (últimos 30 dias).
- **Acesso Rápido**: Atalhos para as funções mais utilizadas.

### 3.2 Contas a Pagar
Gerenciamento de todas as obrigações financeiras da empresa.

#### Campos Principais:
- **Fornecedor**: Entidade credora.
- **Valor Original**: Valor facial do título.
- **Data Vencimento**: Data limite para pagamento.
- **Categoria**: Classificação (Ex: Fornecedores, Impostos, Salários).
- **Status**:
    - `PENDENTE`: Aguardando aprovação/pagamento.
    - `APROVADA`: Liberada para pagamento.
    - `PAGA`: Liquidada (gera movimento no Fluxo de Caixa).
    - `CANCELADA`: Título anulado.

#### Ciclo de Uso:
1. **Cadastro**: Manual ou via Integração (Ex: RH).
2. **Aprovação**: Validação pelo gestor.
3. **Baixa (Pagamento)**: Registro da saída do recurso, informando juros/multa/desconto.

### 3.3 Contas a Receber
Gestão das entradas previstas.

#### Campos Principais:
- **Cliente**: Entidade devedora.
- **Valor**: Valor a receber.
- **Status**:
    - `PENDENTE`: Aguardando recebimento.
    - `RECEBIDA`: Valor entrou em conta.
    - `VENCIDA`: Data de vencimento expirada sem pagamento.
    - `INADIMPLENTE`: Atraso crítico.

### 3.4 Transferências (Novo)
Gerenciamento de movimentações entre contas bancárias internas da empresa.
> **Para detalhes completos e exemplos, consulte:** [Manual de Uso: Financeiro Transferência](./MANUAL_USUARIO_FINANCEIRO_TRANSFERENCIAS.md)

#### Funcionalidade:
Permite mover valores entre "Caixa", "Banco Itaú", "Banco Santander", etc., mantendo o saldo conciliado.

#### Fluxo Lógico:
1. Usuário seleciona **Conta Origem** e **Conta Destino**.
2. Sistema valida se há saldo na origem.
3. Sistema cria automaticamente:
    - Um movimento de **SAÍDA** na conta de origem.
    - Um movimento de **ENTRADA** na conta de destino.
    - Categoria automática: `TRANSFERENCIA`.

### 3.5 Fluxo de Caixa
O coração financeiro do sistema. Registra todas as movimentações efetivas (Regime de Caixa).
- **Entradas**: Vendas, Rendimentos, Aportes.
- **Saídas**: Pagamentos, Transferências, Taxas.
- **Sincronização**: Pode ser alimentado automaticamente pela baixa de Contas a Pagar/Receber.

---

## 4. Relatórios Gerenciais

O sistema oferece relatórios avançados para tomada de decisão, baseados nos dados do Fluxo de Caixa.

### 4.1 DRE (Demonstrativo do Resultado do Exercício)
Visão vertical do resultado financeiro no período selecionado.
- **Regime**: Caixa (considera o que efetivamente entrou/saiu).
- **Estrutura**:
    1. **Receita Bruta**: Soma de Vendas e Serviços.
    2. **(-) Impostos**: Deduções fiscais.
    3. **= Receita Líquida**.
    4. **(-) Custos Variáveis**: Fornecedores, Comissões.
    5. **= Margem de Contribuição**.
    6. **(-) Despesas Fixas**: Aluguel, Salários, Energia.
    7. **= Resultado Operacional (EBITDA)**.

### 4.2 Fluxo de Caixa Diário
Visão horizontal da evolução do saldo dia a dia.
- Mostra Saldo Inicial, Entradas do Dia, Saídas do Dia e Saldo Final acumulado.
- Essencial para gestão de liquidez e tesouraria.

---

## 5. Casos de Uso Reais

### Caso 1: Pagamento de Folha Salarial (Integração RH)
1. **RH**: Processa a folha e clica em "Enviar Financeiro".
2. **Sistema**: Cria automaticamente uma conta a pagar na categoria `SALARIOS`.
3. **Financeiro**: Acessa "Contas a Pagar", confere o valor e realiza a baixa.
4. **Resultado**: O valor sai do saldo bancário e entra como despesa no DRE (Despesas Fixas > Salários).

### Caso 2: Transferência de Aplicação
1. **Cenário**: Empresa quer mover R$ 10.000 do "Itaú" para "Santander".
2. **Ação**: Acessar `Transferências > Nova Transferência`.
3. **Preenchimento**: Origem: Itaú, Destino: Santander, Valor: 10.000.
4. **Resultado**: Saldo do Itaú diminui, Santander aumenta. No fluxo de caixa, aparecem dois movimentos que se anulam para efeito de resultado, mas ajustam os saldos por conta.

### Caso 3: Análise de Lucratividade
1. **Diretoria**: Deseja saber o lucro real do mês passado.
2. **Ação**: Acessar `Relatórios > DRE`.
3. **Filtro**: Selecionar o dia 01 ao 30 do mês anterior.
4. **Análise**: O sistema calcula todas as receitas recebidas menos todas as despesas pagas e exibe o Resultado Operacional.

---

## 6. Glossário de Campos e Termos

| Termo | Definição |
|-------|-----------|
| **Data de Competência** | Mês/Ano a que se refere a despesa/receita (importante para DRE Contábil, futuro). |
| **Data de Pagamento** | Data efetiva da saída do dinheiro (usado no DRE Caixa). |
| **Hash de Integridade** | Código único gerado para garantir que dados vindos do RH não foram alterados. |
| **Centro de Custo** | (Futuro) Classificação por departamento para rateio de despesas. |
| **Conciliação** | Processo de conferir se o saldo do sistema bate com o extrato bancário real. |

---

## 7. Manutenção e Suporte

Para problemas técnicos ou dúvidas de operação:
1. Consulte este manual.
2. Verifique os logs de auditoria em `Configurações > Auditoria`.
3. Contate o suporte técnico interno (TI).
