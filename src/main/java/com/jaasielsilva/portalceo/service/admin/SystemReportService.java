package com.jaasielsilva.portalceo.service.admin;

import com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico;
import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import com.jaasielsilva.portalceo.model.ti.SistemaMetricas;
import com.jaasielsilva.portalceo.repository.ChamadoRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.juridico.DocumentoJuridicoRepository;
import com.jaasielsilva.portalceo.repository.juridico.ProcessoJuridicoRepository;
import com.jaasielsilva.portalceo.repository.ti.SistemaMetricasRepository;
import com.jaasielsilva.portalceo.service.ConfiguracaoService;
import com.jaasielsilva.portalceo.service.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemReportService {

    private final UsuarioRepository usuarioRepository;
    private final SistemaMetricasRepository metricasRepository;
    private final DocumentoJuridicoRepository documentoJuridicoRepository;
    private final ProcessoJuridicoRepository processoJuridicoRepository;
    private final ChamadoRepository chamadoRepository;
    private final EmailService emailService;
    private final ConfiguracaoService configuracaoService;

    public SystemReportService(UsuarioRepository usuarioRepository,
            SistemaMetricasRepository metricasRepository,
            DocumentoJuridicoRepository documentoJuridicoRepository,
            ProcessoJuridicoRepository processoJuridicoRepository,
            ChamadoRepository chamadoRepository,
            EmailService emailService,
            ConfiguracaoService configuracaoService) {
        this.usuarioRepository = usuarioRepository;
        this.metricasRepository = metricasRepository;
        this.documentoJuridicoRepository = documentoJuridicoRepository;
        this.processoJuridicoRepository = processoJuridicoRepository;
        this.chamadoRepository = chamadoRepository;
        this.emailService = emailService;
        this.configuracaoService = configuracaoService;
    }

    public void gerarRelatorioDiario() {
        // --- DATA FETCHING ---
        long totalUsuarios = usuarioRepository.count();
        long totalChamadosAbertos = chamadoRepository.count(); // Aproximado, ideal seria filtrar por status
        SistemaMetricas ultimasMetricas = metricasRepository.findTopByOrderByCreatedAtDesc();

        // Legal Data
        List<ProcessoJuridico> processosAtivos = processoJuridicoRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProcessoJuridico.StatusProcesso.EM_ANDAMENTO)
                .collect(Collectors.toList());

        Map<ProcessoJuridico.TipoAcaoJuridica, Long> processosPorTipo = processosAtivos.stream()
                .collect(Collectors.groupingBy(ProcessoJuridico::getTipo, Collectors.counting()));

        List<DocumentoJuridico> contratosPendentes = documentoJuridicoRepository.findAll().stream()
                .filter(d -> d.getStatusAssinatura() != null && !d.getStatusAssinatura().equalsIgnoreCase("SIGNED")
                        && !d.getStatusAssinatura().equalsIgnoreCase("REJECTED"))
                .collect(Collectors.toList());

        // --- HTML BUILDING ---
        StringBuilder corpo = new StringBuilder();
        corpo.append("<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>");

        // Header
        corpo.append(
                "<div style='background-color: #2c3e50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>");
        corpo.append("<h2 style='margin:0;'>⚖️ Panorama Executivo - Jurídico</h2>");
        corpo.append("<p style='margin:5px 0 0; color: #bdc3c7;'>Relatório Diário • ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>");
        corpo.append("</div>");

        // HERO SECTION: LEGAL
        corpo.append("<div style='padding: 20px; background-color: #f8f9fa; border: 1px solid #e9ecef;'>");
        corpo.append(
                "<h3 style='color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px;'>Destaques Jurídicos</h3>");

        // Cards Grid
        corpo.append("<table width='100%' style='margin-bottom: 20px;'><tr>");

        // Card 1: Contratos Pendentes
        String colorContratos = contratosPendentes.isEmpty() ? "#27ae60" : "#e74c3c"; // Verde se zero, Vermelho se
                                                                                      // houver pendencias
        corpo.append("<td style='background: white; padding: 15px; border-radius: 5px; border-left: 5px solid ")
                .append(colorContratos).append("; width: 48%; box-shadow: 0 2px 5px rgba(0,0,0,0.05);'>");
        corpo.append("<h1 style='margin:0; font-size: 36px; color: ").append(colorContratos).append(";'>")
                .append(contratosPendentes.size()).append("</h1>");
        corpo.append("<p style='margin:0; font-weight:bold; color: #7f8c8d;'>Contratos Pendentes</p>");
        corpo.append("</td>");

        corpo.append("<td style='width: 4%;'></td>"); // Spacer

        // Card 2: Processos Ativos
        corpo.append(
                "<td style='background: white; padding: 15px; border-radius: 5px; border-left: 5px solid #3498db; width: 48%; box-shadow: 0 2px 5px rgba(0,0,0,0.05);'>");
        corpo.append("<h1 style='margin:0; font-size: 36px; color: #3498db;'>").append(processosAtivos.size())
                .append("</h1>");
        corpo.append("<p style='margin:0; font-weight:bold; color: #7f8c8d;'>Processos Ativos</p>");
        corpo.append("</td>");

        corpo.append("</tr></table>");

        // Breakdown Table (Processos)
        if (!processosPorTipo.isEmpty()) {
            corpo.append("<h4 style='color: #2c3e50;'>Resumo por Tipo de Ação</h4>");
            corpo.append("<table style='width: 100%; border-collapse: collapse; font-size: 14px;'>");
            corpo.append(
                    "<tr style='background-color: #ecf0f1;'><th style='padding: 8px; text-align: left;'>Tipo</th><th style='padding: 8px; text-align: right;'>Qtd</th></tr>");
            processosPorTipo.forEach((tipo, qtd) -> {
                corpo.append("<tr>");
                corpo.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>").append(tipo.getDescricao())
                        .append("</td>");
                corpo.append("<td style='padding: 8px; border-bottom: 1px solid #eee; text-align: right;'><strong>")
                        .append(qtd).append("</strong></td>");
                corpo.append("</tr>");
            });
            corpo.append("</table>");
        }

        // Pending Contracts List
        if (!contratosPendentes.isEmpty()) {
            corpo.append("<h4 style='color: #c0392b; margin-top: 20px;'>⚠️ Atenção: Aguardando Assinatura</h4>");
            corpo.append("<ul style='padding-left: 20px; color: #555;'>");
            for (DocumentoJuridico doc : contratosPendentes) {
                corpo.append("<li>")
                        .append("<strong>").append(doc.getTitulo()).append("</strong>")
                        .append(" <span style='font-size: 12px; color: #999;'>(Criado em: ")
                        .append(doc.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM"))).append(")</span>")
                        .append("</li>");
            }
            corpo.append("</ul>");
        } else {
            corpo.append("<p style='color: #27ae60;'>✅ Todos os contratos processados.</p>");
        }
        corpo.append("</div>"); // End Hero

        // SECONDARY STATS (Footer)
        corpo.append("<div style='margin-top: 20px; padding: 15px; background-color: #ecf0f1; border-radius: 5px;'>");
        corpo.append("<h4 style='margin-top: 0; color: #7f8c8d;'>Outros Indicadores</h4>");
        corpo.append("<p style='margin: 5px 0;'>👥 <strong>RH:</strong> ").append(totalUsuarios)
                .append(" Usuários Ativos</p>");
        corpo.append("<p style='margin: 5px 0;'>🛠️ <strong>Suporte:</strong> ").append(totalChamadosAbertos)
                .append(" Chamados Totais no Sistema</p>");

        if (ultimasMetricas != null) {
            corpo.append("<p style='margin: 5px 0; font-size: 12px; color: #95a5a6;'>🖥️ Saúde do Sistema: CPU ")
                    .append(ultimasMetricas.getCpuUsage()).append("% | RAM ").append(ultimasMetricas.getMemoryUsage())
                    .append("%</p>");
        }
        corpo.append("</div>");

        corpo.append(
                "<p style='text-align: center; color: #bdc3c7; font-size: 11px; margin-top: 20px;'>Portal CEO • Inteligência Corporativa</p>");
        corpo.append("</body></html>");

        String destinatario = configuracaoService.getEmailRelatorio();
        emailService.enviarEmail(destinatario,
                "Panorama Jurídico Diário - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM")),
                corpo.toString());
    }
}
