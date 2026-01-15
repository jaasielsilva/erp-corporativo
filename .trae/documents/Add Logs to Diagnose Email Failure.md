# Debug Automation Email

## 1. Update `UserAutomationController.java`
- **Method**: `testAutomation`
- **Action**: Add explicit logging (System.out.println for immediate feedback) of:
  - User ID and Email retrieved.
  - Automation ID and Action Type.
- **Validation**: Check if `usuario.getEmail()` is null/empty. If so, return 400 Bad Request with "E-mail do usuário não encontrado".

## 2. Update `EmailService.java`
- **Method**: `enviarEmail`
- **Action**: Add logging of "Tentando enviar para: " + destinatario.
- **Catch Block**: Ensure `e.printStackTrace()` is called so we see SMTP errors in the console.

## 3. Verification
- **User Action**: Click "Testar" again.
- **Check**: Look at the console.
  - If log says "Tentando enviar para: [email correto]" -> Issue is SMTP/Gmail (App Password).
  - If log says "Tentando enviar para: null" -> Issue is User loading.
