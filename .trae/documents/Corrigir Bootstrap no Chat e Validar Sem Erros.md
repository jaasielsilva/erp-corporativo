## Problema
- As páginas do chat referenciam arquivos locais de Bootstrap (`/css/bootstrap.min.css` e `/js/bootstrap.bundle.min.js`) que não existem na pasta `static`, resultando em 404 e erro de MIME. Por isso `bootstrap` não é definido e o chat falha.

## Correção Proposta
1. Restaurar uso de CDN do Bootstrap (compatível com CSP):
- CSS: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css`
- JS: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js`
2. Manter `Font Awesome 6.4.0` via `cdnjs` e demais CSS/JS locais.
3. Ajustar templates afetados:
- `templates/chat/index.html` e `templates/chat/departamentos.html` para usar CDN.
4. Recarregar o servidor e validar:
- Abrir `/chat` e `/chat/departamentos`, verificar ausência de 404 e de erros de MIME.
- Confirmar `bootstrap` definido e funcionamento do `Modal`.
- Testar envio de mensagem, anexos, reações e digitação.

## Validação
- Console do navegador sem erros.
- WebSocket conectado (Online), ações do chat funcionando.

Posso aplicar as alterações e validar agora?