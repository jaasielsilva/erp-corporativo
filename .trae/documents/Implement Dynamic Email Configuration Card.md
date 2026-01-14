# Implement Dynamic Email Configuration (Production Ready)

## 1. Backend Implementation
### Update `EmailService` logic for Priority
- **Logic**: 
  1. Fetch configs from `ConfiguracaoService` (DB).
  2. If `smtp_host` is present in DB -> **Override** default settings (Production safe).
  3. If not in DB -> Fallback to `application.properties` (Default behavior).
- **Refactoring**: Replace direct `mailSender` usage with a dynamic provider method.

### Update `GlobalConfigController`
- Add endpoints to Manage and Test these configs safely.
- **Security Note**: Ensure passwords are never returned in plain text in the GET endpoint (send `******`).

## 2. Frontend Implementation
- Add the Email Configuration Card to `index.html`.
- Feature: **"Usar Configuração Padrão vs Personalizada"**.
  - If the user saves empty values, the system reverts to the server's `application.properties`.

## 3. Verification
- **Test 1**: Save valid Gmail credentials -> System uses them.
- **Test 2**: Clear credentials (Save empty) -> System reverts to `application.properties`.
