# Refactor Chat Module to "Internal WhatsApp" Style

## 1. Backend Cleanup & Refactor
- **Remove**: Delete `ChatWebController.java` (redundant).
- **Update**: `ChatPageController.java`
  - Route: `/chat`
  - Logic: Ensure `usuarioId` and `usuarioNome` are safely added to the model. Add error handling to prevent chunked encoding errors.
- **New API**: `ChatApiController.java` (or update existing)
  - `GET /api/chat/contacts`: List all users (potential chats).
  - `GET /api/chat/conversations`: List active conversations with last message.
  - `GET /api/chat/messages/{otherUserId}`: Get history with a specific user.

## 2. Frontend Rewrite (`chat/index.html`)
- **Structure**: 2-column layout (Sidebar + Chat Area).
- **Style**: WhatsApp Web inspired (Green/White bubbles, avatars, clean header).
- **Logic**:
  - Remove complex Vue.js dependencies. Use clean JavaScript + SockJS/Stomp.
  - **Features**:
    - Real-time message appending.
    - "Online" status indicator.
    - Scroll to bottom on new message.
    - Click contact to load conversation.

## 3. WebSocket Configuration
- Ensure `WebSocketConfig` allows `/topic` and `/queue` correctly.
- Ensure `ChatWsController` handles sending to specific users (`/user/{id}/queue/messages`).

## 4. Verification
- **Test**: Open `/chat`.
- **Check**: No loading errors.
- **Action**: Send message between two users.
- **Result**: Instant delivery, WhatsApp-like UI.
