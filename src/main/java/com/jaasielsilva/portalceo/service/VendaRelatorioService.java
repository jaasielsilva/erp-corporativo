package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.VendaRepository;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendaRelatorioService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    // ===== RELATÓRIOS DE PERFORMANCE DE VENDAS =====
    
    public RelatorioPerformanceVendas gerarRelatorioPerformance(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.atTime(23, 59, 59);
        
        // Vendas do período
        List<Venda> vendas = vendaRepository.findByDataVendaBetween(inicioDateTime, fimDateTime);
        
        // Cálculos básicos
        BigDecimal totalVendas = vendas.stream()
            .map(Venda::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long quantidadeVendas = vendas.size();
        
        BigDecimal ticketMedio = quantidadeVendas > 0 ? 
            totalVendas.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Vendas por dia
        Map<LocalDate, BigDecimal> vendasPorDia = vendas.stream()
            .collect(Collectors.groupingBy(
                v -> v.getDataVenda().toLocalDate(),
                Collectors.mapping(Venda::getTotal, 
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
        
        // Vendas por forma de pagamento
        Map<String, BigDecimal> vendasPorFormaPagamento = vendas.stream()
            .collect(Collectors.groupingBy(
                Venda::getFormaPagamento,
                Collectors.mapping(Venda::getTotal,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
        
        // Top produtos vendidos
        Map<Produto, Long> topProdutos = vendas.stream()
            .flatMap(v -> v.getItens().stream())
            .collect(Collectors.groupingBy(
                VendaItem::getProduto,
                Collectors.summingLong(VendaItem::getQuantidade)
            ));
        
        List<ProdutoVendido> produtosMaisVendidos = topProdutos.entrySet().stream()
            .map(entry -> new ProdutoVendido(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> Long.compare(b.getQuantidade(), a.getQuantidade()))
            .limit(10)
            .collect(Collectors.toList());
        
        // Top clientes
        Map<Cliente, BigDecimal> topClientes = vendas.stream()
            .filter(v -> v.getCliente() != null)
            .collect(Collectors.groupingBy(
                Venda::getCliente,
                Collectors.mapping(Venda::getTotal,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
        
        List<ClienteComprador> clientesMaisCompraram = topClientes.entrySet().stream()
            .map(entry -> new ClienteComprador(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> b.getValorTotal().compareTo(a.getValorTotal()))
            .limit(10)
            .collect(Collectors.toList());
        
        return new RelatorioPerformanceVendas(
            inicio, fim, totalVendas, quantidadeVendas, ticketMedio,
            vendasPorDia, vendasPorFormaPagamento, 
            produtosMaisVendidos, clientesMaisCompraram
        );
    }
    
    // ===== ANÁLISE DE TENDÊNCIAS =====
    
    public AnaliseComparativa gerarAnaliseComparativa(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.atTime(23, 59, 59);
        
        // Período atual
        List<Venda> vendasAtual = vendaRepository.findByDataVendaBetween(inicioDateTime, fimDateTime);
        BigDecimal totalAtual = calcularTotalVendas(vendasAtual);
        long quantidadeAtual = vendasAtual.size();
        
        // Período anterior (mesmo número de dias)
        long dias = fim.toEpochDay() - inicio.toEpochDay() + 1;
        LocalDate inicioAnterior = inicio.minusDays(dias);
        LocalDate fimAnterior = inicio.minusDays(1);
        
        LocalDateTime inicioAnteriorDateTime = inicioAnterior.atStartOfDay();
        LocalDateTime fimAnteriorDateTime = fimAnterior.atTime(23, 59, 59);
        
        List<Venda> vendasAnterior = vendaRepository.findByDataVendaBetween(inicioAnteriorDateTime, fimAnteriorDateTime);
        BigDecimal totalAnterior = calcularTotalVendas(vendasAnterior);
        long quantidadeAnterior = vendasAnterior.size();
        
        // Calcular crescimento
        BigDecimal crescimentoValor = calcularCrescimento(totalAnterior, totalAtual);
        BigDecimal crescimentoQuantidade = calcularCrescimento(
            BigDecimal.valueOf(quantidadeAnterior), 
            BigDecimal.valueOf(quantidadeAtual)
        );
        
        return new AnaliseComparativa(
            totalAtual, quantidadeAtual, totalAnterior, quantidadeAnterior,
            crescimentoValor, crescimentoQuantidade
        );
    }
    
    // ===== ANÁLISE DE SAZONALIDADE =====
    
    public AnaliseSazonalidade gerarAnaliseSazonalidade(int meses) {
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusMonths(meses);
        
        Map<YearMonth, BigDecimal> vendasPorMes = new LinkedHashMap<>();
        Map<YearMonth, Long> quantidadePorMes = new LinkedHashMap<>();
        
        for (int i = 0; i < meses; i++) {
            YearMonth mes = YearMonth.from(fim.minusMonths(meses - 1 - i));
            LocalDate inicioMes = mes.atDay(1);
            LocalDate fimMes = mes.atEndOfMonth();
            
            LocalDateTime inicioDateTime = inicioMes.atStartOfDay();
            LocalDateTime fimDateTime = fimMes.atTime(23, 59, 59);
            
            List<Venda> vendas = vendaRepository.findByDataVendaBetween(inicioDateTime, fimDateTime);
            
            BigDecimal total = calcularTotalVendas(vendas);
            vendasPorMes.put(mes, total);
            quantidadePorMes.put(mes, (long) vendas.size());
        }
        
        // Calcular médias
        BigDecimal valorMedio = vendasPorMes.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP);
        
        Double quantidadeMedia = quantidadePorMes.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        // Identificar picos e vales
        YearMonth mesPico = vendasPorMes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        YearMonth mesVale = vendasPorMes.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        return new AnaliseSazonalidade(
            vendasPorMes, quantidadePorMes, valorMedio, quantidadeMedia, mesPico, mesVale
        );
    }
    
    // ===== ANÁLISE DE PRODUTOS =====
    
    public AnaliseProdutos gerarAnaliseProdutos(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.atTime(23, 59, 59);
        
        List<Venda> vendas = vendaRepository.findByDataVendaBetween(inicioDateTime, fimDateTime);
        
        // Análise por produto
        Map<Produto, ProdutoAnalise> analisePorProduto = new HashMap<>();
        
        for (Venda venda : vendas) {
            for (VendaItem item : venda.getItens()) {
                Produto produto = item.getProduto();
                ProdutoAnalise analise = analisePorProduto.getOrDefault(produto, new ProdutoAnalise(produto));
                
                analise.adicionarVenda(item.getQuantidade(), item.getSubtotal());
                analisePorProduto.put(produto, analise);
            }
        }
        
        List<ProdutoAnalise> produtosOrdenados = analisePorProduto.values().stream()
            .sorted((a, b) -> b.getFaturamento().compareTo(a.getFaturamento()))
            .collect(Collectors.toList());
        
        // Análise ABC
        BigDecimal faturamentoTotal = produtosOrdenados.stream()
            .map(ProdutoAnalise::getFaturamento)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal acumulado = BigDecimal.ZERO;
        for (ProdutoAnalise analise : produtosOrdenados) {
            acumulado = acumulado.add(analise.getFaturamento());
            BigDecimal percentualAcumulado = acumulado.divide(faturamentoTotal, 4, RoundingMode.HALF_UP);
            
            if (percentualAcumulado.compareTo(BigDecimal.valueOf(0.8)) <= 0) {
                analise.setClassificacaoABC("A");
            } else if (percentualAcumulado.compareTo(BigDecimal.valueOf(0.95)) <= 0) {
                analise.setClassificacaoABC("B");
            } else {
                analise.setClassificacaoABC("C");
            }
        }
        
        return new AnaliseProdutos(produtosOrdenados, faturamentoTotal);
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private BigDecimal calcularTotalVendas(List<Venda> vendas) {
        return vendas.stream()
            .map(Venda::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calcularCrescimento(BigDecimal anterior, BigDecimal atual) {
        if (anterior.compareTo(BigDecimal.ZERO) == 0) {
            return atual.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        
        return atual.subtract(anterior)
            .divide(anterior, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    // ===== CLASSES DE DADOS PARA RELATÓRIOS =====
    
    public static class RelatorioPerformanceVendas {
        private LocalDate inicio;
        private LocalDate fim;
        private BigDecimal totalVendas;
        private long quantidadeVendas;
        private BigDecimal ticketMedio;
        private Map<LocalDate, BigDecimal> vendasPorDia;
        private Map<String, BigDecimal> vendasPorFormaPagamento;
        private List<ProdutoVendido> produtosMaisVendidos;
        private List<ClienteComprador> clientesMaisCompraram;
        
        public RelatorioPerformanceVendas(LocalDate inicio, LocalDate fim, BigDecimal totalVendas,
                                        long quantidadeVendas, BigDecimal ticketMedio,
                                        Map<LocalDate, BigDecimal> vendasPorDia,
                                        Map<String, BigDecimal> vendasPorFormaPagamento,
                                        List<ProdutoVendido> produtosMaisVendidos,
                                        List<ClienteComprador> clientesMaisCompraram) {
            this.inicio = inicio;
            this.fim = fim;
            this.totalVendas = totalVendas;
            this.quantidadeVendas = quantidadeVendas;
            this.ticketMedio = ticketMedio;
            this.vendasPorDia = vendasPorDia;
            this.vendasPorFormaPagamento = vendasPorFormaPagamento;
            this.produtosMaisVendidos = produtosMaisVendidos;
            this.clientesMaisCompraram = clientesMaisCompraram;
        }
        
        // Getters
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFim() { return fim; }
        public BigDecimal getTotalVendas() { return totalVendas; }
        public long getQuantidadeVendas() { return quantidadeVendas; }
        public BigDecimal getTicketMedio() { return ticketMedio; }
        public Map<LocalDate, BigDecimal> getVendasPorDia() { return vendasPorDia; }
        public Map<String, BigDecimal> getVendasPorFormaPagamento() { return vendasPorFormaPagamento; }
        public List<ProdutoVendido> getProdutosMaisVendidos() { return produtosMaisVendidos; }
        public List<ClienteComprador> getClientesMaisCompraram() { return clientesMaisCompraram; }
    }
    
    public static class AnaliseComparativa {
        private BigDecimal totalAtual;
        private long quantidadeAtual;
        private BigDecimal totalAnterior;
        private long quantidadeAnterior;
        private BigDecimal crescimentoValor;
        private BigDecimal crescimentoQuantidade;
        
        public AnaliseComparativa(BigDecimal totalAtual, long quantidadeAtual,
                                BigDecimal totalAnterior, long quantidadeAnterior,
                                BigDecimal crescimentoValor, BigDecimal crescimentoQuantidade) {
            this.totalAtual = totalAtual;
            this.quantidadeAtual = quantidadeAtual;
            this.totalAnterior = totalAnterior;
            this.quantidadeAnterior = quantidadeAnterior;
            this.crescimentoValor = crescimentoValor;
            this.crescimentoQuantidade = crescimentoQuantidade;
        }
        
        // Getters
        public BigDecimal getTotalAtual() { return totalAtual; }
        public long getQuantidadeAtual() { return quantidadeAtual; }
        public BigDecimal getTotalAnterior() { return totalAnterior; }
        public long getQuantidadeAnterior() { return quantidadeAnterior; }
        public BigDecimal getCrescimentoValor() { return crescimentoValor; }
        public BigDecimal getCrescimentoQuantidade() { return crescimentoQuantidade; }
    }
    
    public static class AnaliseSazonalidade {
        private Map<YearMonth, BigDecimal> vendasPorMes;
        private Map<YearMonth, Long> quantidadePorMes;
        private BigDecimal valorMedio;
        private Double quantidadeMedia;
        private YearMonth mesPico;
        private YearMonth mesVale;
        
        public AnaliseSazonalidade(Map<YearMonth, BigDecimal> vendasPorMes,
                                 Map<YearMonth, Long> quantidadePorMes,
                                 BigDecimal valorMedio, Double quantidadeMedia,
                                 YearMonth mesPico, YearMonth mesVale) {
            this.vendasPorMes = vendasPorMes;
            this.quantidadePorMes = quantidadePorMes;
            this.valorMedio = valorMedio;
            this.quantidadeMedia = quantidadeMedia;
            this.mesPico = mesPico;
            this.mesVale = mesVale;
        }
        
        // Getters
        public Map<YearMonth, BigDecimal> getVendasPorMes() { return vendasPorMes; }
        public Map<YearMonth, Long> getQuantidadePorMes() { return quantidadePorMes; }
        public BigDecimal getValorMedio() { return valorMedio; }
        public Double getQuantidadeMedia() { return quantidadeMedia; }
        public YearMonth getMesPico() { return mesPico; }
        public YearMonth getMesVale() { return mesVale; }
    }
    
    public static class AnaliseProdutos {
        private List<ProdutoAnalise> produtos;
        private BigDecimal faturamentoTotal;
        
        public AnaliseProdutos(List<ProdutoAnalise> produtos, BigDecimal faturamentoTotal) {
            this.produtos = produtos;
            this.faturamentoTotal = faturamentoTotal;
        }
        
        public List<ProdutoAnalise> getProdutos() { return produtos; }
        public BigDecimal getFaturamentoTotal() { return faturamentoTotal; }
    }
    
    public static class ProdutoVendido {
        private Produto produto;
        private Long quantidade;
        
        public ProdutoVendido(Produto produto, Long quantidade) {
            this.produto = produto;
            this.quantidade = quantidade;
        }
        
        public Produto getProduto() { return produto; }
        public Long getQuantidade() { return quantidade; }
    }
    
    public static class ClienteComprador {
        private Cliente cliente;
        private BigDecimal valorTotal;
        
        public ClienteComprador(Cliente cliente, BigDecimal valorTotal) {
            this.cliente = cliente;
            this.valorTotal = valorTotal;
        }
        
        public Cliente getCliente() { return cliente; }
        public BigDecimal getValorTotal() { return valorTotal; }
    }
    
    public static class ProdutoAnalise {
        private Produto produto;
        private Long quantidadeVendida = 0L;
        private BigDecimal faturamento = BigDecimal.ZERO;
        private Long numeroVendas = 0L;
        private String classificacaoABC;
        
        public ProdutoAnalise(Produto produto) {
            this.produto = produto;
        }
        
        public void adicionarVenda(Integer quantidade, BigDecimal valor) {
            this.quantidadeVendida += quantidade;
            this.faturamento = this.faturamento.add(valor);
            this.numeroVendas++;
        }
        
        // Getters and setters
        public Produto getProduto() { return produto; }
        public Long getQuantidadeVendida() { return quantidadeVendida; }
        public BigDecimal getFaturamento() { return faturamento; }
        public Long getNumeroVendas() { return numeroVendas; }
        public String getClassificacaoABC() { return classificacaoABC; }
        public void setClassificacaoABC(String classificacaoABC) { this.classificacaoABC = classificacaoABC; }
    }
}