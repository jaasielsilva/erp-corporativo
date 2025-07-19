package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.service.CategoriaService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private FornecedorService fornecedorService;

    @GetMapping
    public String listarEstoque(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos()); 
        model.addAttribute("categorias", categoriaService.findAll()); 
        model.addAttribute("fornecedores", fornecedorService.findAll()); 
        return "estoque/lista";
    }
}
