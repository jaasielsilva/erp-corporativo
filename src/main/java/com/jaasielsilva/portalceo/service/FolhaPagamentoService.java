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

    public String iniciarProcessamentoAsync(Integer mes, Integer ano, Usuario usuarioProcessamento) {
        String jobId = UUID.randomUUID().toString();
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("status", "AGENDADO");
        info.put("message", "Processamento agendado");
        processamentoJobs.put(jobId, info);
        appendJobLog(jobId, String.format("Agendado processamento para %02d/%d", mes, ano));
        pushJobStatus(jobId);
        gerarFolhaPagamentoAsync(mes, ano, usuarioProcessamento, jobId);
        return jobId;
    }

    public Map<String, Object> obterStatusProcessamento(String jobId) {
        return processamentoJobs.getOrDefault(jobId, java.util.Map.of("status", "DESCONHECIDO"));
    }

    @Async("taskExecutor")
    public void gerarFolhaPagamentoAsync(Integer mes, Integer ano, Usuario usuarioProcessamento, String jobId) {
        Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
        info.put("status", "PROCESSANDO");
        info.put("message", "Processando folha");
        appendJobLog(jobId, String.format("Iniciando processamento da folha %02d/%d", mes, ano));
        pushJobStatus(jobId);
        currentJobId.set(jobId);
        try {
            FolhaPagamento folha = gerarFolhaPagamento(mes, ano, usuarioProcessamento);
            info.put("status", "CONCLUIDO");
            info.put("folhaId", folha.getId());
            info.put("message", "Folha gerada com sucesso");
            appendJobLog(jobId, String.format("Folha gerada com sucesso (folhaId=%d)", folha.getId()));
            pushJobStatus(jobId);
        } catch (Exception e) {
            info.put("status", "ERRO");
            info.put("message", e.getMessage());
            appendJobLog(jobId, String.format("Erro no processamento: %s", e.getMessage()));
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
        logger.info("Iniciando geração de folha de pagamento para {}/{}", mes, ano);

        entityManager.setFlushMode(FlushModeType.COMMIT);

        // Verificar se já existe folha para este período
        if (existeFolhaPorMesAno(mes, ano)) {
            throw new IllegalStateException("Já existe folha de pagamento para " + mes + "/" + ano);
        }

        // Criar nova folha de pagamento
        FolhaPagamento folha = new FolhaPagamento();
        folha.setMesReferencia(mes);
        folha.setAnoReferencia(ano);
        folha.setUsuarioProcessamento(usuarioProcessamento);
        folha.setDataProcessamento(LocalDate.now());
        folha.setStatus(FolhaPagamento.StatusFolha.EM_PROCESSAMENTO);

        // Inicializar totais
        BigDecimal totalBruto = BigDecimal.ZERO;
        BigDecimal totalDescontos = BigDecimal.ZERO;
        BigDecimal totalInss = BigDecimal.ZERO;
        BigDecimal totalIrrf = BigDecimal.ZERO;
        BigDecimal totalFgts = BigDecimal.ZERO;

        folha.setTotalBruto(totalBruto);
        folha.setTotalDescontos(totalDescontos);
        folha.setTotalLiquido(totalBruto.subtract(totalDescontos));
        folha.setTotalInss(totalInss);
        folha.setTotalIrrf(totalIrrf);
        folha.setTotalFgts(totalFgts);

        // Salvar folha primeiro para obter ID
        folha = folhaPagamentoRepository.save(folha);
        final Long folhaId = folha.getId();

        // Buscar colaboradores ativos em paginação para evitar carga total na memória
        long tInicio = System.nanoTime();
        String jobId = currentJobId.get();
        long totalAtivos = colaboradorRepository.countByAtivoTrue();
        if (jobId != null) {
            Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
            info.put("total", totalAtivos);
            info.put("processed", 0);
            info.put("progressPct", 0);
            info.put("startedAt", java.time.LocalDateTime.now().toString());
            info.put("message", "Calculando holerites e preparando persistência em blocos...");
            pushJobStatus(jobId);
        }

        // Agregar dados de ponto em uma única consulta por período
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

        java.util.function.IntFunction<java.util.concurrent.Callable<PageResult>> pageTaskFactory = (int idx) -> () -> processarPaginaFolha(idx, PAGE_SIZE, CHUNK_SIZE, folhaId, mes, ano, resumoPorColaborador);

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
                    long total = totalAtivos;
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
                        long total = totalAtivos;
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

        // Atualizar totais da folha
        folha.setTotalBruto(totalBruto);
        folha.setTotalDescontos(totalDescontos);
        folha.setTotalLiquido(totalBruto.subtract(totalDescontos));
        folha.setTotalInss(totalInss);
        folha.setTotalIrrf(totalIrrf);
        folha.setTotalFgts(totalFgts);
        folha.setStatus(FolhaPagamento.StatusFolha.PROCESSADA);

        // Não há bloco restante aqui; cada página faz seus próprios flushes

        folha = folhaPagamentoRepository.save(folha);

        long tFim = System.nanoTime();
        logger.info("Folha de pagamento {}/{} gerada com sucesso. {} holerites criados em {} ms.", mes, ano, totalProcessados, (tFim - tInicio) / 1_000_000);
        if (jobId != null) {
            appendJobLog(jobId, String.format("Conclusão: %d holerites em %d ms", totalProcessados, (tFim - tInicio) / 1_000_000));
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
                                            Map<Long, RegistroPontoRepository.PontoResumoPorColaboradorProjection> resumoPorColaborador) {
        entityManager.setFlushMode(FlushModeType.COMMIT);
        PageResult r = new PageResult();
        org.springframework.data.domain.Page<Colaborador> page = colaboradorRepository.findByAtivoTrue(org.springframework.data.domain.PageRequest.of(pageIndex, PAGE_SIZE, org.springframework.data.domain.Sort.by("nome").ascending()));
        List<Colaborador> colaboradores = page.getContent();
        if (colaboradores == null || colaboradores.isEmpty()) return r;

        Map<Long, BeneficiosInfo> beneficiosPorColaborador = carregarBeneficiosEmLote(colaboradores, mes, ano);
        FolhaPagamento folhaRef = new FolhaPagamento();
        folhaRef.setId(folhaId);

        List<Holerite> holeritesAll = colaboradores.parallelStream()
                .map(col -> {
                    RegistroPontoRepository.PontoResumoPorColaboradorProjection resumo = resumoPorColaborador.get(col.getId());
                    BeneficiosInfo beneficios = beneficiosPorColaborador.getOrDefault(col.getId(), new BeneficiosInfo(java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty()));
                    return gerarHoleriteComBeneficios(col, folhaRef, mes, ano, resumo, beneficios);
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
        return gerarHolerite(colaborador, folha, mes, ano, null);
    }

    /**
     * Gera holerite individual com possibilidade de usar resumo de ponto pré-agregado
     */
    private Holerite gerarHolerite(Colaborador colaborador, FolhaPagamento folha, Integer mes, Integer ano,
                                   RegistroPontoRepository.PontoResumoPorColaboradorProjection resumoAgregado) {
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

        // Calcular proventos
        calcularProventos(holerite, colaborador, mes, ano, beneficios);

        // Calcular descontos
        calcularDescontos(holerite, colaborador, mes, ano, beneficios);

        return holerite;
    }

    private Holerite gerarHoleriteComBeneficios(Colaborador colaborador, FolhaPagamento folha, Integer mes, Integer ano,
                                                RegistroPontoRepository.PontoResumoPorColaboradorProjection resumoAgregado,
                                                BeneficiosInfo beneficios) {
        Holerite holerite = new Holerite();
        holerite.setColaborador(colaborador);
        holerite.setFolhaPagamento(folha);
        holerite.setSalarioBase(colaborador.getSalario());

        if (resumoAgregado != null) {
            aplicarResumoPonto(holerite, resumoAgregado);
        } else {
            calcularDadosPonto(holerite, colaborador, mes, ano);
        }

        calcularProventos(holerite, colaborador, mes, ano, beneficios);
        calcularDescontos(holerite, colaborador, mes, ano, beneficios);
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
    private void calcularProventos(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano, BeneficiosInfo beneficios) {
        // Proventos básicos já definidos
        // Adicionar outros proventos conforme necessário
        holerite.setAdicionalNoturno(BigDecimal.ZERO);
        holerite.setAdicionalPericulosidade(BigDecimal.ZERO);
        holerite.setAdicionalInsalubridade(BigDecimal.ZERO);
        holerite.setComissoes(BigDecimal.ZERO);
        holerite.setBonificacoes(BigDecimal.ZERO);

        // Aplicar benefícios carregados
        calcularBeneficios(holerite, beneficios);
    }

    /**
     * Calcula benefícios do colaborador
     */
    private void calcularBeneficios(Holerite holerite, BeneficiosInfo beneficios) {
        // Vale Transporte
        if (beneficios.valeTransporte.isPresent() && beneficios.valeTransporte.get().isAtivo()) {
            holerite.setValeTransporte(beneficios.valeTransporte.get().getValorSubsidioEmpresa());
        } else {
            holerite.setValeTransporte(BigDecimal.ZERO);
        }

        // Vale Refeição
        if (beneficios.valeRefeicao.isPresent() && beneficios.valeRefeicao.get().isAtivo()) {
            holerite.setValeRefeicao(beneficios.valeRefeicao.get().getValorSubsidioEmpresa());
        } else {
            holerite.setValeRefeicao(BigDecimal.ZERO);
        }

        // Auxílio Saúde
        if (beneficios.planoSaude.isPresent()) {
            BigDecimal subsidio = beneficios.planoSaude.get().getValorSubsidioEmpresa();
            holerite.setAuxilioSaude(subsidio);
        } else {
            holerite.setAuxilioSaude(BigDecimal.ZERO);
        }
    }

    /**
     * Calcula descontos do holerite
     */
    private void calcularDescontos(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano, BeneficiosInfo beneficios) {
        BigDecimal salarioBruto = holerite.getSalarioBase().add(holerite.getHorasExtras());

        holerite.setDescontoInss(holeriteCalculoService.calcularInssProgressivo(salarioBruto));
        BigDecimal baseIrrf = salarioBruto.subtract(holerite.getDescontoInss());
        holerite.setDescontoIrrf(holeriteCalculoService.calcularIrrf(baseIrrf, 0));
        holerite.setDescontoFgts(BigDecimal.ZERO);

        // Descontos de benefícios com dados já carregados
        calcularDescontosBeneficios(holerite, colaborador, mes, ano, beneficios);

        holerite.setOutrosDescontos(BigDecimal.ZERO);
    }

    /**
     * Calcula descontos de benefícios
     */
    private void calcularDescontosBeneficios(Holerite holerite, Colaborador colaborador, Integer mes, Integer ano, BeneficiosInfo beneficios) {
        // Desconto Vale Transporte
        if (beneficios.valeTransporte.isPresent() && beneficios.valeTransporte.get().isAtivo()) {
            holerite.setDescontoValeTransporte(beneficios.valeTransporte.get().getValorDesconto());
        } else {
            holerite.setDescontoValeTransporte(BigDecimal.ZERO);
        }

        holerite.setDescontoValeTransporte(
                holeriteCalculoService.limitarDescontoValeTransporte(holerite.getSalarioBase(), holerite.getDescontoValeTransporte())
        );

        // Desconto Vale Refeição
        if (beneficios.valeRefeicao.isPresent() && beneficios.valeRefeicao.get().isAtivo()) {
            holerite.setDescontoValeRefeicao(beneficios.valeRefeicao.get().getValorDesconto());
        } else {
            holerite.setDescontoValeRefeicao(BigDecimal.ZERO);
        }

        // Desconto Plano de Saúde
        if (beneficios.planoSaude.isPresent()) {
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
}
