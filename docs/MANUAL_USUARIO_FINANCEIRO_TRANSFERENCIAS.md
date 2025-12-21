# Manual de Uso: Financeiro Transferência

A funcionalidade **Financeiro Transferência** serve para registrar a movimentação de valores entre contas internas da própria empresa (ex: do "Caixa Físico" para o "Banco Itaú"), garantindo que os saldos estejam sempre atualizados e que o fluxo de caixa reflita essa operação corretamente.

## 1. Para que serve?

*   **Movimentação Interna:** Mover dinheiro entre contas da empresa sem gerar receita ou despesa (apenas troca de "bolso").
*   **Controle de Saldos:** Atualiza automaticamente o saldo da conta de origem (subtrai) e da conta de destino (soma).
*   **Rastreabilidade:** Cria registros no **Fluxo de Caixa** (uma "SAÍDA" na origem e uma "ENTRADA" no destino) para que você saiba exatamente para onde o dinheiro foi.

---

## 2. Exemplos de Uso

### Exemplo A: Sangria de Caixa (Depósito Bancário)
**Cenário:** Você tem muito dinheiro em espécie na loja e precisa depositar no banco.

*   **Conta Origem:** Caixa Loja (Espécie)
*   **Conta Destino:** Banco Santander (Conta Corrente)
*   **Valor:** R$ 5.000,00
*   **Descrição:** "Depósito do movimento do dia 20/12"
*   **Resultado:** O saldo do "Caixa Loja" diminui R$ 5k e o do "Santander" aumenta R$ 5k.

### Exemplo B: Reposição de Fundo Fixo (Caixinha)
**Cenário:** Você sacou dinheiro do banco para deixar no caixa pequeno para despesas miúdas.

*   **Conta Origem:** Banco Itaú
*   **Conta Destino:** Caixinha (Petty Cash)
*   **Valor:** R$ 500,00
*   **Descrição:** "Reposição de fundo fixo para café/materiais"

---

## 3. Como funciona no Sistema (Fluxo Técnico)

Quando você realiza uma transferência na tela `/financeiro/transferencias`, o sistema executa os seguintes passos:

1.  **Validação:**
    *   Verifica se a **Conta Origem** tem saldo suficiente (se não tiver, o sistema bloqueia com a mensagem "Saldo insuficiente na conta de origem").
    *   Impede que a origem e o destino sejam a mesma conta.

2.  **Processamento:**
    *   Debita o valor da conta de origem.
    *   Credita o valor na conta de destino.

3.  **Registros Automáticos:**
    *   Cria um registro na tabela `Transferencia` (histórico).
    *   Cria **dois** lançamentos no `Fluxo de Caixa`:
        *   1x **SAÍDA** na conta de origem (Categoria: TRANSFERENCIA).
        *   1x **ENTRADA** na conta de destino (Categoria: TRANSFERENCIA).

---

## 4. Campos Necessários

Para usar, você precisa preencher:

*   **Conta de Origem**: De onde o dinheiro sai.
*   **Conta de Destino**: Para onde o dinheiro vai.
*   **Valor**: O montante a ser transferido (deve ser positivo).
*   **Data**: Quando a transferência ocorreu.
*   **Descrição**: Um texto curto para identificar a operação nos extratos.
