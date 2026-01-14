# Implement "Client Recovery Automation" (Smart Triggers)

## 1. Backend Implementation
### Data Model (`UserAutomation`)
- **Entity**: `UserAutomation`
  - `id`, `usuario`
  - `eventType`: `CLIENTE_INATIVO`, `NOVO_LEAD` (Expansion ready)
  - `actionType`: `EMAIL_ALERT`, `SYSTEM_NOTIFICATION` (No WhatsApp yet)
  - `active`: boolean

### Inactivity Detection Logic (`ClientInactivityService`)
- **Job**: Create a `@Scheduled` task running daily.
- **Logic**: Find clients with `lastPurchaseDate < 30 days ago`.
- **Event**: Publish `ClientInactivityEvent` for each found client.

### Event Processing (`AutomationListener`)
- **Logic**: When `ClientInactivityEvent` fires:
  - Find users who subscribed to this event in `UserAutomation`.
  - Execute their chosen action (Send Email using `EmailService` or Create Notification).

### Controller (`UserAutomationController`)
- API to CRUD the automation rules.

## 2. Frontend Implementation (`minha-conta/automacoes.html`)
- **UI**: "Painel de Automações".
- **List**: Show active rules.
- **Create Form**: 
  - Trigger: "Cliente Inativo (+30 dias)"
  - Action: "Enviar E-mail" or "Notificar no Sistema".

## 3. Verification
- **Setup**: Create rule "Cliente Inativo -> Email".
- **Trigger**: Manually trigger the Inactivity Job (using your new Job Panel!).
- **Check**: Verify if the email arrives listing the inactive client.
