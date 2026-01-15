# Implement Standard Deletion Modal (SweetAlert2)

## 1. Backend Update (`ClienteController.java`)
- **Method**: `listarClientes` and `detalhesCliente`.
- **Action**: Inject `podeExcluir` boolean into the Model based on user permissions (`CLIENTE_EXCLUIR` authority or ADMIN role).

## 2. Frontend Update (`listar.html`)
- **Library**: Ensure SweetAlert2 is included.
- **Remove**: Delete old `#modal-excluir` HTML and related legacy JS (`openDesligamentoModal`, `validarEExcluir`).
- **Add JS**: Implement `confirmarExclusao(id)` using `Swal.fire`:
  - Title: "Tem certeza?"
  - Input: Text (Matrícula do Admin).
  - Pre-confirm: Validate input.
  - Action: Fetch POST to `/clientes/{id}/excluir`.
  - Success: Show success alert and reload/refresh table.
- **Update Button**: Change `onclick` to call `confirmarExclusao(id)`.

## 3. Frontend Update (`detalhes.html`)
- **Library**: Add SweetAlert2.
- **Add Button**: Add "Excluir" button in the actions div (red button).
- **Add JS**: Implement same `confirmarExclusao(id)` logic.

## 4. Verification
- **Test**: Navigate to Client List.
- **Action**: Click Trash icon -> SweetAlert appears asking for Matricula -> Enter Matricula -> Client deleted.
- **Test**: Navigate to Client Details.
- **Action**: Click "Excluir" -> SweetAlert appears -> Enter Matricula -> Client deleted -> Redirect to List.
