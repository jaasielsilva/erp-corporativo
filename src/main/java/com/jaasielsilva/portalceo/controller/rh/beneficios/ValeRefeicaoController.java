package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.model.ValeRefeicao;
import com.jaasielsilva.portalceo.service.ValeRefeicaoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/rh/beneficios/vale-refeicao")
public class ValeRefeicaoController {

    @Autowired
    private ValeRefeicaoService valeRefeicaoService;

    @Autowired
    private ColaboradorService colaboradorService;

    /**
     * Lista todos os vales refeição
     */
    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("vales", valeRefeicaoService.listarTodos());
        model.addAttribute("valesAtivos", valeRefeicaoService.listarAtivos());
        
        // Estatísticas
        LocalDate hoje = LocalDate.now();
        BigDecimal totalMes = valeRefeicaoService.calcularTotalGastoMes(hoje.getMonthValue(), hoje.getYear());
        BigDecimal subsidioMes = valeRefeicaoService.calcularSubsidioEmpresaMes(hoje.getMonthValue(), hoje.getYear());
        
        model.addAttribute("totalGastoMes", totalMes);
        model.addAttribute("subsidioEmpresaMes", subsidioMes);
        model.addAttribute("mesReferencia", hoje.getMonthValue() + "/" + hoje.getYear());
        
        return "rh/beneficios/vale-refeicao/listar";
    }

    /**
     * Exibe formulário para novo vale refeição
     */
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("vale", new ValeRefeicao());
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("tiposVale", ValeRefeicao.TipoVale.values());
        return "rh/beneficios/vale-refeicao/form";
    }

    /**
     * Salva novo vale refeição
     */
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("vale") ValeRefeicao vale, 
                        BindingResult result, 
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "rh/beneficios/vale-refeicao/form";
        }
        
        try {
            valeRefeicaoService.salvar(vale);
            redirectAttributes.addFlashAttribute("mensagem", "Vale refeição salvo com sucesso!");
            return "redirect:/rh/beneficios/vale-refeicao/listar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar vale refeição: " + e.getMessage());
            return "redirect:/rh/beneficios/vale-refeicao/novo";
        }
    }

    /**
     * Exibe formulário para editar vale refeição
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Optional<ValeRefeicao> vale = valeRefeicaoService.buscarPorId(id);
        if (vale.isEmpty()) {
            return "redirect:/rh/beneficios/vale-refeicao/listar";
        }
        
        model.addAttribute("vale", vale.get());
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("tiposVale", ValeRefeicao.TipoVale.values());
        return "rh/beneficios/vale-refeicao/form";
    }

    /**
     * Atualiza vale refeição
     */
    @PostMapping("/atualizar")
    public String atualizar(@Valid @ModelAttribute("vale") ValeRefeicao vale, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "rh/beneficios/vale-refeicao/form";
        }
        
        try {
            valeRefeicaoService.salvar(vale);
            redirectAttributes.addFlashAttribute("mensagem", "Vale refeição atualizado com sucesso!");
            return "redirect:/rh/beneficios/vale-refeicao/listar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar vale refeição: " + e.getMessage());
            return "redirect:/rh/beneficios/vale-refeicao/editar/" + vale.getId();
        }
    }

    /**
     * Exclui vale refeição
     */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            valeRefeicaoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagem", "Vale refeição excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir vale refeição: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-refeicao/listar";
    }

    /**
     * Ativa vale refeição para um colaborador
     */
    @PostMapping("/ativar")
    public String ativarVale(@RequestParam Long colaboradorId,
                            @RequestParam BigDecimal valorDiario,
                            @RequestParam ValeRefeicao.TipoVale tipo,
                            @RequestParam(required = false) String operadora,
                            RedirectAttributes redirectAttributes) {
        try {
            valeRefeicaoService.ativarVale(colaboradorId, valorDiario, tipo, operadora);
            redirectAttributes.addFlashAttribute("mensagem", "Vale refeição ativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao ativar vale refeição: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-refeicao/listar";
    }

    /**
     * Cancela vale refeição
     */
    @PostMapping("/cancelar/{id}")
    public String cancelarVale(@PathVariable Long id,
                              @RequestParam(required = false) String motivo,
                              RedirectAttributes redirectAttributes) {
        try {
            valeRefeicaoService.cancelarVale(id, motivo);
            redirectAttributes.addFlashAttribute("mensagem", "Vale refeição cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar vale refeição: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-refeicao/listar";
    }

    /**
     * Suspende vale refeição
     */
    @PostMapping("/suspender/{id}")
    public String suspenderVale(@PathVariable Long id,
                               @RequestParam(required = false) String motivo,
                               RedirectAttributes redirectAttributes) {
        try {
            valeRefeicaoService.suspenderVale(id, motivo);
            redirectAttributes.addFlashAttribute("mensagem", "Vale refeição suspenso com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao suspender vale refeição: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-refeicao/listar";
    }

    /**
     * Reativa vale refeição suspenso
     */
    @PostMapping("/reativar/{id}")
    public String reativarVale(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            valeRefeicaoService.reativarVale(id);
            redirectAttributes.addFlashAttribute("mensagem", "Vale refeição reativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao reativar vale refeição: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-refeicao/listar";
    }

    /**
     * Gera vales para todos os colaboradores no mês
     */
    @PostMapping("/gerar-mes")
    public String gerarValesDoMes(@RequestParam Integer mes,
                                 @RequestParam Integer ano,
                                 @RequestParam BigDecimal valorDiario,
                                 RedirectAttributes redirectAttributes) {
        try {
            int gerados = valeRefeicaoService.gerarValesDoMes(mes, ano, valorDiario);
            redirectAttributes.addFlashAttribute("mensagem", 
                "Gerados " + gerados + " vales refeição para " + mes + "/" + ano);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao gerar vales: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-refeicao/listar";
    }

    /**
     * Lista vales de um colaborador específico
     */
    @GetMapping("/colaborador/{colaboradorId}")
    public String listarPorColaborador(@PathVariable Long colaboradorId, Model model) {
        try {
            model.addAttribute("vales", valeRefeicaoService.listarPorColaborador(colaboradorId));
            model.addAttribute("colaborador", colaboradorService.buscarPorId(colaboradorId));
            return "rh/beneficios/vale-refeicao/por-colaborador";
        } catch (Exception e) {
            return "redirect:/rh/beneficios/vale-refeicao/listar";
        }
    }

    /**
     * Relatórios de vales refeição
     */
    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        LocalDate hoje = LocalDate.now();
        
        // Dados do mês atual
        BigDecimal totalMes = valeRefeicaoService.calcularTotalGastoMes(hoje.getMonthValue(), hoje.getYear());
        BigDecimal subsidioMes = valeRefeicaoService.calcularSubsidioEmpresaMes(hoje.getMonthValue(), hoje.getYear());
        
        model.addAttribute("totalGastoMes", totalMes);
        model.addAttribute("subsidioEmpresaMes", subsidioMes);
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        model.addAttribute("valesAtivos", valeRefeicaoService.listarAtivos());
        
        return "rh/beneficios/vale-refeicao/relatorios";
    }
}
