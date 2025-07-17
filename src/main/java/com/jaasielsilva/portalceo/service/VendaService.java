package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    public void salvar(Venda venda) {
        vendaRepository.save(venda);
    }

    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    public long contarPorCliente(Long clienteId) {
        return vendaRepository.countByClienteId(clienteId);
    }

    public BigDecimal calcularTotalDeVendas() {
        return vendaRepository.calcularTotalDeVendas()
                .orElse(BigDecimal.ZERO);
    }

    public List<Venda> buscarUltimasVendas() {
        return vendaRepository.findTop10ByOrderByDataVendaDesc();
    }

    public BigDecimal getTotalVendas() {
        return calcularTotalDeVendas();
    }

    public List<Venda> buscarPorCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            return listarTodas();
        }
        return vendaRepository.findByClienteCpfCnpjContainingIgnoreCase(cpfCnpj.trim());
    }

    public String formatarValorTotal(List<Venda> vendas) {
        double total = vendas.stream()
                .mapToDouble(v -> v.getTotal().doubleValue())
                .sum();
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(total);
    }
}
