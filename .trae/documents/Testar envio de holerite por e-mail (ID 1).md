## Objetivo
- Reiniciar a aplicação e confirmar o envio do holerite ID 1 para o e-mail do colaborador.

## Passos
- Build: `mvn -q -DskipTests package`.
- Run: `mvn -q spring-boot:run`.
- Confirmar rota ativa: acessar `http://localhost:8080/actuator/mappings` e verificar `/rh/folha-pagamento/holerite/{id}/email`.
- Login: `POST /login` com `username=rh@empresa.com&password=rh123` mantendo sessão.
- Envio: `POST /rh/folha-pagamento/holerite/1/email` (sem parâmetro `email`, usa o cadastrado).
- Validação: confirmar `200 OK` e aguardar sua confirmação de recebimento do PDF no e‑mail do colaborador.

## Notas
- O PDF é gerado a partir do template HTML padronizado (openhtmltopdf).
- Envio do anexo via `EmailService.enviarEmailComAnexo`.

## Critério de Sucesso
- Endpoint responde `200 OK` e o colaborador recebe o holerite em anexo no e‑mail.