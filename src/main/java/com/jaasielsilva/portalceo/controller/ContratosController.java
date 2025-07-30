package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
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
        if (usuario == null || usuario.getDepartamento() == null) {
            return List.of();
        }

        String departamento = usuario.getDepartamento().getNome().toUpperCase();

        return switch (departamento) {
            case "RH" -> List.of(TipoContrato.TRABALHISTA);
            case "TI" -> List.of(TipoContrato.PRESTADOR_SERVICO, TipoContrato.FORNECEDOR);
            case "COMPRAS" -> List.of(TipoContrato.FORNECEDOR);
            case "VENDAS" -> List.of(TipoContrato.CLIENTE);
            case "ADMIN" -> List.of(TipoContrato.values());
            default -> List.of();
        };
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String listarTodos(Model model, @ModelAttribute("usuarioLogado") Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<TipoContrato> tiposPermitidos = tiposPermitidosPorDepartamento(usuarioLogado);

        List<Contrato> contratos = contratoService.findAll().stream()
                .filter(c -> tiposPermitidos.contains(c.getTipo()))
                .collect(Collectors.toList());

        model.addAttribute("contratos", contratos);
        return "contrato/listar";
    }
@GetMapping("/novo")
public String novoContrato(Model model, @ModelAttribute("usuarioLogado") Usuario usuarioLogado) {
    Contrato contrato = new Contrato();
    model.addAttribute("contrato", contrato);

    // Obter departamento e perfil do usuário
    String departamentoNome = usuarioLogado.getDepartamento() != null ? usuarioLogado.getDepartamento().getNome() : "";
    boolean isAdmin = usuarioLogado.getPerfis().stream()
            .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

    List<TipoContrato> tiposPermitidos;

    if (isAdmin) {
        switch (departamentoNome) {
            case "RH":
                tiposPermitidos = List.of(TipoContrato.TRABALHISTA);
                break;
            case "TI":
                tiposPermitidos = List.of(TipoContrato.PRESTADOR_SERVICO, TipoContrato.FORNECEDOR);
                break;
            case "COMPRAS":
                tiposPermitidos = List.of(TipoContrato.FORNECEDOR);
                break;
            case "VENDAS":
                tiposPermitidos = List.of(TipoContrato.CLIENTE);
                break;
            case "ADMIN":
                tiposPermitidos = Arrays.asList(TipoContrato.values());
                break;
            default:
                tiposPermitidos = List.of();
                break;
        }
    } else {
        return "redirect:/acesso-negado";
    }

    model.addAttribute("tiposContrato", tiposPermitidos); // ✅ Mantém esta
    model.addAttribute("statusContrato", StatusContrato.values());
    model.addAttribute("fornecedores", fornecedorService.listarAtivos());
    model.addAttribute("clientes", clienteService.listarAtivosPorTipo("PJ"));
    model.addAttribute("prestadoresServico", prestadorServicoService.findAllAtivos());
    model.addAttribute("departamentoUsuario", usuarioLogado.getDepartamento());
    model.addAttribute("colaboradores", colaboradorService.listarAtivos());

    return "contrato/contrato-form";
}


    @PostMapping("/salvar")
    @PreAuthorize("hasAuthority('ADMIN')")
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
    public String editar(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        model.addAttribute("contrato", contrato);

        model.addAttribute("tiposContrato", TipoContrato.values());
        model.addAttribute("statusContrato", StatusContrato.values());
        model.addAttribute("fornecedores", fornecedorService.listarAtivos());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("prestadoresServico", prestadorServicoService.findAllAtivos());

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
