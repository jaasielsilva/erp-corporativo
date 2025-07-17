package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.FornecedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {

    private final FornecedorService service;

    public FornecedorController(FornecedorService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", service.listarTodos());
        return "fornecedores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedores/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor) {
        service.salvar(fornecedor);
        return "redirect:/fornecedores";
    }
}
