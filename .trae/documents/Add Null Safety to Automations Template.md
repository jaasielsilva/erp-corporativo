# Fix Chunked Encoding Error in Automations Page

## 1. Frontend Fix (`automacoes.html`)
- **Action**: Add null checks to the `th:switch` blocks and data display columns.
- **Logic**:
  - Wrap the Event Type display in `th:if="${rule.eventType != null}"`.
  - Wrap the Action Type display in `th:if="${rule.actionType != null}"`.
  - Add a fallback `span` (e.g., "Configuração Inválida") for null values.
  - Ensure `rule.executionTime` is accessed safely.

## 2. Verification
- **User Action**: Refresh `/minha-conta/automacoes`.
- **Expectation**: Page loads fully (200 OK), even if some rules have null fields (they will show as invalid/empty instead of crashing the page).
