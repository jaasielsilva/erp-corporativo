package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import com.jaasielsilva.portalceo.model.ColaboradorEscala;
import com.jaasielsilva.portalceo.model.EscalaTrabalho;
import com.jaasielsilva.portalceo.repository.ColaboradorEscalaRepository;
import com.jaasielsilva.portalceo.repository.RegistroPontoRepository;
import com.jaasielsilva.portalceo.repository.EscalaTrabalhoRepository;
import com.jaasielsilva.portalceo.repository.RhParametroPontoRepository;
import com.jaasielsilva.portalceo.repository.SolicitacaoFeriasRepository;
import com.jaasielsilva.portalceo.model.RhParametroPonto;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Usuario.Status;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rh/ponto-escalas")
public class PontoEscalasController {

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private EscalaTrabalhoRepository escalaTrabalhoRepository;
    @Autowired
    private ColaboradorEscalaRepository colaboradorEscalaRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private RhParametroPontoRepository pontoRepository;
    @Autowired
    private SolicitacaoFeriasRepository feriasRepository;

    private final java.util.concurrent.ExecutorService gerarExecutor = java.util.concurrent.Executors.newFixedThreadPool(8);
    private final java.util.Map<String, java.util.Map<String, Object>> gerarJobs = new java.util.concurrent.ConcurrentHashMap<>();

    @GetMapping("/registros")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaRegistros() {
        return "rh/ponto-escalas/registros";
    }

    @GetMapping("/escalas")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String paginaEscalas(Model model) {
        YearMonth ym = YearMonth.now();
        model.addAttribute("mesAtual", ym.getMonthValue());
        model.addAttribute("anoAtual", ym.getYear());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("escalasVigentes", escalaTrabalhoRepository.findEscalasVigentes(LocalDate.now()));
        return "rh/ponto-escalas/escalas";
    }

    @GetMapping("/atribuir-massa")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String paginaAtribuirMassa(Model model) {
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("escalasVigentes", escalaTrabalhoRepository.findEscalasVigentes(LocalDate.now()));
        model.addAttribute("colaboradoresAtivos", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/atribuir-massa";
    }

    @PostMapping("/api/atribuir-massa")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo", "escalaAlertas"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> atribuirMassa(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuarioLogado = null;
            if (principal != null && principal.getName() != null) {
                usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            }
            if (payload.get("escalaId") == null) {
                resp.put("success", false);
                resp.put("message", "Escala obrigatória");
                return ResponseEntity.badRequest().body(resp);
            }
            Long escalaId = Long.valueOf(String.valueOf(payload.get("escalaId")));
            EscalaTrabalho escala = escalaTrabalhoRepository.findById(escalaId).orElse(null);
            if (escala == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return ResponseEntity.badRequest().body(resp);
            }
            if (payload.get("dataInicio") == null || String.valueOf(payload.get("dataInicio")).isBlank()) {
                resp.put("success", false);
                resp.put("message", "Data início obrigatória");
                return ResponseEntity.badRequest().body(resp);
            }
            LocalDate dataInicio = LocalDate.parse(String.valueOf(payload.get("dataInicio")));
            Object dfObj = payload.get("dataFim");
            LocalDate dataFim = dfObj != null && !String.valueOf(dfObj).isBlank() ? LocalDate.parse(String.valueOf(dfObj)) : null;
            if (dataFim != null && dataFim.isBefore(dataInicio)) {
                resp.put("success", false);
                resp.put("message", "Data fim não pode ser antes de início");
                return ResponseEntity.badRequest().body(resp);
            }
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) payload.get("colaboradorIds");
            if (ids == null || ids.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Nenhum colaborador informado");
                return ResponseEntity.badRequest().body(resp);
            }

