# Fix Automations Page & Bootstrap Link

## 1. Fix `automacoes.html`
- **Action**: Correct the Bootstrap CDN link (remove trailing dot).
- **Action**: Add extra null checks in the table loop.
  - Verify `rule` object itself isn't null.
  - Wrap the entire row content in a safe block.

## 2. Investigate Chat `save` Issue
- **Hypothesis**: If `content` is null, the fallback logic skips setting `ciphertext`.
- **Action**: Update `ChatMessageService.java` to set a dummy ciphertext even if content is null (or ensure content is never null).
- **Logic**:
  ```java
  if (m.getCiphertext() == null) {
      if (m.getContent() != null) {
          m.setCiphertext(m.getContent().getBytes(...));
      } else {
          m.setCiphertext(new byte[0]); // Empty array fallback
      }
  }
  ```

## 3. Verification
- **User Action**: Refresh Automations page.
- **User Action**: Send message in Chat.
