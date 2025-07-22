package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.AvaliacaoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.AvaliacaoFornecedorService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

@Controller
@RequestMapping("/fornecedor/{fornecedorId}/avaliacoes")
public class AvaliacaoFornecedorController {

    private final AvaliacaoFornecedorService avaliacaoService;
    private final FornecedorService fornecedorService;

    public AvaliacaoFornecedorController(AvaliacaoFornecedorService avaliacaoService,
                                        FornecedorService fornecedorService) {
        this.avaliacaoService = avaliacaoService;
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    public String listarAvaliacoes(@PathVariable Long fornecedorId,
                                   @RequestParam(defaultValue = "0") int page,
                                   Model model) {

        Fornecedor fornecedor = fornecedorService.findById(fornecedorId);

        Pageable pageable = PageRequest.of(page, 10);
        Page<AvaliacaoFornecedor> avaliacoesPage = avaliacaoService.findByFornecedorPaged(fornecedor, pageable);

        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("avaliacoes", avaliacoesPage.getContent());
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", avaliacoesPage.getTotalPages());

        return "fornecedor/avaliacoes";  // pasta singular
    }

    @GetMapping("/novo")
    public String novoFormulario(@PathVariable Long fornecedorId, Model model) {
        Fornecedor fornecedor = fornecedorService.findById(fornecedorId);
        AvaliacaoFornecedor avaliacao = new AvaliacaoFornecedor();
        avaliacao.setData(LocalDate.now());
        avaliacao.setFornecedor(fornecedor);

        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("fornecedor", fornecedor);

        return "fornecedor/avaliacao-form";
    }

    @PostMapping("/salvar")
    public String salvar(@PathVariable Long fornecedorId, @ModelAttribute AvaliacaoFornecedor avaliacao) {
        Fornecedor fornecedor = fornecedorService.findById(fornecedorId);
        avaliacao.setFornecedor(fornecedor);
        avaliacaoService.save(avaliacao);
        return "redirect:/fornecedor/" + fornecedorId + "/avaliacoes";
    }

    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long fornecedorId, @PathVariable Long id, Model model) {
        AvaliacaoFornecedor avaliacao = avaliacaoService.findById(id);
        Fornecedor fornecedor = fornecedorService.findById(fornecedorId);

        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("fornecedor", fornecedor);

        return "fornecedor/avaliacao-form";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long fornecedorId, @PathVariable Long id) {
        avaliacaoService.delete(id);
        return "redirect:/fornecedor/" + fornecedorId + "/avaliacoes";
    }
}
