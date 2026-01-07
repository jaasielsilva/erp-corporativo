package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Pedido;
import com.jaasielsilva.portalceo.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ClienteInsightService {

    @org.springframework.beans.factory.annotation.Autowired
    private ClienteService clienteService;

    private final ClienteContratoService clienteContratoService;
    private final PedidoRepository pedidoRepository;

    public ClienteInsightService(
            ClienteContratoService clienteContratoService,
            PedidoRepository pedidoRepository) {
        this.clienteContratoService = clienteContratoService;
        this.pedidoRepository = pedidoRepository;
    }

    public Map<String, Long> distribuicaoStatusClientes() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        mapa.put("Ativos", clienteService.contarAtivos());
        mapa.put("Inativos", clienteService.contarInativos());
        mapa.put("Pendentes", clienteService.contarPendentes());
        mapa.put("Fidelizados", clienteService.contarFidelizados());
        return mapa;
    }

    public Map<String, Object> overviewContratosClientes() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("totalContratos", clienteContratoService.contarContratosClientes());
        mapa.put("ativos", clienteContratoService.contarContratosClientesAtivos());
        mapa.put("expirados", clienteContratoService.contarContratosClientesExpirados());
        mapa.put("vencendo", clienteContratoService.contarContratosClientesVencendo30Dias());
        mapa.put("valorAtivos", clienteContratoService.somarValorContratosAtivos());
        return mapa;
    }

    public Map<String, Long> pedidosPorStatus() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Pedido.Status status : Pedido.Status.values()) {
            mapa.put(status.name(), pedidoRepository.countByStatus(status));
        }
        return mapa;
    }

    public long contarClientesNovosUltimosDias(int dias) {
        return clienteService.contarNovosPorPeriodo(dias);
    }

    public List<Cliente> clientesRecentes(int limite) {
        return clienteService.listarTodos().stream()
                .sorted(Comparator.comparing(Cliente::getDataCadastro,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    public BigDecimal receitaContratosAtivos() {
        return clienteContratoService.somarValorContratosAtivos();
    }
}

