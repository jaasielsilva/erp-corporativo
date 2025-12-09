package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;

import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.ColaboradorService;

@Controller
@RequestMapping("/rh/relatorios")
public class RhRelatoriosController {
    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private ColaboradorService colaboradorService;
    @GetMapping("/turnover")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String turnover(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Turnover");
        return "rh/relatorios/turnover";
    }

    @GetMapping("/turnover-analytics")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String turnoverAnalytics(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Turnover Analytics");
        return "rh/relatorios/turnover-analytics";
    }

    @GetMapping("/absenteismo")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String absenteismo(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Absenteísmo");
        return "rh/relatorios/absenteismo";
    }

    @GetMapping("/headcount")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String headcount(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Headcount");
        try {
            model.addAttribute("departamentos", departamentoService.listarTodos());
            java.util.Set<String> tipos = new java.util.LinkedHashSet<>(colaboradorService.listarTiposContratoAtivosDistinct());
            model.addAttribute("tiposContrato", tipos);
        } catch (Exception ignore) {}
        return "rh/relatorios/headcount";
    }

    @GetMapping("/indicadores")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String indicadores(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Indicadores");
        return "rh/relatorios/indicadores";
    }

    @GetMapping("/admissoes-demissoes")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String admissoesDemissoes(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Admissões/Demissões");
        return "rh/relatorios/admissoes-demissoes";
    }

    @GetMapping("/ferias-beneficios")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String feriasBeneficios(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Férias e Benefícios");
        return "rh/relatorios/ferias-beneficios";
    }
}
