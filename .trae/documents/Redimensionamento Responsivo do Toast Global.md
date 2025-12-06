## Objetivo
Padronizar e tornar responsivo o componente de alerta (toast global) usando unidades relativas, com proporções consistentes da referência de 24" e transições suaves entre breakpoints.

## Alterações de Estilo (CSS)
- Definir variáveis de escala na raiz para tamanhos e espaçamentos:
  - `:root { --toast-w: clamp(18rem, 28vw, 32rem); --toast-pad-y: clamp(0.6rem, 1.2vh, 1rem); --toast-pad-x: clamp(0.8rem, 1.6vw, 1.2rem); --toast-gap: clamp(0.5rem, 1.2vw, 0.75rem); --toast-font: clamp(0.875rem, 1.6vw, 1rem); --toast-icon: clamp(0.875rem, 1.4vw, 1rem); --toast-top: clamp(0.75rem, 1.8vh, 1.5rem); --toast-right: clamp(0.75rem, 1.8vw, 1.5rem); }`
- `.notification-toast`:
  - `top: var(--toast-top); right: var(--toast-right); width: var(--toast-w); max-width: 90vw;`
  - `padding: var(--toast-pad-y) var(--toast-pad-x); gap: var(--toast-gap); font-size: var(--toast-font);`
  - `line-clamp` para o texto: 2–3 linhas com ellipsis (mantendo legibilidade).
  - Transições: `opacity`, `transform`, e suave para `width/padding`.
- Ícone:
  - Uso de `clamp` para `width/height/font-size` conforme `--toast-icon`.
- Cores/contraste:
  - Manter fundo escuro (`#1f2937`) com texto branco para contraste AA/AAA.

## Breakpoints
- Pequenas (<768px):
  - `width: clamp(18rem, 92vw, 22rem)`; `-webkit-line-clamp: 3` para mensagens mais longas.
  - `font-size: clamp(0.95rem, 2.4vw, 1.05rem)`; espaçamentos otimizados com `clamp`.
- Médias (768–1200px):
  - Proporções próximas da referência de 24": `width: var(--toast-w)`; `font-size: var(--toast-font)`; `gap/padding` conforme variáveis.
- Grandes (>1200px):
  - Aumento controlado: `width: clamp(22rem, 22vw, 28rem)`; `font-size: clamp(1rem, 1.2vw, 1.1rem)`.
  - Limite máximo de expansão com `max-width: min(30rem, 24vw)`.

## JS (ajuste leve)
- `globalToast(message, icon, level)` já trunca a mensagem (140 chars) e aplica prioridade.
- Manter truncamento; nenhuma mudança funcional adicional necessária.

## Testes
- Dispositivos móveis (360–767px): verificar largura, clamping de 3 linhas, legibilidade sem zoom.
- Tablets (768–1024px): validar proporções similares à referência.
- HD (1280–1920px): checar largura e fontes; transições suaves ao redimensionar.
- 4K (>2000px): garantir limite máximo de expansão e espaçamentos consistentes.
- Acessibilidade: tamanho mínimo de texto ≥ 14px, contraste texto/fundo ≥ 7:1, foco/fechamento via botão.

## Critérios de Aceitação
- Proporções visuais mantidas em relação à referência de 24".
- Texto sempre legível sem necessidade de zoom.
- Espaçamentos internos consistentes, sem distorções/overflows.
- Transições suaves entre breakpoints.

## Arquivos a alterar
- `src/main/resources/static/css/notifications.css`: adicionar variáveis e atualizar regras do `.notification-toast`, ícone e texto; breakpoints.
- (Sem mudanças em HTML/JS além das já padronizadas).