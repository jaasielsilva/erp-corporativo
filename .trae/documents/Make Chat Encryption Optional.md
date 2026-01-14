# Fix Chat Message Persistence Error

## 1. Update `ChatMessage.java` (Entity)
- **Action**: Make encryption fields nullable to support plain text messages.
- **Changes**:
  - Remove `nullable = false` from `ciphertext`.
  - Remove `nullable = false` from `iv`.

## 2. Update `ChatMessageService.java` (Service)
- **Action**: Add fallback logic in `save` method to satisfy existing database constraints if schema update fails.
- **Logic**:
  ```java
  public ChatMessage save(ChatMessage m) {
      if (m.getCiphertext() == null && m.getContent() != null) {
          m.setCiphertext(m.getContent().getBytes(StandardCharsets.UTF_8));
      }
      if (m.getIv() == null) {
          m.setIv(new byte[12]); // Dummy IV
      }
      return chatMessageRepository.save(m);
  }
  ```

## 3. Verification
- **User Action**: Send "Oi" in chat again.
- **Expectation**: Message is saved and displayed without 500 error.
