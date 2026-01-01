package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/contratos")
public class ContratosController {

    @Autowired
    private ContratoService contratoService;
    @Autowired
    private FornecedorService fornecedorService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private PrestadorServicoService prestadorServicoService;
    @Autowired
    private ColaboradorService colaboradorService;
    @Autowired
    private DepartamentoService departamentoService;

    private List<TipoContrato> tiposPermitidosPorDepartamento(Usuario usuario) {
        if (usuario == null) {
            return List.of();
        }

        // Se for MASTER ou ADMINISTRADOR, tem acesso a todos os tipos
        boolean isMaster = usuario.getPerfis().stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase("MASTER") || p.getNome().equalsIgnoreCase("ADMINISTRADOR"));
        
        if (isMaster) {
            return Arrays.asList(TipoContrato.values());
        }

        if (usuario.getDepartamento() == null) {
            return List.of();
        }

        String departamento = usuario.getDepartamento().getNome().toUpperCase();

        return switch (departamento) {
            case "RH" -> List.of(TipoContrato.TRABALHISTA);
            case "TI" -> List.of(TipoContrato.PRESTADOR_SERVICO, TipoContrato.FORNECEDOR);
            case "COMPRAS" -> List.of(TipoContrato.FORNECEDOR);
            case "VENDAS" -> List.of(TipoContrato.CLIENTE);
            case "ADMIN" -> Arrays.asList(TipoContrato.values());
            default -> List.of();
        };
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MASTER', 'ROLE_MASTER')")
    public String listarTodos(Model model, @ModelAttribute("usuarioLogado") Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        model.addAttribute("statusContrato", StatusContrato.values());
        model.addAttribute("tiposContrato", TipoContrato.values());
        model.addAttribute("metricas", montarMetricas());

        return "contrato/listar";
    }

    private Map<String, Object> montarMetricas() {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("total", contratoService.contarTotal());
        metricas.put("ativos", contratoService.contarPorStatus(StatusContrato.ATIVO));
        metricas.put("vencendo", contratoService.contarVencendo30Dias());
        metricas.put("valorAtivo", contratoService.somarValorAtivos());
        return metricas;
    }

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarContratosApi(
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute("usuarioLogado") Usuario usuarioLogado) {

        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }

        List<TipoContrato> tiposPermitidos = tiposPermitidosPorDepartamento(usuarioLogado);

        List<TipoContrato> tiposFiltro;
        if (tipo != null && !tipo.isBlank()) {
            TipoContrato t = TipoContrato.valueOf(tipo);
            if (!tiposPermitidos.contains(t)) {
                return ResponseEntity.status(403).build();
            }
            tiposFiltro = List.of(t);
        } else {
            tiposFiltro = tiposPermitidos;
        }

        StatusContrato statusFiltro = (status != null && !status.isBlank()) ? StatusContrato.valueOf(status) : null;

        Page<Contrato> pageResult = contratoService.buscarTodos(busca, tiposFiltro, statusFiltro, page, size);

