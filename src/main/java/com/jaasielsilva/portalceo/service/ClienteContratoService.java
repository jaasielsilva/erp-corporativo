package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.StatusContrato;
import com.jaasielsilva.portalceo.model.TipoContrato;
import com.jaasielsilva.portalceo.repository.ContratoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ClienteContratoService {

    private final ContratoRepository contratoRepository;

    public ClienteContratoService(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    public Page<Contrato> listarContratosClientes(String busca,
            TipoContrato tipo,
            StatusContrato status,
            Long clienteId,
            int page,
            int size) {
        int pagina = Math.max(page, 0);
        int tamanho = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataInicio", "id"));
        String termo = (busca != null && !busca.isBlank()) ? busca.trim().toLowerCase() : null;

        TipoContrato tipoFiltro = tipo != null ? tipo : TipoContrato.CLIENTE;

        return contratoRepository.buscarContratosClientes(
                termo,
                clienteId,
                tipoFiltro,
                status,
                pageable);
    }

    public Contrato buscarContratoCliente(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado: " + id));

        if (contrato.getCliente() == null) {
            throw new IllegalStateException("Contrato não está vinculado a um cliente.");
        }
        return contrato;
    }

    public long contarContratosClientes() {
        return contratoRepository.countByClienteIsNotNull();
    }

    public long contarContratosClientesAtivos() {
        return contratoRepository.countByClienteIsNotNullAndStatus(StatusContrato.ATIVO);
    }

    public long contarContratosClientesExpirados() {
        return contratoRepository.countByClienteIsNotNullAndStatus(StatusContrato.EXPIRADO);
    }

    public long contarContratosClientesVencendo30Dias() {
        LocalDate hoje = LocalDate.now();
        return contratoRepository.countContratosClientesVencendoEntre(hoje, hoje.plusDays(30));
    }

    public BigDecimal somarValorContratosAtivos() {
        BigDecimal total = contratoRepository.somarValorContratosClientes(StatusContrato.ATIVO);
        return total != null ? total : BigDecimal.ZERO;
    }
}

