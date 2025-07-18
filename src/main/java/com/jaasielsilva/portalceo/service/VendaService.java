package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
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

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ProdutoService produtoService;

    // Salva a venda e atualiza o estoque dos produtos vendidos
    @Transactional
    public void salvar(Venda venda) {
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
        vendaRepository.save(venda);
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
        return vendaRepository.findTop2ByOrderByDataVendaDesc(pageable);
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
}
