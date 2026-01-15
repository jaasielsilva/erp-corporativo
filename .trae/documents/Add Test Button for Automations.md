# Implement "Test Automation" Feature

## 1. Backend Implementation (`UserAutomationController.java`)
- **New Endpoint**: `POST /minha-conta/automacoes/test/{id}`
- **Logic**:
  - Fetch the automation rule by ID.
  - Verify if the current user owns the rule.
  - **Force Execution**: Instead of waiting for the event, manually trigger the action service (`EmailService` or `NotificationService`) immediately.
  - Return a success message (JSON or Flash Attribute).

## 2. Frontend Implementation (`automacoes.html`)
- **UI Update**: Add a "Testar" button (e.g., `<i class="fas fa-paper-plane"></i>`) in the "Ações" column of the table.
- **JS Logic**:
  - Add `testarAutomacao(id)` function.
  - Use `fetch` to call the test endpoint.
  - Show a SweetAlert2 loading spinner while sending.
  - Show success/error SweetAlert based on response.

## 3. Verification
- **User Action**: Click the "Testar" button on the "CLIENTE_INATIVO" rule.
- **Expectation**: Receive the email immediately.
