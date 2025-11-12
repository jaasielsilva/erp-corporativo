package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.ti.AlertaSeguranca;
import com.jaasielsilva.portalceo.model.ti.SistemaStatus;
import com.jaasielsilva.portalceo.repository.ti.AlertaSegurancaRepository;
import com.jaasielsilva.portalceo.repository.ti.AlertaSegurancaAckRepository;
import com.jaasielsilva.portalceo.repository.ti.SistemaStatusRepository;
import com.jaasielsilva.portalceo.repository.AcaoUsuarioRepository;
import com.jaasielsilva.portalceo.dto.AcaoUsuarioDTO;
import com.jaasielsilva.portalceo.service.ChamadoService;
import com.jaasielsilva.portalceo.service.ti.BackupService;
import com.jaasielsilva.portalceo.service.ti.MetricasService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/ti")
@RequiredArgsConstructor
public class TiController {

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private BackupService backupService;

    @Autowired
    private SistemaStatusRepository sistemaStatusRepository;

    @Autowired
    private AlertaSegurancaRepository alertaSegurancaRepository;

    @Autowired
    private AlertaSegurancaAckRepository alertaSegurancaAckRepository;

    @Autowired
    private AcaoUsuarioRepository acaoUsuarioRepository;

    @Autowired
    private MetricasService metricasService;

    @Autowired
    private com.jaasielsilva.portalceo.service.UsuarioService usuarioService;

    @Autowired
    private com.jaasielsilva.portalceo.service.TermoService termoService;

    // Página principal do TI
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Tecnologia da Informação - Dashboard");
        model.addAttribute("moduleCSS", "ti");

        // Estatísticas do dashboard
        model.addAttribute("sistemasAtivos", getStatusSistemas().size());
        model.addAttribute("chamadosAbertos", (int) chamadoService.contarPorStatus(StatusChamado.ABERTO));
        model.addAttribute("backupsRecentes", backupService.listarBackupsRecentes());
        model.addAttribute("alertasSeguranca", getAlertasSeguranca());
        // Métricas de performance (dinâmicas)
        model.addAttribute("metricas", getMetricasPerformance());

        // Status dos sistemas principais
        model.addAttribute("statusSistemas", getStatusSistemas());

        // Últimos eventos
        model.addAttribute("ultimosEventos", getUltimosEventos());

