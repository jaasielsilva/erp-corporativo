## Mudança
- Atualizar `@PreAuthorize` do endpoint `POST /rh/folha-pagamento/holerite/{id}/email` para aceitar: `@globalControllerAdvice.podeAcessarRH() or @holeriteService.podeVerHolerite(#id)`.
- Manter a lógica de destinatário do `HoleriteEmailService` (usa e-mail digitado ou o do colaborador quando vazio).

## Validação
- Usuário colaborador consegue enviar seu próprio holerite informando e-mail ou deixando em branco.
- RH/Admin/Master continuam com acesso amplo.

## Entregável
- Controller ajustado para permitir envio por proprietário, sem mudanças no serviço de e-mail (já atende o comportamento desejado).