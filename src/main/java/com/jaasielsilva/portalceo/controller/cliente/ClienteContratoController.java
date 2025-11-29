package com.jaasielsilva.portalceo.controller.cliente;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.StatusContrato;
import com.jaasielsilva.portalceo.model.TipoContrato;
import com.jaasielsilva.portalceo.service.ClienteContratoService;
import com.jaasielsilva.portalceo.service.ClienteService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/clientes/contratos")
public class ClienteContratoController {

    private final ClienteContratoService clienteContratoService;
    private final ClienteService clienteService;

    public ClienteContratoController(ClienteContratoService clienteContratoService,
            ClienteService clienteService) {
        this.clienteContratoService = clienteContratoService;
        this.clienteService = clienteService;
    }

    @GetMapping({ "", "/listar" })
    public String listarContratosClientes(@RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "clienteId", required = false) Long clienteId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        StatusContrato statusFiltro = parseStatus(status);
        TipoContrato tipoFiltro = parseTipo(tipo);

        Page<Contrato> contratosPage = clienteContratoService.listarContratosClientes(
                busca,
                tipoFiltro,
                statusFiltro,
                clienteId,
                page,
                size);

        model.addAttribute("contratosPage", contratosPage);
        model.addAttribute("contratos", contratosPage.getContent());
        model.addAttribute("clientes", clienteService.listarClientesAtivos());
        model.addAttribute("statusContrato", StatusContrato.values());
        model.addAttribute("tiposContrato", List.of(TipoContrato.CLIENTE));

        Map<String, Object> filtrosAtuais = new HashMap<>();
        filtrosAtuais.put("busca", busca);
        filtrosAtuais.put("status", status);
        filtrosAtuais.put("tipo", tipoFiltro != null ? tipoFiltro.name() : TipoContrato.CLIENTE.name());
        filtrosAtuais.put("clienteId", clienteId);
        model.addAttribute("filtros", filtrosAtuais);

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("total", clienteContratoService.contarContratosClientes());
        metricas.put("ativos", clienteContratoService.contarContratosClientesAtivos());
        metricas.put("expirados", clienteContratoService.contarContratosClientesExpirados());
        metricas.put("vencendo", clienteContratoService.contarContratosClientesVencendo30Dias());
        metricas.put("valorAtivo", clienteContratoService.somarValorContratosAtivos());
        model.addAttribute("metricas", metricas);

        return "clientes/contratos/listar";
    }

    @GetMapping("/{id}/detalhes")
    public String detalhesContratoCliente(@PathVariable Long id, Model model) {
        Contrato contrato = clienteContratoService.buscarContratoCliente(id);
        model.addAttribute("contrato", contrato);
        return "clientes/contratos/detalhes";
    }

    @GetMapping("/detalhes")
    public String detalhesSemId(@RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            return "redirect:/clientes/contratos/" + id + "/detalhes";
        }
        return "redirect:/clientes/contratos/listar";
    }

    private StatusContrato parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StatusContrato.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private TipoContrato parseTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return TipoContrato.CLIENTE;
        }
        try {
            return TipoContrato.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TipoContrato.CLIENTE;
        }
    }
}