        List<Map<String, Object>> content = pageResult.getContent().stream()
                .map(c -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("numeroContrato", c.getNumeroContrato());

                    String nomeParte = "—";
                    if (c.getCliente() != null)
                        nomeParte = c.getCliente().getNomeFantasia() != null ? c.getCliente().getNomeFantasia()
                                : c.getCliente().getNome();
                    else if (c.getFornecedor() != null)
                        nomeParte = c.getFornecedor().getRazaoSocial();
                    else if (c.getPrestadorServico() != null)
                        nomeParte = c.getPrestadorServico().getNome();
                    else if (c.getColaborador() != null)
                        nomeParte = c.getColaborador().getNome();

                    item.put("parteNome", nomeParte);
                    item.put("tipo", c.getTipo());
                    item.put("dataInicio", c.getDataInicio());
                    item.put("dataFim", c.getDataFim());
                    item.put("status", c.getStatus());
                    item.put("valor", c.getValor());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", pageResult.getNumber());
        resp.put("totalElements", pageResult.getTotalElements());
        resp.put("totalPages", pageResult.getTotalPages());
        resp.put("hasNext", pageResult.hasNext());
        resp.put("hasPrevious", pageResult.hasPrevious());

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getContratoDetalhesApi(@PathVariable Long id) {
        try {
            Contrato contrato = contratoService.findById(id);
            if (contrato == null)
                return ResponseEntity.notFound().build();

            Map<String, Object> detalhe = new HashMap<>();
            detalhe.put("id", contrato.getId());
            detalhe.put("numeroContrato", contrato.getNumeroContrato());
            detalhe.put("status", contrato.getStatus() != null ? contrato.getStatus().name() : null);
            detalhe.put("valor", contrato.getValor());
            detalhe.put("dataInicio", contrato.getDataInicio());
            detalhe.put("dataFim", contrato.getDataFim());
            detalhe.put("tipo", contrato.getTipo() != null ? contrato.getTipo().name() : null);
            detalhe.put("descricao", contrato.getDescricao());
            detalhe.put("dataCriacao", contrato.getDataCriacao());
            detalhe.put("ultimaAtualizacao", contrato.getUltimaAtualizacao());

            String nomeParte = "—";
            String docParte = "—";

            if (contrato.getCliente() != null) {
                nomeParte = contrato.getCliente().getNomeFantasia() != null ? contrato.getCliente().getNomeFantasia()
                        : contrato.getCliente().getNome();
                docParte = contrato.getCliente().getCpfCnpj();
            } else if (contrato.getFornecedor() != null) {
                nomeParte = contrato.getFornecedor().getRazaoSocial();
                docParte = contrato.getFornecedor().getCnpj();
            } else if (contrato.getPrestadorServico() != null) {
                nomeParte = contrato.getPrestadorServico().getNome();
                docParte = contrato.getPrestadorServico().getCnpjOuCpf();
            } else if (contrato.getColaborador() != null) {
                nomeParte = contrato.getColaborador().getNome();
                docParte = contrato.getColaborador().getCpf();
            }

            detalhe.put("parteNome", nomeParte);
            detalhe.put("parteDocumento", docParte);

            return ResponseEntity.ok(detalhe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/novo")
    public String novoContrato(Model model, @ModelAttribute("usuarioLogado") Usuario usuarioLogado) {
        Contrato contrato = new Contrato();
        contrato.setNumeroContrato(contratoService.gerarProximoNumero()); // Gera número dinâmico
        contrato.setDataInicio(java.time.LocalDate.now()); // Data início padrão hoje
        contrato.setStatus(StatusContrato.ATIVO); // Status padrão
        model.addAttribute("contrato", contrato);

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<TipoContrato> tiposPermitidos = tiposPermitidosPorDepartamento(usuarioLogado);

        if (tiposPermitidos.isEmpty()) {
            return "redirect:/acesso-negado";
        }

        model.addAttribute("tiposContrato", tiposPermitidos);
        model.addAttribute("statusContrato", StatusContrato.values());
        model.addAttribute("fornecedores", fornecedorService.listarAtivosParaSelecao());
        model.addAttribute("prestadoresServico", prestadorServicoService.listarAtivosParaSelecao());
        model.addAttribute("departamentoUsuario", usuarioLogado.getDepartamento());
        model.addAttribute("colaboradores", colaboradorService.listarAtivosParaSelecao());

        return "contrato/contrato-form";
    }

    @PostMapping("/salvar")
    @PreAuthorize("isAuthenticated()")
    public String salvarContrato(@ModelAttribute("contrato") Contrato contrato,
            @ModelAttribute("usuarioLogado") Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Verifica se o tipo de contrato é permitido para o departamento do usuário
        List<TipoContrato> tiposPermitidos = tiposPermitidosPorDepartamento(usuarioLogado);
        if (!tiposPermitidos.contains(contrato.getTipo())) {
            throw new IllegalStateException("Tipo de contrato não permitido para seu departamento.");
        }

        // Atualiza as referências completas para entidades relacionadas
        if (contrato.getCliente() != null && contrato.getCliente().getId() != null) {
            Cliente cliente = clienteService.findById(contrato.getCliente().getId());
            contrato.setCliente(cliente);
        } else {
            contrato.setCliente(null);
        }

        if (contrato.getFornecedor() != null && contrato.getFornecedor().getId() != null) {
            Fornecedor fornecedor = fornecedorService.findById(contrato.getFornecedor().getId());
            contrato.setFornecedor(fornecedor);
        } else {
            contrato.setFornecedor(null);
        }

        if (contrato.getPrestadorServico() != null && contrato.getPrestadorServico().getId() != null) {
            PrestadorServico prestador = prestadorServicoService.findById(contrato.getPrestadorServico().getId());
            contrato.setPrestadorServico(prestador);
        } else {
            contrato.setPrestadorServico(null);
        }

        if (contrato.getColaborador() != null && contrato.getColaborador().getId() != null) {
            Colaborador col = colaboradorService.findById(contrato.getColaborador().getId());
            contrato.setColaborador(col);
        } else {
            contrato.setColaborador(null);
        }

        contratoService.salvar(contrato);

        return "redirect:/contratos";
    }

    /**
     * Carrega o contrato existente para edição
     */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, @ModelAttribute("usuarioLogado") Usuario usuarioLogado) {
        Contrato contrato = contratoService.findById(id);
        
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Verifica permissão do departamento
        List<TipoContrato> tiposPermitidos = tiposPermitidosPorDepartamento(usuarioLogado);
        if (!tiposPermitidos.contains(contrato.getTipo()) && !tiposPermitidos.isEmpty()) {
            // Se o usuário não tem permissão para este tipo, mas é o editor... 
            // Aqui simplificamos: se não pode ver esse tipo, nega acesso.
             return "redirect:/acesso-negado";
        }
        
        // Se a lista de permitidos estiver vazia (sem departamento/perfil), redireciona
        if (tiposPermitidos.isEmpty()) {
            return "redirect:/acesso-negado";
        }

        model.addAttribute("contrato", contrato);
        model.addAttribute("tiposContrato", tiposPermitidos);
        model.addAttribute("statusContrato", StatusContrato.values());
        
        // Carrega as listas para os dropdowns (mesma lógica do novo)
        model.addAttribute("fornecedores", fornecedorService.listarAtivosParaSelecao());
        model.addAttribute("prestadoresServico", prestadorServicoService.listarAtivosParaSelecao());
        model.addAttribute("colaboradores", colaboradorService.listarAtivosParaSelecao());
        
        model.addAttribute("departamentoUsuario", usuarioLogado.getDepartamento());

        return "contrato/contrato-form";
    }

    /**
     * Exibe os detalhes de um contrato específico
     */
    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        model.addAttribute("contrato", contrato);
        return "contrato/detalhes";
    }

    /**
     * Exibe tela para renovar contrato
     */
    @GetMapping("/{id}/renovar")
    public String renovar(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        model.addAttribute("contrato", contrato);
        return "contrato/renovar";
    }

    /**
     * Tela para anexar documentos ao contrato
     */
    @GetMapping("/{id}/documentos")
    public String documentos(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        model.addAttribute("contrato", contrato);
        return "contrato/documentos";
    }
}
