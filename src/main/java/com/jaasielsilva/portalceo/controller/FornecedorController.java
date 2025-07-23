package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.ContratoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.ContratoFornecedorService;
import com.jaasielsilva.portalceo.service.FornecedorService;

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
     * Lista fornecedores ativos para mostrar na tela.
     * Como usamos exclusão lógica, exibir só os ativos evita confusão.
     */
    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {
        Page<Fornecedor> paginaFornecedores = fornecedorService.listarTodosPaginado(pagina, tamanho);
        model.addAttribute("pagina", paginaFornecedores);
        return "fornecedor/listar"; // caminho para o HTML
    }

    /**
     * Exibe o formulário para criar um novo fornecedor.
     */
    @GetMapping("/novo")
    public String novoFornecedor(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedor/form";
    }

    /**
     * Salva um novo fornecedor ou atualiza um existente.
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor) {
        fornecedorService.salvar(fornecedor);
        return "redirect:/fornecedores";
    }

    /**
     * Exibe o formulário para editar um fornecedor existente pelo id.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.findById(id));
        return "fornecedor/editar";
    }

    /**
     * Exclusão lógica: inativa o fornecedor ao invés de excluir do banco.
     * Isso evita erros de integridade e mantém histórico.
     */
    @GetMapping("/excluir/{id}")
    public String excluirFornecedor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        fornecedorService.excluir(id);
        redirectAttributes.addFlashAttribute("msgSucesso", "Fornecedor excluído com sucesso.");
        return "redirect:/fornecedores?pagina=0&tamanho=10"; // redireciona para a primeira página
    }

    // --- Métodos relacionados a contratos ---

    /**
     * Lista os contratos de um fornecedor pelo id do fornecedor.
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
    return "contrato/listar"; // view que lista os contratos desse fornecedor
}


    @GetMapping("/contratos")
    public String listarTodosContratos(Model model) {
    List<ContratoFornecedor> contratos = contratoService.listarTodos();
    model.addAttribute("contratos", contratos);
    return "contrato/listar";
}


    /**
     * Exibe o formulário para criar um novo contrato para o fornecedor especificado
     * pelo id.
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
        return "contrato/contrato-form"; // usa a view localizada em: templates/contrato/contrato-form.html
    }

    /**
     * Salva um novo contrato ou atualiza um contrato existente.
     */
    @PostMapping("/contratos/salvar")
    public String salvarContrato(@ModelAttribute ContratoFornecedor contrato) {
        contratoService.salvar(contrato);
        return "redirect:/fornecedores/" + contrato.getFornecedor().getId() + "/contratos";
    }

    /**
     * Exibe o formulário para editar um contrato existente pelo id do contrato.
     */
    @GetMapping("/{fornecedorId}/contratos/editar")
    public String editarContrato(
            @PathVariable Long fornecedorId,
            @RequestParam("idContrato") Long contratoId,
            Model model) {

        ContratoFornecedor contrato = contratoService.findById(contratoId);
        model.addAttribute("contrato", contrato);
        return "contrato/contrato-form";
    }

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
     * Exclui um contrato pelo id e redireciona para a lista de contratos do
     * fornecedor.
     */
    @GetMapping("/contratos/excluir/{contratoId}")
    public String excluirContrato(@PathVariable Long contratoId) {
        Long fornecedorId = contratoService.findById(contratoId).getFornecedor().getId();
        contratoService.excluir(contratoId);
        return "redirect:/fornecedores/" + fornecedorId + "/contratos";
    }

}
