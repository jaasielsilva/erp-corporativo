## Objetivo

* Disponibilizar no holerite: imprimir, baixar PDF e enviar por email.

## Estado Atual

* Imprimir: disponível via `window.print` com CSS de impressão (`templates/rh/folha-pagamento/holerite.html:158-175`).

* Baixar PDF: disponível em `GET /rh/folha-pagamento/holerite/{id}/pdf` (`src/main/java/.../FolhaPagamentoController.java:294-489`).

* Enviar por Email: botão sem backend (`templates/rh/folha-pagamento/holerite.html:236-238, 493-503`).

## Alterações Backend

* Endpoint `POST /rh/folha-pagamento/holerite/{id}/email` que gera o PDF em memória e envia para `holerite.colaborador.email`, podendo sobrescrever via parâmetro `email`.

* `EmailService`: novo método `enviarEmailComAnexo(...)` com `MimeMessageHelper.addAttachment`.

## Alterações Frontend

* `holerite.html`: adicionar `data-holerite-id` e implementar `enviarEmail(id)` com `fetch` para o novo endpoint, usando o email informado ou do colaborador, exibindo notificação de sucesso/erro.

## Segurança e Acesso

* Manter restrições atuais para RH/Admin. Opcional: criar rotas dedicadas para o próprio colaborador, validando a propriedade do holerite.

## Validação

* Testar impressão, download de PDF e envio de email em `http://localhost:8080/rh/folha-pagamento/holerite/1`, verificando recebimento do anexo e tratamento de erros de SMTP.

## Entregáveis

* Endpoint de envio de holerite por email com PDF.

* Atualização do template com chamada assíncrona e feedback.

