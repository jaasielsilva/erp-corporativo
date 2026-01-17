I will replace the content of `src/main/resources/templates/dashboard/index.html` with the code from `src/main/resources/templates/test.html`.

**Why this approach?**
1.  **Exact Match**: You requested the dashboard to be "exactly identical" to the prototype. The prototype is a self-contained single-page application (SPA) design with its own sidebar, CSS, and navigation logic.
2.  **Mock Data**: You authorized using mock data. The prototype has hardcoded JavaScript objects (`mockData`, `allProcesses`, `agendaEvents`) that drive the interface, so it doesn't need the backend data yet.
3.  **Safety**: The backend controller (`DashboardController`) continues to serve the `/dashboard` endpoint. It will pass data to the view, but the new static view will simply ignore it and render the prototype instead.

**Implications:**
- The dashboard will now look exactly like `test.html`.
- The sidebar in the dashboard will be the one from the prototype (visual only for now), not the global system sidebar.
- Navigation within the dashboard (Views: Dashboard, Judicial, Agenda) will work instantly via the embedded JavaScript.

I will proceed to overwrite `dashboard/index.html`.