            List<Long> processed = new ArrayList<>();
            for (Integer i : ids) {
                Long colId = Long.valueOf(i);
                List<ColaboradorEscala> vigentes = colaboradorEscalaRepository.findVigenteByColaboradorAndData(colId, dataInicio);
                for (ColaboradorEscala ce : vigentes) {
                    LocalDate fim = dataInicio.minusDays(1);
                    ce.setDataFim(fim);
                    colaboradorEscalaRepository.save(ce);
                }
                ColaboradorEscala novo = new ColaboradorEscala();
                Colaborador col = colaboradorService.findById(colId);
                novo.setColaborador(col);
                novo.setEscalaTrabalho(escala);
                novo.setDataInicio(dataInicio);
                novo.setDataFim(dataFim);
                novo.setAtivo(true);
                if (usuarioLogado != null) {
                    novo.setUsuarioCriacao(usuarioLogado);
                }
                colaboradorEscalaRepository.save(novo);
                processed.add(colId);
            }
            resp.put("success", true);
            resp.put("processados", processed.size());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro na atribuição em massa");
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    @PostMapping("/api/escalas/salvar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> salvarEscala(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuarioLogado = null;
            if (principal != null && principal.getName() != null) {
                usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            }
            EscalaTrabalho e;
            Object idObj = payload.get("id");
            if (idObj != null && !String.valueOf(idObj).isBlank()) {
                Long id = Long.valueOf(String.valueOf(idObj));
                e = escalaTrabalhoRepository.findById(id).orElseGet(EscalaTrabalho::new);
                e.setId(id);
            } else {
                e = new EscalaTrabalho();
            }

            if (payload.get("nome") == null || String.valueOf(payload.get("nome")).isBlank()) {
                resp.put("success", false);
                resp.put("message", "Nome da escala é obrigatório");
                return ResponseEntity.badRequest().body(resp);
            }
            e.setNome(String.valueOf(payload.get("nome")));
            e.setDescricao(null);
            String tipo = String.valueOf(payload.get("tipo"));
            if (tipo == null || tipo.isBlank()) {
                resp.put("success", false);
                resp.put("message", "Tipo da escala é obrigatório");
                return ResponseEntity.badRequest().body(resp);
            }
            e.setTipo(EscalaTrabalho.TipoEscala.valueOf(tipo));

            String entrada1 = String.valueOf(payload.get("horarioEntrada1"));
            String saida1 = String.valueOf(payload.get("horarioSaida1"));
            if (entrada1 == null || saida1 == null || entrada1.isBlank() || saida1.isBlank()) {
                resp.put("success", false);
                resp.put("message", "Horários do primeiro período são obrigatórios");
                return ResponseEntity.badRequest().body(resp);
            }
            String entrada2 = String.valueOf(payload.getOrDefault("horarioEntrada2", null));
            String saida2 = String.valueOf(payload.getOrDefault("horarioSaida2", null));

            e.setHorarioEntrada1(parseHoraFlex(entrada1));
            e.setHorarioSaida1(parseHoraFlex(saida1));
            if (entrada2 != null && !entrada2.equals("null") && !entrada2.isBlank()) e.setHorarioEntrada2(parseHoraFlex(entrada2));
            else e.setHorarioEntrada2(null);
            if (saida2 != null && !saida2.equals("null") && !saida2.isBlank()) e.setHorarioSaida2(parseHoraFlex(saida2));
            else e.setHorarioSaida2(null);

            Integer intervaloMinimo = Integer.valueOf(String.valueOf(payload.getOrDefault("intervaloMinimo", 0)));
            if (intervaloMinimo != null && intervaloMinimo < 0) intervaloMinimo = 0;
            e.setIntervaloMinimo(intervaloMinimo);

            Object dvi = payload.get("dataVigenciaInicio");
            Object dvf = payload.get("dataVigenciaFim");
            e.setDataVigenciaInicio(dvi != null && !String.valueOf(dvi).isBlank() ? LocalDate.parse(String.valueOf(dvi)) : null);
            e.setDataVigenciaFim(dvf != null && !String.valueOf(dvf).isBlank() ? LocalDate.parse(String.valueOf(dvf)) : null);
            if (e.getDataVigenciaInicio() != null && e.getDataVigenciaFim() != null && e.getDataVigenciaFim().isBefore(e.getDataVigenciaInicio())) {
                resp.put("success", false);
                resp.put("message", "Vigência fim não pode ser antes do início");
                return ResponseEntity.badRequest().body(resp);
            }

            e.setToleranciaAtraso(Boolean.valueOf(String.valueOf(payload.getOrDefault("toleranciaAtraso", true))));
            e.setMinutosTolerancia(Integer.valueOf(String.valueOf(payload.getOrDefault("minutosTolerancia", 10))));

            e.setTrabalhaSegunda(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaSegunda", true))));
            e.setTrabalhaTerca(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaTerca", true))));
            e.setTrabalhaQuarta(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaQuarta", true))));
            e.setTrabalhaQuinta(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaQuinta", true))));
            e.setTrabalhaSexta(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaSexta", true))));
            e.setTrabalhaSabado(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaSabado", false))));
            e.setTrabalhaDomingo(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaDomingo", false))));

            if (!e.getHorarioEntrada1().isBefore(e.getHorarioSaida1())) {
                resp.put("success", false);
                resp.put("message", "Horários do primeiro período inválidos");
                return ResponseEntity.badRequest().body(resp);
            }
            if (e.getHorarioEntrada2() != null && e.getHorarioSaida2() != null && !e.getHorarioEntrada2().isBefore(e.getHorarioSaida2())) {
                resp.put("success", false);
                resp.put("message", "Horários do segundo período inválidos");
                return ResponseEntity.badRequest().body(resp);
            }

            if (usuarioLogado != null) {
                e.setUsuarioCriacao(usuarioLogado);
            }
            escalaTrabalhoRepository.save(e);
            resp.put("success", true);
            resp.put("id", e.getId());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Erro ao salvar escala");
            return ResponseEntity.internalServerError().body(err);
        }
    }

    @PostMapping("/api/escalas/gerar-automatico/start")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> gerarAutomaticoStart(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            int mes = Integer.parseInt(String.valueOf(payload.get("mes")));
            int ano = Integer.parseInt(String.valueOf(payload.get("ano")));
            Object escalaObj = payload.get("escalaId");
            if (mes < 1 || mes > 12 || ano < 2000 || ano > 2100 || escalaObj == null || String.valueOf(escalaObj).isBlank()) {
                resp.put("success", false);
                resp.put("message", "Parâmetros inválidos");
                return ResponseEntity.badRequest().body(resp);
            }
            Long escalaId = Long.valueOf(String.valueOf(escalaObj));
            EscalaTrabalho escala = escalaTrabalhoRepository.findById(escalaId).orElse(null);
            if (escala == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return ResponseEntity.badRequest().body(resp);
            }
            Object depObj = payload.get("departamentoId");
            Long departamentoId = (depObj != null && !String.valueOf(depObj).isBlank()) ? Long.valueOf(String.valueOf(depObj)) : null;
            String jobId = java.util.UUID.randomUUID().toString();
            java.util.Map<String, Object> status = new java.util.HashMap<>();
            status.put("running", true);
            status.put("success", true);
            status.put("message", "Em processamento");
            status.put("processados", 0);
            status.put("erros", 0);
            status.put("total", 0);
            status.put("mes", mes);
            status.put("ano", ano);
            status.put("departamentoId", departamentoId);
            status.put("escalaId", escalaId);
            status.put("startedAt", java.time.LocalDateTime.now().toString());
            gerarJobs.put(jobId, status);
            gerarExecutor.submit(() -> {
                try {
                    java.time.YearMonth ym = java.time.YearMonth.of(ano, mes);
                    java.time.LocalDate inicio = ym.atDay(1);
                    java.time.LocalDate fim = ym.atEndOfMonth();
                    final Usuario usuarioLogado = (principal != null && principal.getName() != null)
                            ? usuarioService.buscarPorEmail(principal.getName()).orElse(null)
                            : null;
                    java.util.List<com.jaasielsilva.portalceo.model.Colaborador> candidatos = colaboradorService.listarAtivos();
                    if (departamentoId != null) {
                        candidatos = candidatos.stream().filter(c -> c.getDepartamento() != null && departamentoId.equals(c.getDepartamento().getId())).collect(java.util.stream.Collectors.toList());
                    }
                    status.put("total", candidatos.size());
                    java.util.Map<Long, java.util.List<com.jaasielsilva.portalceo.model.ColaboradorEscala>> vigentesMap = new java.util.HashMap<>();
                    if (!candidatos.isEmpty()) {
                        java.util.List<Long> ids = candidatos.stream().map(com.jaasielsilva.portalceo.model.Colaborador::getId).toList();
                        int chunk = candidatos.size() <= 300 ? 100 : (candidatos.size() <= 1000 ? 200 : 500);
                        Object chunkObj = payload.get("chunkSize");
                        if (chunkObj != null) {
                            try {
                                int c = Integer.parseInt(String.valueOf(chunkObj));
                                if (c == 100 || c == 200 || c == 500) {
                                    chunk = c;
                                }
                            } catch (Exception ignore) {}
                        }
                        int batchSize = 200;
                        Object batchObj = payload.get("batchSize");
                        if (batchObj != null) {
                            try {
                                int b = Integer.parseInt(String.valueOf(batchObj));
                                if (b == 100 || b == 200 || b == 500) {
                                    batchSize = b;
                                }
                            } catch (Exception ignore) {}
                        }
                        long t0 = System.nanoTime();
                        for (int i = 0; i < ids.size(); i += chunk) {
                            java.util.List<Long> sub = ids.subList(i, Math.min(i + chunk, ids.size()));
                            java.util.List<com.jaasielsilva.portalceo.model.ColaboradorEscala> list = colaboradorEscalaRepository.findVigentesByColaboradoresAndData(sub, inicio);
                            for (com.jaasielsilva.portalceo.model.ColaboradorEscala ce : list) {
                                Long cid = ce.getColaborador() != null ? ce.getColaborador().getId() : null;
                                if (cid != null) {
                                    vigentesMap.computeIfAbsent(cid, k -> new java.util.ArrayList<>()).add(ce);
                                }
                            }
                        }
                        long t1 = System.nanoTime();
                        status.put("chunkSize", chunk);
                        status.put("batchSize", batchSize);
                        status.put("prefetchMs", (t1 - t0) / 1_000_000);
                    }
                    long tProcessStart = System.nanoTime();
                    java.util.concurrent.ExecutorCompletionService<java.util.Map<String, Object>> ecs = new java.util.concurrent.ExecutorCompletionService<>(gerarExecutor);
                    for (com.jaasielsilva.portalceo.model.Colaborador col : candidatos) {
                        ecs.submit(() -> {
                            java.util.Map<String, Object> log = new java.util.HashMap<>();
                            log.put("colaboradorId", col.getId());
                            log.put("colaborador", col.getNome());
                            try {
                                java.util.List<com.jaasielsilva.portalceo.model.ColaboradorEscala> vigentes = vigentesMap.getOrDefault(col.getId(), java.util.Collections.emptyList());
                                for (com.jaasielsilva.portalceo.model.ColaboradorEscala ce : vigentes) {
                                    ce.setDataFim(inicio.minusDays(1));
                                }
                                com.jaasielsilva.portalceo.model.ColaboradorEscala novo = new com.jaasielsilva.portalceo.model.ColaboradorEscala();
                                novo.setColaborador(col);
                                novo.setEscalaTrabalho(escala);
                                novo.setDataInicio(inicio);
                                novo.setDataFim(fim);
                                novo.setAtivo(true);
                                if (usuarioLogado != null) {
                                    novo.setUsuarioCriacao(usuarioLogado);
                                }
                                java.util.List<com.jaasielsilva.portalceo.model.ColaboradorEscala> toSave = new java.util.ArrayList<>(vigentes);
                                toSave.add(novo);
                                colaboradorEscalaRepository.saveAll(toSave);
                                log.put("status", "atribuido");
                            } catch (Exception e) {
                                log.put("status", "erro");
                                String msg = e.getMessage();
                                Throwable root = e;
                                while (root.getCause() != null) root = root.getCause();
                                if (root != null && root != e && root.getMessage() != null) msg = root.getMessage();
                                log.put("mensagem", msg != null ? msg : "Falha ao atribuir");
                                log.put("exception", e.getClass().getSimpleName());
                            }
                            return log;
                        });
                    }
                    int processed = 0;
                    int ok = 0;
                    int erros = 0;
                    java.util.List<java.util.Map<String, Object>> logs = new java.util.ArrayList<>();
                    for (int i = 0; i < candidatos.size(); i++) {
                        try {
                            java.util.concurrent.Future<java.util.Map<String, Object>> f = ecs.take();
                            java.util.Map<String, Object> l = f.get();
                            logs.add(l);
                            processed++;
                            if ("atribuido".equals(l.get("status"))) ok++; else erros++;
                            status.put("processados", processed);
                            status.put("erros", erros);
                        } catch (Exception ignore) {
                            erros++;
                            status.put("erros", erros);
                        }
                    }
                    status.put("running", false);
                    status.put("success", erros == 0);
                    status.put("message", erros == 0 ? "Concluído" : "Concluído com falhas");
                    status.put("logs", logs);
                    long tProcessEnd = System.nanoTime();
                    status.put("processMs", (tProcessEnd - tProcessStart) / 1_000_000);
                    status.put("finishedAt", java.time.LocalDateTime.now().toString());
                } catch (Exception e) {
                    status.put("running", false);
                    status.put("success", false);
                    status.put("message", "Erro durante processamento");
                    status.put("error", String.valueOf(e.getMessage()));
                    status.put("finishedAt", java.time.LocalDateTime.now().toString());
                }
            });
            resp.put("success", true);
            resp.put("jobId", jobId);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao iniciar geração automática");
            resp.put("error", String.valueOf(ex.getMessage()));
            return ResponseEntity.ok(resp);
        }
    }

    @GetMapping("/api/escalas/gerar-automatico/status")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> gerarAutomaticoStatus(@RequestParam String jobId) {
        java.util.Map<String, Object> st = gerarJobs.get(jobId);
        Map<String, Object> resp = new HashMap<>();
        if (st == null) {
            resp.put("success", false);
            resp.put("message", "Job não encontrado");
            return resp;
        }
        resp.put("success", true);
        resp.putAll(st);
        return resp;
    }

    @PostMapping("/api/escalas/gerar-automatico")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo", "escalaAlertas"}, allEntries = true)
    public ResponseEntity<Map<String, Object>> gerarAutomatico(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            int mes = Integer.parseInt(String.valueOf(payload.get("mes")));
            int ano = Integer.parseInt(String.valueOf(payload.get("ano")));
            if (mes < 1 || mes > 12 || ano < 2000 || ano > 2100) {
                resp.put("success", false);
                resp.put("message", "Mês/Ano inválidos");
                return ResponseEntity.badRequest().body(resp);
            }
            Object escalaObj = payload.get("escalaId");
            if (escalaObj == null || String.valueOf(escalaObj).isBlank()) {
                resp.put("success", false);
                resp.put("message", "Selecione uma escala para gerar automaticamente");
                return ResponseEntity.badRequest().body(resp);
            }
            Long escalaId = Long.valueOf(String.valueOf(escalaObj));
            EscalaTrabalho escala = escalaTrabalhoRepository.findById(escalaId).orElse(null);
            if (escala == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return ResponseEntity.badRequest().body(resp);
            }
            Object depObj = payload.get("departamentoId");
            Long departamentoId = (depObj != null && !String.valueOf(depObj).isBlank())
                    ? Long.valueOf(String.valueOf(depObj))
                    : null;
            java.time.YearMonth ym = java.time.YearMonth.of(ano, mes);
            LocalDate inicio = ym.atDay(1);
            LocalDate fim = ym.atEndOfMonth();
            Usuario usuarioLogado = null;
            if (principal != null && principal.getName() != null) {
                usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            }
            List<Colaborador> candidatos = colaboradorService.listarAtivos();
            if (departamentoId != null) {
                candidatos = candidatos.stream()
                        .filter(c -> c.getDepartamento() != null && departamentoId.equals(c.getDepartamento().getId()))
                        .collect(java.util.stream.Collectors.toList());
            }
            Map<Long, List<ColaboradorEscala>> vigentesMap = new HashMap<>();
            if (!candidatos.isEmpty()) {
                List<Long> ids = candidatos.stream().map(Colaborador::getId).toList();
                int chunk = candidatos.size() <= 300 ? 100 : (candidatos.size() <= 1000 ? 200 : 500);
                Object chunkObj = payload.get("chunkSize");
                if (chunkObj != null) {
                    try {
                        int c = Integer.parseInt(String.valueOf(chunkObj));
                        if (c == 100 || c == 200 || c == 500) {
                            chunk = c;
                        }
                    } catch (Exception ignore) {}
                }
                long t0 = System.nanoTime();
                for (int i = 0; i < ids.size(); i += chunk) {
                    List<Long> sub = ids.subList(i, Math.min(i + chunk, ids.size()));
                    for (ColaboradorEscala ce : colaboradorEscalaRepository.findVigentesByColaboradoresAndData(sub, inicio)) {
                        Long cid = ce.getColaborador() != null ? ce.getColaborador().getId() : null;
                        if (cid != null) vigentesMap.computeIfAbsent(cid, k -> new ArrayList<>()).add(ce);
                    }
                }
                long t1 = System.nanoTime();
                resp.put("chunkSize", chunk);
                resp.put("prefetchMs", (t1 - t0) / 1_000_000);
            }
            long tStart = System.nanoTime();
            int ok = 0;
            List<Map<String, Object>> logs = new ArrayList<>();
            Map<Long, List<ColaboradorEscala>> saveMap = new HashMap<>();
            for (Colaborador col : candidatos) {
                List<ColaboradorEscala> vigentes = vigentesMap.getOrDefault(col.getId(), java.util.Collections.emptyList());
                for (ColaboradorEscala ce : vigentes) {
                    ce.setDataFim(inicio.minusDays(1));
                }
                ColaboradorEscala novo = new ColaboradorEscala();
                novo.setColaborador(col);
                novo.setEscalaTrabalho(escala);
                novo.setDataInicio(inicio);
                novo.setDataFim(fim);
                novo.setAtivo(true);
                if (usuarioLogado != null) {
                    novo.setUsuarioCriacao(usuarioLogado);
                }
                List<ColaboradorEscala> toSave = new ArrayList<>(vigentes);
                toSave.add(novo);
                saveMap.put(col.getId(), toSave);
            }
            int batchSize = 200;
            Object batchObj = payload.get("batchSize");
            if (batchObj != null) {
                try {
                    int b = Integer.parseInt(String.valueOf(batchObj));
                    if (b == 100 || b == 200 || b == 500) {
                        batchSize = b;
                    }
                } catch (Exception ignore) {}
            }
            List<Long> keys = new ArrayList<>(saveMap.keySet());
            long saveStart = System.nanoTime();
            int batchCount = 0;
            for (int i = 0; i < keys.size(); i += batchSize) {
                List<Long> subKeys = keys.subList(i, Math.min(i + batchSize, keys.size()));
                List<ColaboradorEscala> batchList = new ArrayList<>();
                for (Long k : subKeys) {
                    batchList.addAll(saveMap.getOrDefault(k, java.util.Collections.emptyList()));
                }
                try {
                    colaboradorEscalaRepository.saveAll(batchList);
                    for (Long k : subKeys) {
                        Map<String, Object> l = new HashMap<>();
                        l.put("colaboradorId", k);
                        l.put("status", "atribuido");
                        logs.add(l);
                        ok++;
                    }
                } catch (Exception e) {
                    for (Long k : subKeys) {
                        List<ColaboradorEscala> indiv = saveMap.getOrDefault(k, java.util.Collections.emptyList());
                        try {
                            colaboradorEscalaRepository.saveAll(indiv);
                            Map<String, Object> l = new HashMap<>();
                            l.put("colaboradorId", k);
                            l.put("status", "atribuido");
                            logs.add(l);
                            ok++;
                        } catch (Exception ex) {
                            Map<String, Object> l = new HashMap<>();
                            l.put("colaboradorId", k);
                            l.put("status", "erro");
                            String msg = ex.getMessage();
                            Throwable root = ex;
                            while (root.getCause() != null) root = root.getCause();
                            if (root != null && root != ex && root.getMessage() != null) msg = root.getMessage();
                            l.put("mensagem", msg != null ? msg : "Falha ao atribuir");
                            l.put("exception", ex.getClass().getSimpleName());
                            logs.add(l);
                        }
                    }
                }
                batchCount++;
            }
            long saveEnd = System.nanoTime();
            long tEnd = System.nanoTime();
            resp.put("success", true);
            resp.put("processados", ok);
            resp.put("total", candidatos.size());
            resp.put("logs", logs);
            resp.put("batchSize", batchSize);
            resp.put("batchCount", batchCount);
            resp.put("saveAllMs", (saveEnd - saveStart) / 1_000_000);
            resp.put("processMs", (tEnd - tStart) / 1_000_000);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao gerar automaticamente");
            resp.put("error", String.valueOf(ex.getMessage()));
            return ResponseEntity.ok(resp);
        }
    }

    @GetMapping("/nova")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String novaEscala(Model model) {
        model.addAttribute("mesAtual", java.time.LocalDate.now().getMonthValue());
        model.addAttribute("anoAtual", java.time.LocalDate.now().getYear());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        return "rh/ponto-escalas/nova";
    }

    @GetMapping("/editar")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String editarEscalaPage(@RequestParam Long id, Model model) {
        java.time.YearMonth ym = java.time.YearMonth.now();
        model.addAttribute("mesAtual", ym.getMonthValue());
        model.addAttribute("anoAtual", ym.getYear());
        model.addAttribute("escalaId", id);
        model.addAttribute("departamentos", departamentoService.listarTodos());
        return "rh/ponto-escalas/editar";
    }

    @PostMapping("/api/escalas/encerrar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo", "escalaAlertas"}, allEntries = true)
    public Map<String, Object> encerrarEscala(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long id = Long.valueOf(String.valueOf(payload.get("id")));
            EscalaTrabalho e = escalaTrabalhoRepository.findById(id).orElse(null);
            if (e == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            Object dvf = payload.get("dataVigenciaFim");
            LocalDate fim = dvf != null && !String.valueOf(dvf).isBlank() ? LocalDate.parse(String.valueOf(dvf)) : LocalDate.now();
            e.setDataVigenciaFim(fim);
            escalaTrabalhoRepository.save(e);
            resp.put("success", true);
            return resp;
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao encerrar vigência");
            return resp;
        }
    }

    @PostMapping("/api/escalas/clonar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo", "escalaAlertas"}, allEntries = true)
    public Map<String, Object> clonarEscala(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long id = Long.valueOf(String.valueOf(payload.get("id")));
            EscalaTrabalho o = escalaTrabalhoRepository.findById(id).orElse(null);
            if (o == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            EscalaTrabalho e = new EscalaTrabalho();
            e.setNome(o.getNome() + " (cópia)");
            e.setDescricao(o.getDescricao());
            e.setTipo(o.getTipo());
            e.setHorarioEntrada1(o.getHorarioEntrada1());
            e.setHorarioSaida1(o.getHorarioSaida1());
            e.setHorarioEntrada2(o.getHorarioEntrada2());
            e.setHorarioSaida2(o.getHorarioSaida2());
            e.setIntervaloMinimo(o.getIntervaloMinimo());
            e.setTrabalhaSegunda(o.getTrabalhaSegunda());
            e.setTrabalhaTerca(o.getTrabalhaTerca());
            e.setTrabalhaQuarta(o.getTrabalhaQuarta());
            e.setTrabalhaQuinta(o.getTrabalhaQuinta());
            e.setTrabalhaSexta(o.getTrabalhaSexta());
            e.setTrabalhaSabado(o.getTrabalhaSabado());
            e.setTrabalhaDomingo(o.getTrabalhaDomingo());
            e.setToleranciaAtraso(o.getToleranciaAtraso());
            e.setMinutosTolerancia(o.getMinutosTolerancia());
            e.setAtivo(true);
            e.setDataVigenciaInicio(LocalDate.now());
            e.setDataVigenciaFim(null);
            escalaTrabalhoRepository.save(e);
            resp.put("success", true);
            return resp;
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao clonar escala");
            return resp;
        }
    }

    @GetMapping("/api/escalas/detalhar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> detalharEscala(@RequestParam Long id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            EscalaTrabalho e = escalaTrabalhoRepository.findById(id).orElse(null);
            if (e == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            Map<String, Object> d = new HashMap<>();
            d.put("id", e.getId());
            d.put("nome", e.getNome());
            d.put("tipo", e.getTipo() != null ? e.getTipo().name() : null);
            d.put("horarioEntrada1", formatHora(e.getHorarioEntrada1()));
            d.put("horarioSaida1", formatHora(e.getHorarioSaida1()));
            d.put("horarioEntrada2", formatHora(e.getHorarioEntrada2()));
            d.put("horarioSaida2", formatHora(e.getHorarioSaida2()));
            d.put("intervaloMinimo", e.getIntervaloMinimo());
            d.put("dataVigenciaInicio", e.getDataVigenciaInicio() != null ? e.getDataVigenciaInicio().toString() : null);
            d.put("dataVigenciaFim", e.getDataVigenciaFim() != null ? e.getDataVigenciaFim().toString() : null);
            d.put("toleranciaAtraso", e.getToleranciaAtraso());
            d.put("minutosTolerancia", e.getMinutosTolerancia());
            d.put("trabalhaSegunda", e.getTrabalhaSegunda());
            d.put("trabalhaTerca", e.getTrabalhaTerca());
            d.put("trabalhaQuarta", e.getTrabalhaQuarta());
            d.put("trabalhaQuinta", e.getTrabalhaQuinta());
            d.put("trabalhaSexta", e.getTrabalhaSexta());
            d.put("trabalhaSabado", e.getTrabalhaSabado());
            d.put("trabalhaDomingo", e.getTrabalhaDomingo());
            resp.put("success", true);
            resp.put("escala", d);
            return resp;
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao detalhar escala");
            return resp;
        }
    }

    private java.time.LocalTime parseHoraFlex(String s) {
        if (s == null) return null;
        String v = String.valueOf(s).trim();
        if (v.isBlank() || "null".equalsIgnoreCase(v)) return null;
        try {
            return java.time.LocalTime.parse(v);
        } catch (Exception ignore) {
        }
        try {
            String[] parts = v.split(":");
            if (parts.length >= 2) {
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h >= 0 && h <= 23 && m >= 0 && m <= 59) {
                    return java.time.LocalTime.of(h, m);
                }
            }
        } catch (Exception ignore) {
        }
        throw new IllegalArgumentException("Horário inválido: " + v);
    }

    private String formatHora(Object t) {
        if (t == null) return null;
        if (t instanceof java.time.LocalTime) {
            return ((java.time.LocalTime) t).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }
        String s = String.valueOf(t);
        try {
            return java.time.LocalTime.parse(s).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ignore) {
        }
        try {
            String[] parts = s.split(":");
            if (parts.length >= 2) {
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h >= 0 && h <= 23 && m >= 0 && m <= 59) {
                    return String.format("%02d:%02d", h, m);
                }
            }
        } catch (Exception ignore) {
        }
        return s;
    }

