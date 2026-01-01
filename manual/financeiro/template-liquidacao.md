# Processamento Financeiro (Liquidação)

## Objetivo
- Apurar valores do caso vencedor, gerar recibos/comprovantes e submeter à aprovação.

## Fluxo Passo a Passo
1. Acessar Financeiro → Liquidação (ou módulo de contas/fluxo caixa).
2. Lançar valores:
   - Valor bruto da condenação
   - Descontos: custas, honorários, taxas
   - Valor líquido para cliente
3. Anexar comprovantes (sentença, cálculo).
4. Gerar recibo/comprovante.
5. Submeter à aprovação financeira.

## Campos
- Valor bruto* (R$)
- Custas processuais (R$)
- Honorários advocatícios (R$)
- Outras taxas (R$)
- Valor líquido (R$) = bruto − (custas + honorários + taxas)
- Observações

## Validações
- Valores positivos
- Datas dos documentos
- Conferência com sentença e trânsito em julgado

## Template de Lançamento
```
Valor bruto*                  [R$ _____.__]
Custas                        [R$ _____.__]
Honorários                    [R$ _____.__]
Outras taxas                  [R$ _____.__]
Valor líquido (auto)          [R$ _____.__]
Observações                   [__________________________]

Comprovantes
  Sentença                    [+ Selecionar arquivo]
  Cálculo de liquidação       [+ Selecionar arquivo]

[ Gerar Recibo ] [ Submeter à Aprovação ]
```

## Esquema Visual (wireframe)
```
┌───────────────────────────────────────────────┐
│ Financeiro > Liquidação                       │
├───────────────────────────────────────────────┤
│ [Bruto*] [Custas] [Honorários] [Outras]       │
│ [Líquido]                                     │
│ [Observações]                                 │
│                                               │
│ Comprovantes: [Sentença] [Cálculo]            │
│                                               │
│ [Gerar Recibo] [Submeter à Aprovação]         │
└───────────────────────────────────────────────┘
```

## Aprovação Financeira
- Revisar valores e documentos.
- Aprovar para habilitar pagamento ao cliente.
- Registrar responsável e data de aprovação.

