package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Pedido;
import com.jaasielsilva.portalceo.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ClienteHistoricoService {

    private final ClienteService clienteService;
    private final PedidoRepository pedidoRepository;

    public ClienteHistoricoService(ClienteService clienteService, PedidoRepository pedidoRepository) {
        this.clienteService = clienteService;
        this.pedidoRepository = pedidoRepository;
    }

    public List<InteracaoTimeline> listarInteracoes(String busca, String canal) {
        List<Cliente> clientes = clienteService.buscarTodos();
        List<InteracaoTimeline> timeline = new ArrayList<>();

        for (Cliente cliente : clientes) {
            String displayName = resolveNomeCliente(cliente);
            if (cliente.getDataCriacao() != null) {
                timeline.add(new InteracaoTimeline(
                        cliente.getId(),
                        displayName,
                        "CADASTRO",
                        cliente.getDataCriacao(),
                        "Cliente cadastrado no portal",
                        cliente.getEditadoPor() != null ? cliente.getEditadoPor().getNome() : "Sistema"));
            }

            if (cliente.getDataUltimaEdicao() != null) {
                timeline.add(new InteracaoTimeline(
                        cliente.getId(),
                        displayName,
                        "ATUALIZACAO",
                        cliente.getDataUltimaEdicao(),
                        "Dados atualizados (" + (cliente.getStatus() != null ? cliente.getStatus() : "Status não informado") + ")",
                        cliente.getEditadoPor() != null ? cliente.getEditadoPor().getNome() : "Equipe Comercial"));
            }

            if (cliente.getUltimoAcesso() != null) {
                timeline.add(new InteracaoTimeline(
                        cliente.getId(),
                        displayName,
                        "PORTAL",
                        cliente.getUltimoAcesso(),
                        "Acesso ao portal do cliente",
                        displayName));
            }

            if (cliente.getObservacoes() != null && !cliente.getObservacoes().isBlank() && cliente.getDataUltimaEdicao() != null) {
                timeline.add(new InteracaoTimeline(
                        cliente.getId(),
                        displayName,
                        "ANOTACAO",
                        cliente.getDataUltimaEdicao(),
                        cliente.getObservacoes(),
                        cliente.getEditadoPor() != null ? cliente.getEditadoPor().getNome() : "Equipe Comercial"));
            }
        }

        List<InteracaoTimeline> filtrado = timeline.stream()
                .filter(i -> {
                    if (busca == null || busca.isBlank()) {
                        return true;
                    }
                    String termo = busca.toLowerCase(Locale.ROOT);
                    return i.clienteNome.toLowerCase(Locale.ROOT).contains(termo)
                            || (i.resumo != null && i.resumo.toLowerCase(Locale.ROOT).contains(termo))
                            || (i.responsavel != null && i.responsavel.toLowerCase(Locale.ROOT).contains(termo));
                })
                .filter(i -> canal == null || canal.isBlank() || i.canal.equalsIgnoreCase(canal))
                .sorted(Comparator.comparing(InteracaoTimeline::data).reversed())
                .limit(300)
                .toList();

        return filtrado;
    }

    public Page<InteracaoTimeline> listarInteracoesPaginado(String busca, String canal, int page, int size) {
        List<InteracaoTimeline> filtrado = listarInteracoes(busca, canal);
        return paginar(filtrado, page, size);
    }

    public Map<String, Long> contarInteracoesPorCanal(List<InteracaoTimeline> interacoes) {
        return interacoes.stream()
                .collect(Collectors.groupingBy(InteracaoTimeline::canal, Collectors.counting()));
    }

    public long contarInteracoesUltimosDias(List<InteracaoTimeline> interacoes, int dias) {
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);
        return interacoes.stream()
                .filter(i -> i.data().isAfter(limite))
                .count();
    }

    public List<PedidoHistorico> listarPedidos(String busca, Pedido.Status status) {
        List<Pedido> pedidos = status != null
                ? pedidoRepository.findByStatusOrderByDataCriacaoDesc(status)
                : pedidoRepository.findAllByOrderByDataCriacaoDesc();

        return pedidos.stream()
                .filter(p -> {
                    if (busca == null || busca.isBlank()) {
                        return true;
                    }
                    String termo = busca.toLowerCase(Locale.ROOT);
                    String clienteNome = resolveNomeCliente(p.getCliente()).toLowerCase(Locale.ROOT);
                    return clienteNome.contains(termo)
                            || String.valueOf(p.getId()).contains(termo);
                })
                .map(p -> new PedidoHistorico(
                        p.getId(),
                        resolveNomeCliente(p.getCliente()),
                        p.getStatus(),
                        p.getTotal(),
                        p.getDataCriacao(),
                        p.getCliente() != null ? p.getCliente().getTipoCliente() : "-"))
                .limit(200)
                .toList();
    }

    public Page<PedidoHistorico> listarPedidosPaginado(String busca, Pedido.Status status, int page, int size) {
        List<PedidoHistorico> filtrado = listarPedidos(busca, status);
        return paginar(filtrado, page, size);
    }

    public Map<Pedido.Status, Long> contarPedidosPorStatus() {
        Map<Pedido.Status, Long> mapa = new EnumMap<>(Pedido.Status.class);
        for (Pedido.Status status : Pedido.Status.values()) {
            mapa.put(status, pedidoRepository.countByStatus(status));
        }
        return mapa;
    }

    public long contarPedidosUltimosDias(int dias) {
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);
        return pedidoRepository.findAllByOrderByDataCriacaoDesc().stream()
                .filter(p -> p.getDataCriacao() != null && p.getDataCriacao().isAfter(limite))
                .count();
    }

    public BigDecimal somarPedidosFaturados() {
        return pedidoRepository.findByStatusOrderByDataCriacaoDesc(Pedido.Status.FATURADO).stream()
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <T> Page<T> paginar(List<T> dados, int page, int size) {
        int pagina = Math.max(page, 0);
        int tamanho = Math.min(Math.max(size, 1), 100);
        int start = Math.min(pagina * tamanho, dados.size());
        int end = Math.min(start + tamanho, dados.size());
        List<T> subLista = dados.subList(start, end);
        return new PageImpl<>(subLista, PageRequest.of(pagina, tamanho), dados.size());
    }

    private String resolveNomeCliente(Cliente cliente) {
        if (cliente == null) {
            return "Cliente não identificado";
        }
        if (cliente.getNomeFantasia() != null && !cliente.getNomeFantasia().isBlank()) {
            return cliente.getNomeFantasia();
        }
        if (cliente.getNome() != null && !cliente.getNome().isBlank()) {
            return cliente.getNome();
        }
        return "Cliente #" + cliente.getId();
    }

    public record InteracaoTimeline(Long clienteId,
            String clienteNome,
            String canal,
            LocalDateTime data,
            String resumo,
            String responsavel) {
    }

    public record PedidoHistorico(Long pedidoId,
            String clienteNome,
            Pedido.Status status,
            BigDecimal total,
            LocalDateTime dataCriacao,
            String tipoCliente) {
    }
}

