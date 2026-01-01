# Pagamento ao Cliente

## Objetivo
- Liberar o valor líquido ao cliente com conferência bancária e comprovantes.

## Fluxo Passo a Passo
1. Confirmar dados bancários do cliente:
   - Agência
   - Conta
   - Chave Pix (se aplicável)
2. Autorizar pagamento (fluxo de aprovação).
3. Realizar transferência:
   - Financeiro → Transferências → Nova Transferência
   - Ativar “Pagamento a Cliente”
   - Selecionar Conta de Origem
   - Selecionar Cliente Destino
   - Informar valor, data e descrição
   - Confirmar
4. Gerar comprovante de transferência.
5. Emitir recibo de quitação.

## Campos
- Conta de origem* (sai dinheiro)
- Cliente destino* (entra dinheiro)
- Valor* (R$)
- Data* (dd/mm/aaaa)
- Descrição* (ex.: “Pagamento de condenação – Proc. XXXXX”)
- Observações

## Template (Transferência com Pagamento a Cliente)
```
[ ] Pagamento a Cliente (ativado)
Conta de Origem*         [Caixa Principal (Saldo: R$ ...)]
Cliente Destino*         [Selecione cliente]
Valor (R$)*              [_____.__]
Data*                    [__/__/____]
Descrição*               [____________________________________]
Observações              [____________________________________]

[ Confirmar Transferência ]
```

## Esquema Visual (wireframe)
```
┌───────────────────────────────────────────────┐
│ Financeiro > Transferências                   │
├───────────────────────────────────────────────┤
│ (✓) Pagamento a Cliente                       │
│ Conta Origem* [_____________]                 │
│ Cliente Destino* [_____________]              │
│ Valor* [R$ ______] Data* [__/__/____]        │
│ Descrição* [____________________________]     │
│ Observações [___________________________]     │
│                                               │
│ [Confirmar Transferência]                     │
└───────────────────────────────────────────────┘
```

## Comprovante de Transferência
- Após confirmar, obtenha comprovante:
  - Captura da tela de sucesso ou funcionalidade de exportar (se disponível)
  - Registre número da operação, data/hora e conta de origem

## Template de Recibo de Quitação
```
RECIBO DE QUITAÇÃO

Cliente: ______________________________
Processo: _____________________________
Valor Líquido Pago: R$ _______________
Data do Pagamento: ____/____/_________
Forma de Pagamento: [TED | Pix | Outro]
Conta de Origem: ______________________

Declaro ter recebido integralmente o valor acima,
referente à condenação do processo informado.

Assinatura do Cliente: ________________________
Documento: __________________  Data: ___/___/____
```

## Fluxos Alternativos
- Pix (chave e verificação imediata).
- TED/DOC (pode demorar; inclua referência da operação).

## Observações
- Confirme dados bancários no cadastro do cliente antes de pagar.
- Valor deve estar aprovado na liquidação.
- Utilize “Marcar como lido” nos lembretes jurídicos para evitar reexibições após pagamento.

