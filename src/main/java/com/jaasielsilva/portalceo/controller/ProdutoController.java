package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.CategoriaRepository;
import com.jaasielsilva.portalceo.repository.FornecedorRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.service.CategoriaService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import com.jaasielsilva.portalceo.service.ProdutoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private FornecedorService fornecedorService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina,
                     @RequestParam(defaultValue = "10") int tamanho,
                     Model model) {
    Pageable pageable = PageRequest.of(pagina, tamanho);
    Page<Produto> paginaProdutos = produtoService.listarPaginado(pageable);
    model.addAttribute("pagina", paginaProdutos);
    return "produto/lista";
}


    @GetMapping("/novo")
    public String novoProduto(Model model) {
    List<Categoria> categorias = categoriaService.findAll();
    List<Fornecedor> fornecedores = fornecedorService.listarTodos();

    model.addAttribute("categorias", categorias);
    model.addAttribute("fornecedores", fornecedores);

    if (categorias.isEmpty() || fornecedores.isEmpty()) {
        model.addAttribute("aviso", "Você precisa cadastrar categorias e fornecedores antes de cadastrar um produto.");
    }

    model.addAttribute("produto", new Produto());
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

  
}
