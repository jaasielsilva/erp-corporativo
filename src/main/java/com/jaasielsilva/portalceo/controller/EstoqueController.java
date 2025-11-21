package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.TipoMovimentacao;
import com.jaasielsilva.portalceo.service.CategoriaService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import com.jaasielsilva.portalceo.service.MovimentacaoEstoqueService;
import com.jaasielsilva.portalceo.service.ProdutoService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private FornecedorService fornecedorService;

    @Autowired
    private MovimentacaoEstoqueService movimentacaoService;

    // Página principal que lista o estoque, categorias e fornecedores
    @GetMapping
    public String listarEstoque(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String ean,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        model.addAttribute("categorias", categoriaService.findAll());
        model.addAttribute("fornecedores", fornecedorService.listarTodos());

        Long estoqueTotal = produtoService.somarQuantidadeEstoque();
        if (estoqueTotal == null) {
            estoqueTotal = 0L;
        }
        model.addAttribute("totalEstoque", estoqueTotal);

        var pagina = produtoService.filtrarEstoque(nome, ean, categoriaId, fornecedorId, page);
        model.addAttribute("produtos", pagina.getContent());

        List<Produto> produtosCriticos = pagina.getContent().stream()
                .filter(p -> p.getEstoque() != null && p.getMinimoEstoque() != null && p.getEstoque() <= p.getMinimoEstoque())
                .toList();

        model.addAttribute("produtosCriticos", produtosCriticos);
        model.addAttribute("quantidadeProdutosEstoqueBaixo", produtosCriticos.size());

        long produtosZerados = pagina.getContent().stream()
                .filter(p -> p.getEstoque() != null && p.getEstoque() == 0)
                .count();
        model.addAttribute("produtosZerados", produtosZerados);

        model.addAttribute("currentPage", pagina.getNumber());
        model.addAttribute("totalPages", pagina.getTotalPages());
        model.addAttribute("totalElements", pagina.getTotalElements());
        model.addAttribute("hasPrevious", pagina.hasPrevious());
        model.addAttribute("hasNext", pagina.hasNext());

        Map<String, Integer> graficoCategorias = produtoService.countProdutosPorCategoria();
        model.addAttribute("graficoCategorias", graficoCategorias);

        return "estoque/lista";
    }

    // Formulário para entrada de estoque
    @GetMapping("/entrada")
    public String formEntrada(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "estoque/entrada";
    }

    // Salva uma entrada de estoque
    @PostMapping("/entrada")
    public String salvarEntrada(
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam String motivo,
            RedirectAttributes redirectAttrs) {
        try {
            movimentacaoService.registrarMovimentacao(produtoId, quantidade, TipoMovimentacao.ENTRADA, motivo, "admin");
            redirectAttrs.addFlashAttribute("sucesso", "Entrada registrada com sucesso!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erro", e.getMessage());
            return "redirect:/estoque/entrada"; // permanece no formulário em caso de erro
        }
        return "redirect:/estoque"; // redireciona para a lista principal após sucesso
    }

    // Formulário para saída de estoque
    @GetMapping("/saida")
    public String formSaida(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "estoque/saida";
    }

    // Salva uma saída de estoque
    @PostMapping("/saida")
    public String salvarSaida(
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam String motivo,
            RedirectAttributes redirectAttrs) {
        try {
            movimentacaoService.registrarMovimentacao(produtoId, quantidade, TipoMovimentacao.SAIDA, motivo, "admin");
            redirectAttrs.addFlashAttribute("sucesso", "Saída registrada com sucesso!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erro", e.getMessage());
            return "redirect:/estoque/saida"; // mantém no formulário em caso de erro
        }
        return "redirect:/estoque"; // redireciona para lista principal após sucesso
    }
    
}
