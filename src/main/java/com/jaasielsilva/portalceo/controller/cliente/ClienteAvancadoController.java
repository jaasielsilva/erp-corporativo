package com.jaasielsilva.portalceo.controller.cliente;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.service.ClienteContratoService;
import com.jaasielsilva.portalceo.service.ClienteInsightService;
import com.jaasielsilva.portalceo.service.ClienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/clientes/avancado")
public class ClienteAvancadoController {

    @org.springframework.beans.factory.annotation.Autowired
    private ClienteService clienteService;

    private final ClienteInsightService clienteInsightService;
    private final ClienteContratoService clienteContratoService;

    public ClienteAvancadoController(
            ClienteInsightService clienteInsightService,
            ClienteContratoService clienteContratoService) {
        this.clienteInsightService = clienteInsightService;
        this.clienteContratoService = clienteContratoService;
    }

    @GetMapping("/busca")
    @PreAuthorize("hasAuthority('MENU_CLIENTES_AVANCADO_BUSCA')")
    public String buscaAvancada(@RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tipoCliente", required = false) String tipoCliente,
            @RequestParam(value = "vip", required = false) Boolean vip,
            @RequestParam(value = "ativo", required = false) Boolean ativo,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("busca", busca);
        filtros.put("status", status);
        filtros.put("tipoCliente", tipoCliente);
        filtros.put("vip", vip);
        filtros.put("ativo", ativo);
        filtros.put("origem", origem);
        filtros.put("pageSize", size);

        model.addAttribute("filtros", filtros);
        model.addAttribute("tiposCliente", List.of("PF", "PJ"));

        return "clientes/avancado/busca";
    }

    @GetMapping("/relatorios")
    @PreAuthorize("hasAuthority('MENU_CLIENTES_AVANCADO_RELATORIOS')")
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

    @GetMapping("/api/busca")
    @ResponseBody
    @PreAuthorize("hasAuthority('MENU_CLIENTES_AVANCADO_BUSCA')")
    public ResponseEntity<Map<String, Object>> buscaAvancadaApi(
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tipoCliente", required = false) String tipoCliente,
            @RequestParam(value = "vip", required = false) Boolean vip,
            @RequestParam(value = "ativo", required = false) Boolean ativo,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Page<Cliente> clientesPage = clienteService.listarAvancado(busca, status, tipoCliente, vip, ativo, origem, page, size);

        List<Map<String, Object>> content = clientesPage.getContent().stream()
                .map(cliente -> {
                    Map<String, Object> mapa = new HashMap<>();
                    mapa.put("id", cliente.getId());
                    mapa.put("nome", cliente.getNome());
                    mapa.put("nomeFantasia", cliente.getNomeFantasia());
                    mapa.put("status", cliente.getStatus());
                    mapa.put("tipoCliente", cliente.getTipoCliente());
                    mapa.put("vip", cliente.getVip());
                    mapa.put("ativo", cliente.getAtivo());
                    mapa.put("origem", cliente.getOrigem());
                    mapa.put("ultimoAcesso", cliente.getUltimoAcesso());
                    return mapa;
                })
                .collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", clientesPage.getNumber());
        resp.put("totalPages", clientesPage.getTotalPages());
        resp.put("totalElements", clientesPage.getTotalElements());
        resp.put("hasPrevious", clientesPage.hasPrevious());
        resp.put("hasNext", clientesPage.hasNext());

        return ResponseEntity.ok(resp);
    }
}

