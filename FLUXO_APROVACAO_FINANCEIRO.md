# Fluxo de Pagamento: Da Contratação ao Recebimento (Guia Completo)

Este documento serve como um roteiro passo-a-passo para validar o ciclo de vida financeiro de um colaborador no sistema.

## 📊 Fluxograma Visual Completo

```mermaid
graph TD
    %% Módulo RH - Contratação
    subgraph RH_CADASTRO [1. RH: Contratação]
        A[Início] -->|Acessar RH > Colaboradores > Novo| B(Formulário de Cadastro)
        B -->|Preencher: Nome, CPF, Salário, Cargo| C{Salvar?}
        C -->|Erro| B
        C -->|Sucesso| D[Colaborador: ATIVO]
    end

    %% Módulo RH - Folha
    subgraph RH_FOLHA [2. RH: Folha de Pagamento]
        D -->|Fim do Mês| E[Gerar Folha]
        E -->|Selecionar Competência| F[Status: EM PROCESSAMENTO]
        F --> G[Cálculo Automático]
        G --> H{Validar Valores}
        H -- Divergência --> I[Editar Lançamentos]
        I --> G
        H -- OK --> J[Fechar Folha]
        J -->|Status: FECHADA| K[Enviar para Financeiro]
    end

    %% Integração
    K -->|API de Integração| L[Cria Conta a Pagar: PENDENTE]

    %% Módulo Financeiro
    subgraph FIN_PAGAMENTO [3. Financeiro: Pagamento]
        L --> M{Quem vai Aprovar?}
        
        %% Cenário 2
        M -- Mesmo Usuário que Criou (Não Master) --> N[❌ BLOQUEIO DE SEGREGAÇÃO]
        
        %% Cenário 3
        M -- Gestor Financeiro (Diferente)| O{Análise do Gestor}
        O -- Rejeitar (Erro de Valor) --> P[Status: CANCELADA]
        P --> Q[Devolver para RH]
        
        %% Cenário 1
        O -- Aprovar (Tudo OK) --> R[Status: APROVADA]
        R --> S[Executar Pagamento]
        S -->|Selecionar Conta Bancária| T[Status: PAGA]
    end

    %% Conclusão
    T --> U[✅ Dinheiro na Conta]
    N --> V[⛔ Ação Impedida]
    Q --> W[⚠️ Reiniciar Ciclo]

    classDef success fill:#d4edda,stroke:#155724,stroke-width:2px;
    classDef error fill:#f8d7da,stroke:#721c24,stroke-width:2px;
    classDef warning fill:#fff3cd,stroke:#856404,stroke-width:2px;
    
    class U success;
    class N,V error;
    class Q,W warning;
```

---

## 📝 Dados para Preenchimento (Template)

Para executar os cenários, utilize os dados abaixo como padrão. Isso garante que os cálculos de impostos (INSS/IRRF) sejam previsíveis.

### Dados do Colaborador (Fictício)
| Campo | Valor Sugerido |
| :--- | :--- |
| **Nome** | `João da Silva Teste` |
| **CPF** | `123.456.789-00` (Use um gerador de CPF válido se houver validação real) |
| **Email** | `joao.teste@empresa.com` |
| **Data de Admissão** | `01/01/2024` (Início do ano corrente) |
| **Cargo** | `Analista de Sistemas` (Selecione um existente) |
| **Departamento** | `TI` (Selecione um existente) |
| **Salário Base** | `R$ 5.000,00` |
| **Dependentes** | `0` |

---

## 🟢 Cenário 1: Ciclo Completo com Sucesso
**Objetivo:** Validar o "Caminho Feliz" onde tudo ocorre como esperado.

### Parte A: RH (Contratação e Folha)
| Passo | Tela / Ação | Dados a Inserir | Check |
| :--- | :--- | :--- | :--- |
| 1 | **RH > Colaboradores > Novo** | Preencher todos os dados da tabela "Dados do Colaborador" acima. | [ ] |
| 2 | Clicar em **Salvar** | Sistema deve exibir mensagem de sucesso. | [ ] |
| 3 | **RH > Folha de Pagamento > Gerar** | **Mês:** Mês Atual<br>**Ano:** Ano Atual | [ ] |
| 4 | Clicar em **Processar** | Aguardar barra de progresso. Status final: `PROCESSADA`. | [ ] |
| 5 | **Ação: Fechar Folha** | Verificar totais (Aprox. Líquido: R$ 4.100,00*). Clicar em **Fechar**. | [ ] |
| 6 | **Ação: Enviar p/ Financeiro** | Clicar no botão de integração. Mensagem: "Enviado com sucesso". | [ ] |

