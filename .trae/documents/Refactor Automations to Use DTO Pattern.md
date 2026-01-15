# Fix Chunked Encoding via DTO Pattern

## 1. Create DTO in `UserAutomationController.java`
- **Action**: Define a simple inner record/class `AutomationDTO`.
- **Fields**: `Long id`, `String eventType`, `String actionType`, `String executionTime`, `boolean active`.
- **Reason**: Decouple the View from the JPA Entity to prevent LazyLoading issues, null pointer exceptions during rendering, and serialization problems.

## 2. Refactor `listAutomations` Method
- **Action**: Map `List<UserAutomation>` to `List<AutomationDTO>`.
- **Logic**:
  - Handle nulls explicitly during mapping.
  - Format `executionTime` to String immediately.
  - Pass the DTO list to the model.

## 3. Update `automacoes.html`
- **Action**: Revert the simplified debug view to a proper UI, but consuming the DTO fields.
- **Logic**: Use the pre-formatted strings from the DTO.

## 4. Verification
- **User Action**: Refresh page.
- **Expectation**: 200 OK.
