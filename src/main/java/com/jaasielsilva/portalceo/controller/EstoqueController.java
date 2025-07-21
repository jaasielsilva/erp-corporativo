package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.TipoMovimentacao;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.service.MovimentacaoEstoqueService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.CategoriaService;
import com.jaasielsilva.portalceo.service.FornecedorService;

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

    @Autowired
    private ProdutoRepository produtoRepository;

    // Página principal que lista o estoque, categorias e fornecedores
    @GetMapping
    public String listarEstoque(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        model.addAttribute("categorias", categoriaService.findAll());
        model.addAttribute("fornecedores", fornecedorService.findAll());

        Long estoqueTotal = produtoRepository.somarQuantidadeEstoque();
        if (estoqueTotal == null)
            estoqueTotal = 0L;
        model.addAttribute("totalEstoque", estoqueTotal);

        var pagina = produtoService.filtrarEstoque(nome, categoriaId, fornecedorId, page);
        model.addAttribute("produtos", pagina.getContent());

        // Verifica produtos com estoque abaixo ou igual ao mínimo
        List<Produto> produtosCriticos = pagina.getContent().stream()
                .filter(p -> p.getEstoque() <= p.getMinimoEstoque())
                .toList();

        model.addAttribute("produtosCriticos", produtosCriticos);
        model.addAttribute("quantidadeProdutosEstoqueBaixo", produtosCriticos.size());

        // Exemplo: contar produtos zerados
        long produtosZerados = pagina.getContent().stream()
                .filter(p -> p.getEstoque() == 0)
                .count();
        model.addAttribute("produtosZerados", produtosZerados);

        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", pagina.getTotalPages());

        // Montar dados do gráfico de categorias - exemplo Map<String, Integer>
        Map<String, Integer> graficoCategorias = produtoService.countProdutosPorCategoria();
        model.addAttribute("graficoCategorias", graficoCategorias);

        // Adicione outras métricas/relatórios aqui para o dashboard

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
    public String salvarEntrada(@RequestParam Long produtoId,
                            @RequestParam Integer quantidade,
                            @RequestParam String motivo,
                            RedirectAttributes redirectAttrs) {
    try {
        movimentacaoService.registrarMovimentacao(produtoId, quantidade, TipoMovimentacao.ENTRADA, motivo, "admin");
        redirectAttrs.addFlashAttribute("sucesso", "Entrada registrada com sucesso!");
    } catch (RuntimeException e) {
        redirectAttrs.addFlashAttribute("erro", e.getMessage());
        return "redirect:/estoque/entrada"; // mantém na página entrada em caso de erro
    }
    // redireciona para a página entrada para exibir popup e depois redirecionar via JS para /estoque
    return "redirect:/estoque/entrada"; 
}



    // Formulário para saída de estoque
    @GetMapping("/saida")
    public String formSaida(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "estoque/saida";
    }

    // Salva uma saída de estoque
    @PostMapping("/saida")
    public String salvarSaida(@RequestParam Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam String motivo,
            Model model) {
        try {
            movimentacaoService.registrarMovimentacao(produtoId, quantidade, TipoMovimentacao.SAIDA, motivo, "admin");
            model.addAttribute("sucesso", "Saída registrada com sucesso!");
        } catch (RuntimeException e) {
            model.addAttribute("erro", e.getMessage());
            // Retorna para o formulário mostrando erro
            model.addAttribute("produtos", produtoService.listarTodosProdutos());
            return "estoque/saida";
        }
        // Carrega produtos para o formulário (mesmo que já estejam no model)
        model.addAttribute("produtos", produtoService.listarTodosProdutos());

        return "estoque/saida";
    }

    // Formulário para ajustes manuais no estoque
    @GetMapping("/ajustes")
    public String formAjustes(Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        return "estoque/ajustes";
    }

    // Salva um ajuste manual no estoque
    @PostMapping("/ajustes")
    public String salvarAjuste(@RequestParam Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam String motivo) {
        movimentacaoService.registrarMovimentacao(produtoId, quantidade, TipoMovimentacao.AJUSTE, motivo, "admin");
        return "redirect:/estoque/ajustes?sucesso";
    }
}
