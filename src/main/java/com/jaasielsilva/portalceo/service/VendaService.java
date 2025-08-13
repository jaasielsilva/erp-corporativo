package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;
import com.jaasielsilva.portalceo.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;
    
    @Autowired
    private ProdutoService produtoService;

    @Transactional
    public Venda salvar(Venda venda) {
        for (VendaItem item : venda.getItens()) {
            item.setVenda(venda);
            Produto produto = item.getProduto();

            int novaQuantidade = produto.getEstoque() - item.getQuantidade();
            if (novaQuantidade < 0) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            produto.setEstoque(novaQuantidade);
            produtoService.salvar(produto); // atualiza o estoque no banco
        }
        return vendaRepository.save(venda);
    }

    // Lista todas as vendas
    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    // Conta quantas vendas um determinado cliente fez
    public long contarPorCliente(Long clienteId) {
        return vendaRepository.countByClienteId(clienteId);
    }

    // Calcula o total de todas as vendas
    public BigDecimal calcularTotalDeVendas() {
        return vendaRepository.calcularTotalDeVendas().orElse(BigDecimal.ZERO);
    }

    // Retorna o total de vendas
    public BigDecimal getTotalVendas() {
        return calcularTotalDeVendas();
    }

    // Busca vendas por CPF ou CNPJ
    public List<Venda> buscarPorCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            return listarTodas();
        }
        return vendaRepository.findByClienteCpfCnpjContainingIgnoreCase(cpfCnpj.trim());
    }

    // Formata o valor total das vendas para moeda brasileira
    public String formatarValorTotal(List<Venda> vendas) {
        double total = vendas.stream()
                .mapToDouble(v -> v.getTotal().doubleValue())
                .sum();
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(total);
    }

    // Busca as últimas X vendas com paginação
    public List<Venda> buscarUltimasVendas(int quantidade) {
    Pageable pageable = PageRequest.of(0, quantidade);
    return vendaRepository.findAllByOrderByDataVendaDesc(pageable);
}


    // Paginação completa de vendas
    public List<Venda> buscarUltimasVendasPaginadas(Pageable pageable) {
        return vendaRepository.findAllByOrderByDataVendaDesc(pageable);
    }

    // Retorna vendas por mês (últimos X meses) para gráfico
    public Map<YearMonth, BigDecimal> getVendasUltimosMeses(int meses) {
        LocalDate dataInicial = LocalDate.now().minusMonths(meses - 1).withDayOfMonth(1);
        LocalDateTime dataInicialDateTime = dataInicial.atStartOfDay();

        List<Object[]> resultados = vendaRepository.totalVendasPorMesDesde(dataInicialDateTime);
        Map<YearMonth, BigDecimal> vendasPorMes = new LinkedHashMap<>();

        // Inicializa com zero para todos os meses
        for (int i = 0; i < meses; i++) {
            YearMonth ym = YearMonth.from(LocalDate.now().minusMonths(meses - 1 - i));
            vendasPorMes.put(ym, BigDecimal.ZERO);
        }

        // Preenche os meses com os dados reais
        for (Object[] row : resultados) {
            Integer ano = (Integer) row[0];
            Integer mes = (Integer) row[1];
            BigDecimal total = (BigDecimal) row[2];

            YearMonth ym = YearMonth.of(ano, mes);
            vendasPorMes.put(ym, total);
        }

        return vendasPorMes;
    }

    // Método para buscar uma Venda por ID
    public Optional<Venda> buscarPorId(Long id) {
        return vendaRepository.findById(id);
    }

    // Calcula o número de vendas do mês atual
    public long contarVendasMesAtual() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDateTime inicioMesDateTime = inicioMes.atStartOfDay();
        return vendaRepository.countByDataVendaGreaterThanEqual(inicioMesDateTime);
    }

    // Calcula o número de vendas do mês anterior
    public long contarVendasMesAnterior() {
        LocalDate inicioMesAnterior = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fimMesAnterior = LocalDate.now().withDayOfMonth(1).minusDays(1);
        LocalDateTime inicioMesAnteriorDateTime = inicioMesAnterior.atStartOfDay();
        LocalDateTime fimMesAnteriorDateTime = fimMesAnterior.atTime(23, 59, 59);
        return vendaRepository.countByDataVendaBetween(inicioMesAnteriorDateTime, fimMesAnteriorDateTime);
    }

    // Calcula o percentual de crescimento de vendas vs mês anterior
    public String calcularCrescimentoVendas() {
        long vendasMesAtual = contarVendasMesAtual();
        long vendasMesAnterior = contarVendasMesAnterior();
        
        if (vendasMesAnterior == 0) {
            return vendasMesAtual > 0 ? "+100%" : "0%";
        }
        
        double crescimento = ((double) (vendasMesAtual - vendasMesAnterior) / vendasMesAnterior) * 100;
        return String.format("%+.1f%%", crescimento);
    }

    // Calcula o faturamento dos últimos 12 meses
    public BigDecimal calcularFaturamentoUltimos12Meses() {
        LocalDate dataInicio = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDateTime dataInicioDateTime = dataInicio.atStartOfDay();
        return vendaRepository.calcularFaturamentoPorPeriodo(dataInicioDateTime, LocalDateTime.now()).orElse(BigDecimal.ZERO);
    }

    // Calcula o faturamento dos 12 meses anteriores (para comparação)
    public BigDecimal calcularFaturamento12MesesAnteriores() {
        LocalDate dataInicio = LocalDate.now().minusMonths(24).withDayOfMonth(1);
        LocalDate dataFim = LocalDate.now().minusMonths(12).withDayOfMonth(1).minusDays(1);
        LocalDateTime dataInicioDateTime = dataInicio.atStartOfDay();
        LocalDateTime dataFimDateTime = dataFim.atTime(23, 59, 59);
        return vendaRepository.calcularFaturamentoPorPeriodo(dataInicioDateTime, dataFimDateTime).orElse(BigDecimal.ZERO);
    }

    // Calcula o percentual de crescimento do faturamento dos últimos 12 meses vs 12 meses anteriores
    public String calcularCrescimentoFaturamento() {
        BigDecimal faturamentoUltimos12Meses = calcularFaturamentoUltimos12Meses();
        BigDecimal faturamento12MesesAnteriores = calcularFaturamento12MesesAnteriores();
        
        if (faturamento12MesesAnteriores.compareTo(BigDecimal.ZERO) == 0) {
            return faturamentoUltimos12Meses.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        
        BigDecimal crescimento = faturamentoUltimos12Meses.subtract(faturamento12MesesAnteriores)
                .divide(faturamento12MesesAnteriores, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return String.format("%+.1f%%", crescimento.doubleValue());
    }

    
}
