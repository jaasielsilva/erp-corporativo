## Problema
- A geração de PDF processa `holerite.html` completo, que contém fragmentos e estruturas pensadas para web; isso causa `TemplateInputException` na renderização offline.

## Solução
- Criar `templates/rh/folha-pagamento/holerite-pdf.html` minimalista, sem includes/JS, usando apenas variáveis fornecidas pelo controller.
- Atualizar geração de PDF para usar `holerite-pdf.html` em:
  - `FolhaPagamentoController.holeritePdf(...)` e `gerarPdfHolerite(...)`.
  - `HoleriteEmailService.gerarPdf(...)`.

## Validação
- Testar `GET /rh/folha-pagamento/holerite/{id}/pdf` e envio por e-mail sem 500.
- Confirmar que a visualização web continua usando `holerite.html` normal.

## Entregáveis
- Novo template `holerite-pdf.html` e referências atualizadas no controller/serviço de e-mail.