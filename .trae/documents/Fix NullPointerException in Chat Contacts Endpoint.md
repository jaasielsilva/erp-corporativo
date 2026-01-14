# Fix 500 Error in Chat Contacts API

## 1. Refactor `ChatController.java`
- **Method**: `getContacts`
- **Action**: Replace `Map.of(...)` with a defensive `HashMap` creation block inside the stream `.map()`.
- **Logic**:
  - Check if `u.getEmail()` is null before filtering.
  - Handle null `u.getNome()` with a default value "Usuário".
  - Ensure `isUserOnline` result is treated safely.

## 2. Verification
- **User Action**: Refresh `/chat`.
- **Expectation**: The sidebar contact list should load successfully (status 200), even if some users have incomplete data.
