package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.FlushModeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;

 @Service
 public class FolhaPagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(FolhaPagamentoService.class);

    @Autowired
    private FolhaPagamentoRepository folhaPagamentoRepository;

    @Autowired
    private HoleriteRepository holeriteRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private ValeTransporteRepository valeTransporteRepository;

    @Autowired
    private ValeRefeicaoRepository valeRefeicaoRepository;

    @Autowired
    private AdesaoPlanoSaudeRepository adesaoPlanoSaudeRepository;

    @Autowired
    private HoleriteCalculoService holeriteCalculoService;

    private final Map<String, Map<String, Object>> processamentoJobs = new ConcurrentHashMap<>();
    private static final int MAX_LOG_ITEMS = 50;
    private static final ThreadLocal<String> currentJobId = new ThreadLocal<>();

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private com.jaasielsilva.portalceo.service.NotificationService notificationService;
    @Autowired
    private com.jaasielsilva.portalceo.repository.NotificationRepository notificationRepository;
    private static class PageResult {
        int processed;
        int blocks;
        long lastFlushMs;
        BigDecimal bruto = BigDecimal.ZERO;
        BigDecimal descontos = BigDecimal.ZERO;
        BigDecimal inss = BigDecimal.ZERO;
        BigDecimal irrf = BigDecimal.ZERO;
        BigDecimal fgts = BigDecimal.ZERO;
    }

    /**
     * Lista todas as folhas de pagamento ordenadas por ano e mês
     */
    public List<FolhaPagamento> listarTodas() {
        return folhaPagamentoRepository.findAll();
    }

    /**
     * Busca folhas de pagamento por ano
     */
    public List<FolhaPagamento> buscarPorAno(Integer ano) {
        return folhaPagamentoRepository.findByAnoReferenciaOrderByMesReferenciaDesc(ano);
    }

    /**
     * Busca folha de pagamento por mês e ano
     */
    public Optional<FolhaPagamento> buscarPorMesAno(Integer mes, Integer ano) {
        return folhaPagamentoRepository.findFolhaByMesAno(mes, ano);
    }

    public Optional<FolhaPagamento> buscarPorMesAnoTipo(Integer mes, Integer ano, String tipoFolha) {
        String tipo = tipoFolha == null || tipoFolha.isBlank() ? "normal" : tipoFolha.toLowerCase();
        return folhaPagamentoRepository.findFolhaByMesAnoAndTipo(mes, ano, tipo);
    }

    public String iniciarProcessamentoAsync(Integer mes, Integer ano, Usuario usuarioProcessamento) {
        return iniciarProcessamentoAsync(mes, ano, usuarioProcessamento, null, "normal");
    }

    public String iniciarProcessamentoAsync(Integer mes, Integer ano, Usuario usuarioProcessamento, Long departamentoId, String tipoFolha) {
        String jobId = UUID.randomUUID().toString();
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("status", "AGENDADO");
        info.put("message", "Processamento agendado");
        info.put("startedAt", java.time.LocalDateTime.now().toString());
        processamentoJobs.put(jobId, info);
        appendJobLog(jobId, String.format("Agendado processamento para %02d/%d (tipo=%s%s)", mes, ano, String.valueOf(tipoFolha), departamentoId != null ? (", dept=" + departamentoId) : ""));
        pushJobStatus(jobId);
        try {
            com.jaasielsilva.portalceo.model.Notification nStart = notificationService.createNotification(
                    "payroll_processing_start",
                    "Processamento de Folha",
                    String.format("Iniciado — Referência: %02d/%d", mes, ano),
                    com.jaasielsilva.portalceo.model.Notification.Priority.LOW,
                    usuarioProcessamento
            );
            nStart.setActionUrl("/rh/folha-pagamento/gerar");
            nStart.setMetadata(String.format("{\"jobId\":\"%s\",\"mes\":%d,\"ano\":%d,\"tipoFolha\":\"%s\"}", jobId, mes, ano, String.valueOf(tipoFolha)));
            notificationRepository.save(nStart);
        } catch (Exception ignored) {}
        gerarFolhaPagamentoAsync(mes, ano, usuarioProcessamento, departamentoId, tipoFolha, jobId);
        return jobId;
    }

    public Map<String, Object> obterStatusProcessamento(String jobId) {
        return processamentoJobs.getOrDefault(jobId, java.util.Map.of("status", "DESCONHECIDO"));
    }

    @Async("taskExecutor")
    public void gerarFolhaPagamentoAsync(Integer mes, Integer ano, Usuario usuarioProcessamento, String jobId) {
        gerarFolhaPagamentoAsync(mes, ano, usuarioProcessamento, null, "normal", jobId);
    }

    @Async("taskExecutor")
    public void gerarFolhaPagamentoAsync(Integer mes, Integer ano, Usuario usuarioProcessamento, Long departamentoId, String tipoFolha, String jobId) {
        Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
        info.put("status", "PROCESSANDO");
        info.put("message", "Processando folha");
        appendJobLog(jobId, String.format("Iniciando processamento da folha %02d/%d", mes, ano));
        pushJobStatus(jobId);
        currentJobId.set(jobId);
        try {
            FolhaPagamento folha = gerarFolhaPagamento(mes, ano, usuarioProcessamento, departamentoId, tipoFolha);
            info.put("status", "CONCLUIDO");
            info.put("folhaId", folha.getId());
            info.put("message", "Folha gerada com sucesso");
            info.put("finishedAt", java.time.LocalDateTime.now().toString());
            try {
                long durationMs = -1L;
                Object started = info.get("startedAt");
                if (started != null) {
                    // apenas informativo: durationMs já é reportado via métricas no frontend
                    durationMs = 0L;
                }
                com.jaasielsilva.portalceo.model.Notification nDone = notificationService.createNotification(
                        "payroll_processing_complete",
                        "Processamento de Folha",
                        String.format("Concluído — %02d/%d", mes, ano),
                        com.jaasielsilva.portalceo.model.Notification.Priority.MEDIUM,
                        usuarioProcessamento
                );
                nDone.setActionUrl("/rh/folha-pagamento/visualizar/" + folha.getId());
                nDone.setMetadata(String.format("{\"jobId\":\"%s\",\"folhaId\":%d,\"mes\":%d,\"ano\":%d,\"durationMs\":%d}", jobId, folha.getId(), mes, ano, durationMs));
                notificationRepository.save(nDone);
                notificationService.createGlobalNotification(
                        "payroll_processing_complete",
                        "Processamento de Folha",
                        String.format("Concluído %02d/%d — folhaId=%d", mes, ano, folha.getId()),
                        com.jaasielsilva.portalceo.model.Notification.Priority.MEDIUM
                );
            } catch (Exception ignored) {}
            appendJobLog(jobId, String.format("Folha gerada com sucesso (folhaId=%d)", folha.getId()));
            pushJobStatus(jobId);
        } catch (Exception e) {
            info.put("status", "ERRO");
            info.put("message", e.getMessage());
            info.put("finishedAt", java.time.LocalDateTime.now().toString());
            try {
                com.jaasielsilva.portalceo.model.Notification nErr = notificationService.createNotification(
                        "payroll_processing_error",
                        "Processamento de Folha",
                        String.format("Erro — %s", e.getMessage()),
                        com.jaasielsilva.portalceo.model.Notification.Priority.HIGH,
                        usuarioProcessamento
                );
                nErr.setActionUrl("/rh/folha-pagamento");
                nErr.setMetadata(String.format("{\"jobId\":\"%s\",\"mes\":%d,\"ano\":%d}", jobId, mes, ano));
                notificationRepository.save(nErr);
                notificationService.createGlobalNotification(
                        "payroll_processing_error",
                        "Processamento de Folha",
                        String.format("Erro no processamento %02d/%d", mes, ano),
                        com.jaasielsilva.portalceo.model.Notification.Priority.HIGH
                );
            } catch (Exception ignored) {}
            appendJobLog(jobId, String.format("Erro no processamento: %s", e.getMessage()));
            logger.error("Erro no processamento da folha {}/{}: {}", mes, ano, e.getMessage());
            pushJobStatus(jobId);
        } finally {
            currentJobId.remove();
        }
    }

    /**
     * Busca folha de pagamento por ID
     */
    public Optional<FolhaPagamento> buscarPorId(Long id) {
        return folhaPagamentoRepository.findById(id);
    }

    /**
     * Verifica se já existe folha para o mês/ano
     */
    public boolean existeFolhaPorMesAno(Integer mes, Integer ano) {
        return folhaPagamentoRepository.existsByMesReferenciaAndAnoReferencia(mes, ano);
    }

    /**
     * Gera folha de pagamento para um mês/ano específico
     */
    @Transactional
    public FolhaPagamento gerarFolhaPagamento(Integer mes, Integer ano, Usuario usuarioProcessamento) {
        return gerarFolhaPagamento(mes, ano, usuarioProcessamento, null, "normal");
    }

    @Transactional
    public FolhaPagamento gerarFolhaPagamento(Integer mes, Integer ano, Usuario usuarioProcessamento, Long departamentoId, String tipoFolha) {
        logger.info("Iniciando geração de folha de pagamento para {}/{}", mes, ano);

        entityManager.setFlushMode(FlushModeType.COMMIT);

        // Verificar existência e decidir criar ou reutilizar
        String tipoKey = tipoFolha == null || tipoFolha.isBlank() ? "normal" : tipoFolha.toLowerCase();
        boolean tipoEspecial = "ferias".equals(tipoKey) || "decimo_terceiro".equals(tipoKey);
        Optional<FolhaPagamento> existenteOpt = tipoEspecial ? buscarPorMesAnoTipo(mes, ano, tipoKey) : buscarPorMesAno(mes, ano);
        FolhaPagamento folha;
        if (existenteOpt.isPresent()) {
            folha = existenteOpt.get();
            if (folha.getStatus() == FolhaPagamento.StatusFolha.FECHADA || folha.getStatus() == FolhaPagamento.StatusFolha.CANCELADA) {
                throw new IllegalStateException("Folha " + mes + "/" + ano + " está " + folha.getStatus() + ". Não é possível adicionar holerites.");
            }
            if (tipoEspecial && (folha.getTipoFolha() == null || !folha.getTipoFolha().equalsIgnoreCase(tipoKey))) {
                folha.setTipoFolha(tipoKey);
            }
        } else {
            folha = new FolhaPagamento();
            folha.setMesReferencia(mes);
            folha.setAnoReferencia(ano);
            folha.setUsuarioProcessamento(usuarioProcessamento);
            folha.setDataProcessamento(LocalDate.now());
            folha.setStatus(FolhaPagamento.StatusFolha.EM_PROCESSAMENTO);
            folha.setTipoFolha(tipoEspecial ? tipoKey : "normal");
        }

        // Inicializar totais (se reutilizando folha, partir dos totais atuais)
        BigDecimal totalBruto = folha.getTotalBruto() != null ? folha.getTotalBruto() : BigDecimal.ZERO;
        BigDecimal totalDescontos = folha.getTotalDescontos() != null ? folha.getTotalDescontos() : BigDecimal.ZERO;
        BigDecimal totalInss = folha.getTotalInss() != null ? folha.getTotalInss() : BigDecimal.ZERO;
        BigDecimal totalIrrf = folha.getTotalIrrf() != null ? folha.getTotalIrrf() : BigDecimal.ZERO;
        BigDecimal totalFgts = folha.getTotalFgts() != null ? folha.getTotalFgts() : BigDecimal.ZERO;

        folha.setTotalBruto(totalBruto);
        folha.setTotalDescontos(totalDescontos);
        folha.setTotalLiquido(totalBruto.subtract(totalDescontos));
        folha.setTotalInss(totalInss);
        folha.setTotalIrrf(totalIrrf);
        folha.setTotalFgts(totalFgts);

        // Salvar folha (novo) para obter ID; se reutilizada, apenas usar ID existente
        if (folha.getId() == null) {
            folha = folhaPagamentoRepository.save(folha);
        }
        final Long folhaId = folha.getId();

        // Buscar colaboradores conforme filtro
        long tInicio = System.nanoTime();
        String jobId = currentJobId.get();
        if (departamentoId != null) {
            List<Colaborador> colaboradoresFiltrados = colaboradorRepository.findByDepartamentoIdAndAtivoTrue(departamentoId);
            // Excluir colaboradores que já possuem holerite nessa folha
            List<Holerite> existentesFolha = holeriteRepository.findByFolhaPagamento(folha);
            java.util.Set<Long> colabsJaProcessados = existentesFolha.stream()
                    .map(h -> h.getColaborador() != null ? h.getColaborador().getId() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            List<Colaborador> colaboradoresRestantes = colaboradoresFiltrados.stream()
                    .filter(c -> !colabsJaProcessados.contains(c.getId()))
                    .collect(java.util.stream.Collectors.toList());
            long total = colaboradoresRestantes != null ? colaboradoresRestantes.size() : 0L;
            if (jobId != null) {
                Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                info.put("total", total);
                info.put("processed", 0);
                info.put("progressPct", 0);
                info.put("startedAt", java.time.LocalDateTime.now().toString());
                info.put("message", "Processando colaboradores do departamento em blocos...");
                pushJobStatus(jobId);
            }
            if (total == 0L) {
                if (jobId != null) {
                    Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                    info.put("status", "CONCLUIDO");
                    info.put("message", "Nenhum colaborador restante para processar no departamento");
                    pushJobStatus(jobId);
                }
                folha = folhaPagamentoRepository.save(folha);
                return folha;
            }
            YearMonth ymPeriodo = YearMonth.of(ano, mes);
            LocalDate inicioPeriodo = ymPeriodo.atDay(1);
            LocalDate fimPeriodo = ymPeriodo.atEndOfMonth();
            List<RegistroPontoRepository.PontoResumoPorColaboradorProjection> resumosPeriodo =
                    registroPontoRepository.aggregateResumoPorPeriodoGroupByColaborador(inicioPeriodo, fimPeriodo);
            Map<Long, RegistroPontoRepository.PontoResumoPorColaboradorProjection> resumoPorColaborador =
                    resumosPeriodo.stream().collect(Collectors.toMap(
                            RegistroPontoRepository.PontoResumoPorColaboradorProjection::getColaboradorId,
                            Function.identity()
                    ));
            final int CHUNK_SIZE = 500;
            int totalProcessados = 0;
            int blocosProcessados = 0;
            List<Holerite> holeritesChunk = new ArrayList<>(CHUNK_SIZE);
            FolhaPagamento folhaRef = new FolhaPagamento();
            folhaRef.setId(folhaId);
            Map<Long, BeneficiosInfo> beneficiosPorColaborador = carregarBeneficiosEmLote(colaboradoresRestantes, mes, ano);
            for (Colaborador col : colaboradoresRestantes) {
                RegistroPontoRepository.PontoResumoPorColaboradorProjection resumo = resumoPorColaborador.get(col.getId());
                BeneficiosInfo beneficios = beneficiosPorColaborador.getOrDefault(col.getId(), new BeneficiosInfo(java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty()));
                Holerite hol = gerarHoleriteComBeneficios(col, folhaRef, mes, ano, resumo, beneficios, tipoFolha);
                holeritesChunk.add(hol);
                BigDecimal salarioBase = hol.getSalarioBase() != null ? hol.getSalarioBase() : BigDecimal.ZERO;
                BigDecimal horasExtras = hol.getHorasExtras() != null ? hol.getHorasExtras() : BigDecimal.ZERO;
                BigDecimal adicionalNoturno = hol.getAdicionalNoturno() != null ? hol.getAdicionalNoturno() : BigDecimal.ZERO;
                BigDecimal adicionalPericulosidade = hol.getAdicionalPericulosidade() != null ? hol.getAdicionalPericulosidade() : BigDecimal.ZERO;
                BigDecimal adicionalInsalubridade = hol.getAdicionalInsalubridade() != null ? hol.getAdicionalInsalubridade() : BigDecimal.ZERO;
                BigDecimal comissoes = hol.getComissoes() != null ? hol.getComissoes() : BigDecimal.ZERO;
                BigDecimal bonificacoes = hol.getBonificacoes() != null ? hol.getBonificacoes() : BigDecimal.ZERO;
                BigDecimal vt = hol.getValeTransporte() != null ? hol.getValeTransporte() : BigDecimal.ZERO;
                BigDecimal vr = hol.getValeRefeicao() != null ? hol.getValeRefeicao() : BigDecimal.ZERO;
                BigDecimal saude = hol.getAuxilioSaude() != null ? hol.getAuxilioSaude() : BigDecimal.ZERO;
                BigDecimal totalProventosCalc = salarioBase
                        .add(horasExtras)
                        .add(adicionalNoturno)
                        .add(adicionalPericulosidade)
                        .add(adicionalInsalubridade)
                        .add(comissoes)
                        .add(bonificacoes)
                        .add(vt)
                        .add(vr)
                        .add(saude);
                totalBruto = totalBruto.add(totalProventosCalc);

                BigDecimal descInss = hol.getDescontoInss() != null ? hol.getDescontoInss() : BigDecimal.ZERO;
                BigDecimal descIrrf = hol.getDescontoIrrf() != null ? hol.getDescontoIrrf() : BigDecimal.ZERO;
                BigDecimal descFgts = hol.getDescontoFgts() != null ? hol.getDescontoFgts() : BigDecimal.ZERO;
                BigDecimal descVt = hol.getDescontoValeTransporte() != null ? hol.getDescontoValeTransporte() : BigDecimal.ZERO;
                BigDecimal descVr = hol.getDescontoValeRefeicao() != null ? hol.getDescontoValeRefeicao() : BigDecimal.ZERO;
                BigDecimal descSaude = hol.getDescontoPlanoSaude() != null ? hol.getDescontoPlanoSaude() : BigDecimal.ZERO;
                BigDecimal outros = hol.getOutrosDescontos() != null ? hol.getOutrosDescontos() : BigDecimal.ZERO;
                BigDecimal totalDescontosCalc = descInss
                        .add(descIrrf)
                        .add(descFgts)
                        .add(descVt)
                        .add(descVr)
                        .add(descSaude)
                        .add(outros);
                totalDescontos = totalDescontos.add(totalDescontosCalc);
                totalInss = totalInss.add(descInss);
                totalIrrf = totalIrrf.add(descIrrf);
                BigDecimal salarioBrutoHolerite = salarioBase.add(horasExtras);
                if ("ferias".equalsIgnoreCase(tipoFolha)) {
                    salarioBrutoHolerite = salarioBrutoHolerite.add(bonificacoes);
                }
                totalFgts = totalFgts.add(holeriteCalculoService.calcularFgtsPatronal(salarioBrutoHolerite));

                if (holeritesChunk.size() >= CHUNK_SIZE) {
                    long tPersistIni = System.nanoTime();
                    holeriteRepository.saveAll(holeritesChunk);
                    holeriteRepository.flush();
                    long tPersistFim = System.nanoTime();
                    if (jobId != null) {
                        Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                        info.put("lastFlushMs", (tPersistFim - tPersistIni) / 1_000_000);
                    }
                    totalProcessados += holeritesChunk.size();
                    holeritesChunk.clear();
                    blocosProcessados++;
                    if (jobId != null) {
                        Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                        long elapsedMs = (System.nanoTime() - tInicio) / 1_000_000;
                        double rateMsPerItem = totalProcessados > 0 ? (double) elapsedMs / (double) totalProcessados : 0d;
                        long etaMs = rateMsPerItem > 0 ? (long) (((long) total - (long) totalProcessados) * rateMsPerItem) : -1;
                        int pct = total > 0 ? (int) Math.round(100.0 * (double) totalProcessados / (double) total) : 0;
                        info.put("blocksProcessed", blocosProcessados);
                        info.put("processed", totalProcessados);
                        info.put("progressPct", pct);
                        info.put("elapsedMs", elapsedMs);
                        info.put("etaMs", etaMs);
                        info.put("message", "Processando cálculos de holerites (departamento)...");
                        appendJobLog(jobId, String.format("Processados %d de %d (%d%%)", totalProcessados, total, pct));
                        pushJobStatus(jobId);
                    }
                }
            }
            if (!holeritesChunk.isEmpty()) {
                long tPersistIni = System.nanoTime();
                holeriteRepository.saveAll(holeritesChunk);
                holeriteRepository.flush();
                long tPersistFim = System.nanoTime();
                if (jobId != null) {
                    Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                    info.put("lastFlushMs", (tPersistFim - tPersistIni) / 1_000_000);
                }
                totalProcessados += holeritesChunk.size();
                holeritesChunk.clear();
                blocosProcessados++;
            }
        } else {
            long totalAtivos = colaboradorRepository.countByAtivoTrue();
            List<Holerite> existentesFolha = holeriteRepository.findByFolhaPagamento(folha);
            java.util.Set<Long> colabsJaProcessados = existentesFolha.stream()
                    .map(h -> h.getColaborador() != null ? h.getColaborador().getId() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            long totalRestantes = Math.max(0L, totalAtivos - (long) colabsJaProcessados.size());
            if (jobId != null) {
                Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                info.put("total", totalRestantes);
                info.put("processed", 0);
                info.put("progressPct", 0);
                info.put("startedAt", java.time.LocalDateTime.now().toString());
                info.put("message", "Calculando holerites e preparando persistência em blocos...");
                pushJobStatus(jobId);
            }
            if (totalRestantes == 0L) {
                if (jobId != null) {
                    Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                    info.put("status", "CONCLUIDO");
                    info.put("message", "Nenhum colaborador restante para processar");
                    pushJobStatus(jobId);
                }
                folha = folhaPagamentoRepository.save(folha);
                return folha;
            }

            YearMonth ymPeriodo = YearMonth.of(ano, mes);
            LocalDate inicioPeriodo = ymPeriodo.atDay(1);
            LocalDate fimPeriodo = ymPeriodo.atEndOfMonth();
            List<RegistroPontoRepository.PontoResumoPorColaboradorProjection> resumosPeriodo =
                    registroPontoRepository.aggregateResumoPorPeriodoGroupByColaborador(inicioPeriodo, fimPeriodo);
            Map<Long, RegistroPontoRepository.PontoResumoPorColaboradorProjection> resumoPorColaborador =
                    resumosPeriodo.stream().collect(Collectors.toMap(
                            RegistroPontoRepository.PontoResumoPorColaboradorProjection::getColaboradorId,
                            Function.identity()
                    ));
            final int CHUNK_SIZE = 500;
            final int PAGE_SIZE = 800;
            int totalProcessados = 0;
            int blocosProcessados = 0;
            int totalPages = (int) Math.ceil((double) totalAtivos / (double) PAGE_SIZE);
            java.util.function.IntFunction<java.util.concurrent.Callable<PageResult>> pageTaskFactory = (int idx) -> () -> processarPaginaFolha(idx, PAGE_SIZE, CHUNK_SIZE, folhaId, mes, ano, resumoPorColaborador, tipoFolha, colabsJaProcessados);
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex += 2) {
                java.util.concurrent.Future<PageResult> f1 = taskExecutor.submit(pageTaskFactory.apply(pageIndex));
                java.util.concurrent.Future<PageResult> f2 = pageIndex + 1 < totalPages ? taskExecutor.submit(pageTaskFactory.apply(pageIndex + 1)) : null;
                try {
                    PageResult r1 = f1.get();
                    totalProcessados += r1.processed;
                    blocosProcessados += r1.blocks;
                    totalBruto = totalBruto.add(r1.bruto);
                    totalDescontos = totalDescontos.add(r1.descontos);
                    totalInss = totalInss.add(r1.inss);
                    totalIrrf = totalIrrf.add(r1.irrf);
                    totalFgts = totalFgts.add(r1.fgts);
                    if (jobId != null) {
                        Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                        long elapsedMs = (System.nanoTime() - tInicio) / 1_000_000;
                        long total = totalRestantes;
                        double rateMsPerItem = totalProcessados > 0 ? (double) elapsedMs / (double) totalProcessados : 0d;
                        long etaMs = rateMsPerItem > 0 ? (long) ((total - (long) totalProcessados) * rateMsPerItem) : -1;
                        int pct = total > 0 ? (int) Math.round(100.0 * (double) totalProcessados / (double) total) : 0;
                        info.put("lastFlushMs", r1.lastFlushMs);
                        info.put("blocksProcessed", blocosProcessados);
                        info.put("processed", totalProcessados);
                        info.put("progressPct", pct);
                        info.put("elapsedMs", elapsedMs);
                        info.put("etaMs", etaMs);
                        info.put("message", "Processando cálculos de holerites em paralelo...");
                        appendJobLog(jobId, String.format("Processados %d de %d (%d%%)", totalProcessados, total, pct));
                        pushJobStatus(jobId);
                    }
                    if (f2 != null) {
                        PageResult r2 = f2.get();
                        totalProcessados += r2.processed;
                        blocosProcessados += r2.blocks;
                        totalBruto = totalBruto.add(r2.bruto);
                        totalDescontos = totalDescontos.add(r2.descontos);
                        totalInss = totalInss.add(r2.inss);
                        totalIrrf = totalIrrf.add(r2.irrf);
                        totalFgts = totalFgts.add(r2.fgts);
                        if (jobId != null) {
                            Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
                            long elapsedMs = (System.nanoTime() - tInicio) / 1_000_000;
                            long total = totalRestantes;
                            double rateMsPerItem = totalProcessados > 0 ? (double) elapsedMs / (double) totalProcessados : 0d;
                            long etaMs = rateMsPerItem > 0 ? (long) ((total - (long) totalProcessados) * rateMsPerItem) : -1;
                            int pct = total > 0 ? (int) Math.round(100.0 * (double) totalProcessados / (double) total) : 0;
                            info.put("lastFlushMs", r2.lastFlushMs);
                            info.put("blocksProcessed", blocosProcessados);
                            info.put("processed", totalProcessados);
                            info.put("progressPct", pct);
                            info.put("elapsedMs", elapsedMs);
                            info.put("etaMs", etaMs);
                            info.put("message", "Processando cálculos de holerites em paralelo...");
                            appendJobLog(jobId, String.format("Processados %d de %d (%d%%)", totalProcessados, total, pct));
                            pushJobStatus(jobId);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Erro ao processar páginas em paralelo: {}", e.getMessage());
                }
            }
        }

        folha.setTotalBruto(totalBruto);
        folha.setTotalDescontos(totalDescontos);
        folha.setTotalLiquido(totalBruto.subtract(totalDescontos));
        folha.setTotalInss(totalInss);
        folha.setTotalIrrf(totalIrrf);
        folha.setTotalFgts(totalFgts);
        folha.setStatus(FolhaPagamento.StatusFolha.PROCESSADA);

        folha = folhaPagamentoRepository.save(folha);

        long tFim = System.nanoTime();
        logger.info("Folha de pagamento {}/{} gerada com sucesso em {} ms.", mes, ano, (tFim - tInicio) / 1_000_000);
        if (jobId != null) {
            appendJobLog(jobId, String.format("Conclusão em %d ms", (tFim - tInicio) / 1_000_000));
            pushJobStatus(jobId);
        }
        return folha;

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private PageResult processarPaginaFolha(int pageIndex,
                                            int PAGE_SIZE,
                                            int CHUNK_SIZE,
                                            Long folhaId,
                                            Integer mes,
                                            Integer ano,
                                            Map<Long, RegistroPontoRepository.PontoResumoPorColaboradorProjection> resumoPorColaborador,
                                            String tipoFolha,
                                            java.util.Set<Long> colabsJaProcessados) {
        entityManager.setFlushMode(FlushModeType.COMMIT);
        PageResult r = new PageResult();
        org.springframework.data.domain.Page<Colaborador> page = colaboradorRepository.findByAtivoTrue(org.springframework.data.domain.PageRequest.of(pageIndex, PAGE_SIZE, org.springframework.data.domain.Sort.by("nome").ascending()));
        List<Colaborador> colaboradores = page.getContent();
        if (colabsJaProcessados != null && !colabsJaProcessados.isEmpty()) {
            colaboradores = colaboradores.stream()
                    .filter(c -> c.getId() != null && !colabsJaProcessados.contains(c.getId()))
                    .collect(Collectors.toList());
        }
        if (colaboradores == null || colaboradores.isEmpty()) return r;

        Map<Long, BeneficiosInfo> beneficiosPorColaborador = carregarBeneficiosEmLote(colaboradores, mes, ano);
        FolhaPagamento folhaRef = new FolhaPagamento();
        folhaRef.setId(folhaId);

        List<Holerite> holeritesAll = colaboradores.parallelStream()
                .map(col -> {
                    RegistroPontoRepository.PontoResumoPorColaboradorProjection resumo = resumoPorColaborador.get(col.getId());
                    BeneficiosInfo beneficios = beneficiosPorColaborador.getOrDefault(col.getId(), new BeneficiosInfo(java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty()));
                    return gerarHoleriteComBeneficios(col, folhaRef, mes, ano, resumo, beneficios, tipoFolha);
                })
                .collect(Collectors.toList());

        List<Holerite> holeritesChunk = new ArrayList<>(CHUNK_SIZE);
        for (Holerite holerite : holeritesAll) {
            holeritesChunk.add(holerite);
            BigDecimal salarioBase = holerite.getSalarioBase() != null ? holerite.getSalarioBase() : BigDecimal.ZERO;
            BigDecimal horasExtras = holerite.getHorasExtras() != null ? holerite.getHorasExtras() : BigDecimal.ZERO;
            BigDecimal adicionalNoturno = holerite.getAdicionalNoturno() != null ? holerite.getAdicionalNoturno() : BigDecimal.ZERO;
            BigDecimal adicionalPericulosidade = holerite.getAdicionalPericulosidade() != null ? holerite.getAdicionalPericulosidade() : BigDecimal.ZERO;
            BigDecimal adicionalInsalubridade = holerite.getAdicionalInsalubridade() != null ? holerite.getAdicionalInsalubridade() : BigDecimal.ZERO;
            BigDecimal comissoes = holerite.getComissoes() != null ? holerite.getComissoes() : BigDecimal.ZERO;
            BigDecimal bonificacoes = holerite.getBonificacoes() != null ? holerite.getBonificacoes() : BigDecimal.ZERO;
            BigDecimal vt = holerite.getValeTransporte() != null ? holerite.getValeTransporte() : BigDecimal.ZERO;
            BigDecimal vr = holerite.getValeRefeicao() != null ? holerite.getValeRefeicao() : BigDecimal.ZERO;
            BigDecimal saude = holerite.getAuxilioSaude() != null ? holerite.getAuxilioSaude() : BigDecimal.ZERO;
            BigDecimal totalProventosCalc = salarioBase
                    .add(horasExtras)
                    .add(adicionalNoturno)
                    .add(adicionalPericulosidade)
                    .add(adicionalInsalubridade)
                    .add(comissoes)
                    .add(bonificacoes)
                    .add(vt)
                    .add(vr)
                    .add(saude);
            r.bruto = r.bruto.add(totalProventosCalc);

            BigDecimal descInss = holerite.getDescontoInss() != null ? holerite.getDescontoInss() : BigDecimal.ZERO;
            BigDecimal descIrrf = holerite.getDescontoIrrf() != null ? holerite.getDescontoIrrf() : BigDecimal.ZERO;
            BigDecimal descFgts = holerite.getDescontoFgts() != null ? holerite.getDescontoFgts() : BigDecimal.ZERO;
            BigDecimal descVt = holerite.getDescontoValeTransporte() != null ? holerite.getDescontoValeTransporte() : BigDecimal.ZERO;
            BigDecimal descVr = holerite.getDescontoValeRefeicao() != null ? holerite.getDescontoValeRefeicao() : BigDecimal.ZERO;
            BigDecimal descSaude = holerite.getDescontoPlanoSaude() != null ? holerite.getDescontoPlanoSaude() : BigDecimal.ZERO;
            BigDecimal outros = holerite.getOutrosDescontos() != null ? holerite.getOutrosDescontos() : BigDecimal.ZERO;
            BigDecimal totalDescontosCalc = descInss
                    .add(descIrrf)
                    .add(descFgts)
                    .add(descVt)
                    .add(descVr)
                    .add(descSaude)
                    .add(outros);
            r.descontos = r.descontos.add(totalDescontosCalc);

            r.inss = r.inss.add(descInss);
            r.irrf = r.irrf.add(descIrrf);
            BigDecimal salarioBrutoHolerite = salarioBase.add(horasExtras);
            if ("ferias".equalsIgnoreCase(tipoFolha)) {
                BigDecimal bonif = holerite.getBonificacoes() != null ? holerite.getBonificacoes() : BigDecimal.ZERO;
                salarioBrutoHolerite = salarioBrutoHolerite.add(bonif);
            }
            r.fgts = r.fgts.add(holeriteCalculoService.calcularFgtsPatronal(salarioBrutoHolerite));

            if (holeritesChunk.size() >= CHUNK_SIZE) {
                long tPersistIni = System.nanoTime();
                holeriteRepository.saveAll(holeritesChunk);
                holeriteRepository.flush();
                long tPersistFim = System.nanoTime();
                r.lastFlushMs = (tPersistFim - tPersistIni) / 1_000_000;
                r.processed += holeritesChunk.size();
                holeritesChunk.clear();
                r.blocks++;
            }
        }
        if (!holeritesChunk.isEmpty()) {
            long tPersistIni = System.nanoTime();
            holeriteRepository.saveAll(holeritesChunk);
            holeriteRepository.flush();
            long tPersistFim = System.nanoTime();
            r.lastFlushMs = (tPersistFim - tPersistIni) / 1_000_000;
            r.processed += holeritesChunk.size();
            holeritesChunk.clear();
            r.blocks++;
        }
        entityManager.clear();
        return r;
    }

    private void appendJobLog(String jobId, String message) {
        if (jobId == null) return;
        Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
        @SuppressWarnings("unchecked")
        List<String> logs = (List<String>) info.get("logs");
        if (logs == null) {
            logs = new ArrayList<>();
            info.put("logs", logs);
        }
        String timestamp = LocalDateTime.now().toString();
        logs.add(timestamp + " — " + message);
        if (logs.size() > MAX_LOG_ITEMS) {
            // manter apenas os últimos MAX_LOG_ITEMS
            logs.remove(0);
        }
        pushJobStatus(jobId);
    }

    private void pushJobStatus(String jobId) {
        if (jobId == null) return;
        Map<String, Object> info = processamentoJobs.get(jobId);
        if (info != null) {
            try {
                messagingTemplate.convertAndSend("/topic/folha-status/" + jobId, info);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Gera holerite individual para um colaborador
     */
    private Holerite gerarHolerite(Colaborador colaborador, FolhaPagamento folha, Integer mes, Integer ano) {
        return gerarHolerite(colaborador, folha, mes, ano, null, "normal");
    }

    /**
     * Gera holerite individual com possibilidade de usar resumo de ponto pré-agregado
     */
    private Holerite gerarHolerite(Colaborador colaborador, FolhaPagamento folha, Integer mes, Integer ano,
                                   RegistroPontoRepository.PontoResumoPorColaboradorProjection resumoAgregado,
                                   String tipoFolha) {
        Holerite holerite = new Holerite();
        holerite.setColaborador(colaborador);
        holerite.setFolhaPagamento(folha);
        holerite.setSalarioBase(colaborador.getSalario());

        // Calcular dados de ponto (dias e horas trabalhadas)
        if (resumoAgregado != null) {
            aplicarResumoPonto(holerite, resumoAgregado);
        } else {
            calcularDadosPonto(holerite, colaborador, mes, ano);
        }

        // Carregar benefícios uma vez e reutilizar
        BeneficiosInfo beneficios = carregarBeneficios(colaborador, mes, ano);

        calcularProventos(holerite, colaborador, mes, ano, beneficios, tipoFolha);
        calcularDescontos(holerite, colaborador, mes, ano, beneficios, tipoFolha);
        holerite.setTipoFolha(tipoFolha);
        
        return holerite;
    }

    private Holerite gerarHoleriteComBeneficios(Colaborador colaborador, FolhaPagamento folha, Integer mes, Integer ano,
                                                RegistroPontoRepository.PontoResumoPorColaboradorProjection resumoAgregado,
                                                BeneficiosInfo beneficios,
                                                String tipoFolha) {
        Holerite holerite = new Holerite();
        holerite.setColaborador(colaborador);
        holerite.setFolhaPagamento(folha);
        holerite.setSalarioBase(colaborador.getSalario());

        if (resumoAgregado != null) {
            aplicarResumoPonto(holerite, resumoAgregado);
        } else {
            calcularDadosPonto(holerite, colaborador, mes, ano);
        }

        calcularProventos(holerite, colaborador, mes, ano, beneficios, tipoFolha);
        calcularDescontos(holerite, colaborador, mes, ano, beneficios, tipoFolha);
        holerite.setTipoFolha(tipoFolha);
        return holerite;
    }

    /**
     * Calcula dados de ponto para o holerite
     */
    private void calcularDadosPonto(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano) {
        YearMonth ym = YearMonth.of(ano, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();

        RegistroPontoRepository.PontoResumoProjection resumo = registroPontoRepository
                .aggregateResumoByColaboradorAndPeriodo(colaborador.getId(), inicio, fim);
        aplicarResumoPonto(holerite, resumo);
    }

    /**
     * Aplica dados agregados de ponto no holerite
     */
    private void aplicarResumoPonto(Holerite holerite, RegistroPontoRepository.PontoResumoProjection resumo) {
        Long faltas = resumo != null && resumo.getFaltas() != null ? resumo.getFaltas() : 0L;
        Long atrasos = resumo != null && resumo.getAtrasos() != null ? resumo.getAtrasos() : 0L;
        Long minutosTrabalhados = resumo != null && resumo.getMinutosTrabalhados() != null ? resumo.getMinutosTrabalhados() : 0L;
        Long minutosExtras = resumo != null && resumo.getMinutosExtras() != null ? resumo.getMinutosExtras() : 0L;
        Long diasComRegistro = resumo != null && resumo.getDiasComRegistro() != null ? resumo.getDiasComRegistro() : 0L;

        int diasTrabalhados = Math.max(0, diasComRegistro.intValue() - faltas.intValue());
        int horasTrabalhadas = (int) Math.floor(minutosTrabalhados / 60.0);

        holerite.setDiasTrabalhados(diasTrabalhados);
        holerite.setHorasTrabalhadas(horasTrabalhadas);
        holerite.setFaltas(faltas.intValue());
        holerite.setAtrasos(atrasos.intValue());

        BigDecimal valorHoraExtra = calcularValorHoraExtra(holerite.getSalarioBase());
        BigDecimal horasExtrasDecimal = BigDecimal.valueOf(minutosExtras)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        holerite.setHorasExtras(valorHoraExtra.multiply(horasExtrasDecimal).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Calcula proventos do holerite
     */
    private void calcularProventos(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano, BeneficiosInfo beneficios, String tipoFolha) {
        // Proventos básicos já definidos
        // Adicionar outros proventos conforme necessário
        holerite.setAdicionalNoturno(BigDecimal.ZERO);
        holerite.setAdicionalPericulosidade(BigDecimal.ZERO);
        holerite.setAdicionalInsalubridade(BigDecimal.ZERO);
        holerite.setComissoes(BigDecimal.ZERO);
        holerite.setBonificacoes(BigDecimal.ZERO);

        if (tipoFolha != null) {
            switch (tipoFolha.toLowerCase()) {
                case "decimo_terceiro":
                    holerite.setHorasExtras(BigDecimal.ZERO);
                    holerite.setValeTransporte(BigDecimal.ZERO);
                    holerite.setValeRefeicao(BigDecimal.ZERO);
                    holerite.setAuxilioSaude(BigDecimal.ZERO);
                    break;
                case "complementar":
                    holerite.setSalarioBase(BigDecimal.ZERO);
                    holerite.setValeTransporte(BigDecimal.ZERO);
                    holerite.setValeRefeicao(BigDecimal.ZERO);
                    holerite.setAuxilioSaude(BigDecimal.ZERO);
                    break;
                case "ferias":
                    BigDecimal salarioBase = holerite.getSalarioBase() != null ? holerite.getSalarioBase() : BigDecimal.ZERO;
                    holerite.setBonificacoes(salarioBase.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP));
                    holerite.setValeTransporte(BigDecimal.ZERO);
                    holerite.setValeRefeicao(BigDecimal.ZERO);
                    holerite.setAuxilioSaude(BigDecimal.ZERO);
                    break;
                default:
                    calcularBeneficios(holerite, beneficios, tipoFolha);
                    break;
            }
        } else {
            calcularBeneficios(holerite, beneficios, "normal");
        }
    }

    /**
     * Calcula benefícios do colaborador
     */
    private void calcularBeneficios(Holerite holerite, BeneficiosInfo beneficios, String tipoFolha) {
        // Vale Transporte
        if (!"decimo_terceiro".equalsIgnoreCase(tipoFolha) && !"ferias".equalsIgnoreCase(tipoFolha) && !"complementar".equalsIgnoreCase(tipoFolha)
                && beneficios.valeTransporte.isPresent() && beneficios.valeTransporte.get().isAtivo()) {
            holerite.setValeTransporte(beneficios.valeTransporte.get().getValorSubsidioEmpresa());
        } else {
            holerite.setValeTransporte(BigDecimal.ZERO);
        }

        // Vale Refeição
        if (!"decimo_terceiro".equalsIgnoreCase(tipoFolha) && !"ferias".equalsIgnoreCase(tipoFolha) && !"complementar".equalsIgnoreCase(tipoFolha)
                && beneficios.valeRefeicao.isPresent() && beneficios.valeRefeicao.get().isAtivo()) {
            holerite.setValeRefeicao(beneficios.valeRefeicao.get().getValorSubsidioEmpresa());
        } else {
            holerite.setValeRefeicao(BigDecimal.ZERO);
        }

        // Auxílio Saúde
        if (!"decimo_terceiro".equalsIgnoreCase(tipoFolha) && !"ferias".equalsIgnoreCase(tipoFolha) && !"complementar".equalsIgnoreCase(tipoFolha)
                && beneficios.planoSaude.isPresent()) {
            BigDecimal subsidio = beneficios.planoSaude.get().getValorSubsidioEmpresa();
            holerite.setAuxilioSaude(subsidio);
        } else {
            holerite.setAuxilioSaude(BigDecimal.ZERO);
        }
    }

    /**
     * Calcula descontos do holerite
     */
    private void calcularDescontos(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano, BeneficiosInfo beneficios, String tipoFolha) {
        BigDecimal salarioBruto = holerite.getSalarioBase().add(holerite.getHorasExtras());

        holerite.setDescontoInss(holeriteCalculoService.calcularInssProgressivo(salarioBruto));
        BigDecimal baseIrrf = salarioBruto.subtract(holerite.getDescontoInss());
        if ("ferias".equalsIgnoreCase(tipoFolha)) {
            BigDecimal bonif = holerite.getBonificacoes() != null ? holerite.getBonificacoes() : BigDecimal.ZERO;
            baseIrrf = baseIrrf.add(bonif);
        }
        holerite.setDescontoIrrf(holeriteCalculoService.calcularIrrf(baseIrrf, 0));
        holerite.setDescontoFgts(BigDecimal.ZERO);

        // Descontos de benefícios com dados já carregados
        calcularDescontosBeneficios(holerite, colaborador, mes, ano, beneficios, tipoFolha);

        holerite.setOutrosDescontos(BigDecimal.ZERO);
    }

    /**
     * Calcula descontos de benefícios
     */
    private void calcularDescontosBeneficios(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano, BeneficiosInfo beneficios, String tipoFolha) {
        // Desconto Vale Transporte
        if (!"decimo_terceiro".equalsIgnoreCase(tipoFolha) && !"ferias".equalsIgnoreCase(tipoFolha) && !"complementar".equalsIgnoreCase(tipoFolha)
                && beneficios.valeTransporte.isPresent() && beneficios.valeTransporte.get().isAtivo()) {
            holerite.setDescontoValeTransporte(beneficios.valeTransporte.get().getValorDesconto());
        } else {
            holerite.setDescontoValeTransporte(BigDecimal.ZERO);
        }

        holerite.setDescontoValeTransporte(
                holeriteCalculoService.limitarDescontoValeTransporte(holerite.getSalarioBase(), holerite.getDescontoValeTransporte())
        );

        // Desconto Vale Refeição
        if (!"decimo_terceiro".equalsIgnoreCase(tipoFolha) && !"ferias".equalsIgnoreCase(tipoFolha) && !"complementar".equalsIgnoreCase(tipoFolha)
                && beneficios.valeRefeicao.isPresent() && beneficios.valeRefeicao.get().isAtivo()) {
            holerite.setDescontoValeRefeicao(beneficios.valeRefeicao.get().getValorDesconto());
        } else {
            holerite.setDescontoValeRefeicao(BigDecimal.ZERO);
        }

        // Desconto Plano de Saúde
        if (!"decimo_terceiro".equalsIgnoreCase(tipoFolha) && !"ferias".equalsIgnoreCase(tipoFolha) && !"complementar".equalsIgnoreCase(tipoFolha)
                && beneficios.planoSaude.isPresent()) {
            BigDecimal desconto = beneficios.planoSaude.get().getValorDesconto();
            holerite.setDescontoPlanoSaude(desconto);
        } else {
            holerite.setDescontoPlanoSaude(BigDecimal.ZERO);
        }
    }

    // Estrutura interna para consolidar consultas de benefícios por colaborador
    private static class BeneficiosInfo {
        Optional<ValeTransporte> valeTransporte;
        Optional<ValeRefeicao> valeRefeicao;
        Optional<AdesaoPlanoSaude> planoSaude;

        BeneficiosInfo(Optional<ValeTransporte> vt, Optional<ValeRefeicao> vr, Optional<AdesaoPlanoSaude> ps) {
            this.valeTransporte = vt;
            this.valeRefeicao = vr;
            this.planoSaude = ps;
        }
    }

    private BeneficiosInfo carregarBeneficios(Colaborador colaborador, Integer mes, Integer ano) {
        Optional<ValeTransporte> vt = valeTransporteRepository.findByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano);
        Optional<ValeRefeicao> vr = valeRefeicaoRepository.findByColaboradorAndMesReferenciaAndAnoReferencia(colaborador, mes, ano);
        Optional<AdesaoPlanoSaude> ps = adesaoPlanoSaudeRepository.findAdesaoAtivaByColaborador(colaborador.getId());
        return new BeneficiosInfo(vt, vr, ps);
    }

    private Map<Long, BeneficiosInfo> carregarBeneficiosEmLote(List<Colaborador> colaboradores, Integer mes, Integer ano) {
        java.util.Set<Long> ids = colaboradores.stream().map(Colaborador::getId).collect(java.util.stream.Collectors.toSet());

        List<ValeTransporte> vts = valeTransporteRepository.findByMesAnoAndColaboradorIdIn(mes, ano, ids);
        Map<Long, ValeTransporte> vtPorColab = new java.util.HashMap<>();
        for (ValeTransporte vt : vts) {
            if (vt.getColaborador() != null) {
                if (vtPorColab.containsKey(vt.getColaborador().getId())) continue;
                if (vt.isAtivo()) vtPorColab.put(vt.getColaborador().getId(), vt);
            }
        }

        List<ValeRefeicao> vrs = valeRefeicaoRepository.findByMesAnoAndColaboradorIdIn(mes, ano, ids);
        Map<Long, ValeRefeicao> vrPorColab = new java.util.HashMap<>();
        for (ValeRefeicao vr : vrs) {
            if (vr.getColaborador() != null) {
                if (vrPorColab.containsKey(vr.getColaborador().getId())) continue;
                if (vr.isAtivo()) vrPorColab.put(vr.getColaborador().getId(), vr);
            }
        }

        List<AdesaoPlanoSaude> adesoesAtivas = adesaoPlanoSaudeRepository.findByStatusAndColaboradorIdIn(AdesaoPlanoSaude.StatusAdesao.ATIVA, ids);
        Map<Long, AdesaoPlanoSaude> psPorColab = new java.util.HashMap<>();
        for (AdesaoPlanoSaude ps : adesoesAtivas) {
            if (ps.getColaborador() != null) {
                if (psPorColab.containsKey(ps.getColaborador().getId())) continue;
                psPorColab.put(ps.getColaborador().getId(), ps);
            }
        }

        Map<Long, BeneficiosInfo> map = new java.util.HashMap<>();
        for (Long id : ids) {
            Optional<ValeTransporte> vtOpt = Optional.ofNullable(vtPorColab.get(id));
            Optional<ValeRefeicao> vrOpt = Optional.ofNullable(vrPorColab.get(id));
            Optional<AdesaoPlanoSaude> psOpt = Optional.ofNullable(psPorColab.get(id));
            map.put(id, new BeneficiosInfo(vtOpt, vrOpt, psOpt));
        }
        return map;
    }

    /**
     * Calcula INSS baseado na tabela atual (2024)
     */
    private BigDecimal calcularInss(BigDecimal salarioBruto) {
        if (salarioBruto.compareTo(BigDecimal.valueOf(1412.00)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.075)).setScale(2, RoundingMode.HALF_UP);
        } else if (salarioBruto.compareTo(BigDecimal.valueOf(2666.68)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.09)).setScale(2, RoundingMode.HALF_UP);
        } else if (salarioBruto.compareTo(BigDecimal.valueOf(4000.03)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        } else if (salarioBruto.compareTo(BigDecimal.valueOf(7786.02)) <= 0) {
            return salarioBruto.multiply(BigDecimal.valueOf(0.14)).setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(1090.04); // Teto do INSS 2024
        }
    }

    /**
     * Calcula IRRF baseado na tabela atual (2024)
     */
    private BigDecimal calcularIrrf(BigDecimal baseCalculo) {
        if (baseCalculo.compareTo(BigDecimal.valueOf(2112.00)) <= 0) {
            return BigDecimal.ZERO; // Isento
        } else if (baseCalculo.compareTo(BigDecimal.valueOf(2826.65)) <= 0) {
            return baseCalculo.multiply(BigDecimal.valueOf(0.075)).subtract(BigDecimal.valueOf(158.40))
                    .setScale(2, RoundingMode.HALF_UP);
        } else if (baseCalculo.compareTo(BigDecimal.valueOf(3751.05)) <= 0) {
            return baseCalculo.multiply(BigDecimal.valueOf(0.15)).subtract(BigDecimal.valueOf(370.40))
                    .setScale(2, RoundingMode.HALF_UP);
        } else if (baseCalculo.compareTo(BigDecimal.valueOf(4664.68)) <= 0) {
            return baseCalculo.multiply(BigDecimal.valueOf(0.225)).subtract(BigDecimal.valueOf(651.73))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return baseCalculo.multiply(BigDecimal.valueOf(0.275)).subtract(BigDecimal.valueOf(884.96))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcula horas extras do colaborador no mês
     */
    private BigDecimal calcularHorasExtras(Colaborador colaborador, Integer mes, Integer ano) {
        // Por enquanto retorna 0, implementar lógica de ponto depois
        return BigDecimal.ZERO;
    }

    /**
     * Calcula valor da hora extra (salário base / 220 * 1.5)
     */
    private BigDecimal calcularValorHoraExtra(BigDecimal salarioBase) {
        return salarioBase.divide(BigDecimal.valueOf(220), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(1.5))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula dias úteis do mês
     */
    private int calcularDiasUteis(YearMonth yearMonth) {
        // Implementação simplificada - assumir 22 dias úteis por mês
        // Futuramente pode ser melhorada para considerar feriados
        return 22;
    }

    /**
     * Fecha uma folha de pagamento
     */
    @Transactional
    public FolhaPagamento fecharFolha(Long folhaId, Usuario usuario) {
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new IllegalArgumentException("Folha de pagamento não encontrada"));

        if (folha.getStatus() != FolhaPagamento.StatusFolha.PROCESSADA) {
            throw new IllegalStateException("Só é possível fechar folhas que estão processadas");
        }

        folha.setStatus(FolhaPagamento.StatusFolha.FECHADA);
        folha.setDataFechamento(LocalDate.now());
        
        return folhaPagamentoRepository.save(folha);
    }

    /**
     * Cancela uma folha de pagamento
     */
    @Transactional
    public void cancelarFolha(Long folhaId, Usuario usuario) {
        FolhaPagamento folha = folhaPagamentoRepository.findById(folhaId)
                .orElseThrow(() -> new IllegalArgumentException("Folha de pagamento não encontrada"));

        if (folha.getStatus() == FolhaPagamento.StatusFolha.FECHADA) {
            throw new IllegalStateException("Não é possível cancelar folha já fechada");
        }

        folha.setStatus(FolhaPagamento.StatusFolha.CANCELADA);
        folhaPagamentoRepository.save(folha);

        // Excluir holerites associados
        List<Holerite> holerites = holeriteRepository.findByFolhaPagamento(folha);
        holeriteRepository.deleteAll(holerites);

        logger.info("Folha de pagamento {}/{} cancelada por {}", folha.getMesReferencia(), 
                    folha.getAnoReferencia(), usuario.getEmail());
    }

    /**
     * Busca folhas recentes (últimos 2 anos)
     */
    public List<FolhaPagamento> buscarFolhasRecentes() {
        Integer anoInicio = LocalDate.now().getYear() - 2;
        return folhaPagamentoRepository.findFolhasRecentes(anoInicio);
    }

    public void pause(String jobId) {
        // lógica para pausar o job
        System.out.println("Pausando job: " + jobId);
    }

    public void resume(String jobId) {
        // lógica para resumir o job
        System.out.println("Resumindo job: " + jobId);
    }

    public void cancel(String jobId) {
        // lógica para cancelar o job
        System.out.println("Cancelando job: " + jobId);
    }
}
