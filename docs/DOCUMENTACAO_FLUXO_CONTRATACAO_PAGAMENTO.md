# Documentação do Fluxo: Contratação e Pagamento de Salários

Este documento descreve o ciclo completo de vida do colaborador no sistema, desde a sua contratação pelo RH até o efetivo recebimento do salário processado pelo Financeiro.

## 1. Visão Geral

O sistema integra os módulos de **Recursos Humanos (RH)** e **Financeiro** para garantir que os pagamentos sejam calculados corretamente e debitados das contas da empresa com rastreabilidade total.

### Atores Envolvidos
*   **RH (Analista/Gerente)**: Responsável por contratar, manter dados e processar a folha.
*   **Financeiro (Analista/Gerente/Diretor)**: Responsável por conferir saldos, aportar recursos e efetivar o pagamento.

---

## 2. Fluxo de Contratação (RH)

O processo começa com o cadastro do colaborador no sistema.

1.  **Acesse:** Menu Lateral > RH > Colaboradores > **Novo Colaborador**.
2.  **Preencha os Dados Pessoais:** Nome, CPF, Data de Nascimento, Endereço.
3.  **Dados Contratuais:**
    *   **Cargo:** Selecione o cargo (define permissões e nível hierárquico).
    *   **Salário Base:** Valor bruto mensal.
    *   **Data de Admissão:** Essencial para cálculos proporcionais no primeiro mês.
    *   **Tipo de Contrato:** CLT, PJ, Estágio.
4.  **Dados Bancários do Colaborador:**
    *   Informe Banco, Agência e Conta onde o salário será depositado.
5.  **Benefícios (Opcional):**
    *   Configure Vale Transporte, Vale Refeição e Plano de Saúde na aba de benefícios do colaborador.
6.  **Salvar:** O colaborador agora está **Ativo**.

---

## 3. Fluxo da Folha de Pagamento (RH)

Mensalmente, o RH deve gerar e processar a folha de pagamento.

1.  **Acesse:** Menu Lateral > RH > Folha de Pagamento > **Gerar Folha**.
2.  **Configuração:**
    *   Selecione o **Mês** e **Ano** de referência.
    *   Selecione o **Departamento** (opcional, para processar por setor).
3.  **Processar:** Clique em **Gerar Folha**.
    *   O sistema calcula automaticamente: INSS, IRRF, FGTS, Faltas, Atrasos e Benefícios.
    *   Status inicial: `EM_PROCESSAMENTO` → `PROCESSADA`.
4.  **Conferência:**
    *   Acesse a folha gerada em **Visualizar**.
    *   Confira os totais (Bruto, Descontos, Líquido).
    *   Verifique holerites individuais se necessário.
5.  **Fechar Folha:**
    *   Se tudo estiver correto, clique em **Fechar Folha**.
    *   Status muda para: `FECHADA`.
    *   *Neste ponto, a folha não pode mais ser recalculada.*
6.  **Enviar para Financeiro:**
    *   Clique no botão **Enviar Financeiro**.
    *   Status muda para: `ENVIADA_FINANCEIRO`.

---

## 4. Fluxo de Pagamento (Financeiro)

O Financeiro assume a responsabilidade após o envio da folha pelo RH.

### 4.1. Verificação de Saldo
Antes de pagar, verifique se há saldo nas contas bancárias da empresa.

1.  **Acesse:** Menu Lateral > Financeiro > **Contas Bancárias**.
2.  **Consulte:** Veja o saldo disponível em cada conta (Itaú, Santander, Nubank, etc.).
3.  **Aporte (Se necessário):**
    *   Se faltar saldo, clique em **Nova Movimentação**.
    *   Tipo: **Depósito (Crédito)**.
    *   Valor: Insira o montante.
    *   Descrição: Ex: "Aporte Capital de Giro".

### 4.2. Efetivar Pagamento
1.  **Acesse:** Menu Lateral > RH > Folha de Pagamento (ou através de notificação no Dashboard Financeiro).
2.  **Abra a Folha:** Selecione a folha com status `ENVIADA_FINANCEIRO`.
3.  **Pagar:**
    *   Clique no botão verde **Pagar Folha**.
    *   O sistema abrirá uma janela perguntando: **"De qual conta deseja debitar?"**.
4.  **Seleção de Conta:**
    *   Escolha a conta bancária com saldo suficiente.
    *   O sistema exibirá o Saldo Atual da conta para ajudar na decisão.
5.  **Confirmar:**
    *   Ao confirmar, o sistema realiza três ações atômicas:
        1.  **Debita** o valor total líquido da conta bancária selecionada.
        2.  **Cria um registro** de saída no Fluxo de Caixa (Categoria: "Salários").
        3.  **Atualiza** o status da folha para `PAGA` e define a **Data de Pagamento**.

---

## 5. Pós-Pagamento e Auditoria

*   **Holerites:** Após o pagamento, os holerites ficam disponíveis para os colaboradores (via Portal do Colaborador ou envio por e-mail).
*   **Fluxo de Caixa:** O pagamento aparece nos relatórios financeiros como despesa realizada.
*   **Histórico:** A folha permanece no sistema com status `PAGA` para consultas futuras e geração de informes de rendimentos.

---

## Resumo dos Status da Folha

| Status | Descrição | Responsável |
| :--- | :--- | :--- |
| `EM_PROCESSAMENTO` | O sistema está calculando os valores. | Sistema |
| `PROCESSADA` | Cálculos finalizados, aguardando conferência. | RH |
| `FECHADA` | Valores conferidos e travados. Não aceita mais edições. | RH |
| `ENVIADA_FINANCEIRO` | Liberada para pagamento. | RH -> Financeiro |
| `PAGA` | Valor debitado do caixa da empresa. Ciclo encerrado. | Financeiro |