        return "ti/index";
    }

    // =============== SISTEMAS ===============

    @GetMapping("/sistemas")
    public String sistemas(Model model) {
        model.addAttribute("pageTitle", "Monitoramento de Sistemas");
        model.addAttribute("moduleCSS", "ti");

        // Lista de sistemas monitorados
        model.addAttribute("sistemas", getListaSistemas());

        // Métricas de performance vindas do banco
        model.addAttribute("metricas", getMetricasPerformance());

        return "ti/sistemas";
    }

    @GetMapping("/api/sistemas/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatusSistemasApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("sistemas", getStatusSistemas());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // =============== SUPORTE TÉCNICO ===============

    @GetMapping("/suporte")
    public String suporte(Model model) {
        // Descontinuado no módulo de TI: redireciona para módulo de Suporte
        return "redirect:/suporte";
    }

    @PostMapping("/suporte/chamado")
    @ResponseBody
    public ResponseEntity<?> criarChamado(@RequestParam String titulo,
            @RequestParam String descricao,
            @RequestParam String prioridade,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Descontinuado no módulo de TI: responde com redirect para módulo de Suporte
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/suporte")
                .build();
    }

    // =============== BACKUP ===============

    @GetMapping("/backup")
    public String backup(Model model) {
        model.addAttribute("pageTitle", "Gestão de Backup");
        model.addAttribute("moduleCSS", "ti");

        // Status dos backups
        model.addAttribute("backupsRecentes", backupService.listarBackupsRecentes());
        model.addAttribute("agendamentos", backupService.listarAgendamentos());
        model.addAttribute("espacoUtilizado", backupService.obterEspacoUtilizado());

        return "ti/backup";
    }

    @PostMapping("/backup/executar")
    @ResponseBody
    public ResponseEntity<?> executarBackup(@RequestParam String tipo,
            @RequestParam(required = false) String descricao) {
        Map<String, Object> result = backupService.iniciarBackup(tipo, descricao);
        return ResponseEntity.ok(result);
    }

    // =============== SEGURANÇA ===============
    @GetMapping("/seguranca")
    public String seguranca(Model model,
            @RequestParam(value = "novoAlerta", required = false) String novoAlerta) {

        model.addAttribute("pageTitle", "Segurança da Informação");
        model.addAttribute("moduleCSS", "ti");

        // Alertas de segurança
        model.addAttribute("alertasSeguranca", getAlertasSeguranca());

        // Logs de acesso
        model.addAttribute("logsAcesso", getLogsAcesso());

        // Políticas de segurança
        model.addAttribute("politicas", getPoliticasSeguranca());

        // Caso tenha um novo alerta vindo de algum evento (login, acesso negado, etc)
        if (novoAlerta != null) {
            model.addAttribute("novoAlerta", novoAlerta);
        }

        return "ti/seguranca";
    }

    @GetMapping("/api/seguranca/alertas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAlertasSegurancaApi(Authentication authentication) {
        List<Map<String, Object>> base = getAlertasSeguranca();
        try {
            var usuarioOpt = usuarioService.buscarPorEmail(authentication.getName());
            Long userId = usuarioOpt.map(u -> u.getId()).orElse(null);
            if (userId != null) {
                for (Map<String, Object> a : base) {
                    Object idObj = a.get("id");
                    if (idObj instanceof Long) {
                        Long alertaId = (Long) idObj;
                        boolean ack = alertaSegurancaAckRepository.findByAlertaAndUsuario(alertaId, userId).isPresent();
                        a.put("estado", ack ? "ACK" : "NOVO");
                    } else {
                        a.put("estado", "NOVO");
                    }
                }
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok(base);
    }

    @PostMapping("/api/seguranca/alertas/{id}/ack")
    @ResponseBody
    public ResponseEntity<?> reconhecerAlerta(@PathVariable("id") Long alertaId, Authentication authentication) {
        try {
            var usuarioOpt = usuarioService.buscarPorEmail(authentication.getName());
            if (usuarioOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            Long userId = usuarioOpt.get().getId();
            var existente = alertaSegurancaAckRepository.findByAlertaAndUsuario(alertaId, userId);
            if (existente.isPresent()) {
                return ResponseEntity.ok(Map.of("ack", true, "already", true));
            }
            alertaSegurancaAckRepository.save(new com.jaasielsilva.portalceo.model.ti.AlertaSegurancaAck(alertaId, userId, java.time.LocalDateTime.now()));
            return ResponseEntity.ok(Map.of("ack", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/api/seguranca/logs")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getLogsAcessoApi() {
        return ResponseEntity.ok(getLogsAcesso());
    }

    @GetMapping("/api/seguranca/politicas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getPoliticasSegurancaApi() {
        return ResponseEntity.ok(getPoliticasSeguranca());
    }

    // =============== MÉTODOS AUXILIARES ===============

    private List<Map<String, Object>> getStatusSistemas() {
        List<SistemaStatus> lista = sistemaStatusRepository.findAll();
        if (lista.isEmpty()) {
            sistemaStatusRepository.saveAll(Arrays.asList(
                    new SistemaStatus(null, "ERP Corporativo", "ONLINE", "99.9%", LocalDateTime.now().minusMinutes(1)),
                    new SistemaStatus(null, "Banco de Dados", "ONLINE", "99.8%", LocalDateTime.now().minusMinutes(2)),
                    new SistemaStatus(null, "Servidor Email", "ONLINE", "99.5%", LocalDateTime.now().minusMinutes(3))));
            lista = sistemaStatusRepository.findAll();
        }
        List<Map<String, Object>> sistemas = new ArrayList<>();
        for (SistemaStatus s : lista) {
            Map<String, Object> m = new HashMap<>();
            m.put("nome", s.getNome());
            m.put("status", s.getStatus());
            m.put("uptime", s.getUptime());
            m.put("ultimaVerificacao", s.getUltimaVerificacao());
            sistemas.add(m);
        }
        return sistemas;
    }

    private List<Map<String, Object>> getListaSistemas() {
        return getStatusSistemas(); // Reutilizar para simplicidade
    }

    private Map<String, Object> getMetricasPerformance() {
        return metricasService.obterMetricasAtuais();
    }

    private Map<String, Object> getSLAMetrics() {
        Map<String, Object> sla = new HashMap<>();
        Double tempoMedioHoras = chamadoService.calcularTempoMedioResolucaoGeral();
        Double avaliacaoMedia = chamadoService.calcularAvaliacaoMedia();
        Map<String, Object> slaMap = chamadoService.calcularMetricasSLA();
        double percentualSLA = 0.0;
        if (slaMap != null && slaMap.get("percentualSLACumprido") != null) {
            percentualSLA = ((Number) slaMap.get("percentualSLACumprido")).doubleValue();
        }
        double satisfacaoPercent = avaliacaoMedia != null ? (avaliacaoMedia / 5.0) * 100.0 : 0.0;
        sla.put("tempoMedioResolucao", String.format("%.1f horas", tempoMedioHoras != null ? tempoMedioHoras : 0.0));
        sla.put("satisfacaoCliente", String.format("%.1f%%", satisfacaoPercent));
        sla.put("slaAtendido", String.format("%.1f%%", percentualSLA));
        return sla;
    }

    private List<Map<String, Object>> getBackupsRecentes() {
        List<Map<String, Object>> backups = new ArrayList<>();

        Map<String, Object> backup1 = new HashMap<>();
        backup1.put("tipo", "Completo");
        backup1.put("status", "SUCESSO");
        backup1.put("data", LocalDateTime.now().minusHours(2));
        backup1.put("tamanho", "2.3 GB");
        backups.add(backup1);

        Map<String, Object> backup2 = new HashMap<>();
        backup2.put("tipo", "Incremental");
        backup2.put("status", "SUCESSO");
        backup2.put("data", LocalDateTime.now().minusHours(8));
        backup2.put("tamanho", "456 MB");
        backups.add(backup2);

        return backups;
    }

    private List<Map<String, Object>> getAgendamentosBackup() {
        List<Map<String, Object>> agendamentos = new ArrayList<>();

        Map<String, Object> ag1 = new HashMap<>();
        ag1.put("tipo", "Completo");
        ag1.put("frequencia", "Semanal");
        ag1.put("proximaExecucao", LocalDateTime.now().plusDays(3));
        agendamentos.add(ag1);

        Map<String, Object> ag2 = new HashMap<>();
        ag2.put("tipo", "Incremental");
        ag2.put("frequencia", "Diário");
        ag2.put("proximaExecucao", LocalDateTime.now().plusHours(6));
        agendamentos.add(ag2);

        return agendamentos;
    }

    private Map<String, Object> getEspacoBackup() {
        Map<String, Object> espaco = new HashMap<>();
        espaco.put("utilizado", "45.2 GB");
        espaco.put("disponivel", "154.8 GB");
        espaco.put("percentualUso", 22.6);
        return espaco;
    }

    // metodo que retorna alertas no banco de dados
    private List<Map<String, Object>> getAlertasSeguranca() {
        List<AlertaSeguranca> lista = alertaSegurancaRepository.findAll();

        // Se não houver alertas, retorna lista vazia (sem criar registros de exemplo)
        if (lista.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> alertas = new ArrayList<>();
        for (AlertaSeguranca a : lista) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("tipo", a.getTitulo());
            m.put("severidade", normalizarSeveridade(a.getSeveridade()));
            m.put("data", a.getData());
            m.put("ip", a.getOrigem());
            alertas.add(m);
        }

        return alertas;
    }

    private String normalizarSeveridade(String severidade) {
        if (severidade == null)
            return "INFO";
        String s = severidade.trim().toUpperCase(Locale.ROOT);
        // Mapear valores fora do conjunto esperado para estilos conhecidos
        if ("CRITICA".equals(s) || "CRÍTICA".equals(s))
            return "ALTA";
        if ("ALTA".equals(s) || "MEDIA".equals(s) || "BAIXA".equals(s) || "INFO".equals(s) || "WARNING".equals(s))
            return s;
        return "INFO";
    }

    private List<Map<String, Object>> getLogsAcesso() {
        List<Map<String, Object>> logs = new ArrayList<>();
        // Buscar últimas 10 ações de usuários para compor logs de acesso/auditoria
        try {
            List<AcaoUsuarioDTO> acoes = acaoUsuarioRepository
                    .buscarUltimasAcoes(org.springframework.data.domain.PageRequest.of(0, 50))
                    .getContent();
            for (AcaoUsuarioDTO a : acoes) {
                Map<String, Object> m = new HashMap<>();
                m.put("usuario", a.getUsuario());
                m.put("acao", a.getAcao());
                m.put("ip", a.getIp() != null ? a.getIp() : "N/A");
                m.put("data", a.getData());
                // Status genérico: SUCESSO (uma ação registrada pressupõe sucesso)
                m.put("status", "SUCESSO");
                logs.add(m);
            }
        } catch (Exception e) {
            // Fallback com dados simulados quando não há repositório/registro
            Map<String, Object> l1 = new HashMap<>();
            l1.put("usuario", "sistema");
            l1.put("acao", "Login");
            l1.put("ip", "127.0.0.1");
            l1.put("data", LocalDateTime.now().minusMinutes(5));
            l1.put("status", "SUCESSO");
            logs.add(l1);

            Map<String, Object> l2 = new HashMap<>();
            l2.put("usuario", "admin");
            l2.put("acao", "Alteração de permissão");
            l2.put("ip", "10.0.0.5");
            l2.put("data", LocalDateTime.now().minusMinutes(20));
            l2.put("status", "SUCESSO");
            logs.add(l2);
        }
        return logs;
    }

    private List<Map<String, Object>> getPoliticasSeguranca() {
        List<Map<String, Object>> politicas = new ArrayList<>();
        try {
            var termos = termoService.buscarPorTipo(com.jaasielsilva.portalceo.model.Termo.TipoTermo.POLITICA_SEGURANCA);
            for (var t : termos) {
                Map<String, Object> p = new HashMap<>();
                p.put("nome", t.getTitulo());

                // Mapear status profissional
                String statusLabel;
                boolean vigente = t.isVigente();
                var st = t.getStatus();
                if (st == com.jaasielsilva.portalceo.model.Termo.StatusTermo.PUBLICADO && vigente) {
                    statusLabel = "ATIVA";
                } else if (st == com.jaasielsilva.portalceo.model.Termo.StatusTermo.ARQUIVADO
                        || st == com.jaasielsilva.portalceo.model.Termo.StatusTermo.CANCELADO
                        || (st == com.jaasielsilva.portalceo.model.Termo.StatusTermo.PUBLICADO && !vigente)) {
                    statusLabel = "OBSOLETA";
                } else {
                    statusLabel = "EM REVISÃO";
                }
                p.put("status", statusLabel);

                // Última atualização: prioriza publicação, depois aprovação, depois criação
                java.time.LocalDateTime ultima = t.getDataPublicacao();
                if (ultima == null) ultima = t.getDataAprovacao();
                if (ultima == null) ultima = t.getDataCriacao();
                p.put("ultimaAtualizacao", ultima);

                politicas.add(p);
            }
        } catch (Exception e) {
            // Fallback ao comportamento antigo se houver problemas
            Map<String, Object> p1 = new HashMap<>();
            p1.put("nome", "Política de Senhas");
            p1.put("status", "ATIVA");
            p1.put("ultimaAtualizacao", LocalDateTime.now().minusDays(10));
            politicas.add(p1);

            Map<String, Object> p2 = new HashMap<>();
            p2.put("nome", "Controle de Acesso e Perfis");
            p2.put("status", "ATIVA");
            p2.put("ultimaAtualizacao", LocalDateTime.now().minusDays(30));
            politicas.add(p2);

            Map<String, Object> p3 = new HashMap<>();
            p3.put("nome", "Proteção de Dados e LGPD");
            p3.put("status", "EM REVISÃO");
            p3.put("ultimaAtualizacao", LocalDateTime.now().minusDays(3));
            politicas.add(p3);
        }
        return politicas;
    }

    private List<Map<String, Object>> getUltimosEventos() {
        // Exibir somente os 3 alertas de exemplo solicitados
        List<Map<String, Object>> eventos = new ArrayList<>();

        Map<String, Object> ev1 = new HashMap<>();
        ev1.put("tipo", "Alerta: Tentativa de login suspeita");
        ev1.put("descricao", "Origem: Web");
        ev1.put("data", java.time.LocalDateTime.of(java.time.LocalDate.now().getYear(), 11, 4, 18, 29));
        ev1.put("severidade", "WARNING");
        eventos.add(ev1);

        Map<String, Object> ev2 = new HashMap<>();
        ev2.put("tipo", "Alerta: Falha de autenticação API");
        ev2.put("descricao", "Origem: API Interna");
        ev2.put("data", java.time.LocalDateTime.of(java.time.LocalDate.now().getYear(), 11, 4, 16, 44));
        ev2.put("severidade", "ERROR");
        eventos.add(ev2);

        Map<String, Object> ev3 = new HashMap<>();
        ev3.put("tipo", "Alerta: Ataque DDoS mitigado");
        ev3.put("descricao", "Origem: Gateway");
        ev3.put("data", java.time.LocalDateTime.of(java.time.LocalDate.now().getYear(), 11, 4, 15, 44));
        ev3.put("severidade", "INFO");
        eventos.add(ev3);

        // Garantir ordenação desc por data (mais recente primeiro)
        eventos.sort((e1, e2) -> ((LocalDateTime) e2.get("data")).compareTo((LocalDateTime) e1.get("data")));
        return eventos;
    }

    @GetMapping("/api/sistemas/metricas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricasHistoricoApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("atual", metricasService.obterMetricasAtuais());

        // Normaliza as chaves do histórico para o formato esperado pelo front-end
        List<Map<String, Object>> historicoRaw = metricasService.listarHistorico(20);
        List<Map<String, Object>> historico = new ArrayList<>();
        for (Map<String, Object> h : historicoRaw) {
            Map<String, Object> m = new HashMap<>();
            m.put("createdAt", h.get("ts"));
            m.put("cpuUsage", h.get("cpu"));
            m.put("memoryUsage", h.get("mem"));
            m.put("diskUsage", h.get("disk"));
            m.put("networkLatency", h.get("latency"));
            historico.add(m);
        }
        response.put("historico", historico);

        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // ===== NOVOS ENDPOINTS: /api/ti/metricas/atual e /api/ti/metricas/historico
    // =====
    @GetMapping("/api/ti/metricas/atual")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricasAtualTi() {
        // Retorna exatamente as chaves esperadas pelo front-end
        Map<String, Object> atual = metricasService.obterMetricasAtuais();
        return ResponseEntity.ok(atual);
    }

    @GetMapping("/api/ti/metricas/historico")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getMetricasHistoricoTi() {
        List<Map<String, Object>> historicoRaw = metricasService.listarHistorico(20);
        List<Map<String, Object>> historico = new ArrayList<>();
        for (Map<String, Object> h : historicoRaw) {
            Map<String, Object> m = new HashMap<>();
            m.put("createdAt", h.get("ts"));
            m.put("cpuUsage", h.get("cpu"));
            m.put("memoryUsage", h.get("mem"));
            m.put("diskUsage", h.get("disk"));
            m.put("networkLatency", h.get("latency"));
            historico.add(m);
        }
        return ResponseEntity.ok(historico);
    }
}