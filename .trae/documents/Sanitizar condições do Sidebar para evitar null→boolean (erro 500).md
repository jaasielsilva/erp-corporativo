## Problema
- O erro 500 ocorre ao avaliar expressões `th:if` no `components/sidebar.html` quando variáveis de acesso (ex.: `podeAcessarFinanceiro`) não estão presentes no contexto do PDF/email.
- SpEL tenta converter `null` em boolean e lança exceção.

## Solução
- Ajustar `th:if` do sidebar para comparar explicitamente com `true`, evitando conversão de `null` para boolean.
- Pontos a alterar:
  - Seção Gestão: `podeAcessarFinanceiro or podeAcessarMarketing or podeAcessarTI or podeAcessarJuridico` → cada um `== true`.
  - Menus Financeiro, Marketing, TI, Jurídico: `th:if="${podeAcessarX == true}"`.

## Validação
- Regerar PDF (`/holerite/{id}/pdf`) e enviar email; não deve ocorrer erro de `TemplateProcessingException`.
- Navegação normal continua funcionando (quando variáveis são `true`, menus aparecem; `null/false` ficam ocultos).

## Entregável
- `components/sidebar.html` com condições seguras para SpEL.