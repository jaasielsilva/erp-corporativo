package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", fornecedorService.findAll());
        return "fornecedor/lista";
    }

    // Método para abrir o formulário de cadastro com valores padrão
    @GetMapping("/novo")
    public String novoFornecedor(Model model) {
        Fornecedor fornecedor = new Fornecedor();
        model.addAttribute("fornecedor", fornecedor);
        return "fornecedor/form";
    }


    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor) {
        fornecedorService.salvar(fornecedor);
        return "redirect:/fornecedores";
    }
}
