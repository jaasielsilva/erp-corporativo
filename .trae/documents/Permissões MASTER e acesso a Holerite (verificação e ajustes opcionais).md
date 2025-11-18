## Situação Atual
- MASTER já tem acesso total via `NivelAcesso.MASTER` e `GlobalControllerAdvice.isMaster()`.
- Permissões por área incluem MASTER:
  - RH: `podeAcessarRH()` retorna `isRH || podeGerenciarRH || isMaster || isAdmin` (GlobalControllerAdvice.java:217–220).
  - Financeiro: `NivelAcesso.podeAcessarFinanceiro()` inclui MASTER (NivelAcesso.java:81–83) e é exposto em `podeAcessarFinanceiro()` (GlobalControllerAdvice.java:77–81).
  - Marketing/TI/Jurídico: `podeAcessarX()` inclui MASTER (GlobalControllerAdvice.java:237–250, 242–245).
- Holerite:
  - Visualização: `@PreAuthorize("@globalControllerAdvice.podeAcessarRH()")` (FolhaPagamentoController.java:221–236).
  - PDF: `@PreAuthorize("@globalControllerAdvice.podeAcessarRH()")` (FolhaPagamentoController.java:299–329).
  - Email: `@PreAuthorize("@globalControllerAdvice.podeAcessarRH()")` (FolhaPagamentoController.java:331–350).
  - Resultado: MASTER, ADMIN, e quem tem acesso/gestão de RH podem visualizar, baixar PDF e enviar por email.

## Problema Relatado
- Erros 500 ao gerar PDF/Email foram causados por avaliação de `th:if` com variáveis nulas no sidebar; já sanitizado para evitar `null → boolean` e scripts desativados no modo PDF em `holerite.html`.

## Ações Propostas
1. Confirmar que nenhuma outra tela usa condições sem fallback; revisar rapidamente `components/sidebar.html` e endpoints RH (somente verificação, sem alteração).
2. Opcional: permitir que o próprio colaborador baixe o próprio holerite mesmo sem `podeAcessarRH`, adicionando verificação de ownership:
   - Ajustar `@PreAuthorize` dos endpoints `/holerite/{id}` e `/holerite/{id}/pdf` para aceitar `podeAcessarRH() or @holeriteService.podeVerHolerite(#id)`.
   - Implementar `podeVerHolerite(id)` que valida se `holerite.colaborador.usuario.email == auth.name`.
   - Manter envio por email restrito a RH/Master/Admin.

## Validação
- Testar MASTER em rotas RH e Financeiro; conferir acesso completo.
- Testar geração de PDF/Email de Holerite sem 500.
- Se implementar ownership: testar usuário comum acessando apenas seus holerites.

## Entregáveis
- Verificação concluída sem mudanças para MASTER (se nada pendente).
- Se aprovado, ajuste opcional de ownership nos endpoints de Holerite.