package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.repository.VendaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    // Salva uma venda no banco
    public void salvar(Venda venda) {
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

    // Calcula o total de todas as vendas (soma dos valores)
    public BigDecimal calcularTotalDeVendas() {
        return vendaRepository.calcularTotalDeVendas()
                .orElse(BigDecimal.ZERO);
    }

    // Retorna o total de vendas (atalho)
    public BigDecimal getTotalVendas() {
        return calcularTotalDeVendas();
    }

    // Busca vendas por CPF ou CNPJ do cliente (filtro)
    public List<Venda> buscarPorCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            return listarTodas();
        }
        return vendaRepository.findByClienteCpfCnpjContainingIgnoreCase(cpfCnpj.trim());
    }

    // Formata o valor total das vendas para o formato de moeda brasileiro
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

    // busca com controle total via Pageable (útil se quiser paginação de verdade)
    public List<Venda> buscarUltimasVendasPaginadas(Pageable pageable) {
        return vendaRepository.findAllByOrderByDataVendaDesc(pageable);
    }

    // metodo pra criar o grafico das vendas
    public Map<YearMonth, BigDecimal> getVendasUltimosMeses(int meses) {
    // Pega data inicial (5 meses atrás, no primeiro dia do mês) e converte para LocalDateTime início do dia
    LocalDate dataInicial = LocalDate.now().minusMonths(meses - 1).withDayOfMonth(1);
    LocalDateTime dataInicialDateTime = dataInicial.atStartOfDay();

    // Consulta os totais agrupados por ano e mês
    List<Object[]> resultados = vendaRepository.totalVendasPorMesDesde(dataInicialDateTime);

    Map<YearMonth, BigDecimal> vendasPorMes = new LinkedHashMap<>();

    // Inicializa mapa com meses que podem não ter vendas
    for (int i = 0; i < meses; i++) {
        YearMonth ym = YearMonth.from(LocalDate.now().minusMonths(meses - 1 - i));
        vendasPorMes.put(ym, BigDecimal.ZERO);
    }

    // Preenche valores vindos da consulta
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
