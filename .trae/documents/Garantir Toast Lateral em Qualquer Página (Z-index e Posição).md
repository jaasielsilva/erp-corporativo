## Objetivo
Fazer o toast lateral aparecer sempre, independentemente do conteúdo da página (tabelas, overlays, containers com overflow).

## Causas Prováveis
- Z-index insuficiente do `.notification-toast` sendo coberto por elementos da página.
- Posição não fixa ou adicionada em um container com contexto de empilhamento.

## Correções
1. CSS: padronizar `.notification-toast` como `position: fixed; right/bottom; z-index` alto; transições suaves.
2. CSS: garantir `.notification-overlay` e `.notification-center` com z-index abaixo do toast.
3. JS: manter `showToast` anexando no `document.body` (já feito) para evitar contexto de empilhamento.

## Verificação
- Na página `/utilidades/processar-cnpj` com tabela populada, iniciar e concluir processamento → toast lateral aparece.
- Em páginas aleatórias (ex.: `/dashboard`, `produto/lista`) → toast lateral aparece na conclusão.

## Observações
- Não altera lógica de negócio; foco em consistência visual e sobreposição correta do toast.