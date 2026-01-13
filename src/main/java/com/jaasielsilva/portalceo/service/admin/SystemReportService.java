package com.jaasielsilva.portalceo.service.admin;

import com.jaasielsilva.portalceo.model.ti.SistemaMetricas;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.ti.SistemaMetricasRepository;
import com.jaasielsilva.portalceo.service.ConfiguracaoService;
import com.jaasielsilva.portalceo.service.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SystemReportService {

    private final UsuarioRepository usuarioRepository;
    private final SistemaMetricasRepository metricasRepository;
    private final EmailService emailService;
    private final ConfiguracaoService configuracaoService;

    public SystemReportService(UsuarioRepository usuarioRepository,
                               SistemaMetricasRepository metricasRepository,
                               EmailService emailService,
                               ConfiguracaoService configuracaoService) {
        this.usuarioRepository = usuarioRepository;
        this.metricasRepository = metricasRepository;
        this.emailService = emailService;
        this.configuracaoService = configuracaoService;
    }

    public void gerarRelatorioDiario() {
        long totalUsuarios = usuarioRepository.count();
        SistemaMetricas ultimasMetricas = metricasRepository.findTopByOrderByCreatedAtDesc();

        StringBuilder corpo = new StringBuilder();
        corpo.append("<h1>Relatório Diário do Sistema</h1>");
        corpo.append("<p>Data: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
        corpo.append("<h3>Estatísticas Gerais</h3>");
        corpo.append("<ul>");
        corpo.append("<li>Total de Usuários: <strong>").append(totalUsuarios).append("</strong></li>");

        if (ultimasMetricas != null) {
            corpo.append("<li>Uso de CPU: ").append(ultimasMetricas.getCpuUsage()).append("%</li>");
            corpo.append("<li>Uso de Memória: ").append(ultimasMetricas.getMemoryUsage()).append("%</li>");
            corpo.append("<li>Uso de Disco: ").append(ultimasMetricas.getDiskUsage()).append(" GB</li>");
        } else {
            corpo.append("<li>Métricas de sistema indisponíveis no momento.</li>");
        }
        corpo.append("</ul>");
        corpo.append("<hr>");
        corpo.append("<p><small>Relatório gerado automaticamente pelo Painel de Controle.</small></p>");

        String destinatario = configuracaoService.getEmailRelatorio();
        emailService.enviarEmail(destinatario, "Relatório Diário do Sistema", corpo.toString());
    }
}
