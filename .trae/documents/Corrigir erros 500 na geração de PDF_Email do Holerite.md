## Causa Provável
- O PDF é gerado processando o mesmo template HTML (holerite.html). Scripts e recursos interativos presentes podem causar falhas no renderizador de PDF (OpenHTMLtoPDF), resultando em 500.

## Correções Propostas (sem criar novo template)
1. Desabilitar todos os `<script>` na renderização de PDF adicionando `th:if="${pdf == null}"`:
   - Inline funções JS (baixarPDF, enviarEmail, etc.).
   - Inclusões de JS externos (jQuery, notifications.js, sidebar.js).
2. Manter CSS já condicionado por `pdf == null`; garantir layout limpo para PDF.
3. Preservar os ícones `<i>` (sem CSS no PDF eles apenas não aparecem, mas não causam erro).

## Validação
- Acessar `GET /rh/folha-pagamento/holerite/{id}/pdf`: baixar sem 500.
- `POST /rh/folha-pagamento/holerite/{id}/email`: enviar sem erro.

## Entregável
- Atualização em `templates/rh/folha-pagamento/holerite.html` para condicionar scripts ao modo não-PDF.