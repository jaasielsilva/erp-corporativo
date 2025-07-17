package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.CategoriaRepository;
import com.jaasielsilva.portalceo.repository.FornecedorRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.ProdutoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

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
        model.addAttribute("produtos", produtoService.listarTodos());
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
    return "redirect:/produtos";  // Corrigido para URL correta da lista
}


}
