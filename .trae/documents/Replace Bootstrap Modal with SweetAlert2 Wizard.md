# Modernize Automation Creation with SweetAlert2

## 1. Frontend Implementation
### Update `templates/minha-conta/automacoes.html`
- **Include Library**: Add SweetAlert2 CDN script.
- **Remove**: Delete the existing Bootstrap Modal HTML block.
- **Update Button**: Change `data-bs-toggle` to `onclick="criarAutomacao()"`.
- **Add Script**:
  - Implement `criarAutomacao()` function.
  - Use `Swal.fire` with `input: 'select'` for Step 1 (Trigger) and Step 2 (Action).
  - Use `fetch` to POST data to `/minha-conta/automacoes/create` upon completion.
  - Show success/error feedback using Swal.

## 2. Backend Adjustment (Optional but Recommended)
- **Controller**: Ensure `/create` endpoint accepts JSON body if we switch to AJAX, or handle form-data if we simulate a form submission via JS. *Note: Current controller expects `@RequestParam`. I will adapt the JS to send Form Data to match existing backend without changing Java code.*

## 3. Verification
- **Test**: Click "Nova Automação".
- **Check**: Verify SweetAlert appears.
- **Flow**: Select "Cliente Inativo" -> Select "Email" -> Confirm.
- **Result**: Page reloads (or updates) with the new rule.
