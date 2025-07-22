package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.MovimentacaoEstoqueDTO;
import com.jaasielsilva.portalceo.model.MovimentacaoEstoque;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.TipoMovimentacao;
import com.jaasielsilva.portalceo.service.MovimentacaoEstoqueService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/estoque")
public class MovimentacaoEstoqueController {

    @Autowired
    private MovimentacaoEstoqueService movimentacaoService;

    @Autowired
    private ProdutoService produtoService;

    // Página do histórico com filtros e paginação
    @GetMapping("/historico")
    public String historicoHtml(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "TODOS") String tipo,
            @RequestParam(defaultValue = "") String busca,
            Model model) {
        Pageable pageable = PageRequest.of(pagina - 1, 10, Sort.by("dataHora").descending());
        Page<MovimentacaoEstoque> page;

        if ("TODOS".equalsIgnoreCase(tipo)) {
            page = movimentacaoService.buscarPorNomeProduto(busca, pageable);
        } else {
            page = movimentacaoService.buscarPorNomeProdutoETipo(busca, tipo, pageable);
        }

        model.addAttribute("pagina", page.map(MovimentacaoEstoqueDTO::fromEntity));
        model.addAttribute("busca", busca);
        model.addAttribute("tipo", tipo);

        return "estoque/historico";
    }

    // Novo endpoint REST para API que retorna JSON para a página carregar via AJAX
    @GetMapping("/api/historico")
    @ResponseBody
    public Page<MovimentacaoEstoqueDTO> historicoApi(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "TODOS") String tipo,
            @RequestParam(defaultValue = "") String busca) {
        Pageable pageable = PageRequest.of(pagina - 1, 10, Sort.by("dataHora").descending());
        Page<MovimentacaoEstoque> page;

        if ("TODOS".equalsIgnoreCase(tipo)) {
            page = movimentacaoService.buscarPorNomeProduto(busca, pageable);
        } else {
            page = movimentacaoService.buscarPorNomeProdutoETipo(busca, tipo, pageable);
        }

        return page.map(MovimentacaoEstoqueDTO::fromEntity);
    }

    // Formulário para ajustes manuais no estoque com lista dos ajustes recentes
    @GetMapping("/ajuste")
    public String formAjuste(@RequestParam(required = false) Long produtoId, Model model) {
        model.addAttribute("produtos", produtoService.listarTodosProdutos());
        model.addAttribute("produtoSelecionadoId", produtoId);
        return "estoque/ajuste"; // Ajustado para singular, nome do arquivo HTML
    }

    // Salva um ajuste de estoque
    @PostMapping("/ajuste")
    public String salvarAjuste(
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam String motivo,
            Principal principal,
            RedirectAttributes redirectAttrs) {
        try {
            String usuario = principal != null ? principal.getName() : "admin";
            movimentacaoService.registrarMovimentacao(produtoId, quantidade, TipoMovimentacao.AJUSTE, motivo, usuario);
            redirectAttrs.addFlashAttribute("sucesso", "Ajuste registrado com sucesso!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erro", e.getMessage());
            return "redirect:/estoque/ajuste";
        }
        return "redirect:/estoque";
    }

    // Endpoint REST para listar movimentações por produto (opcional)
    @GetMapping("/{id}/movimentacoes")
    @ResponseBody
    public List<MovimentacaoEstoqueDTO> listarMovimentacoesPorProduto(@PathVariable Long id) {
        Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
        if (produtoOpt.isEmpty()) {
            return List.of();
        }

        Produto produto = produtoOpt.get();
        return movimentacaoService.buscarPorProduto(produto)
                .stream()
                .map(MovimentacaoEstoqueDTO::fromEntity)
                .toList();
    }

    // Página detalhada do produto com suas movimentações
    @GetMapping("/{id}/detalhes")
public String detalhesProduto(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
    Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
    if (produtoOpt.isEmpty()) {
        redirectAttrs.addFlashAttribute("erro", "Produto não encontrado.");
        return "redirect:/estoque";
    }

    Produto produto = produtoOpt.get();

    // Converter LocalDateTime para Date
    Date dataCadastroDate = Date.from(produto.getDataCadastro().atZone(ZoneId.systemDefault()).toInstant());

    List<MovimentacaoEstoqueDTO> movimentacoes = movimentacaoService.buscarPorProduto(produto)
            .stream()
            .map(MovimentacaoEstoqueDTO::fromEntity)
            .toList();

    model.addAttribute("produto", produto);
    model.addAttribute("dataCadastroDate", dataCadastroDate); // adiciona a data convertida
    model.addAttribute("movimentacoes", movimentacoes);
    return "estoque/detalhes";
}


}
