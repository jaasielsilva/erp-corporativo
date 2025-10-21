package com.jaasielsilva.portalceo.controller.financeiro;

import com.jaasielsilva.portalceo.model.ContaPagar;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ContaPagarService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/financeiro/contas-pagar")
@PreAuthorize("@globalControllerAdvice.podeAcessarFinanceiro()")
public class ContaPagarController {

    @Autowired
    private ContaPagarService contaPagarService;

    @Autowired
    private FornecedorService fornecedorService;

    // ================= LISTAR =================
    @GetMapping
    public String listar(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam(value = "categoria", required = false) String categoria,
            Model model) {

        List<ContaPagar> contas = contaPagarService.listarTodas();

        if (texto != null && !texto.isEmpty()) {
            contas = contas.stream()
                    .filter(c -> c.getDescricao() != null
                            && c.getDescricao().toLowerCase().contains(texto.toLowerCase()))
                    .toList();
        }

        if (status != null && !status.isEmpty()) {
            try {
                ContaPagar.StatusContaPagar statusEnum = ContaPagar.StatusContaPagar.valueOf(status.toUpperCase());
                contas = contas.stream()
                        .filter(c -> c.getStatus() == statusEnum)
                        .toList();
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (categoria != null && !categoria.isEmpty()) {
            contas = contas.stream()
                    .filter(c -> c.getCategoria() != null && c.getCategoria().name().equalsIgnoreCase(categoria))
                    .toList();
        }

        model.addAttribute("contas", contas);
        model.addAttribute("statusEnum", ContaPagar.StatusContaPagar.values());
        model.addAttribute("categorias", ContaPagar.CategoriaContaPagar.values());

        BigDecimal totalPendente = contas.stream()
                .filter(c -> c.getStatus() == ContaPagar.StatusContaPagar.PENDENTE)
                .map(ContaPagar::getValorOriginal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPago = contas.stream()
                .filter(c -> c.getStatus() == ContaPagar.StatusContaPagar.PAGA)
                .map(ContaPagar::getValorOriginal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVencido = contas.stream()
                .filter(c -> c.getStatus() == ContaPagar.StatusContaPagar.VENCIDA)
                .map(ContaPagar::getValorOriginal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalPendente", totalPendente);
        model.addAttribute("totalPago", totalPago);
        model.addAttribute("totalVencido", totalVencido);

        model.addAttribute("texto", texto);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("categoriaSelecionada", categoria);

        return "financeiro/contas-pagar/lista";
    }

    // ================= CRIAR =================
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("contaPagar", new ContaPagar());
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        model.addAttribute("categorias", ContaPagar.CategoriaContaPagar.values());
        model.addAttribute("statusOptions", ContaPagar.StatusContaPagar.values());
        model.addAttribute("pageTitle", "Nova Conta a Pagar");
        return "financeiro/contas-pagar/conta-pagar-form";
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("contaPagar") ContaPagar contaPagar,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            @ModelAttribute("usuarioLogado") Usuario usuario) {

        if (result.hasErrors()) {
            model.addAttribute("fornecedores", fornecedorService.listarAtivos());
            model.addAttribute("categorias", ContaPagar.CategoriaContaPagar.values());
            model.addAttribute("statusOptions", ContaPagar.StatusContaPagar.values());
            return "financeiro/contas-pagar/conta-pagar-form";
        }

        try {
            contaPagarService.salvar(contaPagar, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Conta salva com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
        }

        return "redirect:/financeiro/contas-pagar";
    }

    // ================= EDITAR =================
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ContaPagar> contaOpt = contaPagarService.buscarPorId(id);
        if (contaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Conta não encontrada");
            return "redirect:/financeiro/contas-pagar";
        }
        model.addAttribute("contaPagar", contaOpt.get());
        model.addAttribute("fornecedores", fornecedorService.listarAtivos());
        model.addAttribute("categorias", ContaPagar.CategoriaContaPagar.values());
        model.addAttribute("statusOptions", ContaPagar.StatusContaPagar.values());
        model.addAttribute("pageTitle", "Editar Conta a Pagar");
        return "financeiro/contas-pagar/conta-pagar-form";
    }

    // ================= DETALHES =================
    @GetMapping("/detalhes/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ContaPagar> contaOpt = contaPagarService.buscarPorId(id);
        if (contaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Conta não encontrada");
            return "redirect:/financeiro/contas-pagar";
        }
        model.addAttribute("conta", contaOpt.get());
        return "financeiro/contas-pagar/detalhes";
    }

    // ================= EXCLUIR =================
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contaPagarService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Conta excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/financeiro/contas-pagar";
    }

    // ================= APROVAR =================
    @PostMapping("/aprovar/{id}")
    public String aprovar(@PathVariable Long id,
            @ModelAttribute("usuarioLogado") Usuario usuario,
            RedirectAttributes redirectAttributes) {
        try {
            contaPagarService.aprovar(id, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Conta aprovada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao aprovar: " + e.getMessage());
        }
        return "redirect:/financeiro/contas-pagar";
    }

    // ================= PAGAR =================
    @PostMapping("/pagar/{id}")
    public String pagar(@PathVariable Long id,
            @RequestParam("valorPago") BigDecimal valorPago,
            @RequestParam(value = "formaPagamento", required = false) String formaPagamento,
            @ModelAttribute("usuarioLogado") Usuario usuario,
            RedirectAttributes redirectAttributes) {
        try {
            contaPagarService.efetuarPagamento(id, valorPago, formaPagamento, usuario, null);
            redirectAttributes.addFlashAttribute("sucesso", "Conta paga com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao pagar: " + e.getMessage());
        }
        return "redirect:/financeiro/contas-pagar";
    }

    // ================= CANCELAR =================
    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id,
            @RequestParam("motivo") String motivo,
            RedirectAttributes redirectAttributes) {
        try {
            contaPagarService.cancelar(id, motivo);
            redirectAttributes.addFlashAttribute("sucesso", "Conta cancelada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar: " + e.getMessage());
        }
        return "redirect:/financeiro/contas-pagar";
    }

    // ================= HISTÓRICO =================
    @GetMapping("/historico/{id}")
    public String historico(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ContaPagar> contaOpt = contaPagarService.buscarPorId(id);
        if (contaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Conta não encontrada");
            return "redirect:/financeiro/contas-pagar";
        }

        model.addAttribute("conta", contaOpt.get());
        model.addAttribute("historico", contaPagarService.listarHistorico(id));
        return "financeiro/contas-pagar/historico";
    }
}
