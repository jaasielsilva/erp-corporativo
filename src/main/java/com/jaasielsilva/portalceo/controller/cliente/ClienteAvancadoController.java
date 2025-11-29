package com.jaasielsilva.portalceo.controller.cliente;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Pedido;
import com.jaasielsilva.portalceo.service.ClienteContratoService;
import com.jaasielsilva.portalceo.service.ClienteInsightService;
import com.jaasielsilva.portalceo.service.ClienteService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/clientes/avancado")
public class ClienteAvancadoController {

    private final ClienteService clienteService;
    private final ClienteInsightService clienteInsightService;
    private final ClienteContratoService clienteContratoService;

    public ClienteAvancadoController(ClienteService clienteService,
            ClienteInsightService clienteInsightService,
            ClienteContratoService clienteContratoService) {
        this.clienteService = clienteService;
        this.clienteInsightService = clienteInsightService;
        this.clienteContratoService = clienteContratoService;
    }

    @GetMapping("/busca")
    public String buscaAvancada(@RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tipoCliente", required = false) String tipoCliente,
            @RequestParam(value = "vip", required = false) Boolean vip,
            @RequestParam(value = "ativo", required = false) Boolean ativo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Page<Cliente> clientesPage = clienteService.listarAvancado(busca, status, tipoCliente, vip, ativo, page, size);

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("busca", busca);
        filtros.put("status", status);
        filtros.put("tipoCliente", tipoCliente);
        filtros.put("vip", vip);
        filtros.put("ativo", ativo);

        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("clientes", clientesPage.getContent());
        model.addAttribute("filtros", filtros);
        model.addAttribute("tiposCliente", List.of("PF", "PJ"));

        return "clientes/avancado/busca";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        Map<String, Long> statusClientes = clienteInsightService.distribuicaoStatusClientes();
        Map<String, Object> contratosClientes = clienteInsightService.overviewContratosClientes();
        Map<String, Long> pedidosPorStatus = clienteInsightService.pedidosPorStatus();

        model.addAttribute("statusClientes", statusClientes);
        model.addAttribute("contratosClientes", contratosClientes);
        model.addAttribute("pedidosPorStatus", pedidosPorStatus);
        model.addAttribute("clientesNovos30", clienteInsightService.contarClientesNovosUltimosDias(30));
        model.addAttribute("clientesRecentes", clienteInsightService.clientesRecentes(6));
        model.addAttribute("totalClientes", clienteService.contarTotal());
        model.addAttribute("valorContratos", clienteContratoService.somarValorContratosAtivos());

        return "clientes/avancado/relatorios";
    }
}

