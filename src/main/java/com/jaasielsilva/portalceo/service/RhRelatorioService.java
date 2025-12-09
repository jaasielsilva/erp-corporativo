package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// Importações das entidades
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.ColaboradorBeneficio;
import com.jaasielsilva.portalceo.model.Holerite;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.HoleriteRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorBeneficioRepository;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;

/**
 * Service para geração de relatórios detalhados do módulo RH
 */
@Service
@Transactional(readOnly = true)
public class RhRelatorioService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;
    
    @Autowired
    private DepartamentoRepository departamentoRepository;
    
    @Autowired
    private CargoRepository cargoRepository;
    
    @Autowired
    private BeneficioRepository beneficioRepository;
    
    @Autowired
    private ProcessoAdesaoRepository processoAdesaoRepository;
    
    @Autowired
    private HoleriteRepository holeriteRepository;
    
    @Autowired
    private ColaboradorBeneficioRepository colaboradorBeneficioRepository;

    @Autowired
    private HistoricoColaboradorRepository historicoColaboradorRepository;

    @Autowired
    private SolicitacaoFeriasRepository solicitacaoFeriasRepository;

    @Autowired
    private AvaliacaoDesempenhoRepository avaliacaoDesempenhoRepository;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;
    @Autowired
    private ColaboradorEscalaRepository colaboradorEscalaRepository;

    private final Map<String, CacheItem<RelatorioTurnoverDetalhado>> turnoverCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // atualiza o cach TTL em 15 em 15 minutos.
    private static final long TURNOVER_CACHE_TTL_MS = java.util.concurrent.TimeUnit.MINUTES.toMillis(15);

    @org.springframework.beans.factory.annotation.Value("${rh.turnover.meta:5}")
    private double defaultTurnoverMeta;

    /**
     * Gera relatório analítico da folha de pagamento
     */
    public RelatorioFolhaAnalitica gerarRelatorioFolhaAnalitica(LocalDate inicio, LocalDate fim, Long departamentoId) {
        List<Colaborador> colaboradores;
        
        if (departamentoId != null) {
            colaboradores = colaboradorRepository.findByDepartamentoIdAndAtivoTrue(departamentoId);
        } else {
            colaboradores = colaboradorRepository.findByAtivoTrue();
        }
        
        List<ItemFolhaAnalitica> itens = colaboradores.stream()
            .map(this::criarItemFolhaAnalitica)
            .sorted(Comparator.comparing(item -> item.getNomeColaborador()))
            .collect(Collectors.toList());
        
        BigDecimal totalSalarios = itens.stream()
            .map(ItemFolhaAnalitica::getSalarioBase)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalDescontos = itens.stream()
            .map(ItemFolhaAnalitica::getTotalDescontos)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalLiquido = totalSalarios.subtract(totalDescontos);
        
        return new RelatorioFolhaAnalitica(
            inicio, fim, itens, totalSalarios, totalDescontos, totalLiquido
        );
    }
    
    /**
     * Gera relatório gerencial resumido
     */
    public RelatorioGerencial gerarRelatorioGerencial(LocalDate inicio, LocalDate fim) {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        List<Departamento> departamentos = departamentoRepository.findAll();
        
        Map<String, ResumoGerencial> resumoPorDepartamento = new HashMap<>();
        
        for (Departamento dept : departamentos) {
            List<Colaborador> colaboradoresDept = colaboradores.stream()
                .filter(c -> c.getDepartamento() != null && c.getDepartamento().getId().equals(dept.getId()))
                .collect(Collectors.toList());
            
            BigDecimal totalSalarios = colaboradoresDept.stream()
                .map(c -> c.getSalario() != null ? c.getSalario() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            resumoPorDepartamento.put(dept.getNome(), new ResumoGerencial(
                dept.getNome(),
                colaboradoresDept.size(),
                totalSalarios,
                calcularMediaSalarial(colaboradoresDept)
            ));
        }
        
        return new RelatorioGerencial(
            inicio, fim, colaboradores.size(), 
            calcularTotalFolha(colaboradores),
            resumoPorDepartamento
        );
    }
    
    /**
     * Gera relatório de obrigações trabalhistas
     */
    public RelatorioObrigacoes gerarRelatorioObrigacoes(LocalDate inicio, LocalDate fim) {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        
        List<ItemObrigacao> itens = colaboradores.stream()
            .map(this::calcularObrigacoesColaborador)
            .collect(Collectors.toList());
        
        BigDecimal totalINSS = itens.stream()
            .map(ItemObrigacao::getInss)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalIRRF = itens.stream()
            .map(ItemObrigacao::getIrrf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalFGTS = itens.stream()
            .map(ItemObrigacao::getFgts)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new RelatorioObrigacoes(
            inicio, fim, itens, totalINSS, totalIRRF, totalFGTS
        );
    }
    
    /**
     * Gera relatório por departamento
     */
    public RelatorioDepartamento gerarRelatorioDepartamento(LocalDate inicio, LocalDate fim, Long departamentoId) {
        Departamento departamento = departamentoRepository.findById(departamentoId)
            .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));
        
        List<Colaborador> colaboradores = colaboradorRepository.findByDepartamentoIdAndAtivoTrue(departamentoId);
        
        Map<String, Integer> colaboradoresPorCargo = colaboradores.stream()
            .filter(c -> c.getCargo() != null)
            .collect(Collectors.groupingBy(
                c -> c.getCargo().getNome(),
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
        
        BigDecimal custoTotal = colaboradores.stream()
            .map(c -> c.getSalario() != null ? c.getSalario() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new RelatorioDepartamento(
            departamento.getNome(),
            colaboradores.size(),
            custoTotal,
            colaboradoresPorCargo,
            calcularMediaSalarial(colaboradores)
        );
    }
    
    /**
     * Gera relatório comparativo mensal
     */
    public RelatorioComparativo gerarRelatorioComparativo(LocalDate inicio, LocalDate fim) {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        
        Map<String, DadosComparativos> dadosPorMes = new LinkedHashMap<>();
        
        LocalDate dataAtual = inicio;
        while (!dataAtual.isAfter(fim)) {
            String chave = dataAtual.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            // Simular dados históricos (em produção, buscar dados reais)
            DadosComparativos dados = new DadosComparativos(
                chave,
                colaboradores.size(),
                calcularTotalFolha(colaboradores),
                calcularNovasContratacoes(dataAtual),
                calcularDesligamentos(dataAtual)
            );
            
            dadosPorMes.put(chave, dados);
            dataAtual = dataAtual.plusMonths(1);
        }
        
        return new RelatorioComparativo(inicio, fim, dadosPorMes);
    }
    
    /**
     * Gera relatório de benefícios
     */
    public RelatorioBeneficios gerarRelatorioBeneficios(LocalDate inicio, LocalDate fim) {
        List<Beneficio> beneficios = beneficioRepository.findAll();
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        
        Map<String, ItemBeneficio> beneficiosPorTipo = new HashMap<>();
        
        for (Beneficio beneficio : beneficios) {
            long colaboradoresComBeneficio = colaboradores.stream()
                .filter(c -> c.getBeneficios() != null && 
                           c.getBeneficios().stream().anyMatch(cb -> cb.getBeneficio().getId().equals(beneficio.getId())))
                .count();
            
            BigDecimal custoTotal = colaboradores.stream()
                .filter(c -> c.getBeneficios() != null)
                .flatMap(c -> c.getBeneficios().stream())
                .filter(cb -> cb.getBeneficio().getId().equals(beneficio.getId()))
                .map(cb -> cb.getValor() != null ? cb.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            beneficiosPorTipo.put(beneficio.getNome(), new ItemBeneficio(
                beneficio.getNome(),
                (int) colaboradoresComBeneficio,
                custoTotal
            ));
        }
        
        BigDecimal custoTotalBeneficios = beneficiosPorTipo.values().stream()
            .map(ItemBeneficio::getCustoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new RelatorioBeneficios(
            inicio, fim, beneficiosPorTipo, custoTotalBeneficios
        );
    }
    
    // Métodos auxiliares
    
    private ItemFolhaAnalitica criarItemFolhaAnalitica(Colaborador colaborador) {
        BigDecimal salarioBase = colaborador.getSalario() != null ? colaborador.getSalario() : BigDecimal.ZERO;
        BigDecimal inss = calcularINSS(salarioBase);
        BigDecimal irrf = calcularIRRF(salarioBase);
        BigDecimal totalDescontos = inss.add(irrf);
        BigDecimal salarioLiquido = salarioBase.subtract(totalDescontos);
        
        return new ItemFolhaAnalitica(
            colaborador.getNome(),
            colaborador.getDepartamento() != null ? colaborador.getDepartamento().getNome() : "N/A",
            colaborador.getCargo() != null ? colaborador.getCargo().getNome() : "N/A",
            salarioBase,
            inss,
            irrf,
            totalDescontos,
            salarioLiquido
        );
    }
    
    private ItemObrigacao calcularObrigacoesColaborador(Colaborador colaborador) {
        BigDecimal salarioBase = colaborador.getSalario() != null ? colaborador.getSalario() : BigDecimal.ZERO;
        
        return new ItemObrigacao(
            colaborador.getNome(),
            salarioBase,
            calcularINSS(salarioBase),
            calcularIRRF(salarioBase),
            calcularFGTS(salarioBase)
        );
    }
    
    private BigDecimal calcularINSS(BigDecimal salario) {
        // Cálculo simplificado do INSS (8% até R$ 1.412,00)
        if (salario.compareTo(new BigDecimal("1412.00")) <= 0) {
            return salario.multiply(new BigDecimal("0.08"));
        }
        return new BigDecimal("112.96"); // Teto simplificado
    }
    
    private BigDecimal calcularIRRF(BigDecimal salario) {
        // Cálculo simplificado do IRRF
        if (salario.compareTo(new BigDecimal("2000.00")) <= 0) {
            return BigDecimal.ZERO;
        }
        return salario.multiply(new BigDecimal("0.075")); // 7.5% simplificado
    }
    
    private BigDecimal calcularFGTS(BigDecimal salario) {
        return salario.multiply(new BigDecimal("0.08")); // 8% do salário
    }
    
    private BigDecimal calcularTotalFolha(List<Colaborador> colaboradores) {
        return colaboradores.stream()
            .map(c -> c.getSalario() != null ? c.getSalario() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calcularMediaSalarial(List<Colaborador> colaboradores) {
        if (colaboradores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = calcularTotalFolha(colaboradores);
        return total.divide(new BigDecimal(colaboradores.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    private int calcularNovasContratacoes(LocalDate mes) {
        // Em produção, buscar dados reais do banco
        return (int) (Math.random() * 5); // Simulação
    }
    
    private int calcularDesligamentos(LocalDate mes) {
        LocalDateTime inicio = mes.atStartOfDay();
        LocalDateTime fim = mes.withDayOfMonth(mes.lengthOfMonth()).atTime(23,59,59);
        return Math.toIntExact(
            historicoColaboradorRepository.countByEventoAndDataRegistroBetween("Desligamento", inicio, fim)
        );
    }

    public RelatorioAdmissoesDemissoes gerarRelatorioAdmissoesDemissoes(LocalDate inicio, LocalDate fim) {
        List<Colaborador> admitidos = colaboradorRepository.findAll().stream()
            .filter(c -> c.getDataAdmissao() != null && !c.getDataAdmissao().isBefore(inicio) && !c.getDataAdmissao().isAfter(fim))
            .sorted(Comparator.comparing(Colaborador::getDataAdmissao))
            .collect(Collectors.toList());

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23,59,59);

        List<HistoricoColaborador> desligamentos = historicoColaboradorRepository
            .findByEventoAndDataRegistroBetween("Demissao", inicioDt, fimDt)
            .stream()
            .sorted(Comparator.comparing(HistoricoColaborador::getDataRegistro))
            .collect(Collectors.toList());

        long headcountInicio = colaboradorRepository.countByAtivoTrue();
        long headcountFim = colaboradorRepository.countByAtivoTrue();
        long totalAdmissoes = admitidos.size();
        long totalDemissoes = desligamentos.size();

        double turnoverRate = calcularTurnover(totalDemissoes, headcountInicio, headcountFim);

        return new RelatorioAdmissoesDemissoes(inicio, fim, totalAdmissoes, totalDemissoes, turnoverRate, admitidos, desligamentos);
    }

    private double calcularTurnover(long desligamentos, long headcountInicio, long headcountFim) {
        double mediaHeadcount = (headcountInicio + headcountFim) / 2.0;
        if (mediaHeadcount <= 0) return 0.0;
        return (desligamentos / mediaHeadcount) * 100.0;
    }

    public RelatorioFeriasBeneficios gerarRelatorioFeriasBeneficios(LocalDate inicio, LocalDate fim) {
        var feriasSolicitadas = solicitacaoFeriasRepository.pesquisar(null, inicio, fim, org.springframework.data.domain.PageRequest.of(0, 10000));
        long qtdSolicitadas = feriasSolicitadas.getTotalElements();

        long qtdAprovadas = feriasSolicitadas.getContent().stream()
            .filter(s -> s.getStatus() == com.jaasielsilva.portalceo.model.SolicitacaoFerias.StatusSolicitacao.APROVADA)
            .count();

        long qtdPendentes = feriasSolicitadas.getContent().stream()
            .filter(s -> s.getStatus() == com.jaasielsilva.portalceo.model.SolicitacaoFerias.StatusSolicitacao.SOLICITADA)
            .count();

        RelatorioBeneficios beneficiosRel = gerarRelatorioBeneficios(inicio, fim);

        return new RelatorioFeriasBeneficios(
            inicio, fim,
            Math.toIntExact(qtdSolicitadas),
            Math.toIntExact(qtdAprovadas),
            Math.toIntExact(qtdPendentes),
            beneficiosRel
        );
    }

    public RelatorioIndicadoresDesempenho gerarRelatorioIndicadores(LocalDate inicio, LocalDate fim) {
        var avaliacoes = avaliacaoDesempenhoRepository.pesquisar(null, inicio, fim, org.springframework.data.domain.PageRequest.of(0, 10000));
        var lista = avaliacoes.getContent();

        long total = avaliacoes.getTotalElements();
        long submetidas = lista.stream().filter(a -> a.getStatus() == com.jaasielsilva.portalceo.model.AvaliacaoDesempenho.StatusAvaliacao.SUBMETIDA).count();
        long aprovadas = lista.stream().filter(a -> a.getStatus() == com.jaasielsilva.portalceo.model.AvaliacaoDesempenho.StatusAvaliacao.APROVADA).count();
        double mediaNotas = lista.stream().filter(a -> a.getNota() != null).mapToDouble(a -> a.getNota()).average().orElse(0.0);

        return new RelatorioIndicadoresDesempenho(inicio, fim, Math.toIntExact(total), Math.toIntExact(submetidas), Math.toIntExact(aprovadas), mediaNotas);
    }

    public RelatorioTurnover gerarRelatorioTurnover(LocalDate inicio, LocalDate fim) {
        LocalDate cursor = inicio.withDayOfMonth(1);
        Map<String, Integer> contrataçõesPorMes = new LinkedHashMap<>();
        Map<String, Integer> desligamentosPorMes = new LinkedHashMap<>();
        Map<String, Double> turnoverPorMes = new LinkedHashMap<>();

        while (!cursor.isAfter(fim)) {
            String chave = cursor.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDate mesInicio = cursor.withDayOfMonth(1);
            LocalDate mesFim = cursor.withDayOfMonth(cursor.lengthOfMonth());

            java.util.List<Object[]> adm = colaboradorRepository.contarAdmissoesPorMes(mesInicio, mesFim, null, null);
            java.util.List<Object[]> des = historicoColaboradorRepository.contarDesligamentosPorMes(mesInicio.atStartOfDay(), mesFim.atTime(23,59,59), null, null, null);
            int hires = adm.stream().map(r -> ((Number) r[2]).intValue()).findFirst().orElse(0);
            int terms = des.stream().map(r -> ((Number) r[2]).intValue()).findFirst().orElse(0);

            long headcountInicio = colaboradorRepository.countByAtivoTrue();
            long headcountFim = headcountInicio; // aproximação: sem headcount diário, usa estático
            double rate = calcularTurnover(terms, headcountInicio, headcountFim);

            contrataçõesPorMes.put(chave, hires);
            desligamentosPorMes.put(chave, terms);
            turnoverPorMes.put(chave, rate);

            cursor = cursor.plusMonths(1);
        }

        return new RelatorioTurnover(inicio, fim, contrataçõesPorMes, desligamentosPorMes, turnoverPorMes);
    }

    public RelatorioTurnoverDetalhado gerarRelatorioTurnoverDetalhado(LocalDate inicio, LocalDate fim,
                                                                      String departamentoNome,
                                                                      String cargoNome,
                                                                      String tipoMovimento,
                                                                      Double metaTurnover,
                                                                      boolean compararPeriodoAnterior) {
        String cacheKey = String.join("|",
                inicio.toString(), fim.toString(),
                departamentoNome != null ? departamentoNome : "",
                cargoNome != null ? cargoNome : "",
                tipoMovimento != null ? tipoMovimento : "",
                metaTurnover != null ? String.valueOf(metaTurnover) : "",
                String.valueOf(compararPeriodoAnterior)
        );
        CacheItem<RelatorioTurnoverDetalhado> cached = turnoverCache.get(cacheKey);
        if (cached != null && cached.expiry >= System.currentTimeMillis()) {
            return cached.value;
        }
        String dep = (departamentoNome != null && !departamentoNome.isBlank()) ? departamentoNome.trim() : null;
        String cargo = (cargoNome != null && !cargoNome.isBlank()) ? cargoNome.trim() : null;
        String tipo = (tipoMovimento != null && !tipoMovimento.isBlank()) ? tipoMovimento.trim() : null;

        LocalDate atualInicio = inicio.withDayOfMonth(1);
        Map<String, Integer> hires = new LinkedHashMap<>();
        Map<String, Integer> terms = new LinkedHashMap<>();
        Map<String, Double> rates = new LinkedHashMap<>();

        LocalDateTime inicioDtPeriodo = inicio.atStartOfDay();
        LocalDateTime fimDtPeriodo = fim.atTime(23,59,59);

        List<Object[]> admPorMes = colaboradorRepository.contarAdmissoesPorMes(inicio, fim, dep, cargo);
        List<Object[]> desPorMes = historicoColaboradorRepository.contarDesligamentosPorMes(inicioDtPeriodo, fimDtPeriodo, dep, cargo, tipo);

        Map<String, Integer> admMap = new HashMap<>();
        for (Object[] row : admPorMes) {
            int ano = ((Number) row[0]).intValue();
            int mes = ((Number) row[1]).intValue();
            int qtd = ((Number) row[2]).intValue();
            String key = String.format("%04d-%02d", ano, mes);
            admMap.put(key, qtd);
        }

        Map<String, Integer> desMap = new HashMap<>();
        for (Object[] row : desPorMes) {
            int ano = ((Number) row[0]).intValue();
            int mes = ((Number) row[1]).intValue();
            int qtd = ((Number) row[2]).intValue();
            String key = String.format("%04d-%02d", ano, mes);
            desMap.put(key, qtd);
        }

        long headcountInicio = colaboradorRepository.countByAtivoTrue();
        long headcountFim = colaboradorRepository.countByAtivoTrue();

        while (!atualInicio.isAfter(fim)) {
            String chave = atualInicio.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            int h = admMap.getOrDefault(chave, 0);
            int t = desMap.getOrDefault(chave, 0);
            double rate = calcularTurnover(t, headcountInicio, headcountFim);
            hires.put(chave, h);
            terms.put(chave, t);
            rates.put(chave, rate);
            atualInicio = atualInicio.plusMonths(1);
        }

        Map<String, Integer> hiresPrev = new LinkedHashMap<>();
        Map<String, Integer> termsPrev = new LinkedHashMap<>();
        Map<String, Double> ratesPrev = new LinkedHashMap<>();

        if (compararPeriodoAnterior) {
            long meses = hires.size();
            LocalDate prevFim = inicio.minusDays(1);
            LocalDate prevInicio = prevFim.minusMonths(meses - 1).withDayOfMonth(1);
            LocalDate cursor = prevInicio;
            while (!cursor.isAfter(prevFim)) {
                String chave = cursor.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                LocalDate current = cursor;
                List<Object[]> admPrev = colaboradorRepository.contarAdmissoesPorMes(current.withDayOfMonth(1), current.withDayOfMonth(current.lengthOfMonth()), dep, cargo);
                List<Object[]> desPrev = historicoColaboradorRepository.contarDesligamentosPorMes(current.atStartOfDay(), current.withDayOfMonth(current.lengthOfMonth()).atTime(23,59,59), dep, cargo, tipo);
                int h = admPrev.stream().map(r -> ((Number) r[2]).intValue()).findFirst().orElse(0);
                int t = desPrev.stream().map(r -> ((Number) r[2]).intValue()).findFirst().orElse(0);
                double rate = calcularTurnover(t, headcountInicio, headcountFim);

                hiresPrev.put(chave, h);
                termsPrev.put(chave, t);
                ratesPrev.put(chave, rate);
                cursor = cursor.plusMonths(1);
            }
        }

        double avgRate = 0.0;
        if (!rates.isEmpty()) {
            double sum = 0.0; int count = 0;
            for (Double v : rates.values()) { if (v != null && !Double.isNaN(v) && Double.isFinite(v)) { sum += v; count++; } }
            avgRate = count > 0 ? (sum / count) : 0.0;
        }
        Map<String, Integer> admDept = new LinkedHashMap<>();
        for (Object[] row : colaboradorRepository.contarAdmissoesPorDepartamento(inicio, fim)) {
            String nome = (String) row[0]; int qtd = ((Number) row[1]).intValue(); admDept.put(nome, qtd);
        }
        Map<String, Integer> admCargo = new LinkedHashMap<>();
        for (Object[] row : colaboradorRepository.contarAdmissoesPorCargo(inicio, fim)) {
            String nome = (String) row[0]; int qtd = ((Number) row[1]).intValue(); admCargo.put(nome, qtd);
        }
        Map<String, Integer> desDept = new LinkedHashMap<>();
        for (Object[] row : historicoColaboradorRepository.contarDesligamentosPorDepartamento(inicioDtPeriodo, fimDtPeriodo)) {
            String nome = (String) row[0]; int qtd = ((Number) row[1]).intValue(); desDept.put(nome, qtd);
        }
        Map<String, Integer> desCargo = new LinkedHashMap<>();
        for (Object[] row : historicoColaboradorRepository.contarDesligamentosPorCargo(inicioDtPeriodo, fimDtPeriodo)) {
            String nome = (String) row[0]; int qtd = ((Number) row[1]).intValue(); desCargo.put(nome, qtd);
        }

        Double metaVal = metaTurnover != null ? metaTurnover : defaultTurnoverMeta;
        RelatorioTurnoverDetalhado rel = new RelatorioTurnoverDetalhado(inicio, fim, hires, terms, rates, hiresPrev, termsPrev, ratesPrev, metaVal);
        rel.contratacoesPorDepartamento = admDept;
        rel.contratacoesPorCargo = admCargo;
        rel.desligamentosPorDepartamento = desDept;
        rel.desligamentosPorCargo = desCargo;
        rel.turnoverMedio = avgRate;
        turnoverCache.put(cacheKey, new CacheItem<>(rel, System.currentTimeMillis() + TURNOVER_CACHE_TTL_MS));
        return rel;
    }

    public OrcamentoFeriasBeneficios gerarOrcamentoFeriasBeneficios(LocalDate inicio, LocalDate fim) {
        Map<String, BigDecimal> custoPorBeneficio = new LinkedHashMap<>();
        Map<String, Integer> feriasPorTrimestre = new LinkedHashMap<>();
        Map<String, BigDecimal> custosPorTrimestre = new LinkedHashMap<>();

        RelatorioBeneficios base = gerarRelatorioBeneficios(inicio, fim);
        base.getBeneficiosPorTipo().forEach((nome, item) -> custoPorBeneficio.put(nome, item.getCustoTotal()));

        var ferias = solicitacaoFeriasRepository.pesquisar(null, inicio, fim, org.springframework.data.domain.PageRequest.of(0, 10000));
        ferias.getContent().forEach(s -> {
            int q = ((s.getPeriodoInicio().getMonthValue()-1)/3)+1;
            String key = s.getPeriodoInicio().getYear()+"-Q"+q;
            feriasPorTrimestre.put(key, feriasPorTrimestre.getOrDefault(key, 0)+1);
            BigDecimal estimativa = BigDecimal.valueOf(0);
            custosPorTrimestre.put(key, custosPorTrimestre.getOrDefault(key, BigDecimal.ZERO).add(estimativa));
        });

        List<String> chaves = new ArrayList<>(feriasPorTrimestre.keySet());
        chaves.sort(Comparator.naturalOrder());
        Map<String, Integer> projFerias = new LinkedHashMap<>();
        Map<String, BigDecimal> projCustos = new LinkedHashMap<>();
        int mediaMov = feriasPorTrimestre.values().stream().mapToInt(Integer::intValue).sum() / Math.max(1, feriasPorTrimestre.size());
        BigDecimal mediaCustos = custosPorTrimestre.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, custosPorTrimestre.size())), 2, BigDecimal.ROUND_HALF_UP);
        for (String k : chaves) {
            projFerias.put(k, mediaMov);
            projCustos.put(k, mediaCustos);
        }

        return new OrcamentoFeriasBeneficios(inicio, fim, feriasPorTrimestre, custosPorTrimestre, projFerias, projCustos, custoPorBeneficio);
    }

    public SerieIndicadores12Meses gerarSerieIndicadores12Meses(LocalDate fim) {
        LocalDate inicio = fim.minusMonths(11).withDayOfMonth(1);
        Map<String, IndicadorMensal> serie = new LinkedHashMap<>();
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fim)) {
            LocalDate mInicio = cursor.withDayOfMonth(1);
            LocalDate mFim = cursor.withDayOfMonth(cursor.lengthOfMonth());
            var r = gerarRelatorioIndicadores(mInicio, mFim);
            String key = cursor.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            serie.put(key, new IndicadorMensal(r.getTotalAvaliacoes(), r.getSubmetidas(), r.getAprovadas(), r.getMediaNotas()));
            cursor = cursor.plusMonths(1);
        }
        return new SerieIndicadores12Meses(inicio, fim, serie, "KPIs por mês (12M)", "AvaliacoesDesempenho, SolicitacoesFerias, Beneficios");
    }

    public RelatorioAbsenteismoDetalhado gerarRelatorioAbsenteismoDetalhado(LocalDate inicio, LocalDate fim,
                                                                            String departamentoNome,
                                                                            String cargoNome,
                                                                            String tipoAusencia,
                                                                            Double metaAbsenteismo,
                                                                            boolean compararPeriodoAnterior) {
        Map<String, Integer> faltasPorMes = new LinkedHashMap<>();
        Map<String, Integer> atrasosPorMes = new LinkedHashMap<>();
        Map<String, Integer> minutosTrabPorMes = new LinkedHashMap<>();
        Map<String, Double> taxaAbsenteismoPorMes = new LinkedHashMap<>();

        List<Colaborador> ativos = colaboradorRepository.findByAtivoTrue();
        if (departamentoNome != null && !departamentoNome.isBlank()) {
            ativos = ativos.stream()
                    .filter(c -> c.getDepartamento() != null && departamentoNome.equalsIgnoreCase(c.getDepartamento().getNome()))
                    .collect(Collectors.toList());
        }
        if (cargoNome != null && !cargoNome.isBlank()) {
            ativos = ativos.stream()
                    .filter(c -> c.getCargo() != null && cargoNome.equalsIgnoreCase(c.getCargo().getNome()))
                    .collect(Collectors.toList());
        }
        final int headcountFiltrado = ativos.size();
        final List<RegistroPonto.StatusPonto> ausenciaStatuses = ausenciaStatusesFor(tipoAusencia);

        List<com.jaasielsilva.portalceo.repository.RegistroPontoRepository.PontoMensalAggregationProjection> agg =
                registroPontoRepository.aggregateMensal(inicio, fim, 
                        departamentoNome != null && !departamentoNome.isBlank() ? departamentoNome : null,
                        cargoNome != null && !cargoNome.isBlank() ? cargoNome : null,
                        ausenciaStatuses);

        for (var row : agg) {
            String key = String.format(java.util.Locale.ROOT, "%04d-%02d", row.getAno(), row.getMes());
            int faltas = row.getFaltas() != null ? row.getFaltas().intValue() : 0;
            int atrasos = row.getAtrasos() != null ? row.getAtrasos().intValue() : 0;
            int minutos = row.getMinutos() != null ? row.getMinutos().intValue() : 0;
            LocalDate mInicio = java.time.LocalDate.of(row.getAno(), row.getMes(), 1);
            LocalDate mFim = mInicio.withDayOfMonth(mInicio.lengthOfMonth());
            int diasUteis = contarDiasUteis(mInicio, mFim);
            double taxa = headcountFiltrado > 0 && diasUteis > 0 ? (double) faltas / (double) (diasUteis * headcountFiltrado) * 100.0 : 0.0;
            faltasPorMes.put(key, faltas);
            atrasosPorMes.put(key, atrasos);
            minutosTrabPorMes.put(key, minutos);
            taxaAbsenteismoPorMes.put(key, taxa);
        }

        Map<String, Integer> faltasPrevPorMes = new LinkedHashMap<>();
        Map<String, Integer> atrasosPrevPorMes = new LinkedHashMap<>();
        Map<String, Integer> minutosPrevPorMes = new LinkedHashMap<>();
        Map<String, Double> taxaPrevPorMes = new LinkedHashMap<>();

        if (compararPeriodoAnterior && !faltasPorMes.isEmpty()) {
            long meses = faltasPorMes.size();
            LocalDate prevFim = inicio.minusDays(1);
            LocalDate prevInicio = prevFim.minusMonths(meses - 1).withDayOfMonth(1);
            List<com.jaasielsilva.portalceo.repository.RegistroPontoRepository.PontoMensalAggregationProjection> prevAgg =
                    registroPontoRepository.aggregateMensal(prevInicio, prevFim, 
                            departamentoNome != null && !departamentoNome.isBlank() ? departamentoNome : null,
                            cargoNome != null && !cargoNome.isBlank() ? cargoNome : null,
                            ausenciaStatuses);
            for (var row : prevAgg) {
                String key = String.format(java.util.Locale.ROOT, "%04d-%02d", row.getAno(), row.getMes());
                int faltas = row.getFaltas() != null ? row.getFaltas().intValue() : 0;
                int atrasos = row.getAtrasos() != null ? row.getAtrasos().intValue() : 0;
                int minutos = row.getMinutos() != null ? row.getMinutos().intValue() : 0;
                LocalDate mInicio = java.time.LocalDate.of(row.getAno(), row.getMes(), 1);
                LocalDate mFim = mInicio.withDayOfMonth(mInicio.lengthOfMonth());
                int diasUteis = contarDiasUteis(mInicio, mFim);
                double taxa = headcountFiltrado > 0 && diasUteis > 0 ? (double) faltas / (double) (diasUteis * headcountFiltrado) * 100.0 : 0.0;
                faltasPrevPorMes.put(key, faltas);
                atrasosPrevPorMes.put(key, atrasos);
                minutosPrevPorMes.put(key, minutos);
                taxaPrevPorMes.put(key, taxa);
            }
        }

        return new RelatorioAbsenteismoDetalhado(inicio, fim, faltasPorMes, atrasosPorMes, minutosTrabPorMes, taxaAbsenteismoPorMes,
                faltasPrevPorMes, atrasosPrevPorMes, minutosPrevPorMes, taxaPrevPorMes, metaAbsenteismo);
    }

    private int contarDiasUteis(LocalDate inicio, LocalDate fim) {
        int count = 0;
        LocalDate d = inicio;
        while (!d.isAfter(fim)) {
            switch (d.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    break;
                default:
                    count++;
            }
            d = d.plusDays(1);
        }
        return count;
    }

    private List<RegistroPonto.StatusPonto> ausenciaStatusesFor(String tipoAusencia) {
        if (tipoAusencia == null || tipoAusencia.isBlank()) {
            return Arrays.asList(RegistroPonto.StatusPonto.FALTA, RegistroPonto.StatusPonto.ATESTADO);
        }
        switch (tipoAusencia.toUpperCase(Locale.ROOT)) {
            case "FALTA":
                return Collections.singletonList(RegistroPonto.StatusPonto.FALTA);
            case "ATESTADO":
                return Collections.singletonList(RegistroPonto.StatusPonto.ATESTADO);
            case "FERIAS":
                return Collections.singletonList(RegistroPonto.StatusPonto.FERIAS);
            default:
                return Arrays.asList(RegistroPonto.StatusPonto.FALTA, RegistroPonto.StatusPonto.ATESTADO);
        }
    }

    public static class RelatorioTurnoverDetalhado {
        private LocalDate inicio;
        private LocalDate fim;
        private Map<String, Integer> contrataçõesPorMes;
        private Map<String, Integer> desligamentosPorMes;
        private Map<String, Double> turnoverPorMes;
        private Map<String, Integer> contrataçõesPrevPorMes;
        private Map<String, Integer> desligamentosPrevPorMes;
        private Map<String, Double> turnoverPrevPorMes;
        private Double metaTurnover;
        private double turnoverMedio;
        private Map<String, Integer> contratacoesPorDepartamento;
        private Map<String, Integer> desligamentosPorDepartamento;
        private Map<String, Integer> contratacoesPorCargo;
        private Map<String, Integer> desligamentosPorCargo;

        public RelatorioTurnoverDetalhado(LocalDate inicio, LocalDate fim,
                                          Map<String, Integer> contrataçõesPorMes,
                                          Map<String, Integer> desligamentosPorMes,
                                          Map<String, Double> turnoverPorMes,
                                          Map<String, Integer> contrataçõesPrevPorMes,
                                          Map<String, Integer> desligamentosPrevPorMes,
                                          Map<String, Double> turnoverPrevPorMes,
                                          Double metaTurnover) {
            this.inicio = inicio;
            this.fim = fim;
            this.contrataçõesPorMes = contrataçõesPorMes;
            this.desligamentosPorMes = desligamentosPorMes;
            this.turnoverPorMes = turnoverPorMes;
            this.contrataçõesPrevPorMes = contrataçõesPrevPorMes;
            this.desligamentosPrevPorMes = desligamentosPrevPorMes;
            this.turnoverPrevPorMes = turnoverPrevPorMes;
            this.metaTurnover = metaTurnover;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public Map<String, Integer> getContrataçõesPorMes() { return contrataçõesPorMes; }
        public Map<String, Integer> getDesligamentosPorMes() { return desligamentosPorMes; }
        public Map<String, Double> getTurnoverPorMes() { return turnoverPorMes; }
        public Map<String, Integer> getContrataçõesPrevPorMes() { return contrataçõesPrevPorMes; }
        public Map<String, Integer> getDesligamentosPrevPorMes() { return desligamentosPrevPorMes; }
        public Map<String, Double> getTurnoverPrevPorMes() { return turnoverPrevPorMes; }
        public Double getMetaTurnover() { return metaTurnover; }
        public double getTurnoverMedio() { return turnoverMedio; }
        public Map<String, Integer> getContratacoesPorDepartamento() { return contratacoesPorDepartamento; }
        public Map<String, Integer> getDesligamentosPorDepartamento() { return desligamentosPorDepartamento; }
        public Map<String, Integer> getContratacoesPorCargo() { return contratacoesPorCargo; }
        public Map<String, Integer> getDesligamentosPorCargo() { return desligamentosPorCargo; }
    }

    public void invalidateTurnoverCache() {
        turnoverCache.clear();
    }

    public static class OrcamentoFeriasBeneficios {
        private LocalDate inicio;
        private LocalDate fim;
        private Map<String, Integer> feriasPorTrimestre;
        private Map<String, BigDecimal> custosPorTrimestre;
        private Map<String, Integer> projFerias;
        private Map<String, BigDecimal> projCustos;
        private Map<String, BigDecimal> custoPorBeneficio;

        public OrcamentoFeriasBeneficios(LocalDate inicio, LocalDate fim,
                                         Map<String, Integer> feriasPorTrimestre,
                                         Map<String, BigDecimal> custosPorTrimestre,
                                         Map<String, Integer> projFerias,
                                         Map<String, BigDecimal> projCustos,
                                         Map<String, BigDecimal> custoPorBeneficio) {
            this.inicio = inicio;
            this.fim = fim;
            this.feriasPorTrimestre = feriasPorTrimestre;
            this.custosPorTrimestre = custosPorTrimestre;
            this.projFerias = projFerias;
            this.projCustos = projCustos;
            this.custoPorBeneficio = custoPorBeneficio;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public Map<String, Integer> getFeriasPorTrimestre() { return feriasPorTrimestre; }
        public Map<String, BigDecimal> getCustosPorTrimestre() { return custosPorTrimestre; }
        public Map<String, Integer> getProjFerias() { return projFerias; }
        public Map<String, BigDecimal> getProjCustos() { return projCustos; }
        public Map<String, BigDecimal> getCustoPorBeneficio() { return custoPorBeneficio; }
    }

    public static class SerieIndicadores12Meses {
        private LocalDate inicio;
        private LocalDate fim;
        private Map<String, IndicadorMensal> serie;
        private String metodologia;
        private String fontes;

        public SerieIndicadores12Meses(LocalDate inicio, LocalDate fim, Map<String, IndicadorMensal> serie, String metodologia, String fontes) {
            this.inicio = inicio;
            this.fim = fim;
            this.serie = serie;
            this.metodologia = metodologia;
            this.fontes = fontes;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public Map<String, IndicadorMensal> getSerie() { return serie; }
        public String getMetodologia() { return metodologia; }
        public String getFontes() { return fontes; }
    }

    public static class IndicadorMensal {
        private int total;
        private int submetidas;
        private int aprovadas;
        private double media;

        public IndicadorMensal(int total, int submetidas, int aprovadas, double media) {
            this.total = total;
            this.submetidas = submetidas;
            this.aprovadas = aprovadas;
            this.media = media;
        }

        public int getTotal() { return total; }
        public int getSubmetidas() { return submetidas; }
        public int getAprovadas() { return aprovadas; }
        public double getMedia() { return media; }
    }

    @org.springframework.cache.annotation.Cacheable(value = "rhHeadcountReport", key = "#inicio.toString() + ':' + #fim.toString() + ':' + #departamentoNome + ':' + #tipoContrato")
    public RelatorioHeadcount gerarRelatorioHeadcount(LocalDate inicio, LocalDate fim,
                                                      String departamentoNome,
                                                      String tipoContrato,
                                                      String periodo) {
        java.util.List<Object[]> ativosMin = colaboradorRepository.findAtivosMinimalForHeadcount(
                departamentoNome != null && !departamentoNome.isBlank() ? departamentoNome : null,
                tipoContrato != null && !tipoContrato.isBlank() ? tipoContrato : null);

        java.util.Map<String, Long> porDepartamento = new java.util.LinkedHashMap<>();
        java.util.Map<String, Long> porCargo = new java.util.LinkedHashMap<>();
        java.util.Map<String, Long> porTurno = new java.util.LinkedHashMap<>();
        java.util.Map<String, Long> sexoDistribuicao = new java.util.LinkedHashMap<>();
        java.util.Map<String, Long> faixaEtariaDistribuicao = new java.util.LinkedHashMap<>();

        for (Object[] r : ativosMin) {
            String dnome = (String) r[3];
            String cnome = (String) r[4];
            com.jaasielsilva.portalceo.model.Colaborador.Sexo sexoEnum = (com.jaasielsilva.portalceo.model.Colaborador.Sexo) r[1];
            java.time.LocalDate nasc = (java.time.LocalDate) r[2];

            porDepartamento.put(dnome != null ? dnome : "Sem departamento", porDepartamento.getOrDefault(dnome != null ? dnome : "Sem departamento", 0L) + 1);
            porCargo.put(cnome != null ? cnome : "Sem cargo", porCargo.getOrDefault(cnome != null ? cnome : "Sem cargo", 0L) + 1);

            String sexo = sexoEnum != null ? sexoEnum.name() : "NAO_INFORMADO";
            sexoDistribuicao.put(sexo, sexoDistribuicao.getOrDefault(sexo, 0L) + 1);
            int idade = nasc != null ? java.time.Period.between(nasc, java.time.LocalDate.now()).getYears() : -1;
            String faixa = faixaEtaria(idade);
            faixaEtariaDistribuicao.put(faixa, faixaEtariaDistribuicao.getOrDefault(faixa, 0L) + 1);
        }

        long totalAtivos = ativosMin.size();
        java.time.LocalDate inicioMes = fim.withDayOfMonth(1);
        java.time.LocalDate fimMes = fim.withDayOfMonth(fim.lengthOfMonth());
        long admMes = contarAdmissoes(inicioMes, fimMes, departamentoNome, null);
        long desMes = contarDesligamentos(inicioMes.atStartOfDay(), fimMes.atTime(23, 59, 59), departamentoNome, null, null);

        double turnover = totalAtivos > 0 ? (double) desMes / (double) totalAtivos * 100.0 : 0.0;
        long netChange = admMes - desMes;
        long projecao = totalAtivos + netChange;

        java.time.LocalDate fimPrevMes = inicioMes.minusDays(1);
        java.time.LocalDate inicioPrevMes = fimPrevMes.withDayOfMonth(1);
        long admPrevMes = contarAdmissoes(inicioPrevMes, fimPrevMes, departamentoNome, null);
        long desPrevMes = contarDesligamentos(inicioPrevMes.atStartOfDay(), fimPrevMes.atTime(23, 59, 59), departamentoNome, null, null);
        long totalPrevMes = totalAtivos - netChange;

        java.time.LocalDate fimPrevAno = fim.minusYears(1);
        java.time.LocalDate inicioPrevAno = inicio.minusYears(1);
        long admPrevAno = contarAdmissoes(inicioPrevAno, fimPrevAno, departamentoNome, null);
        long desPrevAno = contarDesligamentos(inicioPrevAno.atStartOfDay(), fimPrevAno.atTime(23, 59, 59), departamentoNome, null, null);

        java.util.Map<String, Object> charts = new java.util.LinkedHashMap<>();
        charts.put("contratacoesVsDesligamentos", java.util.Map.of(
                "labels", java.util.List.of("Atual", "Mês anterior", "Ano anterior"),
                "admissoes", java.util.List.of(admMes, admPrevMes, admPrevAno),
                "desligamentos", java.util.List.of(desMes, desPrevMes, desPrevAno)
        ));

        return new RelatorioHeadcount(
                inicio, fim, totalAtivos, porDepartamento, porCargo, porTurno,
                turnover, sexoDistribuicao, faixaEtariaDistribuicao,
                projecao,
                java.util.Map.of("mesAnterior", totalPrevMes),
                charts
        );
    }

    private long contarAdmissoes(LocalDate inicio, LocalDate fim, String departamentoNome, String cargoNome) {
        java.util.List<Object[]> rows = colaboradorRepository.contarAdmissoesPorMes(inicio, fim, departamentoNome, cargoNome);
        return rows.stream().mapToLong(r -> ((Number) r[2]).longValue()).sum();
    }

    private long contarDesligamentos(java.time.LocalDateTime inicio, java.time.LocalDateTime fim, String departamentoNome, String cargoNome, String tipoMovimento) {
        java.util.List<Object[]> rows = historicoColaboradorRepository.contarDesligamentosPorMes(inicio, fim, departamentoNome, cargoNome, tipoMovimento);
        return rows.stream().mapToLong(r -> ((Number) r[2]).longValue()).sum();
    }

    private String faixaEtaria(int idade) {
        if (idade < 0) return "NAO_INFORMADO";
        if (idade < 25) return "<25";
        if (idade < 35) return "25-34";
        if (idade < 45) return "35-44";
        if (idade < 55) return "45-54";
        return "55+";
    }

    public static class RelatorioHeadcount {
        private LocalDate inicio;
        private LocalDate fim;
        private long totalAtivos;
        private java.util.Map<String, Long> porDepartamento;
        private java.util.Map<String, Long> porCargo;
        private java.util.Map<String, Long> porTurno;
        private double taxaTurnover;
        private java.util.Map<String, Long> distribuicaoDemograficaSexo;
        private java.util.Map<String, Long> distribuicaoFaixaEtaria;
        private long projecaoCrescimento;
        private java.util.Map<String, Object> comparativos;
        private java.util.Map<String, Object> charts;

        public RelatorioHeadcount(LocalDate inicio, LocalDate fim, long totalAtivos,
                                  java.util.Map<String, Long> porDepartamento,
                                  java.util.Map<String, Long> porCargo,
                                  java.util.Map<String, Long> porTurno,
                                  double taxaTurnover,
                                  java.util.Map<String, Long> distribuicaoDemograficaSexo,
                                  java.util.Map<String, Long> distribuicaoFaixaEtaria,
                                  long projecaoCrescimento,
                                  java.util.Map<String, Object> comparativos,
                                  java.util.Map<String, Object> charts) {
            this.inicio = inicio;
            this.fim = fim;
            this.totalAtivos = totalAtivos;
            this.porDepartamento = porDepartamento;
            this.porCargo = porCargo;
            this.porTurno = porTurno;
            this.taxaTurnover = taxaTurnover;
            this.distribuicaoDemograficaSexo = distribuicaoDemograficaSexo;
            this.distribuicaoFaixaEtaria = distribuicaoFaixaEtaria;
            this.projecaoCrescimento = projecaoCrescimento;
            this.comparativos = comparativos;
            this.charts = charts;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public long getTotalAtivos() { return totalAtivos; }
        public java.util.Map<String, Long> getPorDepartamento() { return porDepartamento; }
        public java.util.Map<String, Long> getPorCargo() { return porCargo; }
        public java.util.Map<String, Long> getPorTurno() { return porTurno; }
        public double getTaxaTurnover() { return taxaTurnover; }
        public java.util.Map<String, Long> getDistribuicaoDemograficaSexo() { return distribuicaoDemograficaSexo; }
        public java.util.Map<String, Long> getDistribuicaoFaixaEtaria() { return distribuicaoFaixaEtaria; }
        public long getProjecaoCrescimento() { return projecaoCrescimento; }
        public java.util.Map<String, Object> getComparativos() { return comparativos; }
        public java.util.Map<String, Object> getCharts() { return charts; }
    }

    public static class RelatorioAbsenteismoDetalhado {
        private LocalDate inicio;
        private LocalDate fim;
        private Map<String, Integer> faltasPorMes;
        private Map<String, Integer> atrasosPorMes;
        private Map<String, Integer> minutosTrabPorMes;
        private Map<String, Double> taxaAbsenteismoPorMes;
        private Map<String, Integer> faltasPrevPorMes;
        private Map<String, Integer> atrasosPrevPorMes;
        private Map<String, Integer> minutosPrevPorMes;
        private Map<String, Double> taxaPrevPorMes;
        private Double metaAbsenteismo;

        public RelatorioAbsenteismoDetalhado(LocalDate inicio, LocalDate fim,
                                             Map<String, Integer> faltasPorMes,
                                             Map<String, Integer> atrasosPorMes,
                                             Map<String, Integer> minutosTrabPorMes,
                                             Map<String, Double> taxaAbsenteismoPorMes,
                                             Map<String, Integer> faltasPrevPorMes,
                                             Map<String, Integer> atrasosPrevPorMes,
                                             Map<String, Integer> minutosPrevPorMes,
                                             Map<String, Double> taxaPrevPorMes,
                                             Double metaAbsenteismo) {
            this.inicio = inicio;
            this.fim = fim;
            this.faltasPorMes = faltasPorMes;
            this.atrasosPorMes = atrasosPorMes;
            this.minutosTrabPorMes = minutosTrabPorMes;
            this.taxaAbsenteismoPorMes = taxaAbsenteismoPorMes;
            this.faltasPrevPorMes = faltasPrevPorMes;
            this.atrasosPrevPorMes = atrasosPrevPorMes;
            this.minutosPrevPorMes = minutosPrevPorMes;
            this.taxaPrevPorMes = taxaPrevPorMes;
            this.metaAbsenteismo = metaAbsenteismo;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public Map<String, Integer> getFaltasPorMes() { return faltasPorMes; }
        public Map<String, Integer> getAtrasosPorMes() { return atrasosPorMes; }
        public Map<String, Integer> getMinutosTrabPorMes() { return minutosTrabPorMes; }
        public Map<String, Double> getTaxaAbsenteismoPorMes() { return taxaAbsenteismoPorMes; }
        public Map<String, Integer> getFaltasPrevPorMes() { return faltasPrevPorMes; }
        public Map<String, Integer> getAtrasosPrevPorMes() { return atrasosPrevPorMes; }
        public Map<String, Integer> getMinutosPrevPorMes() { return minutosPrevPorMes; }
        public Map<String, Double> getTaxaPrevPorMes() { return taxaPrevPorMes; }
        public Double getMetaAbsenteismo() { return metaAbsenteismo; }
    }

    private static class CacheItem<T> {
        T value;
        long expiry;
        CacheItem(T value, long expiry) { this.value = value; this.expiry = expiry; }
    }

    public static class RelatorioAdmissoesDemissoes {
        private LocalDate inicio;
        private LocalDate fim;
        private long totalAdmissoes;
        private long totalDemissoes;
        private double turnoverRate;
        private List<Colaborador> admitidos;
        private List<HistoricoColaborador> desligamentos;

        public RelatorioAdmissoesDemissoes(LocalDate inicio, LocalDate fim, long totalAdmissoes, long totalDemissoes, double turnoverRate,
                                           List<Colaborador> admitidos, List<HistoricoColaborador> desligamentos) {
            this.inicio = inicio;
            this.fim = fim;
            this.totalAdmissoes = totalAdmissoes;
            this.totalDemissoes = totalDemissoes;
            this.turnoverRate = turnoverRate;
            this.admitidos = admitidos;
            this.desligamentos = desligamentos;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public long getTotalAdmissoes() { return totalAdmissoes; }
        public long getTotalDemissoes() { return totalDemissoes; }
        public double getTurnoverRate() { return turnoverRate; }
        public List<Colaborador> getAdmitidos() { return admitidos; }
        public List<HistoricoColaborador> getDesligamentos() { return desligamentos; }
    }

    public static class RelatorioFeriasBeneficios {
        private LocalDate inicio;
        private LocalDate fim;
        private int solicitadas;
        private int aprovadas;
        private int pendentes;
        private RelatorioBeneficios beneficios;

        public RelatorioFeriasBeneficios(LocalDate inicio, LocalDate fim, int solicitadas, int aprovadas, int pendentes, RelatorioBeneficios beneficios) {
            this.inicio = inicio;
            this.fim = fim;
            this.solicitadas = solicitadas;
            this.aprovadas = aprovadas;
            this.pendentes = pendentes;
            this.beneficios = beneficios;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public int getSolicitadas() { return solicitadas; }
        public int getAprovadas() { return aprovadas; }
        public int getPendentes() { return pendentes; }
        public RelatorioBeneficios getBeneficios() { return beneficios; }
    }

    public static class RelatorioIndicadoresDesempenho {
        private LocalDate inicio;
        private LocalDate fim;
        private int totalAvaliacoes;
        private int submetidas;
        private int aprovadas;
        private double mediaNotas;

        public RelatorioIndicadoresDesempenho(LocalDate inicio, LocalDate fim, int totalAvaliacoes, int submetidas, int aprovadas, double mediaNotas) {
            this.inicio = inicio;
            this.fim = fim;
            this.totalAvaliacoes = totalAvaliacoes;
            this.submetidas = submetidas;
            this.aprovadas = aprovadas;
            this.mediaNotas = mediaNotas;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public int getTotalAvaliacoes() { return totalAvaliacoes; }
        public int getSubmetidas() { return submetidas; }
        public int getAprovadas() { return aprovadas; }
        public double getMediaNotas() { return mediaNotas; }
    }

    public static class RelatorioTurnover {
        private LocalDate inicio;
        private LocalDate fim;
        private Map<String, Integer> contrataçõesPorMes;
        private Map<String, Integer> desligamentosPorMes;
        private Map<String, Double> turnoverPorMes;

        public RelatorioTurnover(LocalDate inicio, LocalDate fim, Map<String, Integer> contrataçõesPorMes, Map<String, Integer> desligamentosPorMes, Map<String, Double> turnoverPorMes) {
            this.inicio = inicio;
            this.fim = fim;
            this.contrataçõesPorMes = contrataçõesPorMes;
            this.desligamentosPorMes = desligamentosPorMes;
            this.turnoverPorMes = turnoverPorMes;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public Map<String, Integer> getContrataçõesPorMes() { return contrataçõesPorMes; }
        public Map<String, Integer> getDesligamentosPorMes() { return desligamentosPorMes; }
        public Map<String, Double> getTurnoverPorMes() { return turnoverPorMes; }
    }
    
    // Classes internas para estruturar os dados dos relatórios
    
    public static class RelatorioFolhaAnalitica {
        private LocalDate inicio;
        private LocalDate fim;
        private List<ItemFolhaAnalitica> itens;
        private BigDecimal totalSalarios;
        private BigDecimal totalDescontos;
        private BigDecimal totalLiquido;
        
        public RelatorioFolhaAnalitica(LocalDate inicio, LocalDate fim, List<ItemFolhaAnalitica> itens,
                                     BigDecimal totalSalarios, BigDecimal totalDescontos, BigDecimal totalLiquido) {
            this.inicio = inicio;
            this.fim = fim;
            this.itens = itens;
            this.totalSalarios = totalSalarios;
            this.totalDescontos = totalDescontos;
            this.totalLiquido = totalLiquido;
        }
        
        // Getters
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public List<ItemFolhaAnalitica> getItens() { return itens; }
        public BigDecimal getTotalSalarios() { return totalSalarios; }
        public BigDecimal getTotalDescontos() { return totalDescontos; }
        public BigDecimal getTotalLiquido() { return totalLiquido; }
    }
    
    public static class ItemFolhaAnalitica {
        private String nomeColaborador;
        private String departamento;
        private String cargo;
        private BigDecimal salarioBase;
        private BigDecimal inss;
        private BigDecimal irrf;
        private BigDecimal totalDescontos;
        private BigDecimal salarioLiquido;
        
        public ItemFolhaAnalitica(String nomeColaborador, String departamento, String cargo,
                                BigDecimal salarioBase, BigDecimal inss, BigDecimal irrf,
                                BigDecimal totalDescontos, BigDecimal salarioLiquido) {
            this.nomeColaborador = nomeColaborador;
            this.departamento = departamento;
            this.cargo = cargo;
            this.salarioBase = salarioBase;
            this.inss = inss;
            this.irrf = irrf;
            this.totalDescontos = totalDescontos;
            this.salarioLiquido = salarioLiquido;
        }
        
        // Getters
        public String getNomeColaborador() { return nomeColaborador; }
        public String getDepartamento() { return departamento; }
        public String getCargo() { return cargo; }
        public BigDecimal getSalarioBase() { return salarioBase; }
        public BigDecimal getInss() { return inss; }
        public BigDecimal getIrrf() { return irrf; }
        public BigDecimal getTotalDescontos() { return totalDescontos; }
        public BigDecimal getSalarioLiquido() { return salarioLiquido; }
    }
    
    public static class RelatorioGerencial {
        private LocalDate inicio;
        private LocalDate fim;
        private int totalColaboradores;
        private BigDecimal totalFolha;
        private Map<String, ResumoGerencial> resumoPorDepartamento;
        
        public RelatorioGerencial(LocalDate inicio, LocalDate fim, int totalColaboradores,
                                BigDecimal totalFolha, Map<String, ResumoGerencial> resumoPorDepartamento) {
            this.inicio = inicio;
            this.fim = fim;
            this.totalColaboradores = totalColaboradores;
            this.totalFolha = totalFolha;
            this.resumoPorDepartamento = resumoPorDepartamento;
        }
        
        // Getters
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public int getTotalColaboradores() { return totalColaboradores; }
        public BigDecimal getTotalFolha() { return totalFolha; }
        public Map<String, ResumoGerencial> getResumoPorDepartamento() { return resumoPorDepartamento; }
    }
    
    public static class ResumoGerencial {
        private String departamento;
        private int colaboradores;
        private BigDecimal custoTotal;
        private BigDecimal mediaSalarial;
        
        public ResumoGerencial(String departamento, int colaboradores, BigDecimal custoTotal, BigDecimal mediaSalarial) {
            this.departamento = departamento;
            this.colaboradores = colaboradores;
            this.custoTotal = custoTotal;
            this.mediaSalarial = mediaSalarial;
        }
        
        // Getters
        public String getDepartamento() { return departamento; }
        public int getColaboradores() { return colaboradores; }
        public BigDecimal getCustoTotal() { return custoTotal; }
        public BigDecimal getMediaSalarial() { return mediaSalarial; }
    }
    
    public static class RelatorioObrigacoes {
        private LocalDate inicio;
        private LocalDate fim;
        private List<ItemObrigacao> itens;
        private BigDecimal totalINSS;
        private BigDecimal totalIRRF;
        private BigDecimal totalFGTS;
        
        public RelatorioObrigacoes(LocalDate inicio, LocalDate fim, List<ItemObrigacao> itens,
                                 BigDecimal totalINSS, BigDecimal totalIRRF, BigDecimal totalFGTS) {
            this.inicio = inicio;
            this.fim = fim;
            this.itens = itens;
            this.totalINSS = totalINSS;
            this.totalIRRF = totalIRRF;
            this.totalFGTS = totalFGTS;
        }
        
        // Getters
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public List<ItemObrigacao> getItens() { return itens; }
        public BigDecimal getTotalINSS() { return totalINSS; }
        public BigDecimal getTotalIRRF() { return totalIRRF; }
        public BigDecimal getTotalFGTS() { return totalFGTS; }
    }
    
    public static class ItemObrigacao {
        private String nomeColaborador;
        private BigDecimal salarioBase;
        private BigDecimal inss;
        private BigDecimal irrf;
        private BigDecimal fgts;
        
        public ItemObrigacao(String nomeColaborador, BigDecimal salarioBase,
                           BigDecimal inss, BigDecimal irrf, BigDecimal fgts) {
            this.nomeColaborador = nomeColaborador;
            this.salarioBase = salarioBase;
            this.inss = inss;
            this.irrf = irrf;
            this.fgts = fgts;
        }
        
        // Getters
        public String getNomeColaborador() { return nomeColaborador; }
        public BigDecimal getSalarioBase() { return salarioBase; }
        public BigDecimal getInss() { return inss; }
        public BigDecimal getIrrf() { return irrf; }
        public BigDecimal getFgts() { return fgts; }
    }
    
    public static class RelatorioDepartamento {
        private String nomeDepartamento;
        private int totalColaboradores;
        private BigDecimal custoTotal;
        private Map<String, Integer> colaboradoresPorCargo;
        private BigDecimal mediaSalarial;
        
        public RelatorioDepartamento(String nomeDepartamento, int totalColaboradores,
                                   BigDecimal custoTotal, Map<String, Integer> colaboradoresPorCargo,
                                   BigDecimal mediaSalarial) {
            this.nomeDepartamento = nomeDepartamento;
            this.totalColaboradores = totalColaboradores;
            this.custoTotal = custoTotal;
            this.colaboradoresPorCargo = colaboradoresPorCargo;
            this.mediaSalarial = mediaSalarial;
        }
        
        // Getters
        public String getNomeDepartamento() { return nomeDepartamento; }
        public int getTotalColaboradores() { return totalColaboradores; }
        public BigDecimal getCustoTotal() { return custoTotal; }
        public Map<String, Integer> getColaboradoresPorCargo() { return colaboradoresPorCargo; }
        public BigDecimal getMediaSalarial() { return mediaSalarial; }
    }
    
    public static class RelatorioComparativo {
        private LocalDate inicio;
        private LocalDate fim;
        private Map<String, DadosComparativos> dadosPorMes;
        
        public RelatorioComparativo(LocalDate inicio, LocalDate fim, Map<String, DadosComparativos> dadosPorMes) {
            this.inicio = inicio;
            this.fim = fim;
            this.dadosPorMes = dadosPorMes;
        }
        
        // Getters
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public Map<String, DadosComparativos> getDadosPorMes() { return dadosPorMes; }
    }
    
    public static class DadosComparativos {
        private String mes;
        private int colaboradores;
        private BigDecimal totalFolha;
        private int novasContratacoes;
        private int desligamentos;
        
        public DadosComparativos(String mes, int colaboradores, BigDecimal totalFolha,
                               int novasContratacoes, int desligamentos) {
            this.mes = mes;
            this.colaboradores = colaboradores;
            this.totalFolha = totalFolha;
            this.novasContratacoes = novasContratacoes;
            this.desligamentos = desligamentos;
        }
        
        // Getters
        public String getMes() { return mes; }
        public int getColaboradores() { return colaboradores; }
        public BigDecimal getTotalFolha() { return totalFolha; }
        public int getNovasContratacoes() { return novasContratacoes; }
        public int getDesligamentos() { return desligamentos; }
    }
    
    public static class RelatorioBeneficios {
        private LocalDate inicio;
        private LocalDate fim;
        private Map<String, ItemBeneficio> beneficiosPorTipo;
        private BigDecimal custoTotalBeneficios;
        
        public RelatorioBeneficios(LocalDate inicio, LocalDate fim,
                                 Map<String, ItemBeneficio> beneficiosPorTipo,
                                 BigDecimal custoTotalBeneficios) {
            this.inicio = inicio;
            this.fim = fim;
            this.beneficiosPorTipo = beneficiosPorTipo;
            this.custoTotalBeneficios = custoTotalBeneficios;
        }
        
        // Getters
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public Map<String, ItemBeneficio> getBeneficiosPorTipo() { return beneficiosPorTipo; }
        public BigDecimal getCustoTotalBeneficios() { return custoTotalBeneficios; }
    }
    
    public static class ItemBeneficio {
        private String nomeBeneficio;
        private int colaboradoresAtendidos;
        private BigDecimal custoTotal;
        
        public ItemBeneficio(String nomeBeneficio, int colaboradoresAtendidos, BigDecimal custoTotal) {
            this.nomeBeneficio = nomeBeneficio;
            this.colaboradoresAtendidos = colaboradoresAtendidos;
            this.custoTotal = custoTotal;
        }
        
        // Getters
        public String getNomeBeneficio() { return nomeBeneficio; }
        public int getColaboradoresAtendidos() { return colaboradoresAtendidos; }
        public BigDecimal getCustoTotal() { return custoTotal; }
    }
}
