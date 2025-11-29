package com.jaasielsilva.portalceo.controller.cliente;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.StatusContrato;
import com.jaasielsilva.portalceo.model.TipoContrato;
import com.jaasielsilva.portalceo.service.ClienteContratoService;
import com.jaasielsilva.portalceo.service.ClienteService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        StatusContrato statusFiltro = parseStatus(status);
        TipoContrato tipoFiltro = parseTipo(tipo);

        model.addAttribute("clientes", clienteService.listarClientesAtivos());
        model.addAttribute("statusContrato", StatusContrato.values());
        model.addAttribute("tiposContrato", List.of(TipoContrato.CLIENTE));

        Map<String, Object> filtrosAtuais = new HashMap<>();
        filtrosAtuais.put("busca", busca);
        filtrosAtuais.put("status", status);
        filtrosAtuais.put("tipo", tipoFiltro != null ? tipoFiltro.name() : TipoContrato.CLIENTE.name());
        filtrosAtuais.put("clienteId", clienteId);
        filtrosAtuais.put("pageSize", size);
        model.addAttribute("filtros", filtrosAtuais);

        model.addAttribute("metricas", montarMetricas());

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

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarContratosClientesApi(
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "clienteId", required = false) Long clienteId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        StatusContrato statusFiltro = parseStatus(status);
        TipoContrato tipoFiltro = parseTipo(tipo);

        Page<Contrato> contratosPage = clienteContratoService.listarContratosClientes(
                busca,
                tipoFiltro,
                statusFiltro,
                clienteId,
                page,
                size);

        List<Map<String, Object>> content = contratosPage.getContent().stream()
                .map(c -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("numeroContrato", c.getNumeroContrato());
                    item.put("clienteNome", c.getCliente() != null
                            ? (c.getCliente().getNomeFantasia() != null && !c.getCliente().getNomeFantasia().isBlank()
                                    ? c.getCliente().getNomeFantasia()
                                    : c.getCliente().getNome())
                            : "—");
                    item.put("clienteId", c.getCliente() != null ? c.getCliente().getId() : null);
                    item.put("status", c.getStatus() != null ? c.getStatus().name() : null);
                    item.put("tipo", c.getTipo() != null ? c.getTipo().name() : null);
                    item.put("valor", c.getValor());
                    item.put("dataInicio", c.getDataInicio());
                    item.put("dataFim", c.getDataFim());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", contratosPage.getNumber());
        resp.put("totalPages", contratosPage.getTotalPages());
        resp.put("totalElements", contratosPage.getTotalElements());
        resp.put("hasPrevious", contratosPage.hasPrevious());
        resp.put("hasNext", contratosPage.hasNext());
        resp.put("metrics", montarMetricas());

        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> montarMetricas() {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("total", clienteContratoService.contarContratosClientes());
        metricas.put("ativos", clienteContratoService.contarContratosClientesAtivos());
        metricas.put("expirados", clienteContratoService.contarContratosClientesExpirados());
        metricas.put("vencendo", clienteContratoService.contarContratosClientesVencendo30Dias());
        metricas.put("valorAtivo", clienteContratoService.somarValorContratosAtivos());
        return metricas;
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

