package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.MovimentacaoEstoqueDTO;
import com.jaasielsilva.portalceo.model.MovimentacaoEstoque;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.service.MovimentacaoEstoqueService;
import com.jaasielsilva.portalceo.service.ProdutoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/estoque")
public class MovimentacaoEstoqueController {

    @Autowired
    private MovimentacaoEstoqueService movimentacaoService;

    @Autowired
    private ProdutoService produtoService;

    // Histórico geral de movimentações com filtro e paginação
    @GetMapping("/historico-html")
    public String historicoHtml(
        @RequestParam(defaultValue = "1") int pagina,
        @RequestParam(defaultValue = "TODOS") String tipo,
        @RequestParam(defaultValue = "") String busca,
        Model model
) {
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
    return "estoque/historico"; // este HTML precisa estar em templates/estoque/historico.html
}

    // Lista movimentações por produto
    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoEstoqueDTO> listarMovimentacoesPorProduto(@PathVariable Long id) {
        Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
        if (produtoOpt.isEmpty()) {
            return List.of(); // ou lançar ResponseStatusException
        }

        Produto produto = produtoOpt.get();
        return movimentacaoService.buscarPorProduto(produto)
                .stream()
                .map(MovimentacaoEstoqueDTO::fromEntity)
                .toList();
    }
}
