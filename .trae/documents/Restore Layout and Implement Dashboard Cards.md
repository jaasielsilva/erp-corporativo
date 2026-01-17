I will restore the standard layout (Sidebar, Topbar, Footer) in `dashboard/index.html` while keeping the new "Cards" and dashboard functionality from the prototype.

**Plan:**
1.  **Reconstruct `dashboard/index.html`**:
    *   Use the standard Thymeleaf layout structure (`app-container`, `main-content`, `content-area`) and include the system fragments:
        *   `~{components/sidebar :: sidebar}`
        *   `~{components/topbar :: topbar}`
        *   `~{components/footer :: footer}`
2.  **Integrate Prototype Content**:
    *   Inject the **CSS** from `test.html` (scoped to the dashboard elements like `.dash-card`, `.timeline`, etc., avoiding conflicts with the main sidebar).
    *   Inject the **HTML Views** (Dashboard Grid, Process List, Agenda, Flow Detail) into the main content area.
    *   Add a **Local Tab Navigation** bar at the top of the content area so you can switch between "Dashboard", "Processos", and "Agenda" without leaving the page (since the main sidebar links might point elsewhere).
3.  **Integrate Logic**:
    *   Include the JavaScript from `test.html` (Mock Data, Event Handlers).
    *   Update the `switchView` function to toggle the local tabs instead of the (now removed) custom sidebar links.

This ensures the **Cards** look exactly like the prototype, but the **Application Shell** (Sidebar/Header) remains consistent with the rest of the ERP.