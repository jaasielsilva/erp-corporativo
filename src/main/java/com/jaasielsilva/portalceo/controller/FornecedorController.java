package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ContratoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.ContratoFornecedorService;
import com.jaasielsilva.portalceo.service.FornecedorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @Autowired
    private ContratoFornecedorService contratoService;

    /**
     * Lista fornecedores ativos, paginados.
     * @param pagina Número da página atual (começa em 0)
     * @param tamanho Quantidade de itens por página
     */
    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {
        Page<Fornecedor> paginaFornecedores = fornecedorService.listarTodosPaginado(pagina, tamanho);
        model.addAttribute("pagina", paginaFornecedores);
        return "fornecedor/listar";
    }

    /**
     * Exibe o formulário para cadastrar um novo fornecedor.
     */
    @GetMapping("/novo")
    public String novoFornecedor(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedor/form";
    }

    /**
     * Salva ou atualiza um fornecedor no banco de dados.
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor) {
        fornecedorService.salvar(fornecedor);
        return "redirect:/fornecedores";
    }

    /**
     * Exibe o formulário de edição de um fornecedor pelo ID.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.findById(id));
        return "fornecedor/editar";
    }

    /**
     * Realiza a exclusão lógica de um fornecedor (seta como inativo).
     */
    @GetMapping("/excluir/{id}")
    public String excluirFornecedor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        fornecedorService.excluir(id);
        redirectAttributes.addFlashAttribute("msgSucesso", "Fornecedor excluído com sucesso.");
        return "redirect:/fornecedores?pagina=0&tamanho=10";
    }

    // --- Contratos ---

    /**
     * Lista todos os contratos de um fornecedor específico.
     */
    @GetMapping("/{id}/contratos")
    public String listarContratos(@PathVariable Long id, Model model) {
        Fornecedor fornecedor = fornecedorService.findById(id);
        if (fornecedor == null) {
            return "redirect:/fornecedores?erro=FornecedorNaoEncontrado";
        }
        List<ContratoFornecedor> contratos = contratoService.findByFornecedor(fornecedor);
        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("contratos", contratos);
        return "fornecedor/contratos-listar";
    }

    /**
     * Lista todos os contratos de todos os fornecedores.
     */
    @GetMapping("/contratos")
    public String listarTodosContratos(Model model) {
        List<ContratoFornecedor> contratos = contratoService.listarTodos();
        model.addAttribute("contratos", contratos);
        return "contrato/listar";
    }

    /**
     * Exibe o formulário para criar um novo contrato vinculado a um fornecedor.
     */
    @GetMapping("/{id}/contratos/novo")
    public String novoContrato(@PathVariable Long id, Model model) {
        Fornecedor fornecedor = fornecedorService.findById(id);
        if (fornecedor == null) {
            return "redirect:/fornecedores?erro=FornecedorNaoEncontrado";
        }
        ContratoFornecedor contrato = new ContratoFornecedor();
        contrato.setFornecedor(fornecedor);
        contrato.setDataInicio(LocalDate.now());
        model.addAttribute("contrato", contrato);
        return "fornecedor/contrato-form";
    }

    /**
     * Salva ou atualiza um contrato de fornecedor, com tratamento para valor formatado (ex: R$ 1.000,00).
     */
    @PostMapping("/contratos/salvar")
    public String salvarContrato(
            @ModelAttribute ContratoFornecedor contrato,
            @RequestParam(value = "valorFormatado", required = false) String valorFormatado,
            RedirectAttributes redirectAttributes) {
        try {
            if (valorFormatado != null && !valorFormatado.isBlank()) {
                String cleaned = valorFormatado.replaceAll("[^\\d,]", "").replace(",", ".");
                contrato.setValor(new BigDecimal(cleaned));
            }
            contratoService.salvar(contrato);
            return "redirect:/fornecedores/" + contrato.getFornecedor().getId() + "/contratos";
        } catch (Exception e) {
            e.printStackTrace(); // Substituir por log.error se usar Logger
            redirectAttributes.addFlashAttribute("msgErro", "Erro ao salvar contrato: " + e.getMessage());
            return "redirect:/fornecedores/" + contrato.getFornecedor().getId() + "/contratos/novo";
        }
    }

    /**
     * Exibe o formulário para editar um contrato pelo ID do fornecedor e ID do contrato (via parâmetro).
     */
    @GetMapping("/{fornecedorId}/contratos/editar")
    public String editarContrato(
            @PathVariable Long fornecedorId,
            @RequestParam("idContrato") Long contratoId,
            Model model) {

        ContratoFornecedor contrato = contratoService.findById(contratoId);
        if (contrato == null) {
            return "redirect:/fornecedores/" + fornecedorId + "/contratos?erro=ContratoNaoEncontrado";
        }

        model.addAttribute("contrato", contrato);
        return "fornecedor/contrato-form";
    }

    /**
     * Exibe o formulário para editar um contrato diretamente pelo ID do contrato.
     */
    @GetMapping("/contratos/editar/{contratoId}")
    public String editarContratoDireto(@PathVariable Long contratoId, Model model) {
        ContratoFornecedor contrato = contratoService.findById(contratoId);

        if (contrato == null) {
            return "redirect:/fornecedores";
        }

        model.addAttribute("contrato", contrato);
        return "contrato/contrato-form";
    }

    /**
     * Exclui um contrato pelo ID e redireciona para os contratos do fornecedor.
     */
    @GetMapping("/contratos/excluir/{contratoId}")
    public String excluirContrato(@PathVariable Long contratoId) {
        Long fornecedorId = contratoService.findById(contratoId).getFornecedor().getId();
        contratoService.excluir(contratoId);
        return "redirect:/fornecedores/" + fornecedorId + "/contratos";
    }

}
