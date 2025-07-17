package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.CategoriaRepository;
import com.jaasielsilva.portalceo.repository.FornecedorRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.service.ProdutoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController 
@RequestMapping("/api/produtos")
public class ProdutoRestController {

    @Autowired
    private  ProdutoService produtoService;
    @Autowired
    private  CategoriaRepository categoriaRepository;
    @Autowired
    private  FornecedorRepository fornecedorRepository;
    @Autowired
    private  ProdutoRepository produtoRepository;


    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "produto/lista";
    }

    @GetMapping("/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("fornecedores", fornecedorRepository.findAll());
        return "produto/form";
    }

    @PostMapping("/salvar")
public String salvarProduto(@ModelAttribute Produto produto) {
    Long categoriaId = produto.getCategoria().getId();
    Long fornecedorId = produto.getFornecedor().getId();

    produto.setCategoria(categoriaRepository.findById(categoriaId).orElse(null));
    produto.setFornecedor(fornecedorRepository.findById(fornecedorId).orElse(null));

    produtoRepository.save(produto);
    return "redirect:/produtos";
    }

    @GetMapping("/buscar-por-ean")
    public ResponseEntity<?> buscarPorEan(@RequestParam String ean) {
        Produto produto = produtoRepository.findByEan(ean);
        if (produto == null) {
            return ResponseEntity.status(404).body("Produto não encontrado");
        }
        if (produto.getEstoque() == null || produto.getEstoque() <= 0) {
            return ResponseEntity.status(400).body("Produto sem estoque disponível");
        }
        // Retorna o produto (pode criar um DTO para não vazar dados sensíveis)
        return ResponseEntity.ok(produto);
    }


}
