# Add "Contract Expiring" Automation Option

## 1. Backend Implementation
### Update `AutomationEventType.java`
- Add `CONTRATO_VENCENDO` to the enum list.

## 2. Frontend Implementation (`templates/minha-conta/automacoes.html`)
### Update JavaScript (SweetAlert Wizard)
- **Trigger Options**:
  - Remove `'NOVO_LEAD'`.
  - Add `'CONTRATO_VENCENDO': 'Contrato Próximo do Vencimento (30 dias)'`.

### Update HTML Table (Visual Translation)
- **Gatilho Column**: Use `th:switch` to show friendly text:
  - `CLIENTE_INATIVO` -> "Cliente Inativo (+30 dias sem compra)"
  - `CONTRATO_VENCENDO` -> "Contrato Vencendo (30 dias)"
- **Ação Column**: Use `th:switch` to show friendly text:
  - `EMAIL_ALERT` -> "Enviar E-mail"
  - `SYSTEM_NOTIFICATION` -> "Notificação no Sistema"

## 3. Verification
- **Test**: Open "Nova Automação" wizard.
- **Check**: Verify "Contrato Próximo do Vencimento" is selectable.
- **Check**: Verify the table displays the friendly names correctly after saving.
