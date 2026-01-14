package com.jaasielsilva.portalceo.controller.admin;

import com.jaasielsilva.portalceo.service.BacklogChamadoService;
import com.jaasielsilva.portalceo.service.SlaMonitoramentoService;
import com.jaasielsilva.portalceo.service.admin.SystemReportService;
import com.jaasielsilva.portalceo.service.ti.SistemaMetricasColetorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/jobs")
@PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
public class JobControlController {

    private final SlaMonitoramentoService slaService;
    private final BacklogChamadoService backlogService;
    private final SistemaMetricasColetorService metricsService;
    private final SystemReportService reportService;

    public JobControlController(SlaMonitoramentoService slaService,
                                BacklogChamadoService backlogService,
                                SistemaMetricasColetorService metricsService,
                                SystemReportService reportService) {
        this.slaService = slaService;
        this.backlogService = backlogService;
        this.metricsService = metricsService;
        this.reportService = reportService;
    }

    @PostMapping("/run/sla")
    public ResponseEntity<String> runSlaJob() {
        try {
            slaService.verificarSlasChamados();
            return ResponseEntity.ok("Job de SLA executado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao executar Job SLA: " + e.getMessage());
        }
    }

    @PostMapping("/run/backlog")
    public ResponseEntity<String> runBacklogJob() {
        try {
            backlogService.recalcularPrioridades();
            return ResponseEntity.ok("Job de Backlog executado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao executar Job Backlog: " + e.getMessage());
        }
    }

    @PostMapping("/run/metrics")
    public ResponseEntity<String> runMetricsJob() {
        try {
            metricsService.coletarMetricas();
            return ResponseEntity.ok("Coleta de métricas realizada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao coletar métricas: " + e.getMessage());
        }
    }

    @PostMapping("/run/daily-report")
    public ResponseEntity<String> runReportJob() {
        try {
            reportService.gerarRelatorioDiario();
            return ResponseEntity.ok("Relatório gerado e enviado por e-mail.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao gerar relatório: " + e.getMessage());
        }
    }
}
