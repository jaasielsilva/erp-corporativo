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
        // Em produção, buscar dados reais do banco
        return (int) (Math.random() * 3); // Simulação
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