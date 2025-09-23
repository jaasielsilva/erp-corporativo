package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoRestController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/buscar-por-ean")
    public ResponseEntity<?> buscarPorEan(@RequestParam String ean) {
        Produto produto = produtoRepository.findByEan(ean);
        if (produto == null) {
            return ResponseEntity.status(404).body("Produto não encontrado");
        }
        if (produto.getEstoque() == null || produto.getEstoque() <= 0) {
            return ResponseEntity.status(400).body("Produto sem estoque disponível");
        }
        return ResponseEntity.ok(produto);
    }
}
