# Cleanup Test Button and Plan Enhancements

## 1. Frontend Cleanup (`automacoes.html`)
- **Action**: Comment out or remove the "Testar Agora" button (`<button ... title="Testar Agora">`) in the table.
- **Action**: Comment out the `testarAutomacao(id)` JavaScript function to keep the code clean but preserved if needed later (or delete if preferred).

## 2. Backend Cleanup (`UserAutomationController.java`)
- **Action**: Comment out or remove the `@PostMapping("/test/{id}")` endpoint.

## 3. Proposal for Next Step (Optional Implementation)
- **Feature**: **Toggle Active/Inactive Switch**.
- **Plan**:
  - Add a toggle switch in the "Status" column.
  - Add JS to call an endpoint `/toggle/{id}`.
  - Update Entity/Controller to handle the status change.
