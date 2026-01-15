# Fix Generic Type Mismatch in UserAutomationController

## 1. Update `UserAutomationController.java`
- **Method**: `getAutomationLogs`
- **Action**: Fix type inference issue by explicitly collecting to a typed list.
- **Change**:
  ```java
  .collect(Collectors.<Map<String, Object>>toList());
  ```

## 2. Verification
- **Check**: Compile error should disappear.
