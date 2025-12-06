## Problema
O toast global está com largura/estilo não padronizados, aparecendo visualmente "gigantesco" ao lado quando o processamento de CNPJs conclui.

## Solução Proposta
- Tamanho e layout fixos:
  - Largura máxima: 320px (com `max-width: 60vw` para responsivo).
  - Fonte: 14px; padding consistente (10–12px); ícone 14px.
  - Clamping de linhas: limitar texto a no máximo 2 linhas com ellipsis.
  - Quebra de palavras e truncamento para mensagens longas.
  - Posição top-right com espaçamento e sombra leve; z-index padronizado.
- CSS centralizado:
  - Adicionar classe `.global-toast` e `.global-toast-container` em `/css/notifications.css` ou `style.css` para evitar inline styles.
  - Mapear severidades (info/success/warning/error) em cores consistentes.
- Comportamento:
  - Duração padrão: 4s; fila simples para múltiplos toasts (stack até 3, com espaçamento).
  - Função `window.globalToast(msg, icon, level)` passa a truncar mensagens (>140 chars) com ellipsis.
- Mensagens de CNPJ:
  - Ajustar o texto de conclusão para formato compacto: "CNPJs concluído — Sucessos: X, Inválidos: Y, Erros: Z".

## Arquivos a alterar
- `templates/components/topbar.html`: substituir inline styles pelo uso de classes.
- `css/notifications.css` (ou `style.css`): novo bloco de estilos para toasts.
- `js/notifications.js` (apenas se necessário): padronizar mensagens muito longas.

## Verificação
- Disparar uma notificação de conclusão de CNPJs e observar o toast: tamanho fixo, texto truncado, sem overflow.
- Testar em viewport mobile e desktop.

Confirma que eu aplique essas mudanças de estilo e truncamento para padronizar o toast global?