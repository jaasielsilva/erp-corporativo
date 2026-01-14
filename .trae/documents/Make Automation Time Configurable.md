# Implement Configurable Automation Schedule

## 1. Backend Implementation
### Update `UserAutomation` Entity
- Add field `LocalTime executionTime` (or `Integer hour`) to store user preference (e.g., 08:00).
- Default to `08:00` if null.

### Update `ClientInactivityService`
- Change `@Scheduled` to run **hourly** (e.g., `cron = "0 0 * * * ?"`).
- In the execution logic:
  - Get current hour.
  - Fetch users who have automation rules configured for **this specific hour**.
  - Only fire events/actions for those specific users.
  *(Note: This changes the architecture from "One Global Event" to "User-Specific Checks", which is more precise).*

### Update `UserAutomationController`
- Accept `executionTime` in the `/create` endpoint.

## 2. Frontend Implementation (`automacoes.html`)
### Update Wizard (SweetAlert)
- **Step 3 (New)**: "Que horas você quer receber?"
- Input: `type: 'time'` or a simple Select (08:00, 09:00, ...).
- Pass this value to the backend.

### Update Table
- Show the configured time in a new column or as a badge (e.g., "🔔 às 09:00").

## 3. Verification
- **Test**: Create rule for current hour + 1 minute (conceptually).
- **Simulate**: Trigger the job manually.
- **Check**: Verify logic filters correctly by time.
