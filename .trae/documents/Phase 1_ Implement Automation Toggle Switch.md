# Phase 1: Implement Automation Toggle (On/Off)

## 1. Backend Implementation (`UserAutomationController.java`)
- **Endpoint**: `POST /minha-conta/automacoes/toggle/{id}`
- **Logic**:
  - Fetch automation by ID.
  - Verify ownership.
  - Flip the `active` boolean (`setActive(!isActive)`).
  - Save.
  - Return JSON `{ "newStatus": true/false, "message": "..." }`.

## 2. Frontend Implementation (`automacoes.html`)
- **UI**: Replace the "Status" badge with a Bootstrap Switch (`form-check-input`).
- **JS**: Add event listener to the switch.
  - Call `/toggle/{id}`.
  - On success: Show small toast/notification.
  - On error: Revert switch state and show alert.

## 3. Verification
- **User Action**: Click the switch.
- **Check**: Switch toggles, database updates.
- **Check**: Listener (`AutomationListener`) respects the `active` flag (it already does, but worth double-checking logic).
