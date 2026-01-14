# Phase 2: Implement Automation Editing

## 1. Backend Implementation (`UserAutomationController.java`)
- **Endpoint**: `POST /minha-conta/automacoes/update/{id}`
- **Logic**:
  - Fetch automation by ID and verify owner.
  - Update fields (`eventType`, `actionType`, `executionTime`).
  - Save.
  - Redirect with success message.

## 2. Frontend Implementation (`automacoes.html`)
- **UI**: Add "Editar" button (`<i class="fas fa-edit"></i>`) next to delete button.
- **JS**: Create `editarAutomacao(id, currentEvent, currentAction, currentTime)`.
  - Reuse the SweetAlert Queue logic from `criarAutomacao`.
  - **Difference**: Pass `inputValue: currentX` to each step so it pre-fills.
  - **Submit**: Change form action to `/update/{id}`.

## 3. Verification
- **User Action**: Click Edit on a rule (e.g., change 08:00 to 09:00).
- **Check**: Database updates, list shows new time.
