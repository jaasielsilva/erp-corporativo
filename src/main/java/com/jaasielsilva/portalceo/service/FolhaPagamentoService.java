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

    @PersistenceContext
    private EntityManager entityManager;

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
        gerarFolhaPagamentoAsync(mes, ano, usuarioProcessamento, jobId);
        return jobId;
    }

    public Map<String, Object> obterStatusProcessamento(String jobId) {
        return processamentoJobs.getOrDefault(jobId, java.util.Map.of("status", "DESCONHECIDO"));
    }

    @Async("taskExecutor")
    @Transactional
    public void gerarFolhaPagamentoAsync(Integer mes, Integer ano, Usuario usuarioProcessamento, String jobId) {
        Map<String, Object> info = processamentoJobs.computeIfAbsent(jobId, k -> new java.util.HashMap<>());
        info.put("status", "PROCESSANDO");
        info.put("message", "Processando folha");
        try {
            FolhaPagamento folha = gerarFolhaPagamento(mes, ano, usuarioProcessamento);
            info.put("status", "CONCLUIDO");
            info.put("folhaId", folha.getId());
            info.put("message", "Folha gerada com sucesso");
        } catch (Exception e) {
            info.put("status", "ERRO");
            info.put("message", e.getMessage());
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

        // Buscar colaboradores ativos
        long tInicio = System.nanoTime();
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();

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
        final int CHUNK_SIZE = 200;
        List<Holerite> holeritesChunk = new ArrayList<>(CHUNK_SIZE);
        int totalProcessados = 0;

        for (Colaborador colaborador : colaboradores) {
            try {
                long tColabInicio = System.nanoTime();
                RegistroPontoRepository.PontoResumoPorColaboradorProjection resumo =
                        resumoPorColaborador.get(colaborador.getId());
                Holerite holerite = gerarHolerite(colaborador, folha, mes, ano, resumo);
                holeritesChunk.add(holerite);
                totalProcessados++;

                // Somar aos totais da folha
                totalBruto = totalBruto.add(holerite.getTotalProventos());
                totalDescontos = totalDescontos.add(holerite.getTotalDescontos());
                totalInss = totalInss.add(holerite.getDescontoInss());
                totalIrrf = totalIrrf.add(holerite.getDescontoIrrf());
                BigDecimal salarioBrutoHolerite = holerite.getSalarioBase().add(holerite.getHorasExtras());
                totalFgts = totalFgts.add(holeriteCalculoService.calcularFgtsPatronal(salarioBrutoHolerite));

                long tColabFim = System.nanoTime();
                if (totalProcessados % 100 == 0) {
                    logger.info("Processados {} holerites em {} ms", totalProcessados, (tColabFim - tInicio) / 1_000_000);
                }

                // Persistir em blocos de 200 para aliviar memória e I/O
                if (holeritesChunk.size() >= CHUNK_SIZE) {
                    holeriteRepository.saveAll(holeritesChunk);
                    holeriteRepository.flush();
                    holeritesChunk.clear();

                    // Limpar contexto para evitar crescimento da persistence context em grandes volumes
                    entityManager.clear();

                    // Reanexar 'folha' para continuar atualizações dos totais/status
                    folha = entityManager.merge(folha);
                }
            } catch (Exception e) {
                logger.error("Erro ao gerar holerite para colaborador {}: {}", colaborador.getNome(), e.getMessage());
                // Continua processando outros colaboradores
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

        // Persistir holerites restantes do último bloco
        if (!holeritesChunk.isEmpty()) {
            holeriteRepository.saveAll(holeritesChunk);
            holeriteRepository.flush();
            holeritesChunk.clear();
        }

        folha = folhaPagamentoRepository.save(folha);

        long tFim = System.nanoTime();
        logger.info("Folha de pagamento {}/{} gerada com sucesso. {} holerites criados em {} ms.", mes, ano, totalProcessados, (tFim - tInicio) / 1_000_000);
        return folha;
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
