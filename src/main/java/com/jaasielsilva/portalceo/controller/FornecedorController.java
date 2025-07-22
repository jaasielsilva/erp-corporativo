package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ContratoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.ContratoFornecedorService;
import com.jaasielsilva.portalceo.service.FornecedorService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @Autowired
    private ContratoFornecedorService contratoService;

    // Lista todos os fornecedores
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", fornecedorService.findAll());
        return "fornecedor/lista";
    }

    // Exibe o formulário para criar um novo fornecedor
    @GetMapping("/novo")
    public String novoFornecedor(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedor/form";
    }

    // Salva um novo fornecedor ou atualiza um existente
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor) {
        fornecedorService.salvar(fornecedor);
        return "redirect:/fornecedores";
    }

    // Exibe o formulário para editar um fornecedor existente pelo id
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.findById(id));
        return "fornecedor/editar";
    }

    // Exclui um fornecedor pelo id
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        fornecedorService.excluir(id);
        return "redirect:/fornecedores";
    }

    // --- Métodos relacionados a contratos ---

    // Lista os contratos de um fornecedor pelo id do fornecedor
    @GetMapping("/{id}/contratos")
    public String listarContratos(@PathVariable Long id, Model model) {
        Fornecedor fornecedor = fornecedorService.findById(id);
        List<ContratoFornecedor> contratos = contratoService.findByFornecedor(fornecedor);

        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("contratos", contratos);
        return "fornecedor/contratos";
    }


    // Exibe o formulário para criar um novo contrato para o fornecedor especificado
    // pelo id
    @GetMapping("/{id}/contratos/novo")
    public String novoContrato(@PathVariable Long id, Model model) {
        Fornecedor fornecedor = fornecedorService.findById(id);
        ContratoFornecedor contrato = new ContratoFornecedor();
        contrato.setFornecedor(fornecedor);
        // Definir data início padrão para hoje
        contrato.setDataInicio(LocalDate.now());

        model.addAttribute("contrato", contrato);
        return "contrato/contrato-form"; // Certifique que o path do template está correto
    }

    // Salva um novo contrato ou atualiza um contrato existente
    @PostMapping("/contratos/salvar")
    public String salvarContrato(@ModelAttribute ContratoFornecedor contrato) {
        contratoService.salvar(contrato);
        return "redirect:/fornecedores/" + contrato.getFornecedor().getId() + "/contratos";
    }

    // Exibe o formulário para editar um contrato existente pelo id do contrato
    @GetMapping("/contratos/editar/{contratoId}")
    public String editarContrato(@PathVariable Long contratoId, Model model) {
        ContratoFornecedor contrato = contratoService.findById(contratoId);
        model.addAttribute("contrato", contrato);
        return "fornecedor/contrato-form";
    }

    // Exclui um contrato pelo id e redireciona para a lista de contratos do
    // fornecedor
    @GetMapping("/contratos/excluir/{contratoId}")
    public String excluirContrato(@PathVariable Long contratoId) {
        Long fornecedorId = contratoService.findById(contratoId).getFornecedor().getId();
        contratoService.excluir(contratoId);
        return "redirect:/fornecedores/" + fornecedorId + "/contratos";
    }
}
