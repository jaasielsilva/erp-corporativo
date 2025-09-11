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
import java.util.HashMap;
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

    @Autowired
    private CaixaService caixaService;

    @Transactional
    public Venda salvar(Venda venda) {
        // Calcular total se não foi definido
        if (venda.getTotal() == null) {
            venda.setTotal(venda.calcularTotal());
        }

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

        Venda vendaSalva = vendaRepository.save(venda);

        // Registrar venda no caixa se estiver aberto
        try {
            caixaService.registrarVenda(vendaSalva);
        } catch (Exception e) {
            // Log do erro, mas não falha a venda
            System.err.println("Erro ao registrar venda no caixa: " + e.getMessage());
        }

        return vendaSalva;
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
        return vendaRepository.calcularFaturamentoPorPeriodo(dataInicioDateTime, LocalDateTime.now())
                .orElse(BigDecimal.ZERO);
    }

    // Calcula o faturamento dos 12 meses anteriores (para comparação)
    public BigDecimal calcularFaturamento12MesesAnteriores() {
        LocalDate dataInicio = LocalDate.now().minusMonths(24).withDayOfMonth(1);
        LocalDate dataFim = LocalDate.now().minusMonths(12).withDayOfMonth(1).minusDays(1);
        LocalDateTime dataInicioDateTime = dataInicio.atStartOfDay();
        LocalDateTime dataFimDateTime = dataFim.atTime(23, 59, 59);
        return vendaRepository.calcularFaturamentoPorPeriodo(dataInicioDateTime, dataFimDateTime)
                .orElse(BigDecimal.ZERO);
    }

    // Calcula o percentual de crescimento do faturamento dos últimos 12 meses vs 12
    // meses anteriores
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

    // Obtém vendas por categoria com dados reais
    public Map<String, BigDecimal> getVendasPorCategoria() {
        Map<String, BigDecimal> vendasPorCategoria = new LinkedHashMap<>();
        List<Object[]> resultados = vendaRepository.calcularVendasPorCategoria();

        for (Object[] resultado : resultados) {
            String categoria = (String) resultado[0];
            BigDecimal valor = (BigDecimal) resultado[1];
            vendasPorCategoria.put(categoria, valor != null ? valor : BigDecimal.ZERO);
        }

        return vendasPorCategoria;
    }

    // Conta o total de vendas (quantidade de vendas realizadas)
    public long contarTotalVendas() {
        return vendaRepository.count();
    }

    // Calcula performance de vendas baseada no crescimento mensal
    public int calcularPerformanceVendas() {
        String crescimento = calcularCrescimentoVendas();
        // Remove o símbolo % e converte para número
        String numeroStr = crescimento.replace("%", "").replace("+", "");
        try {
            double percentual = Double.parseDouble(numeroStr);
            // Normaliza para uma escala de 0-100, considerando 20% como 100%
            int performance = (int) Math.min(100, Math.max(0, 50 + (percentual * 2.5)));
            return performance;
        } catch (NumberFormatException e) {
            return 75; // Valor padrão se houver erro
        }
    }

    // ===== MÉTODOS ESPECÍFICOS PARA PDV =====

    @Transactional
    public Venda processarVendaPdv(Venda venda) {
        // Validações específicas do PDV
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new IllegalArgumentException("Venda deve ter pelo menos um item");
        }

        if (venda.getFormaPagamento() == null || venda.getFormaPagamento().trim().isEmpty()) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }

        // Verificar se existe caixa aberto
        if (!caixaService.existeCaixaAberto()) {
            throw new IllegalStateException("Não há caixa aberto. Abra o caixa antes de realizar vendas.");
        }

        // Definir dados padrão do PDV
        venda.setDataVenda(LocalDateTime.now());
        venda.setStatus("FINALIZADA");

        // Calcular totais
        BigDecimal subtotal = venda.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        venda.setSubtotal(subtotal);
        venda.setTotal(subtotal.subtract(venda.getDesconto() != null ? venda.getDesconto() : BigDecimal.ZERO));

        // Calcular troco se pagamento em dinheiro
        if ("Dinheiro".equalsIgnoreCase(venda.getFormaPagamento()) && venda.getValorPago() != null) {
            BigDecimal troco = venda.getValorPago().subtract(venda.getTotal());
            venda.setTroco(troco.compareTo(BigDecimal.ZERO) > 0 ? troco : BigDecimal.ZERO);
        }

        return salvar(venda);
    }

    // Buscar vendas do dia atual
    public List<Venda> buscarVendasDoDia() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(23, 59, 59);
        return vendaRepository.findByDataVendaBetween(inicioHoje, fimHoje);
    }

    // Calcular total de vendas do dia
    public BigDecimal calcularTotalVendasDoDia() {
        return buscarVendasDoDia().stream()
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Contar vendas do dia
    public long contarVendasDoDia() {
        return buscarVendasDoDia().size();
    }

    // Buscar vendas recentes
    public List<Venda> buscarVendasRecentes(int limite) {
        return vendaRepository.findTop10ByOrderByDataVendaDesc().stream()
                .limit(limite)
                .collect(java.util.stream.Collectors.toList());
    }

    // Buscar vendas por forma de pagamento
    public List<Venda> buscarPorFormaPagamento(String formaPagamento) {
        return vendaRepository.findByFormaPagamentoIgnoreCase(formaPagamento);
    }

    // Calcular total por forma de pagamento no dia
    public BigDecimal calcularTotalPorFormaPagamentoDia(String formaPagamento) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(23, 59, 59);

        return vendaRepository.findByFormaPagamentoIgnoreCaseAndDataVendaBetween(
                formaPagamento, inicioHoje, fimHoje)
                .stream()
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Relatório resumido de vendas do dia
    public ResumoVendasDia obterResumoVendasDia() {
        List<Venda> vendasDia = buscarVendasDoDia();

        BigDecimal totalDinheiro = calcularTotalPorFormaPagamentoDia("Dinheiro");
        BigDecimal totalPix = calcularTotalPorFormaPagamentoDia("PIX");
        BigDecimal totalCartaoDebito = calcularTotalPorFormaPagamentoDia("Cartão de Débito");
        BigDecimal totalCartaoCredito = calcularTotalPorFormaPagamentoDia("Cartão de Crédito");

        return new ResumoVendasDia(
                vendasDia.size(),
                calcularTotalVendasDoDia(),
                totalDinheiro,
                totalPix,
                totalCartaoDebito,
                totalCartaoCredito);
    }

    // Buscar venda por número da venda
    public Optional<Venda> buscarPorNumeroVenda(String numeroVenda) {
        return vendaRepository.findByNumeroVenda(numeroVenda);
    }

    // Buscar vendas por cliente
    public List<Venda> buscarPorCliente(Long clienteId) {
        return vendaRepository.findAll().stream()
                .filter(venda -> venda.getCliente() != null && venda.getCliente().getId().equals(clienteId))
                .sorted((v1, v2) -> v2.getDataVenda().compareTo(v1.getDataVenda()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Buscar vendas por cliente com filtros
    public List<Venda> buscarPorClienteComFiltros(Long clienteId, LocalDate dataInicio, LocalDate dataFim,
            String status) {
        List<Venda> vendas = buscarPorCliente(clienteId);

        if (dataInicio != null) {
            LocalDateTime inicioDateTime = dataInicio.atStartOfDay();
            vendas = vendas.stream()
                    .filter(venda -> venda.getDataVenda().isAfter(inicioDateTime)
                            || venda.getDataVenda().isEqual(inicioDateTime))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (dataFim != null) {
            LocalDateTime fimDateTime = dataFim.atTime(23, 59, 59);
            vendas = vendas.stream()
                    .filter(venda -> venda.getDataVenda().isBefore(fimDateTime)
                            || venda.getDataVenda().isEqual(fimDateTime))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            vendas = vendas.stream()
                    .filter(venda -> venda.getStatus().equalsIgnoreCase(status))
                    .collect(java.util.stream.Collectors.toList());
        }

        return vendas;
    }

    // Classe para resumo de vendas do dia
    public static class ResumoVendasDia {
        private int quantidadeVendas;
        private BigDecimal totalVendas;
        private BigDecimal totalDinheiro;
        private BigDecimal totalPix;
        private BigDecimal totalCartaoDebito;
        private BigDecimal totalCartaoCredito;

        public ResumoVendasDia(int quantidadeVendas, BigDecimal totalVendas,
                BigDecimal totalDinheiro, BigDecimal totalPix,
                BigDecimal totalCartaoDebito, BigDecimal totalCartaoCredito) {
            this.quantidadeVendas = quantidadeVendas;
            this.totalVendas = totalVendas;
            this.totalDinheiro = totalDinheiro;
            this.totalPix = totalPix;
            this.totalCartaoDebito = totalCartaoDebito;
            this.totalCartaoCredito = totalCartaoCredito;
        }

        // Getters
        public int getQuantidadeVendas() {
            return quantidadeVendas;
        }

        public BigDecimal getTotalVendas() {
            return totalVendas;
        }

        public BigDecimal getTotalDinheiro() {
            return totalDinheiro;
        }

        public BigDecimal getTotalPix() {
            return totalPix;
        }

        public BigDecimal getTotalCartaoDebito() {
            return totalCartaoDebito;
        }

        public BigDecimal getTotalCartaoCredito() {
            return totalCartaoCredito;
        }

        // Propriedades calculadas para compatibilidade com o template
        public BigDecimal getTotalCartao() {
            return (totalCartaoDebito != null ? totalCartaoDebito : BigDecimal.ZERO)
                    .add(totalCartaoCredito != null ? totalCartaoCredito : BigDecimal.ZERO);
        }

        public BigDecimal getTicketMedio() {
            if (quantidadeVendas > 0 && totalVendas != null) {
                return totalVendas.divide(BigDecimal.valueOf(quantidadeVendas), 2, BigDecimal.ROUND_HALF_UP);
            }
            return BigDecimal.ZERO;
        }
    }

    // metodo pra serviro o controller para o grafico de vendas por categoria
    public Map<String, BigDecimal> calcularVendasPorCategoria() {
        Map<String, BigDecimal> vendasPorCategoria = new HashMap<>();

        List<Venda> todasVendas = vendaRepository.findAll(); // ou filtrar por período
        for (Venda venda : todasVendas) {
            for (VendaItem item : venda.getItens()) {
                String categoria = item.getProduto().getCategoria().getNome(); // pega o nome da categoria
                BigDecimal subtotal = item.getSubtotal();
                vendasPorCategoria.put(categoria,
                        vendasPorCategoria.getOrDefault(categoria, BigDecimal.ZERO).add(subtotal));
            }
        }

        return vendasPorCategoria;
    }

}
