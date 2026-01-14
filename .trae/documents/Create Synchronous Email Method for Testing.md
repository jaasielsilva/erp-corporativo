# Improve Email Testing Debugging

## 1. Update `EmailService.java`
- **New Method**: `public void enviarEmailTeste(String destinatario, String assunto, String corpo) throws Exception`
- **Characteristics**:
  - **NOT** `@Async` (Synchronous).
  - **Throws Exception**: Does not catch the exception internally; lets it propagate to the controller.
  - **Logic**: Reuses `getMailSender()` and sends the message.

## 2. Update `UserAutomationController.java`
- **Method**: `testAutomation`
- **Change**: Call `emailService.enviarEmailTeste(...)` instead of `enviarEmail(...)`.
- **Reason**: This allows the `try-catch` block in the controller to actually catch the SMTP error (like "AuthenticationFailed") and return it in the JSON response, so the SweetAlert displays the real error.

## 3. Verification
- **User Action**: Click "Testar" again.
- **Expected Result**: If email config is wrong, SweetAlert will show the exact error (e.g., "Username and Password not accepted"). If config is right, email will arrive.
