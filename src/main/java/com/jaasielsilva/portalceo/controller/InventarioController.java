package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.service.InventarioService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/estoque/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProdutoService produtoService;

    // Página do inventário
    @GetMapping
    public String mostrarInventario(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "estoque/inventario";
    }

    // Salva inventário de um produto
    @PostMapping
    public String registrarInventario(@RequestParam Long produtoId,
                                      @RequestParam Integer quantidadeContada,
                                      @RequestParam String observacoes) {
        inventarioService.registrarInventario(produtoId, quantidadeContada, "admin", observacoes);
        return "redirect:/estoque/inventario?sucesso";
    }
}
