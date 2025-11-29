package com.jaasielsilva.portalceo.controller.cliente;

import com.jaasielsilva.portalceo.model.Pedido;
import com.jaasielsilva.portalceo.service.ClienteHistoricoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        model.addAttribute("interacoes", interacoes);
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
        List<ClienteHistoricoService.PedidoHistorico> pedidos = clienteHistoricoService.listarPedidos(busca, statusFiltro);

        Map<String, Long> pedidosPorStatus = clienteHistoricoService.contarPedidosPorStatus().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        String statusMaisRecorrente = pedidosPorStatus.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("busca", busca);
        filtros.put("status", status);

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("totalPedidos", pedidos.size());
        model.addAttribute("pedidos7dias", clienteHistoricoService.contarPedidosUltimosDias(7));
        model.addAttribute("valorFaturado", clienteHistoricoService.somarPedidosFaturados());
        model.addAttribute("pedidosPorStatus", pedidosPorStatus);
        model.addAttribute("statusMaisRecorrente", statusMaisRecorrente);
        model.addAttribute("filtros", filtros);

        return "clientes/historico/pedidos";
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

