package com.jaasielsilva.portalceo.controller.rh.folha;

import com.jaasielsilva.portalceo.model.FolhaPagamento;
import com.jaasielsilva.portalceo.model.Holerite;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.FolhaPagamentoService;
import com.jaasielsilva.portalceo.service.HoleriteService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/rh/folha-pagamento")
public class FolhaPagamentoController {

    @Autowired
    private FolhaPagamentoService folhaPagamentoService;

    @Autowired
    private HoleriteService holeriteService;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Página principal da folha de pagamento
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("folhasRecentes", folhaPagamentoService.buscarFolhasRecentes());
        
        // Dados do mês atual
        LocalDate hoje = LocalDate.now();
        Optional<FolhaPagamento> folhaAtual = folhaPagamentoService.buscarPorMesAno(hoje.getMonthValue(), hoje.getYear());
        model.addAttribute("folhaAtual", folhaAtual.orElse(null));
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        
        return "rh/folha-pagamento/index";
    }

    /**
     * Formulário para gerar nova folha de pagamento
     */
    @GetMapping("/gerar")
    public String gerar(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        
        // Verificar se já existe folha para o mês atual
        LocalDate hoje = LocalDate.now();
        boolean existeFolha = folhaPagamentoService.existeFolhaPorMesAno(hoje.getMonthValue(), hoje.getYear());
        model.addAttribute("existeFolhaAtual", existeFolha);
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        
        return "rh/folha-pagamento/gerar";
    }

    /**
     * Processa geração da folha de pagamento
     */
    @PostMapping("/processar")
    public String processar(@RequestParam Integer mes, 
                           @RequestParam Integer ano,
                           RedirectAttributes redirectAttributes) {
        try {
            // Obter usuário logado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            FolhaPagamento folha = folhaPagamentoService.gerarFolhaPagamento(mes, ano, usuario);
            
            redirectAttributes.addFlashAttribute("mensagem", 
                "Folha de pagamento " + mes + "/" + ano + " gerada com sucesso!");
            
            return "redirect:/rh/folha-pagamento/visualizar/" + folha.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao gerar folha: " + e.getMessage());
            return "redirect:/rh/folha-pagamento/gerar";
        }
    }

    /**
     * Visualiza uma folha de pagamento específica
     */
    @GetMapping("/visualizar/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        Optional<FolhaPagamento> folhaOpt = folhaPagamentoService.buscarPorId(id);
        if (folhaOpt.isEmpty()) {
            return "redirect:/rh/folha-pagamento";
        }
        
        FolhaPagamento folha = folhaOpt.get();
        model.addAttribute("folha", folha);
        model.addAttribute("holerites", holeriteService.listarPorFolha(id));
        model.addAttribute("resumo", holeriteService.calcularResumoFolha(id));
        
        return "rh/folha-pagamento/visualizar";
    }

    /**
     * Lista todas as folhas de pagamento
     */
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) Integer ano, Model model) {
        if (ano != null) {
            model.addAttribute("folhas", folhaPagamentoService.buscarPorAno(ano));
            model.addAttribute("anoSelecionado", ano);
        } else {
            model.addAttribute("folhas", folhaPagamentoService.listarTodas());
        }
        
        return "rh/folha-pagamento/listar";
    }

    /**
     * Fecha uma folha de pagamento
     */
    @PostMapping("/fechar/{id}")
    public String fechar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            folhaPagamentoService.fecharFolha(id, usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Folha de pagamento fechada com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao fechar folha: " + e.getMessage());
        }
        
        return "redirect:/rh/folha-pagamento/visualizar/" + id;
    }

    /**
     * Cancela uma folha de pagamento
     */
    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            folhaPagamentoService.cancelarFolha(id, usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Folha de pagamento cancelada com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar folha: " + e.getMessage());
        }
        
        return "redirect:/rh/folha-pagamento";
    }

    /**
     * Exibe holerite individual
     */
    @GetMapping("/holerite/{id}")
    public String holerite(@PathVariable Long id, Model model) {
        Optional<Holerite> holeriteOpt = holeriteService.buscarPorId(id);
        if (holeriteOpt.isEmpty()) {
            return "redirect:/rh/folha-pagamento";
        }
        
        Holerite holerite = holeriteOpt.get();
        model.addAttribute("holerite", holerite);
        model.addAttribute("numeroHolerite", holeriteService.gerarNumeroHolerite(holerite));
        model.addAttribute("periodoReferencia", holeriteService.gerarDescricaoPeriodo(holerite));
        model.addAttribute("dataReferencia", holeriteService.gerarDataReferencia(holerite));
        
        return "rh/folha-pagamento/holerite";
    }

    /**
     * Lista holerites por colaborador
     */
    @GetMapping("/holerites/colaborador/{colaboradorId}")
    public String holeritesPorColaborador(@PathVariable Long colaboradorId, Model model) {
        try {
            model.addAttribute("holerites", holeriteService.listarPorColaborador(colaboradorId));
            model.addAttribute("colaborador", colaboradorService.buscarPorId(colaboradorId));
            return "rh/folha-pagamento/holerites-colaborador";
        } catch (Exception e) {
            return "redirect:/rh/folha-pagamento";
        }
    }

    /**
     * Página de descontos
     */
    @GetMapping("/descontos")
    public String descontos(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/folha-pagamento/descontos";
    }

    /**
     * Relatórios da folha de pagamento
     */
    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("folhasRecentes", folhaPagamentoService.buscarFolhasRecentes());
        
        // Dados do mês atual para resumo
        LocalDate hoje = LocalDate.now();
        Optional<FolhaPagamento> folhaAtual = folhaPagamentoService.buscarPorMesAno(hoje.getMonthValue(), hoje.getYear());
        if (folhaAtual.isPresent()) {
            model.addAttribute("resumoAtual", holeriteService.calcularResumoFolha(folhaAtual.get().getId()));
        }
        
        return "rh/folha-pagamento/relatorios";
    }
}
