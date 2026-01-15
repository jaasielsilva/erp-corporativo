# Secure System Configuration Pages

## 1. Backend Security
### Secure `ConfiguracoesController`
- **Action**: Add `@PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")` to the class level.
- **Why**: Ensures only system administrators can access the configuration UI, even via direct URL.

### Secure `JobControlController`
- **Action**: Add `@PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")` to the class level.
- **Why**: Prevents unauthorized users from triggering system jobs via API calls (Postman/Curl).

### Secure `GlobalConfigController`
- **Action**: Add `@PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")` to the class level.
- **Why**: Protects the API endpoints that read/write global settings and email configs.

## 2. Verification
- **Test**: Try to access `/configuracoes` as a standard user (if possible) or verify the annotation is present.
- **Result**: Standard users should receive a 403 Forbidden error.
