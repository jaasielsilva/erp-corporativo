package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.model.PrestadorServico;
import com.jaasielsilva.portalceo.model.StatusContrato;
import com.jaasielsilva.portalceo.model.TipoContrato;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ContratoService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import com.jaasielsilva.portalceo.service.PrestadorServicoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * Lista todos os contratos cadastrados no banco
     */
    @GetMapping
    public String listarTodos(Model model) {
        List<Contrato> contratos = contratoService.findAll();
        model.addAttribute("contratos", contratos);
        return "contrato/listar";
    }

    // Exibe o formulário de novo contrato
    @GetMapping("/novo")
    public String novoContrato(Model model) {
        model.addAttribute("contrato", new Contrato());

        model.addAttribute("tiposContrato", TipoContrato.values());
        model.addAttribute("statusContrato", StatusContrato.values());

        model.addAttribute("fornecedores", fornecedorService.listarAtivos());
        model.addAttribute("clientes", clienteService.listarAtivosPorTipo("PJ"));
        model.addAttribute("prestadoresServico", prestadorServicoService.findAllAtivos());

        return "contrato/contrato-form";
    }

    /**
     * Salva um contrato (tanto novo quanto edição)
     */
    @PostMapping("/salvar")
public String salvarContrato(@ModelAttribute("contrato") Contrato contrato) {

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