    @PostMapping("/api/escalas/excluir")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo"}, allEntries = true)
    public Map<String, Object> excluirEscala(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long id = Long.valueOf(String.valueOf(payload.get("id")));
            EscalaTrabalho e = escalaTrabalhoRepository.findById(id).orElse(null);
            if (e == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            long refs = colaboradorEscalaRepository.countByEscalaTrabalho_Id(id);
            if (refs > 0) {
                resp.put("success", false);
                resp.put("message", "Não é possível excluir: escala referenciada em atribuições de colaboradores");
                return resp;
            }
            escalaTrabalhoRepository.deleteById(id);
            resp.put("success", true);
            return resp;
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao excluir escala");
            return resp;
        }
    }

    @GetMapping("/api/calendario")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.Cacheable(
        value = "escalaCalendario",
        key = "#mes + '_' + #ano + '_' + (#departamentoId == null ? 'all' : #departamentoId) + '_' + (#escalaId == null ? 'all' : #escalaId)"
    )
    public Map<String, Object> calendario(@RequestParam Integer mes,
                                          @RequestParam Integer ano,
                                          @RequestParam(required = false) Long departamentoId,
                                          @RequestParam(required = false) Long escalaId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            YearMonth ym = YearMonth.of(ano, mes);
            LocalDate inicio = ym.atDay(1);
            LocalDate fim = ym.atEndOfMonth();

            List<Object[]> atribs = colaboradorEscalaRepository.listarAtribuicoesNoPeriodo(inicio, fim, departamentoId, escalaId);
            int diasMes = ym.lengthOfMonth();
            int[] counts = new int[diasMes + 1];

            for (Object[] arr : atribs) {
                EscalaTrabalho.TipoEscala tipo = (EscalaTrabalho.TipoEscala) arr[6];
                Boolean seg = (Boolean) arr[7];
                Boolean ter = (Boolean) arr[8];
                Boolean qua = (Boolean) arr[9];
                Boolean qui = (Boolean) arr[10];
                Boolean sex = (Boolean) arr[11];
                Boolean sab = (Boolean) arr[12];
                Boolean dom = (Boolean) arr[13];
                LocalDate atribInicio = (LocalDate) arr[15];
                LocalDate atribFim = (LocalDate) arr[16];
                LocalDate vigEscInicio = (LocalDate) arr[17];
                LocalDate vigEscFim = (LocalDate) arr[18];

                LocalDate startBound = inicio;
                if (atribInicio != null && atribInicio.isAfter(startBound)) startBound = atribInicio;
                if (vigEscInicio != null && vigEscInicio.isAfter(startBound)) startBound = vigEscInicio;
                LocalDate endBound = fim;
                if (atribFim != null && atribFim.isBefore(endBound)) endBound = atribFim;
                if (vigEscFim != null && vigEscFim.isBefore(endBound)) endBound = vigEscFim;
                if (startBound.isAfter(endBound)) continue;

                if (tipo == EscalaTrabalho.TipoEscala.TURNO_12X36) {
                    LocalDate base = atribInicio != null ? atribInicio : startBound;
                    LocalDate cursor = startBound;
                    long diff = java.time.temporal.ChronoUnit.DAYS.between(base, cursor);
                    if (diff % 2 != 0) cursor = cursor.plusDays(1);
                    while (!cursor.isAfter(endBound)) {
                        int idx = cursor.getDayOfMonth();
                        counts[idx]++;
                        cursor = cursor.plusDays(2);
                    }
                } else if (tipo == EscalaTrabalho.TipoEscala.TURNO_6X1) {
                    LocalDate base = atribInicio != null ? atribInicio : startBound;
                    LocalDate cursor = startBound;
                    while (!cursor.isAfter(endBound)) {
                        long d = java.time.temporal.ChronoUnit.DAYS.between(base, cursor);
                        if ((d % 7) < 6) {
                            int idx = cursor.getDayOfMonth();
                            counts[idx]++;
                        }
                        cursor = cursor.plusDays(1);
                    }
                } else {
                    if (Boolean.TRUE.equals(seg)) incrementarDias(counts, startBound, endBound, java.time.DayOfWeek.MONDAY);
                    if (Boolean.TRUE.equals(ter)) incrementarDias(counts, startBound, endBound, java.time.DayOfWeek.TUESDAY);
                    if (Boolean.TRUE.equals(qua)) incrementarDias(counts, startBound, endBound, java.time.DayOfWeek.WEDNESDAY);
                    if (Boolean.TRUE.equals(qui)) incrementarDias(counts, startBound, endBound, java.time.DayOfWeek.THURSDAY);
                    if (Boolean.TRUE.equals(sex)) incrementarDias(counts, startBound, endBound, java.time.DayOfWeek.FRIDAY);
                    if (Boolean.TRUE.equals(sab)) incrementarDias(counts, startBound, endBound, java.time.DayOfWeek.SATURDAY);
                    if (Boolean.TRUE.equals(dom)) incrementarDias(counts, startBound, endBound, java.time.DayOfWeek.SUNDAY);
                }
            }

            List<Map<String, Object>> days = new ArrayList<>();
            for (int d = 1; d <= diasMes; d++) {
                Map<String, Object> m = new HashMap<>();
                m.put("day", d);
                m.put("total", counts[d]);
                days.add(m);
            }
            resp.put("success", true);
            resp.put("mes", mes);
            resp.put("ano", ano);
            resp.put("days", days);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao carregar calendário");
            return resp;
        }
    }

    @GetMapping("/correcoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaCorrecoes(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/correcoes";
    }

    @PostMapping("/api/teste/criar-usuario")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> criarUsuarioTeste() {
        Map<String, Object> resp = new HashMap<>();
        try {
            String email = "teste.ponto@sistema.com";
            Optional<Colaborador> existenteCol = colaboradorService.listarAtivos().stream()
                    .filter(c -> email.equalsIgnoreCase(c.getEmail()))
                    .findFirst();
            if (existenteCol.isPresent()) {
                Colaborador col = existenteCol.get();
                Usuario u = col.getUsuario();
                resp.put("success", true);
                resp.put("message", "Usuário de teste já existe");
                resp.put("matricula", u != null ? u.getMatricula() : null);
                resp.put("colaboradorId", col.getId());
                return resp;
            }

            Colaborador col = new Colaborador();
            col.setNome("Teste Ponto");
            col.setEmail(email);
            col.setCpf("999.999.990-01");
            col.setTelefone("(11) 99999-9999");
            col.setDataAdmissao(LocalDate.now().minusDays(1));
            col.setStatus(Colaborador.StatusColaborador.ATIVO);
            col.setAtivo(true);
            col = colaboradorService.salvar(col);

            Usuario usuario = new Usuario();
            usuario.setNome(col.getNome());
            usuario.setEmail(email);
            usuario.setSenha("teste123");
            usuario.setMatricula(usuarioService.gerarMatriculaUnica());
            usuario.setNivelAcesso(NivelAcesso.USER);
            usuario.setStatus(Status.ATIVO);
            usuario.setColaborador(col);
            usuarioService.salvarUsuario(usuario);

            col.setUsuario(usuario);
            colaboradorService.salvar(col);

            resp.put("success", true);
            resp.put("message", "Usuário e colaborador de teste criados");
            resp.put("matricula", usuario.getMatricula());
            resp.put("colaboradorId", col.getId());
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao criar usuário de teste");
            return resp;
        }
    }

    @GetMapping("/relatorios")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaRelatorios() {
        return "rh/ponto-escalas/relatorios";
    }

    @PostMapping("/correcoes/solicitar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> solicitarCorrecao(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        RhParametroPonto config = pontoRepository.findAll().stream().findFirst().orElse(null);
        if (config != null && Boolean.FALSE.equals(config.getPermitirCorrecaoManual())) {
            resp.put("success", false);
            resp.put("message", "Correção manual desabilitada pela configuração de ponto");
            return resp;
        }

        boolean exigeAprovacao = config == null || Boolean.TRUE.equals(config.getExigeAprovacaoGerente());
        resp.put("success", true);
        resp.put("message", exigeAprovacao ? "Solicitação registrada e enviada para aprovação" : "Correção registrada automaticamente");
        return resp;
    }

    @GetMapping("/api/alertas")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(
        value = "escalaAlertas",
        key = "#mes + '_' + #ano + '_' + (#departamentoId == null ? 'all' : #departamentoId) + '_' + (#escalaId == null ? 'all' : #escalaId) + '_' + #minimoPorTurno + '_' + #page + '_' + #size"
    )
    public Map<String, Object> alertas(@RequestParam Integer mes,
                                       @RequestParam Integer ano,
                                       @RequestParam(required = false) Long departamentoId,
                                       @RequestParam(required = false) Long escalaId,
                                       @RequestParam(required = false, defaultValue = "5") Integer minimoPorTurno,
                                       @RequestParam(required = false, defaultValue = "0") Integer page,
                                       @RequestParam(required = false, defaultValue = "10") Integer size) {
        Map<String, Object> resp = new HashMap<>();
        try {
            YearMonth ym = YearMonth.of(ano, mes);
            LocalDate inicio = ym.atDay(1);
            LocalDate fim = ym.atEndOfMonth();

            List<Object[]> atribs = colaboradorEscalaRepository.listarAtribuicoesNoPeriodo(inicio, fim, departamentoId, escalaId);

            Map<LocalDate, List<Map<String, Object>>> porDiaEscalados = new HashMap<>();
            Map<String, Integer> contagemPorColaboradorDia = new HashMap<>(); // key: dia|colabId
            List<Map<String, Object>> itens = new ArrayList<>();

            for (Object[] arr : atribs) {
                Long colabId = (Long) arr[1];
                String colabNome = (String) arr[2];
                Long escalaIdRow = (Long) arr[4];
                String escalaNome = (String) arr[5];
                EscalaTrabalho.TipoEscala tipo = (EscalaTrabalho.TipoEscala) arr[6];
                Boolean seg = (Boolean) arr[7];
                Boolean ter = (Boolean) arr[8];
                Boolean qua = (Boolean) arr[9];
                Boolean qui = (Boolean) arr[10];
                Boolean sex = (Boolean) arr[11];
                Boolean sab = (Boolean) arr[12];
                Boolean dom = (Boolean) arr[13];
                Integer cargaDiariaMin = (Integer) arr[14];
                LocalDate atribInicio = (LocalDate) arr[15];
                LocalDate atribFim = (LocalDate) arr[16];
                LocalDate vigEscInicio = (LocalDate) arr[17];
                LocalDate vigEscFim = (LocalDate) arr[18];

                LocalDate startBound = inicio;
                if (atribInicio != null && atribInicio.isAfter(startBound)) startBound = atribInicio;
                if (vigEscInicio != null && vigEscInicio.isAfter(startBound)) startBound = vigEscInicio;
                LocalDate endBound = fim;
                if (atribFim != null && atribFim.isBefore(endBound)) endBound = atribFim;
                if (vigEscFim != null && vigEscFim.isBefore(endBound)) endBound = vigEscFim;
                if (startBound.isAfter(endBound)) continue;

                LocalDate cursor = startBound;
                while (!cursor.isAfter(endBound)) {
                    java.time.DayOfWeek dow = cursor.getDayOfWeek();
                    boolean trabalhaDia = (dow == java.time.DayOfWeek.MONDAY && Boolean.TRUE.equals(seg)) ||
                                          (dow == java.time.DayOfWeek.TUESDAY && Boolean.TRUE.equals(ter)) ||
                                          (dow == java.time.DayOfWeek.WEDNESDAY && Boolean.TRUE.equals(qua)) ||
                                          (dow == java.time.DayOfWeek.THURSDAY && Boolean.TRUE.equals(qui)) ||
                                          (dow == java.time.DayOfWeek.FRIDAY && Boolean.TRUE.equals(sex)) ||
                                          (dow == java.time.DayOfWeek.SATURDAY && Boolean.TRUE.equals(sab)) ||
                                          (dow == java.time.DayOfWeek.SUNDAY && Boolean.TRUE.equals(dom));
                    if (!trabalhaDia) {
                        cursor = cursor.plusDays(1);
                        continue;
                    }
                    porDiaEscalados.computeIfAbsent(cursor, k -> new ArrayList<>()).add(Map.of(
                            "colaboradorId", colabId,
                            "colaboradorNome", colabNome,
                            "escalaId", escalaIdRow,
                            "escalaNome", escalaNome,
                            "cargaMinutos", cargaDiariaMin != null ? cargaDiariaMin : 0
                    ));
                    String key = cursor.toString() + "|" + colabId;
                    contagemPorColaboradorDia.put(key, contagemPorColaboradorDia.getOrDefault(key, 0) + 1);
                    cursor = cursor.plusDays(1);
                }
            }

            for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : porDiaEscalados.entrySet()) {
                LocalDate dia = entry.getKey();
                List<Map<String, Object>> escalados = entry.getValue();

                int total = escalados.size();
                if (total < minimoPorTurno) {
                    Map<String, Object> alerta = new HashMap<>();
                    alerta.put("tipo", "Cobertura");
                    alerta.put("descricao", "Cobertura insuficiente: " + total + " escalados (mínimo: " + minimoPorTurno + ")");
                    alerta.put("colaborador", "-");
                    alerta.put("data", dia.toString());
                    alerta.put("prioridade", "Alta");
                    alerta.put("escala", escalados.isEmpty() ? null : escalados.get(0).get("escalaNome"));
                    alerta.put("escalaId", escalados.isEmpty() ? null : escalados.get(0).get("escalaId"));
                    itens.add(alerta);
                }

                java.util.List<Long> idsDia = escalados.stream()
                        .map(m -> (Long) m.get("colaboradorId"))
                        .distinct()
                        .toList();
                java.util.Set<Long> feriasDia = new java.util.HashSet<>(feriasRepository.colaboradoresEmFeriasNoDia(dia, idsDia));
                for (Map<String, Object> esc : escalados) {
                    Long colabId = (Long) esc.get("colaboradorId");
                    String key = dia.toString() + "|" + colabId;
                    Integer count = contagemPorColaboradorDia.getOrDefault(key, 0);
                    if (count != null && count > 1) {
                        Map<String, Object> confl = new HashMap<>();
                        confl.put("tipo", "Conflito");
                        confl.put("descricao", "Colaborador escalado em múltiplos turnos no mesmo dia (" + count + ")");
                        confl.put("colaborador", esc.get("colaboradorNome"));
                        confl.put("colaboradorId", esc.get("colaboradorId"));
                        confl.put("data", dia.toString());
                        confl.put("prioridade", "Crítica");
                        itens.add(confl);
                        contagemPorColaboradorDia.put(key, 1);
                    }
                    if (feriasDia.contains(colabId)) {
                        Map<String, Object> aus = new HashMap<>();
                        aus.put("tipo", "Ausência");
                        aus.put("descricao", "Colaborador em férias - substituição necessária");
                        aus.put("colaborador", esc.get("colaboradorNome"));
                        aus.put("colaboradorId", esc.get("colaboradorId"));
                        aus.put("escalaId", esc.get("escalaId"));
                        aus.put("data", dia.toString());
                        aus.put("prioridade", "Média");
                        itens.add(aus);
                    }
                }
            }

            int total = itens.size();
            int from = Math.max(0, page * size);
            int to = Math.min(total, from + size);
            List<Map<String, Object>> content = from >= to ? java.util.Collections.emptyList() : itens.subList(from, to);
            int totalPages = (int) Math.ceil(total / (double) size);
            boolean hasPrevious = page > 0 && totalPages > 0;
            boolean hasNext = page + 1 < totalPages;
            resp.put("success", true);
            resp.put("content", content);
            resp.put("currentPage", page);
            resp.put("totalPages", totalPages);
            resp.put("totalElements", total);
            resp.put("hasPrevious", hasPrevious);
            resp.put("hasNext", hasNext);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao gerar alertas de escalas");
            return resp;
        }
    }

    @PostMapping("/api/alertas/atribuir-cobertura")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo", "escalaAlertas"}, allEntries = true)
    public Map<String, Object> atribuirCobertura(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuarioLogado = null;
            if (principal != null && principal.getName() != null) {
                usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            }
            Long colaboradorId = Long.valueOf(String.valueOf(payload.get("colaboradorId")));
            Long escalaId = Long.valueOf(String.valueOf(payload.get("escalaId")));
            LocalDate dia = LocalDate.parse(String.valueOf(payload.get("dia")));
            EscalaTrabalho escala = escalaTrabalhoRepository.findById(escalaId).orElse(null);
            if (escala == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            java.util.List<ColaboradorEscala> vigColabDia = colaboradorEscalaRepository.findVigenteByColaboradorAndData(colaboradorId, dia);
            boolean mesmaEscala = vigColabDia.stream().anyMatch(ce -> ce.getEscalaTrabalho() != null && escalaId.equals(ce.getEscalaTrabalho().getId()));
            if (!mesmaEscala) {
                resp.put("success", false);
                resp.put("message", "Só é permitido atribuir cobertura com colaborador da mesma escala de trabalho");
                return resp;
            }
            List<ColaboradorEscala> vigentes = colaboradorEscalaRepository.findVigenteByColaboradorAndData(colaboradorId, dia);
            for (ColaboradorEscala ce : vigentes) {
                LocalDate fim = dia.minusDays(1);
                ce.setDataFim(fim);
                colaboradorEscalaRepository.save(ce);
            }
            ColaboradorEscala novo = new ColaboradorEscala();
            novo.setColaborador(colaboradorService.findById(colaboradorId));
            novo.setEscalaTrabalho(escala);
            novo.setDataInicio(dia);
            novo.setDataFim(dia);
            novo.setAtivo(true);
            if (usuarioLogado != null) {
                novo.setUsuarioCriacao(usuarioLogado);
            }
            colaboradorEscalaRepository.save(novo);
            resp.put("success", true);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao atribuir cobertura");
            return resp;
        }
    }

    @PostMapping("/api/alertas/resolver-conflito")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo", "escalaAlertas"}, allEntries = true)
    public Map<String, Object> resolverConflito(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long colaboradorId = Long.valueOf(String.valueOf(payload.get("colaboradorId")));
            LocalDate dia = LocalDate.parse(String.valueOf(payload.get("dia")));
            Long manterEscalaId = payload.get("manterEscalaId") != null && !String.valueOf(payload.get("manterEscalaId")).isBlank()
                    ? Long.valueOf(String.valueOf(payload.get("manterEscalaId"))) : null;
            List<ColaboradorEscala> vigentes = colaboradorEscalaRepository.findVigenteByColaboradorAndData(colaboradorId, dia);
            if (vigentes == null || vigentes.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Nenhuma atribuição encontrada para o dia");
                return resp;
            }
            for (ColaboradorEscala ce : vigentes) {
                if (manterEscalaId != null && ce.getEscalaTrabalho() != null && manterEscalaId.equals(ce.getEscalaTrabalho().getId())) {
                    continue;
                }
                LocalDate fim = dia.minusDays(1);
                ce.setDataFim(fim);
                colaboradorEscalaRepository.save(ce);
            }
            resp.put("success", true);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao resolver conflito");
            return resp;
        }
    }

    @GetMapping("/api/alertas/atribuicoes-dia")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> atribuicoesDia(@RequestParam Long colaboradorId, @RequestParam String dia) {
        Map<String, Object> resp = new HashMap<>();
        try {
            LocalDate data = LocalDate.parse(dia);
            List<ColaboradorEscala> vigentes = colaboradorEscalaRepository.findVigenteByColaboradorAndData(colaboradorId, data);
            List<Map<String, Object>> content = new ArrayList<>();
            for (ColaboradorEscala ce : vigentes) {
                Map<String, Object> it = new HashMap<>();
                it.put("atribId", ce.getId());
                it.put("escalaId", ce.getEscalaTrabalho() != null ? ce.getEscalaTrabalho().getId() : null);
                it.put("escalaNome", ce.getEscalaTrabalho() != null ? ce.getEscalaTrabalho().getNome() : "-");
                content.add(it);
            }
            resp.put("success", true);
            resp.put("content", content);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao buscar atribuições");
            return resp;
        }
    }

    @GetMapping("/api/colaboradores-disponiveis")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> colaboradoresDisponiveis(@RequestParam String dia,
                                                        @RequestParam(required = false) Long departamentoId,
                                                        @RequestParam(required = false) Long escalaId,
                                                        @RequestParam(required = false, defaultValue = "false") Boolean incluirEscalados) {
        Map<String, Object> resp = new HashMap<>();
        try {
            LocalDate data = LocalDate.parse(dia);
            List<ColaboradorEscala> escalados = colaboradorEscalaRepository.findVigentesByData(data);
            java.util.Set<Long> idsEscalados = new java.util.HashSet<>();
            for (ColaboradorEscala ce : escalados) {
                if (ce.getColaborador() != null && ce.getColaborador().getId() != null) {
                    idsEscalados.add(ce.getColaborador().getId());
                }
            }
            List<Colaborador> ativos = colaboradorService.listarAtivos();
            java.util.Set<Long> idsComEscala = new java.util.HashSet<>();
            if (escalaId != null) {
                idsComEscala.addAll(colaboradorEscalaRepository.findColaboradorIdsComEscalaNoDia(data, escalaId));
            }
            List<Map<String, Object>> disponiveis = new ArrayList<>();
            for (Colaborador c : ativos) {
                if (!Boolean.TRUE.equals(incluirEscalados) && idsEscalados.contains(c.getId())) continue;
                if (departamentoId != null) {
                    if (c.getDepartamento() == null || c.getDepartamento().getId() == null || !departamentoId.equals(c.getDepartamento().getId())) {
                        continue;
                    }
                }
                if (escalaId != null && !idsComEscala.contains(c.getId())) continue;
                Map<String, Object> it = new HashMap<>();
                it.put("id", c.getId());
                it.put("nome", c.getNome());
                it.put("escaladoNoDia", idsEscalados.contains(c.getId()));
                disponiveis.add(it);
            }
            resp.put("success", true);
            resp.put("content", disponiveis);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao listar colaboradores disponíveis");
            return resp;
        }
    }

    @PostMapping("/api/alertas/substituir-ferias")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.cache.annotation.CacheEvict(value = {"escalaCalendario", "escalaResumo", "escalaAlertas"}, allEntries = true)
    public Map<String, Object> substituirFerias(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuarioLogado = null;
            if (principal != null && principal.getName() != null) {
                usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            }
            Long colaboradorAusenteId = Long.valueOf(String.valueOf(payload.get("colaboradorAusenteId")));
            Long substitutoId = Long.valueOf(String.valueOf(payload.get("substitutoId")));
            LocalDate dia = LocalDate.parse(String.valueOf(payload.get("dia")));
            if (substitutoId != null && colaboradorAusenteId != null && substitutoId.equals(colaboradorAusenteId)) {
                resp.put("success", false);
                resp.put("message", "Colaborador não pode ser substituído por ele mesmo");
                return resp;
            }
            List<ColaboradorEscala> vigentesAusente = colaboradorEscalaRepository.findVigenteByColaboradorAndData(colaboradorAusenteId, dia);
            if (vigentesAusente == null || vigentesAusente.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Colaborador ausente sem escala no dia informado");
                return resp;
            }
            ColaboradorEscala ref = vigentesAusente.get(0);
            List<ColaboradorEscala> vigSubDia = colaboradorEscalaRepository.findVigenteByColaboradorAndData(substitutoId, dia);
            boolean mesmaEscala = vigSubDia.stream().anyMatch(ce -> ce.getEscalaTrabalho() != null && ref.getEscalaTrabalho() != null && ref.getEscalaTrabalho().getId().equals(ce.getEscalaTrabalho().getId()));
            if (!mesmaEscala) {
                resp.put("success", false);
                resp.put("message", "Substituição permitida apenas entre colaboradores da mesma escala de trabalho");
                return resp;
            }
            for (ColaboradorEscala ce : vigentesAusente) {
                LocalDate fim = dia.minusDays(1);
                ce.setDataFim(fim);
                colaboradorEscalaRepository.save(ce);
            }
            List<ColaboradorEscala> vigentesSub = colaboradorEscalaRepository.findVigenteByColaboradorAndData(substitutoId, dia);
            for (ColaboradorEscala ce : vigentesSub) {
                LocalDate fim = dia.minusDays(1);
                ce.setDataFim(fim);
                colaboradorEscalaRepository.save(ce);
            }
            ColaboradorEscala novo = new ColaboradorEscala();
            novo.setColaborador(colaboradorService.findById(substitutoId));
            novo.setEscalaTrabalho(ref.getEscalaTrabalho());
            novo.setDataInicio(dia);
            novo.setDataFim(dia);
            novo.setAtivo(true);
            if (usuarioLogado != null) {
                novo.setUsuarioCriacao(usuarioLogado);
            }
            colaboradorEscalaRepository.save(novo);
            resp.put("success", true);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao substituir colaborador em férias");
            return resp;
        }
    }

    @PostMapping("/registros/validar-matricula")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> validarMatricula(@RequestParam String matricula) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Optional<Colaborador> colOpt = colaboradorService.buscarPorMatriculaUsuario(matricula);
            if (colOpt.isEmpty()) {
                resp.put("success", true);
                resp.put("valida", false);
                resp.put("message", "Matrícula não encontrada");
                return resp;
            }
            Colaborador col = colOpt.get();
            boolean ativo = Boolean.TRUE.equals(col.getAtivo()) && col.getStatus() == Colaborador.StatusColaborador.ATIVO;
            if (!ativo) {
                resp.put("success", true);
                resp.put("valida", false);
                resp.put("message", "Colaborador inativo");
                return resp;
            }
            LocalDate hoje = LocalDate.now();
            Optional<RegistroPonto> registroOpt = registroPontoRepository.findByColaboradorAndData(col, hoje);
            String proximoTipo = calcularProximoTipo(registroOpt.orElse(null));
            resp.put("success", true);
            resp.put("valida", true);
            resp.put("colaborador", col.getNome());
            resp.put("proximoTipo", proximoTipo);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", "Erro interno");
            return resp;
        }
    }

    @PostMapping("/registros/registrar-por-matricula")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> registrarPorMatricula(@RequestParam String matricula,
                                                     @RequestParam(required = false) String senha) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Optional<Colaborador> colOpt = colaboradorService.buscarPorMatriculaUsuario(matricula);
            if (colOpt.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Matrícula não encontrada");
                return resp;
            }
            Colaborador col = colOpt.get();
            boolean ativo = Boolean.TRUE.equals(col.getAtivo()) && col.getStatus() == Colaborador.StatusColaborador.ATIVO;
            if (!ativo) {
                resp.put("success", false);
                resp.put("message", "Colaborador inativo");
                return resp;
            }

            LocalDate hoje = LocalDate.now();
            LocalTime agora = LocalTime.now();
            RegistroPonto registro = registroPontoRepository.findByColaboradorAndData(col, hoje)
                    .orElseGet(() -> {
                        RegistroPonto r = new RegistroPonto();
                        r.setColaborador(col);
                        r.setData(hoje);
                        r.setDataCriacao(LocalDateTime.now());
                        return r;
                    });

            Usuario usuarioCriacao = usuarioService.buscarPorMatricula(matricula).orElse(null);
            registro.setUsuarioCriacao(usuarioCriacao);
            registro.setTipoRegistro(RegistroPonto.TipoRegistro.AUTOMATICO);

            List<ColaboradorEscala> escalasVigentes = colaboradorEscalaRepository
                    .findVigenteByColaboradorAndData(col.getId(), hoje);
            EscalaTrabalho escala = escalasVigentes != null && !escalasVigentes.isEmpty()
                    ? escalasVigentes.get(0).getEscalaTrabalho()
                    : null;

            if (escala != null) {
                int minutosPrevistos = escala.getCargaHorariaDiaria() != null
                        ? Math.max(0, escala.getCargaHorariaDiaria())
                        : 0;
                if (minutosPrevistos == 0 && escala.getHorarioEntrada1() != null && escala.getHorarioSaida1() != null) {
                    minutosPrevistos += (int) java.time.Duration.between(escala.getHorarioEntrada1(), escala.getHorarioSaida1()).toMinutes();
                }
                if (escala.getHorarioEntrada2() != null && escala.getHorarioSaida2() != null) {
                    minutosPrevistos += (int) java.time.Duration.between(escala.getHorarioEntrada2(), escala.getHorarioSaida2()).toMinutes();
                }

                registro.setMinutosJornadaPrevista(minutosPrevistos > 0 ? minutosPrevistos : null);
                registro.setHorarioPrevistoEntrada1(escala.getHorarioEntrada1());
                registro.setToleranciaAtrasoAtiva(escala.getToleranciaAtraso());
                registro.setMinutosToleranciaAtraso(escala.getMinutosTolerancia());
            } else {
                registro.setMinutosJornadaPrevista(null);
                registro.setHorarioPrevistoEntrada1(null);
                registro.setToleranciaAtrasoAtiva(null);
                registro.setMinutosToleranciaAtraso(null);
            }

            String proximoTipo = calcularProximoTipo(registro);
            if (proximoTipo == null) {
                resp.put("success", false);
                resp.put("message", "Todos os registros de hoje já foram efetuados");
                return resp;
            }

            switch (proximoTipo) {
                case "Entrada" -> registro.setEntrada1(agora);
                case "Saída" -> {
                    if (registro.getEntrada1() != null && registro.getSaida1() == null) {
                        registro.setSaida1(agora);
                    } else if (registro.getEntrada2() != null && registro.getSaida2() == null) {
                        registro.setSaida2(agora);
                    } else {
                        registro.setSaida1(agora);
                    }
                }
                case "Retorno" -> registro.setEntrada2(agora);
            }

            registro.setDataUltimaEdicao(LocalDateTime.now());
            registroPontoRepository.save(registro);

            resp.put("success", true);
            resp.put("horario", agora.toString());
            resp.put("message", "Registro de " + proximoTipo + " efetuado com sucesso");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao registrar ponto");
            return resp;
        }
    }

    @GetMapping("/registros/ultimos")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> ultimosRegistros() {
        Map<String, Object> resp = new HashMap<>();
        try {
            LocalDate hoje = LocalDate.now();
            List<RegistroPonto> registros = registroPontoRepository.findByData(hoje);
            List<Map<String, Object>> lista = new ArrayList<>();

            for (RegistroPonto r : registros.stream().limit(20).collect(Collectors.toList())) {
                List<Map<String, Object>> eventos = new ArrayList<>();
                if (r.getEntrada1() != null) eventos.add(evento("Entrada", r.getEntrada1(), r));
                if (r.getSaida1() != null) eventos.add(evento("Saída", r.getSaida1(), r));
                if (r.getEntrada2() != null) eventos.add(evento("Retorno", r.getEntrada2(), r));
                if (r.getSaida2() != null) eventos.add(evento("Saída", r.getSaida2(), r));
                for (Map<String, Object> ev : eventos) {
                    lista.add(ev);
                }
            }

            // Ordenar por horário desc
            lista.sort((a, b) -> ((String) b.get("horarioFormatado")).compareTo((String) a.get("horarioFormatado")));

            resp.put("success", true);
            resp.put("registros", lista);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao carregar registros");
            return resp;
        }
    }

    @GetMapping("/relatorio-mensal/pdf")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public ResponseEntity<byte[]> gerarRelatorioMensalPdf(@RequestParam String matricula,
                                                          @RequestParam Integer mes,
                                                          @RequestParam Integer ano) {
        Optional<Colaborador> colOpt = colaboradorService.buscarPorMatriculaUsuario(matricula);
        if (colOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Colaborador não encontrado".getBytes());
        }
        Colaborador col = colOpt.get();
        YearMonth ym = YearMonth.of(ano, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();
        List<RegistroPonto> registros = registroPontoRepository
                .findByColaboradorAndDataBetweenOrderByDataDesc(col, inicio, fim);

        Map<LocalDate, RegistroPonto> porDia = new HashMap<>();
        for (RegistroPonto r : registros) porDia.put(r.getData(), r);

        Context ctx = new Context();
        ctx.setVariable("colaborador", col);
        ctx.setVariable("mes", mes);
        ctx.setVariable("ano", ano);
        ctx.setVariable("dias", ym.lengthOfMonth());
        ctx.setVariable("porDia", porDia);

        String html = templateEngine.process("rh/ponto-escalas/espelho-ponto-pdf", ctx);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=espelho_ponto_" + matricula + "_" + String.format("%02d", mes) + "_" + ano + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao gerar PDF".getBytes());
        }
    }

    @GetMapping("/api/colaboradores-dia")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> colaboradoresPorDia(@RequestParam String dia,
                                                   @RequestParam(required = false) Long departamentoId,
                                                   @RequestParam(required = false) Long escalaId,
                                                   @RequestParam(name = "q", required = false) String q,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "20") int size) {
        Map<String, Object> resp = new HashMap<>();
        try {
            LocalDate data = LocalDate.parse(dia);
            java.time.DayOfWeek dow = data.getDayOfWeek();
            boolean seg = dow == java.time.DayOfWeek.MONDAY;
            boolean ter = dow == java.time.DayOfWeek.TUESDAY;
            boolean qua = dow == java.time.DayOfWeek.WEDNESDAY;
            boolean qui = dow == java.time.DayOfWeek.THURSDAY;
            boolean sex = dow == java.time.DayOfWeek.FRIDAY;
            boolean sab = dow == java.time.DayOfWeek.SATURDAY;
            boolean dom = dow == java.time.DayOfWeek.SUNDAY;

            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100), org.springframework.data.domain.Sort.by("c.nome").ascending()
            );

            org.springframework.data.domain.Page<Object[]> pagina = colaboradorEscalaRepository.listarParaDia(
                data,
                departamentoId,
                escalaId,
                q != null && !q.isBlank() ? q.trim() : null,
                seg, ter, qua, qui, sex, sab, dom,
                pageable
            );

            List<Object[]> registrosDia = registroPontoRepository.findCamposDia(data);
            Map<Long, Object[]> registroPorColaborador = new HashMap<>();
            for (Object[] r : registrosDia) {
                Long cid = (Long) r[0];
                if (cid != null) registroPorColaborador.put(cid, r);
            }

            List<Map<String, Object>> content = pagina.getContent().stream()
                .filter(arr -> {
                    EscalaTrabalho.TipoEscala tipo = (EscalaTrabalho.TipoEscala) arr[10];
                    LocalDate atribInicio = (LocalDate) arr[11];
                    if (tipo == EscalaTrabalho.TipoEscala.TURNO_12X36) {
                        if (atribInicio == null) return true;
                        long diff = java.time.temporal.ChronoUnit.DAYS.between(atribInicio, data);
                        return diff >= 0 && diff % 2 == 0;
                    } else if (tipo == EscalaTrabalho.TipoEscala.TURNO_6X1) {
                        if (atribInicio == null) return true;
                        long diff = java.time.temporal.ChronoUnit.DAYS.between(atribInicio, data);
                        return diff >= 0 && (diff % 7) < 6;
                    }
                    return true;
                })
                .map(arr -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", (Long) arr[0]);
                m.put("nome", (String) arr[1]);
                m.put("departamento", (String) arr[2]);
                m.put("matricula", (String) arr[3]);
                m.put("escalaId", (Long) arr[4]);
                m.put("escalaNome", (String) arr[5]);
                m.put("entrada1", arr[6] != null ? arr[6].toString() : null);
                m.put("saida1", arr[7] != null ? arr[7].toString() : null);
                m.put("entrada2", arr[8] != null ? arr[8].toString() : null);
                m.put("saida2", arr[9] != null ? arr[9].toString() : null);
                EscalaTrabalho.TipoEscala tipo = (EscalaTrabalho.TipoEscala) arr[10];
                m.put("tipoEscala", tipo != null ? tipo.name() : null);

                Object[] r = registroPorColaborador.get((Long) arr[0]);
                String status = "Previsto";
                String proximoTipo = null;
                Integer atrasoMin = null;
                Integer minutosTrabalhados = null;
                boolean isHoje = LocalDate.now().equals(data);
                java.time.LocalTime hEnt1 = (java.time.LocalTime) arr[6];

                if (r != null) {
                    Boolean falta = (Boolean) r[5];
                    java.time.LocalTime rEnt1 = (java.time.LocalTime) r[1];
                    java.time.LocalTime rSai1 = (java.time.LocalTime) r[2];
                    java.time.LocalTime rEnt2 = (java.time.LocalTime) r[3];
                    java.time.LocalTime rSai2 = (java.time.LocalTime) r[4];
                    Integer rMinTrab = (Integer) r[7];
                    if (Boolean.TRUE.equals(falta)) {
                        status = "Falta";
                    } else if (rSai2 != null || (rSai1 != null && rEnt2 == null)) {
                        status = "Concluído";
                    } else if (rEnt1 != null) {
                        status = "Em andamento";
                        proximoTipo = calcularProximoTipoTimes(rEnt1, rSai1, rEnt2, rSai2);
                    }
                    if (rEnt1 != null && hEnt1 != null) {
                        long diffMin = java.time.Duration.between(hEnt1, rEnt1).toMinutes();
                        if (diffMin > 0) {
                            atrasoMin = (int) diffMin;
                            if (!"Falta".equals(status)) status = "Atrasado";
                        }
                    }
                    minutosTrabalhados = rMinTrab;
                } else {
                    if (isHoje && hEnt1 != null) {
                        java.time.LocalTime agora = java.time.LocalTime.now();
                        if (agora.isAfter(hEnt1)) {
                            status = "Aguardando (após início)";
                        } else {
                            status = "Aguardando";
                        }
                    } else if (data.isBefore(LocalDate.now())) {
                        status = "Sem registro";
                    }
                }

                m.put("status", status);
                if (proximoTipo != null) m.put("proximaBatida", proximoTipo);
                if (atrasoMin != null) m.put("atrasoMinutos", atrasoMin);
                if (minutosTrabalhados != null) m.put("minutosTrabalhados", minutosTrabalhados);
                return m;
            }).collect(java.util.stream.Collectors.toList());

            return montarRespostaPagina(resp, pagina, content);
        
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao carregar colaboradores do dia");
            return resp;
        }
    }

    private Map<String, Object> montarRespostaPagina(Map<String, Object> resp, org.springframework.data.domain.Page<Object[]> pagina, List<Map<String, Object>> content) {
        resp.put("success", true);
        resp.put("content", content);
        resp.put("currentPage", pagina.getNumber());
        resp.put("totalPages", pagina.getTotalPages());
        resp.put("totalElements", pagina.getTotalElements());
        resp.put("hasPrevious", pagina.hasPrevious());
        resp.put("hasNext", pagina.hasNext());
        return resp;
    }

    private String calcularProximoTipoTimes(java.time.LocalTime e1, java.time.LocalTime s1, java.time.LocalTime e2, java.time.LocalTime s2) {
        if (e1 != null && s1 == null) return "Saída";
        if (e1 != null && s1 != null && e2 != null && s2 == null) return "Saída";
        if (e1 != null && s1 != null && e2 == null) return "Retorno";
        return null;
    }

    @GetMapping("/api/resumo")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(
        value = "escalaResumo",
        key = "#mes + '_' + #ano + '_' + (#departamentoId == null ? 'all' : #departamentoId) + '_' + (#escalaId == null ? 'all' : #escalaId)"
    )
    public Map<String, Object> resumoMensal(@RequestParam int mes,
                                            @RequestParam int ano,
                                            @RequestParam(required = false) Long departamentoId,
                                            @RequestParam(required = false) Long escalaId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            java.time.YearMonth ym = java.time.YearMonth.of(ano, mes);
            LocalDate inicio = ym.atDay(1);
            LocalDate fim = ym.atEndOfMonth();

            List<Object[]> atribs = colaboradorEscalaRepository.listarAtribuicoesNoPeriodo(inicio, fim, departamentoId, escalaId);
            java.util.Set<Long> colaboradoresEscalados = new java.util.HashSet<>();
            java.util.Set<Long> turnosNoMes = new java.util.HashSet<>();

            int minutosPlanejadosTotal = 0;
            int diasComCobertura = 0;
            int diasUteisNoMes = 0;

            // Precomputar dias úteis do mês
            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                java.time.DayOfWeek dow = ym.atDay(d).getDayOfWeek();
                if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY) diasUteisNoMes++;
            }

            // Mapa de dia -> houve cobertura
            boolean[] coberturaDia = new boolean[ym.lengthOfMonth()+1];

            for (Object[] arr : atribs) {
                Long colabId = (Long) arr[1];
                Long escalaIdRow = (Long) arr[4];
                Boolean seg = (Boolean) arr[7];
                Boolean ter = (Boolean) arr[8];
                Boolean qua = (Boolean) arr[9];
                Boolean qui = (Boolean) arr[10];
                Boolean sex = (Boolean) arr[11];
                Boolean sab = (Boolean) arr[12];
                Boolean dom = (Boolean) arr[13];
                Integer cargaDiariaMin = (Integer) arr[14];
                LocalDate atribInicio = (LocalDate) arr[15];
                LocalDate atribFim = (LocalDate) arr[16];
                LocalDate vigEscInicio = (LocalDate) arr[17];
                LocalDate vigEscFim = (LocalDate) arr[18];

                colaboradoresEscalados.add(colabId);
                turnosNoMes.add(escalaIdRow);

                LocalDate startBound = inicio;
                if (atribInicio != null && atribInicio.isAfter(startBound)) startBound = atribInicio;
                if (vigEscInicio != null && vigEscInicio.isAfter(startBound)) startBound = vigEscInicio;
                LocalDate endBound = fim;
                if (atribFim != null && atribFim.isBefore(endBound)) endBound = atribFim;
                if (vigEscFim != null && vigEscFim.isBefore(endBound)) endBound = vigEscFim;
                if (startBound.isAfter(endBound)) continue;

                int diasTrabalhados = contarDiasTrabalhados(startBound, endBound, seg, ter, qua, qui, sex, sab, dom);
                minutosPlanejadosTotal += (cargaDiariaMin != null ? cargaDiariaMin : 0) * diasTrabalhados;

                LocalDate cursor = startBound;
                while (!cursor.isAfter(endBound)) {
                    java.time.DayOfWeek dow = cursor.getDayOfWeek();
                    boolean trabalhaDiaUtil = (dow == java.time.DayOfWeek.MONDAY && Boolean.TRUE.equals(seg)) ||
                                              (dow == java.time.DayOfWeek.TUESDAY && Boolean.TRUE.equals(ter)) ||
                                              (dow == java.time.DayOfWeek.WEDNESDAY && Boolean.TRUE.equals(qua)) ||
                                              (dow == java.time.DayOfWeek.THURSDAY && Boolean.TRUE.equals(qui)) ||
                                              (dow == java.time.DayOfWeek.FRIDAY && Boolean.TRUE.equals(sex));
                    if (trabalhaDiaUtil) {
                        int dIndex = cursor.getDayOfMonth();
                        coberturaDia[dIndex] = true;
                    }
                    cursor = cursor.plusDays(1);
                }
            }

            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                java.time.DayOfWeek dow = ym.atDay(d).getDayOfWeek();
                if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) continue;
                if (coberturaDia[d]) diasComCobertura++;
            }

            int totalColaboradoresAtivos = colaboradorService.listarAtivos().size();

            Map<String, Object> resumo = new HashMap<>();
            resumo.put("mes", mes);
            resumo.put("ano", ano);
            resumo.put("colaboradoresAtivos", totalColaboradoresAtivos);
            resumo.put("escalados", colaboradoresEscalados.size());
            resumo.put("turnos", turnosNoMes.size());
            resumo.put("horasMesMinutos", minutosPlanejadosTotal);
            resumo.put("horasMesFormatado", String.format("%d:%02d", (minutosPlanejadosTotal/60), (minutosPlanejadosTotal%60)));
            double coberturaPercent = diasUteisNoMes == 0 ? 0.0 : (diasComCobertura * 100.0 / diasUteisNoMes);
            resumo.put("coberturaPercent", Math.round(coberturaPercent));

            resp.put("success", true);
            resp.put("resumo", resumo);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao calcular resumo do mês");
            return resp;
        }
    }

    @GetMapping("/excecoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaExcecoes(Model model) {
        model.addAttribute("colaboradoresAtivos", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/excecoes";
    }

    @PostMapping("/api/excecoes/salvar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> salvarExcecao(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long colaboradorId = Long.valueOf(String.valueOf(payload.get("colaboradorId")));
            String data = String.valueOf(payload.get("data"));
            String entrada1 = payload.get("entrada1") != null ? String.valueOf(payload.get("entrada1")) : null;
            String saida1 = payload.get("saida1") != null ? String.valueOf(payload.get("saida1")) : null;
            String entrada2 = payload.get("entrada2") != null ? String.valueOf(payload.get("entrada2")) : null;
            String saida2 = payload.get("saida2") != null ? String.valueOf(payload.get("saida2")) : null;
            String motivo = payload.get("motivo") != null ? String.valueOf(payload.get("motivo")) : "";
            if (colaboradorId == null || data == null || data.isBlank()) {
                resp.put("success", false);
                resp.put("message", "Colaborador e data são obrigatórios");
                return resp;
            }
            resp.put("success", true);
            resp.put("message", "Exceção registrada");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao salvar exceção");
            return resp;
        }
    }

    private String calcularProximoTipo(RegistroPonto r) {
        if (r == null || r.getEntrada1() == null) return "Entrada";
        if (r.getSaida1() == null) return "Saída";
        if (r.getEntrada2() == null) return "Retorno";
        if (r.getSaida2() == null) return "Saída";
        return null;
    }

    private Map<String, Object> evento(String tipo, LocalTime hora, RegistroPonto r) {
        Map<String, Object> m = new HashMap<>();
        m.put("tipo", tipo);
        m.put("horarioFormatado", String.format("%02d:%02d", hora.getHour(), hora.getMinute()));
        m.put("dataFormatada", String.format("%02d/%02d/%04d", r.getData().getDayOfMonth(), r.getData().getMonthValue(), r.getData().getYear()));
        m.put("colaboradorNome", r.getColaborador().getNome());
        return m;
    }

    private int contarDiasTrabalhados(LocalDate inicio, LocalDate fim,
                                      Boolean seg, Boolean ter, Boolean qua, Boolean qui, Boolean sex, Boolean sab, Boolean dom) {
        int count = 0;
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fim)) {
            java.time.DayOfWeek dow = cursor.getDayOfWeek();
            boolean trabalhaDia = (dow == java.time.DayOfWeek.MONDAY && Boolean.TRUE.equals(seg)) ||
                                  (dow == java.time.DayOfWeek.TUESDAY && Boolean.TRUE.equals(ter)) ||
                                  (dow == java.time.DayOfWeek.WEDNESDAY && Boolean.TRUE.equals(qua)) ||
                                  (dow == java.time.DayOfWeek.THURSDAY && Boolean.TRUE.equals(qui)) ||
                                  (dow == java.time.DayOfWeek.FRIDAY && Boolean.TRUE.equals(sex)) ||
                                  (dow == java.time.DayOfWeek.SATURDAY && Boolean.TRUE.equals(sab)) ||
                                  (dow == java.time.DayOfWeek.SUNDAY && Boolean.TRUE.equals(dom));
            if (trabalhaDia) count++;
            cursor = cursor.plusDays(1);
        }
        return count;
    }

    private void incrementarDias(int[] counts, LocalDate startBound, LocalDate endBound, java.time.DayOfWeek dow) {
        LocalDate first = startBound;
        int shift = (dow.getValue() - first.getDayOfWeek().getValue() + 7) % 7;
        first = first.plusDays(shift);
        while (!first.isAfter(endBound)) {
            int idx = first.getDayOfMonth();
            counts[idx]++;
            first = first.plusDays(7);
        }
    }
}
