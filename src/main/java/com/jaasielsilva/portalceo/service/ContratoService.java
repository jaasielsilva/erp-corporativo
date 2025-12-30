package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.StatusContrato;
import com.jaasielsilva.portalceo.model.TipoContrato;
import com.jaasielsilva.portalceo.repository.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepository;

    public List<Contrato> findAll() {
        return contratoRepository.findAll();
    }

    public Contrato findById(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado: " + id));
    }

    public Contrato save(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    public void deleteById(Long id) {
        contratoRepository.deleteById(id);
    }

    // metodo pra salvar um contrato
    public Contrato salvar(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    public List<Contrato> findByTipo(TipoContrato tipo) {
        return contratoRepository.findByTipo(tipo);
    }

    public long contarTotal() {
        return contratoRepository.count();
    }

    public long contarPorStatus(StatusContrato status) {
        return contratoRepository.countByStatus(status);
    }

    public long contarVencendo30Dias() {
        LocalDate hoje = LocalDate.now();
        return contratoRepository.countByDataFimBetween(hoje, hoje.plusDays(30));
    }

    public BigDecimal somarValorAtivos() {
        BigDecimal total = contratoRepository.somarValorTotalPorStatus(StatusContrato.ATIVO);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Page<Contrato> buscarTodos(String busca, List<TipoContrato> tipos, StatusContrato status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataInicio", "id"));
        return contratoRepository.buscarTodosContratos(
                (busca != null && !busca.isBlank()) ? busca.trim().toLowerCase() : null,
                tipos,
                status,
                pageable);
    }

    public String gerarProximoNumero() {
        int ano = java.time.LocalDate.now().getYear();
        String prefix = "CTR-" + ano + "-";
        List<String> ultimos = contratoRepository.findUltimoNumeroContrato(prefix);
        
        if (ultimos.isEmpty()) {
            return prefix + "0001";
        }
        
        String ultimo = ultimos.get(0);
        try {
            String[] parts = ultimo.split("-");
            // Esperado: CTR, YYYY, SEQUENCIA
            if (parts.length >= 3) {
                int sequencia = Integer.parseInt(parts[parts.length - 1]);
                return prefix + String.format("%04d", sequencia + 1);
            }
        } catch (Exception e) {
            // Fallback se o formato estiver estranho
        }
        
        return prefix + System.currentTimeMillis(); // Fallback seguro
    }
}
