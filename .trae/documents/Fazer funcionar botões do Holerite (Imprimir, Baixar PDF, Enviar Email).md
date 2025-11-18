## Diagnóstico
- Página: `templates/rh/folha-pagamento/holerite.html`.
- Botões atuais:
  - Imprimir: `onclick="window.print()"` (funciona se o navegador abrir diálogo; CSS `@media print` preparado).
  - Baixar PDF: link direto para `/rh/folha-pagamento/holerite/{id}/pdf` (controller gera PDF com `OpenHTMLtoPDF`).
  - Enviar por Email: `fetch` POST para `/rh/folha-pagamento/holerite/{id}/email` (retorna texto de status).
- Controlador:
  - `GET /holerite/{id}` renderiza a página com `holerite`, `periodoReferencia`, `dataReferencia`.
  - `GET /holerite/{id}/pdf` gera PDF com `templateEngine.process("rh/folha-pagamento/holerite", ctx)` e `pdf=true`.
  - `POST /holerite/{id}/email` anexa PDF e envia.

Possíveis causas para “não funciona”:
- ID inexistente ou sem permissão (retorno 404/403/500 sem feedback visual).
- Exceção na geração de PDF (mostrada como erro 500), botão parece não responder.
- Interferência de UI (overlay) improvável; código do botão está correto.

## Ajustes Propostos
1. Baixar PDF (robustez e feedback)
- Manter `<a th:href>` atual e adicionar fallback JS: usar `baixarPDF()` com spinner/disabled enquanto navega.
- Exibir alerta se resposta for 404/500 (capturar via `onerror` em navegação JS alternativa com `fetch` Blob e `URL.createObjectURL`).

2. Enviar por Email (feedback claro)
- Desabilitar botão durante envio, mostrar mensagens de sucesso/erro do endpoint (já retorna texto), manter `Content-Type: application/x-www-form-urlencoded`.

3. Imprimir Holerite
- Manter `window.print()` e garantir que `@media print` oculte ações/sidebar/topbar (já presente). Adicionar `title` do documento com período.

4. Visibilidade
- Garantir `data-holerite-id` está no container (já está) e que `holerite.id` está preenchido na navegação.

## Validação
- Acessar `http://localhost:8080/rh/folha-pagamento/holerite/1` e testar três botões.
- Testar `.../pdf` direto no navegador confirmando download.
- Simular erro e verificar feedback visual.

## Entregáveis
- Atualizações em `holerite.html` (melhorias JS/UX nos três botões).