package com.jaasielsilva.portalceo.controller.financeiro;

import com.jaasielsilva.portalceo.model.ContaBancaria;
import com.jaasielsilva.portalceo.model.FluxoCaixa;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ContaBancariaService;
import com.jaasielsilva.portalceo.service.FluxoCaixaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/financeiro/contas-bancarias")
@PreAuthorize("hasAuthority('FINANCEIRO_VER_SALDO')")
public class ContaBancariaController {

    @Autowired
    private ContaBancariaService contaBancariaService;

    @Autowired
    private FluxoCaixaService fluxoCaixaService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("contas", contaBancariaService.listarContasAtivas());
        model.addAttribute("pendentes", fluxoCaixaService.listarPendentesAprovacao());
        model.addAttribute("pageTitle", "Contas Bancárias");
        return "financeiro/contas-bancarias/lista";
    }

    @PostMapping("/aprovar/{id}")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR')")
    public String aprovar(@PathVariable Long id, @RequestAttribute("usuarioLogado") Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            fluxoCaixaService.aprovarAjuste(id, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Ajuste aprovado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao aprovar: " + e.getMessage());
        }
        return "redirect:/financeiro/contas-bancarias";
    }

    @PostMapping("/rejeitar/{id}")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR')")
    public String rejeitar(@PathVariable Long id, @RequestAttribute("usuarioLogado") Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            fluxoCaixaService.rejeitarAjuste(id, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Solicitação rejeitada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao rejeitar: " + e.getMessage());
        }
        return "redirect:/financeiro/contas-bancarias";
    }

    /**
     * Processa movimentações manuais (depósitos ou saques) nas contas bancárias.
     * 
     * Este método:
     * 1. Recebe os dados do formulário (ID da conta, tipo de operação, valor e descrição).
     * 2. Converte e valida o valor monetário.
     * 3. Executa a operação de crédito ou débito via serviço.
     * 4. Registra a operação no Fluxo de Caixa para auditoria.
     * 
     * @param contaId ID da conta bancária alvo
     * @param tipo Tipo de operação: "CREDITO" ou "DEBITO"
     * @param valorStr Valor em string (formato monetário)
     * @param descricao Motivo da movimentação
     * @param usuario Usuário logado que realizou a ação
     * @param redirectAttributes Atributos para mensagens de feedback
     * @return Redirecionamento para a lista de contas
     */
    @PostMapping("/movimentacao")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAR')")
    public String movimentacaoManual(
            @RequestParam Long contaId,
            @RequestParam String tipo, // "CREDITO" ou "DEBITO"
            @RequestParam String valorStr,
            @RequestParam String descricao,
            @RequestAttribute("usuarioLogado") Usuario usuario,
            RedirectAttributes redirectAttributes) {

        try {
            // Converte string monetária (ex: 1.000,00) para BigDecimal
            BigDecimal valor = new BigDecimal(valorStr.replace(".", "").replace(",", "."));
            
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("O valor deve ser positivo.");
            }

            FluxoCaixa fluxo = new FluxoCaixa();
            fluxo.setDescricao(descricao + " (Ajuste Manual)");
            fluxo.setValor(valor);
            fluxo.setData(LocalDate.now());
            fluxo.setContaBancaria(contaBancariaService.buscarPorId(contaId).orElse(null));
            fluxo.setObservacoes("Solicitação de ajuste por " + usuario.getNome());

            if ("CREDITO".equalsIgnoreCase(tipo)) {
                fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.ENTRADA);
                fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.OUTRAS_RECEITAS);
            } else {
                fluxo.setTipoMovimento(FluxoCaixa.TipoMovimento.SAIDA);
                fluxo.setCategoria(FluxoCaixa.CategoriaFluxo.OUTRAS_DESPESAS);
            }

            fluxoCaixaService.solicitarAjuste(fluxo, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Solicitação de ajuste enviada para aprovação!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao realizar movimentação: " + e.getMessage());
        }

        return "redirect:/financeiro/contas-bancarias";
    }
}
