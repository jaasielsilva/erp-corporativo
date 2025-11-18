## Entendimento da lógica atual

* E-mails na adesão são disparados em `AdesaoColaboradorService.finalizarAdesao(...)` via `EmailService.enviarEmailBoasVindas(colaborador)`.

* `EmailService` já fornece métodos genéricos para envio (simples, template e com anexo) e templates prontos para novo processo, aprovação e rejeição.

## Adaptação para holerite

* Criar `HoleriteEmailService` para centralizar o envio:

  * Gera o PDF reaproveitando o template HTML de `holerite` (openhtmltopdf).

  * Monta um corpo HTML simples (como nos templates de adesão) com nome do colaborador e competência.

  * Envia com `EmailService.enviarEmailComAnexo(destinatario, assunto, corpoHtml, nomeArquivo, pdfBytes)`.

  * Suporta múltiplos destinatários: colaborador e lista opcional de RH (`rh.folha.email.destinatariosExtras`).

## Controller

* Garantir o endpoint `POST /rh/folha-pagamento/holerite/{id}/email` no `FolhaPagamentoController` e delegar ao `HoleriteEmailService`.

* Respostas padronizadas: `200 OK` (mensagem com destinatário), `404 NOT_FOUND` (holerite ausente), `400 BAD_REQUEST` (sem e-mail).

* Adicionar `produces="application/json"` para evitar mapeamento como recurso estático.

## Segurança

* Manter `@PreAuthorize("@globalControllerAdvice.podeAcessarRH()")`.

## Validação

* Build e restart.

* Confirmar rota via actuator.

* Login RH e enviar holerite ID 1 (sem parâmetro `email`).

* Confirmar `200 OK` e aguardar sua confirmação de recebimento.

## Critério de sucesso

* E-mail entregue ao colaborador com PDF do holerite padronizado, reaproveitando a infraestrutura de e-mails usada na adesão.

