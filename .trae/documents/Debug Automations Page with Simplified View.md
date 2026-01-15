# Debug Automations Page Error

## 1. Simplify `automacoes.html`
- **Action**: Replace the complex `th:switch` blocks with simple text output for now.
- **Reason**: To isolate if the error comes from the `th:switch` logic failing on invalid Enums or nulls.
- **Change**:
  ```html
  <!-- Temporarily simplified -->
  <td>[[${rule.eventType}]]</td>
  <td>[[${rule.actionType}]]</td>
  ```

## 2. Add Row Safety
- **Action**: Add `th:if="${rule != null}"` to the `<tr>` tag.

## 3. Backend Check (`UserAutomationController.java`)
- **Action**: Ensure `rule.active` is initialized (it's a boolean, defaults to false/null).
- **Check**: Verify if `UserAutomation` entity has `@Column(nullable = false)` on `active`.

## 4. Verification
- **User Action**: Refresh page.
- **Result**: If it loads, we know the issue was the Switch logic. We can then rebuild it safely.
