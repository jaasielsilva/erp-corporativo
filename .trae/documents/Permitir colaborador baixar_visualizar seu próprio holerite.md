## Mudanças
- Adicionar método `podeVerHolerite(id)` em `HoleriteService` que valida se o usuário logado é o dono do holerite (compara e-mail do `Authentication` com `holerite.colaborador.email`).
- Atualizar `@PreAuthorize` nos endpoints:
  - `GET /rh/folha-pagamento/holerite/{id}` → `@globalControllerAdvice.podeAcessarRH() or @holeriteService.podeVerHolerite(#id)`.
  - `GET /rh/folha-pagamento/holerite/{id}/pdf` → mesmo ajuste.
- Manter `POST /holerite/{id}/email` restrito ao RH/Admin/Master.

## Validação
- Usuário comum consegue abrir e baixar apenas o seu holerite.
- Usuários com acesso RH/Admin/Master continuam acessando todos.
- Email permanece restrito.

## Entregável
- Código atualizado em `HoleriteService` e `FolhaPagamentoController` com a lógica de ownership integrada ao controle de acesso.