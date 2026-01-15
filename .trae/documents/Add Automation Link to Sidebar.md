# Add Automation Link to Sidebar

## 1. Frontend Implementation
### Update `templates/components/sidebar.html`
- **Location**: Inside the "Pessoal" section (`MENU_PESSOAL`), around line 593.
- **Add Link**:
  - **Label**: "Minhas Automações"
  - **Icon**: `fa-magic` (Magic wand icon represents automation well)
  - **URL**: `/minha-conta/automacoes`
  - **Security**: No specific permission needed beyond authentication (or reuse `MENU_PESSOAL` check).

## 2. Verification
- **Test**: Refresh the page.
- **Check**: Verify "Minhas Automações" appears under "Pessoal".
- **Click**: Ensure it navigates to the automation list page.
