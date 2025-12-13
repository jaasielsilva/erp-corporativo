package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;

@Controller
@RequestMapping("/rh/auditoria")
public class RhAuditoriaController {

    @Autowired
    private AuditoriaRhLogService auditoriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String index(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Início");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/auditoria", usuario, ip, "Visualização da página de Auditoria RH - Início", true);
        } catch (Exception ignore) {}
        return "rh/auditoria/index";
    }

    @GetMapping("/acessos")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String acessos(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Log de Acessos");
        model.addAttribute("categoria", "ACESSO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/auditoria/acessos", usuario, ip, "Visualização da página Log de Acessos RH", true);
        } catch (Exception ignore) {}
        return "rh/auditoria/acessos";
    }

    @GetMapping("/alteracoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String alteracoes(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Alterações de Dados");
        model.addAttribute("categoria", "ALTERACAO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/auditoria/alteracoes", usuario, ip, "Visualização da página Alterações de Dados RH", true);
        } catch (Exception ignore) {}
        return "rh/auditoria/alteracoes";
    }

    @GetMapping("/exportacoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String exportacoes(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Exportações");
        model.addAttribute("categoria", "EXPORTACAO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/auditoria/exportacoes", usuario, ip, "Visualização da página Exportações RH", true);
        } catch (Exception ignore) {}
        return "rh/auditoria/exportacoes";
    }

    @GetMapping("/revisoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String revisoes(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Revisões Periódicas");
        model.addAttribute("categoria", "REVISAO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/auditoria/revisoes", usuario, ip, "Visualização da página Revisões Periódicas RH", true);
        } catch (Exception ignore) {}
        return "rh/auditoria/revisoes";
    }
}
