# Enable Delete Button Visibility

## 1. Frontend Fix (`listar.html`)
- **File**: `templates/clientes/geral/listar.html`
- **Action**: Add `window.podeExcluir = /*[[${podeExcluir}]]*/ false;` and `window.podeEditar = /*[[${podeEditar}]]*/ false;` inside the `<script th:inline="javascript">` block.
- **Goal**: Pass the permission flags from the server (Java) to the client (JavaScript) so the buttons can be rendered.

## 2. Verification
- **User Action**: Refresh page.
- **Result**: Delete button appears.
- **Click**: Opens SweetAlert2 modal (Standard).
