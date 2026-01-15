# Implement Job Control Panel & Daily Report Job

## 1. Backend Implementation
### Create `SystemReportService`
- **Location**: `src/main/java/com/jaasielsilva/portalceo/service/admin/SystemReportService.java`
- **Logic**:
  - Inject `UsuarioRepository` to count active users.
  - Inject `EmailService` to send the report.
  - Method `gerarRelatorioDiario()`: Formats a summary string and sends it to the configured admin email (hardcoded or from properties for now).

### Create `JobControlController`
- **Location**: `src/main/java/com/jaasielsilva/portalceo/controller/admin/JobControlController.java`
- **Purpose**: API to manually trigger jobs.
- **Endpoints**:
  - `POST /api/admin/jobs/run/sla` -> calls `SlaMonitoramentoService`
  - `POST /api/admin/jobs/run/backlog` -> calls `BacklogChamadoService`
  - `POST /api/admin/jobs/run/metrics` -> calls `SistemaMetricasColetorService`
  - `POST /api/admin/jobs/run/daily-report` -> calls `SystemReportService`

## 2. Frontend Implementation
### Update `templates/configuracoes/index.html`
- **Layout**:
  - **Header**: "Painel de Controle e Rotinas"
  - **Grid**: 2 columns (Technical Jobs vs Management Jobs).
- **Cards**:
  1. **Monitor de SLA** (Technical)
  2. **Prioridade de Backlog** (Technical)
  3. **Snapshot de Métricas** (Technical)
  4. **Relatório Gerencial Diário** (Management - triggers the new Email Job)
- **Script**:
  - Add JavaScript functions to `fetch` the endpoints and show Toast notifications upon success/failure.

## 3. Verification
- **Action**: Go to `/configuracoes`, click "Gerar e Enviar Agora" on the Report card.
- **Check**: Verify console logs for "Email sent" confirmation (since we can't check actual inbox).
