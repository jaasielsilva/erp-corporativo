package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ContaPagar;
import com.jaasielsilva.portalceo.model.ContratoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.service.ContratoFornecedorService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import com.jaasielsilva.portalceo.service.ContaPagarService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private ContaPagarService contaPagarService;

    /**
     * Lista fornecedores ativos, paginados.
     * @param pagina Número da página atual (começa em 0)
     * @param tamanho Quantidade de itens por página
     */
    @GetMapping
    public String listar(@RequestParam(value = "busca", required = false) String busca,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "20") int size,
                         Model model) {
        Page<Fornecedor> fornecedoresPage = fornecedorService.listarPaginado(busca, status, page, size);
        model.addAttribute("fornecedores", fornecedoresPage.getContent());
        model.addAttribute("currentPage", fornecedoresPage.getNumber());
        model.addAttribute("totalPages", fornecedoresPage.getTotalPages());
        model.addAttribute("totalElements", fornecedoresPage.getTotalElements());
        model.addAttribute("hasPrevious", fornecedoresPage.hasPrevious());
        model.addAttribute("hasNext", fornecedoresPage.hasNext());
        model.addAttribute("busca", busca);
        model.addAttribute("statusFiltro", status);
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
    public String salvar(@ModelAttribute Fornecedor fornecedor, RedirectAttributes redirectAttributes, Model model) {
        try {
            boolean edicao = fornecedor.getId() != null;
            fornecedorService.salvar(fornecedor);
            redirectAttributes.addFlashAttribute("msgSucesso", edicao ? "Fornecedor atualizado com sucesso." : "Fornecedor cadastrado com sucesso.");
            return "redirect:/fornecedores";
        } catch (IllegalArgumentException e) {
            model.addAttribute("msgErro", e.getMessage());
            return "fornecedor/form";
        } catch (Exception e) {
            model.addAttribute("msgErro", "Erro ao salvar fornecedor: " + e.getMessage());
            return "fornecedor/form";
        }
    }

    /**
     * Exibe o formulário de edição de um fornecedor pelo ID.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Fornecedor fornecedor = fornecedorService.findById(id);
        if (fornecedor == null) {
            redirectAttributes.addFlashAttribute("msgErro", "Fornecedor não encontrado.");
            return "redirect:/fornecedores";
        }
        model.addAttribute("fornecedor", fornecedor);
        return "fornecedor/form";
    }

    /**
     * Realiza a exclusão lógica de um fornecedor (seta como inativo).
     */
    @GetMapping("/excluir/{id}")
    public String excluirFornecedor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fornecedorService.excluir(id);
            redirectAttributes.addFlashAttribute("msgSucesso", "Fornecedor excluído com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msgErro", "Erro ao excluir fornecedor: " + e.getMessage());
        }
        return "redirect:/fornecedores";
    }

    // --- Pagamentos ---

    /**
     * Lista todos os pagamentos (contas a pagar) de um fornecedor específico.
     */
    @GetMapping("/{id}/pagamentos")
    public String listarPagamentos(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Fornecedor fornecedor = fornecedorService.findById(id);
        if (fornecedor == null) {
            redirectAttributes.addFlashAttribute("msgErro", "Fornecedor não encontrado.");
            return "redirect:/fornecedores";
        }
        List<ContaPagar> pagamentos = contaPagarService.listarPorFornecedor(fornecedor);
        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("pagamentos", pagamentos);
        return "fornecedor/pagamentos";
    }

    // --- Contratos ---

    /**
     * Lista todos os contratos de um fornecedor específico.
     */
    @GetMapping("/{id}/contratos")
    public String listarContratos(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Fornecedor fornecedor = fornecedorService.findById(id);
        if (fornecedor == null) {
            redirectAttributes.addFlashAttribute("msgErro", "Fornecedor não encontrado.");
            return "redirect:/fornecedores";
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
        contrato.setNumeroContrato(contratoService.gerarProximoNumeroContrato());
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
            redirectAttributes.addFlashAttribute("msgSucesso", "Contrato salvo com sucesso.");
            return "redirect:/fornecedores/" + contrato.getFornecedor().getId() + "/contratos";
        } catch (Exception e) {
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
            Model model,
            RedirectAttributes redirectAttributes) {

        ContratoFornecedor contrato;
        try {
            contrato = contratoService.findById(contratoId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msgErro", "Contrato não encontrado.");
            return "redirect:/fornecedores/" + fornecedorId + "/contratos";
        }

        model.addAttribute("contrato", contrato);
        return "fornecedor/contrato-form";
    }

    /**
     * Exibe o formulário para editar um contrato diretamente pelo ID do contrato.
     */
    @GetMapping("/contratos/editar/{contratoId}")
    public String editarContratoDireto(@PathVariable Long contratoId, Model model, RedirectAttributes redirectAttributes) {
        ContratoFornecedor contrato;
        try {
            contrato = contratoService.findById(contratoId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msgErro", "Contrato não encontrado.");
            return "redirect:/fornecedores";
        }

        model.addAttribute("contrato", contrato);
        return "contrato/contrato-form";
    }

    /**
     * Exclui um contrato pelo ID e redireciona para os contratos do fornecedor.
     */
    @GetMapping("/contratos/excluir/{contratoId}")
    public String excluirContrato(@PathVariable Long contratoId, RedirectAttributes redirectAttributes) {
        Long fornecedorId = null;
        try {
            fornecedorId = contratoService.findById(contratoId).getFornecedor().getId();
            contratoService.excluir(contratoId);
            redirectAttributes.addFlashAttribute("msgSucesso", "Contrato excluído com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msgErro", "Erro ao excluir contrato: " + e.getMessage());
        }
        if (fornecedorId != null) {
            return "redirect:/fornecedores/" + fornecedorId + "/contratos";
        }
        return "redirect:/fornecedores";
    }

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarAjax(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) String status) {
        try {
            Page<Fornecedor> pagina = fornecedorService.listarPaginado(q, status, page, size);

            List<Map<String, Object>> content = pagina.getContent().stream().map(f -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", f.getId());
                m.put("razaoSocial", f.getRazaoSocial());
                m.put("cnpj", f.getCnpj());
                m.put("telefone", f.getTelefone());
                m.put("email", f.getEmail());
                m.put("status", f.getStatus());
                m.put("ativo", f.getAtivo());
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> resp = new HashMap<>();
            resp.put("content", content);
            resp.put("currentPage", pagina.getNumber());
            resp.put("totalPages", pagina.getTotalPages());
            resp.put("totalElements", pagina.getTotalElements());
            resp.put("hasPrevious", pagina.hasPrevious());
            resp.put("hasNext", pagina.hasNext());
            
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
             return ResponseEntity.status(500).body(Map.of("erro", "Falha ao carregar fornecedores: " + e.getMessage()));
        }
    }

}