### Parte B: Financeiro (Aprovação e Pagamento)
*Nota: Para este passo, idealmente, faça logout e entre com um usuário diferente (Perfil Gestor/Diretor).*

| Passo | Tela / Ação | Dados a Inserir | Check |
| :--- | :--- | :--- | :--- |
| 7 | **Financeiro > Contas a Pagar** | Filtrar por Status: `PENDENTE`. Localizar "Folha de Pagamento - [Mês]/[Ano]". | [ ] |
| 8 | **Ação: Aprovar** | Clicar no botão **Aprovar**. O status deve mudar para `APROVADA`. | [ ] |
| 9 | **Ação: Pagar** | Clicar em **Pagar**.<br>**Conta Bancária:** Selecionar conta com saldo.<br>**Data:** Hoje. | [ ] |
| 10 | **Confirmação** | Verificar se o status mudou para `PAGA` e se o saldo do banco diminuiu. | [ ] |

---

## ⛔ Cenário 2: Teste de Segregação (Segurança)
**Objetivo:** Garantir que o criador da despesa não possa aprová-la sozinho (Prevenção de Fraude).

### Execução
*Nota: Realize este teste com um usuário que tenha perfil "Financeiro - Operacional" ou "RH", mas **NÃO** seja "MASTER/ADMIN".*

| Passo | Tela / Ação | Resultado Esperado | Check |
| :--- | :--- | :--- | :--- |
| 1 | **Login** | Entrar com usuário `operador_rh` (ou similar). | [ ] |
| 2 | **Gerar Folha** | Repetir passos 3 a 6 do Cenário 1 (Gerar e Enviar Folha). | [ ] |
| 3 | **Acessar Financeiro** | Ir para **Financeiro > Contas a Pagar** com o **MESMO** usuário. | [ ] |
| 4 | **Tentativa de Aprovação** | Tentar clicar em **Aprovar** na conta que acabou de criar. | [ ] |
| 5 | **Validação** | O sistema deve exibir erro: *"Você não pode aprovar uma conta criada por você"* ou o botão deve estar inativo. | [ ] |

---

## ⚠️ Cenário 3: Rejeição e Correção
**Objetivo:** Validar o fluxo de retorno quando o Financeiro encontra um erro.

### Execução

| Passo | Tela / Ação | Detalhes | Check |
| :--- | :--- | :--- | :--- |
| 1 | **Preparação** | Gerar uma folha onde o valor esteja "errado" propositalmente (ex: adicione um bônus manual de R$ 100.000,00). | [ ] |
| 2 | **Envio** | Enviar para o Financeiro normalmente. | [ ] |
| 3 | **Análise (Gestor)** | Gestor acessa **Contas a Pagar**, vê o valor de R$ 100k e identifica o erro. | [ ] |
| 4 | **Ação: Cancelar** | Clicar em **Cancelar/Rejeitar**. | [ ] |
| 5 | **Motivo** | Preencher: *"Valor incorreto, bônus não autorizado."* | [ ] |
| 6 | **Validação RH** | Voltar ao módulo RH. A folha não deve estar marcada como paga. | [ ] |
| 7 | **Correção** | RH deve cancelar/reabrir a folha, remover o bônus e reiniciar o processo. | [ ] |

---

## 🛠️ Resumo Técnico para Suporte

Caso algo dê errado, verifique:

1.  **Logs do Servidor:** Procure por `FluxoCaixaService` ou `IntegracaoFinanceiraService`.
2.  **Banco de Dados:**
    *   Tabela `colaboradores`: O registro foi criado?
    *   Tabela `folha_pagamento`: O `status` está correto?
    *   Tabela `contas_pagar`: O campo `usuario_criacao_id` está preenchido corretamente? (Essencial para o teste de segregação).
