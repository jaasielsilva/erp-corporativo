package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.TransferenciaEstoqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/estoque/transferencias")
public class TransferenciaEstoqueController {

    @Autowired
    private TransferenciaEstoqueService transferenciaService;

    @Autowired
    private ProdutoService produtoService;

    // Página de transferência de estoque
    @GetMapping
    public String mostrarTransferencias(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "estoque/transferencias";
    }

    // Registra uma nova transferência
    @PostMapping
    public String registrarTransferencia(@RequestParam Long produtoId,
                                         @RequestParam Integer quantidade,
                                         @RequestParam String localOrigem,
                                         @RequestParam String localDestino) {
        transferenciaService.transferir(produtoId, quantidade, localOrigem, localDestino, "admin");
        return "redirect:/estoque/transferencias?sucesso";
    }
}
