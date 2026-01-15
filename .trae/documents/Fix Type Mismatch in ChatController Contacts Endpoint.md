# Fix Generic Type Mismatch in ChatController

## 1. Update `ChatController.java`
- **Method**: `getContacts`
- **Action**: Fix type inference issue by explicitly collecting to a typed list or casting.
- **Change**:
  ```java
  // From
  List<Map<String, Object>> contacts = users.stream()...collect(Collectors.toList());
  
  // To (Explicit mapping)
  List<Map<String, Object>> contacts = users.stream()
      .filter(...)
      .map(u -> {
          Map<String, Object> map = new HashMap<>();
          // ... puts ...
          return map;
      })
      .collect(Collectors.toList());
  ```
  *(Note: The previous code looked correct but the compiler is strict. I will ensure the return type of the map lambda is strictly treated as `Map<String, Object>`)*.

## 2. Verification
- **Check**: Compile error should disappear.
