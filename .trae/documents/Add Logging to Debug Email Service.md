# Debug Email Sending Failure

## 1. Update `EmailService.java`
- **Action**: Add detailed logging to the `enviarEmail` method.
- **Log 1**: "Tentando enviar e-mail para: [destinatario]"
- **Log 2**: "Usando remetente: [username configurado]" (to check if it's using the DB config or application.properties).
- **Log 3**: In the catch block, print the full stack trace of `MailException` to see exactly why Gmail rejected it (e.g., "AuthenticationFailed", "BadCredentials").

## 2. Verification Steps for User
- **Action**: Ask user to click "Testar" again after I apply the logs.
- **Action**: Check the terminal output. It will tell us if it's an authentication error (App Password needed) or a wrong recipient address.
