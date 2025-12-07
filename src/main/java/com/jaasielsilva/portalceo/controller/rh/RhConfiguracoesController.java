package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;

import com.jaasielsilva.portalceo.model.RhPoliticaFerias;
import com.jaasielsilva.portalceo.repository.RhPoliticaFeriasRepository;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;

@Controller
@RequestMapping("/rh/configuracoes")
public class RhConfiguracoesController {

    @Autowired
    private RhPoliticaFeriasRepository feriasRepository;

    @Autowired
    private AuditoriaRhLogService auditoriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String index(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Configurações RH - Início");
        try {
            String usuario = null;
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/configuracoes", usuario, ip, "Visualização da página de Configurações RH", true);
        } catch (Exception ignore) {}
        return "rh/configuracoes/index";
    }

    @GetMapping("/politicas-ferias")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String politicasFerias(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Configurações RH - Políticas de Férias");
        RhPoliticaFerias atual = feriasRepository.findAll().stream().findFirst().orElse(new RhPoliticaFerias());
        model.addAttribute("politica", atual);
        return "rh/configuracoes/politicas-ferias";
    }

    @GetMapping("/ponto")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String ponto(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Configurações RH - Parâmetros de Ponto");
        return "rh/configuracoes/ponto";
    }

    @GetMapping("/integracoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String integracoes(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Configurações RH - Integrações");
        return "rh/configuracoes/integracoes";
    }

    @PostMapping("/politicas-ferias")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String salvarPoliticasFerias(@ModelAttribute("politica") RhPoliticaFerias politica, Model model) {
        RhPoliticaFerias salvo = feriasRepository.save(politica);
        auditoriaService.registrar("CONFIGURACAO", "SALVAR_POLITICAS_FERIAS", "/rh/configuracoes/politicas-ferias", null, null,
                "Atualizado id=" + salvo.getId(), true);
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Configurações RH - Políticas de Férias");
        model.addAttribute("politica", salvo);
        model.addAttribute("successMessage", "Políticas de férias salvas com sucesso");
        return "rh/configuracoes/politicas-ferias";
    }
}
