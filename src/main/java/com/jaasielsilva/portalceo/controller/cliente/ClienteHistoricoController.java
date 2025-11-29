package com.jaasielsilva.portalceo.controller.cliente;

import com.jaasielsilva.portalceo.model.Pedido;
import com.jaasielsilva.portalceo.service.ClienteHistoricoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/clientes/historico")
public class ClienteHistoricoController {

    private final ClienteHistoricoService clienteHistoricoService;

    public ClienteHistoricoController(ClienteHistoricoService clienteHistoricoService) {
        this.clienteHistoricoService = clienteHistoricoService;
    }

    @GetMapping("/interacoes")
    public String listarInteracoes(@RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "canal", required = false) String canal,
            Model model) {

        List<ClienteHistoricoService.InteracaoTimeline> interacoes = clienteHistoricoService.listarInteracoes(busca, canal);

        model.addAttribute("canalDistribuicao", clienteHistoricoService.contarInteracoesPorCanal(interacoes));
        model.addAttribute("totalInteracoes", interacoes.size());
        model.addAttribute("interacoes7dias", clienteHistoricoService.contarInteracoesUltimosDias(interacoes, 7));
        model.addAttribute("interacoes30dias", clienteHistoricoService.contarInteracoesUltimosDias(interacoes, 30));
        model.addAttribute("filtros", Map.of("busca", busca, "canal", canal));

        return "clientes/historico/interacoes";
    }

    @GetMapping("/pedidos")
    public String listarPedidos(@RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        Pedido.Status statusFiltro = parseStatus(status);
        Map<String, Long> pedidosPorStatus = clienteHistoricoService.contarPedidosPorStatus().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        String statusMaisRecorrente = pedidosPorStatus.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("busca", busca);
        filtros.put("status", status);

        model.addAttribute("totalPedidos", clienteHistoricoService.listarPedidos(busca, statusFiltro).size());
        model.addAttribute("pedidos7dias", clienteHistoricoService.contarPedidosUltimosDias(7));
        model.addAttribute("valorFaturado", clienteHistoricoService.somarPedidosFaturados());
        model.addAttribute("pedidosPorStatus", pedidosPorStatus);
        model.addAttribute("statusMaisRecorrente", statusMaisRecorrente);
        model.addAttribute("filtros", filtros);

        return "clientes/historico/pedidos";
    }

    @GetMapping("/api/interacoes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarInteracoesApi(
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "canal", required = false) String canal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        var pagina = clienteHistoricoService.listarInteracoesPaginado(busca, canal, page, size);

        List<Map<String, Object>> content = pagina.getContent().stream()
                .map(interacao -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("clienteId", interacao.clienteId());
                    map.put("clienteNome", interacao.clienteNome());
                    map.put("canal", interacao.canal());
                    map.put("data", interacao.data());
                    map.put("resumo", interacao.resumo());
                    map.put("responsavel", interacao.responsavel());
                    return map;
                })
                .toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", pagina.getNumber());
        resp.put("totalPages", pagina.getTotalPages());
        resp.put("totalElements", pagina.getTotalElements());
        resp.put("hasPrevious", pagina.hasPrevious());
        resp.put("hasNext", pagina.hasNext());

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/pedidos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPedidosApi(
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pedido.Status statusFiltro = parseStatus(status);
        var pagina = clienteHistoricoService.listarPedidosPaginado(busca, statusFiltro, page, size);

        List<Map<String, Object>> content = pagina.getContent().stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pedidoId", p.pedidoId());
                    map.put("clienteNome", p.clienteNome());
                    map.put("status", p.status() != null ? p.status().name() : null);
                    map.put("tipoCliente", p.tipoCliente());
                    map.put("total", p.total());
                    map.put("dataCriacao", p.dataCriacao());
                    return map;
                })
                .toList();

        Map<String, Long> pedidosPorStatus = clienteHistoricoService.contarPedidosPorStatus().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        String statusMaisRecorrente = pedidosPorStatus.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");

        Map<String, Object> resp = new HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", pagina.getNumber());
        resp.put("totalPages", pagina.getTotalPages());
        resp.put("totalElements", pagina.getTotalElements());
        resp.put("hasPrevious", pagina.hasPrevious());
        resp.put("hasNext", pagina.hasNext());
        resp.put("pedidos7dias", clienteHistoricoService.contarPedidosUltimosDias(7));
        resp.put("valorFaturado", clienteHistoricoService.somarPedidosFaturados());
        resp.put("pedidosPorStatus", pedidosPorStatus);
        resp.put("statusMaisRecorrente", statusMaisRecorrente);

        return ResponseEntity.ok(resp);
    }

    private Pedido.Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return Pedido.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

