package com.jaasielsilva.portalceo.controller.juridico;

import com.jaasielsilva.portalceo.service.AuditoriaJuridicoLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/juridico/auditoria")
public class JuridicoAuditoriaController {

    @Autowired
    private AuditoriaJuridicoLogService auditoriaService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MASTER','ROLE_JURIDICO_GERENTE')")
    public String index(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "Jurídico");
        model.addAttribute("titulo", "Auditoria Jurídico - Início");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/juridico/auditoria", usuario, ip, "Visualização da página de Auditoria Jurídico - Início", true);
        } catch (Exception ignore) {}
        return "juridico/auditoria/index";
    }

    @GetMapping("/acessos")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MASTER','ROLE_JURIDICO_GERENTE')")
    public String acessos(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "Jurídico");
        model.addAttribute("titulo", "Auditoria Jurídico - Log de Acessos");
        model.addAttribute("categoria", "ACESSO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/juridico/auditoria/acessos", usuario, ip, "Visualização da página Log de Acessos Jurídico", true);
        } catch (Exception ignore) {}
        return "juridico/auditoria/acessos";
    }

    @GetMapping("/alteracoes")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MASTER','ROLE_JURIDICO_GERENTE')")
    public String alteracoes(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "Jurídico");
        model.addAttribute("titulo", "Auditoria Jurídico - Alterações de Dados");
        model.addAttribute("categoria", "ALTERACAO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/juridico/auditoria/alteracoes", usuario, ip, "Visualização da página Alterações de Dados Jurídico", true);
        } catch (Exception ignore) {}
        return "juridico/auditoria/alteracoes";
    }

    @GetMapping("/exportacoes")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MASTER','ROLE_JURIDICO_GERENTE')")
    public String exportacoes(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "Jurídico");
        model.addAttribute("titulo", "Auditoria Jurídico - Exportações");
        model.addAttribute("categoria", "EXPORTACAO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/juridico/auditoria/exportacoes", usuario, ip, "Visualização da página Exportações Jurídico", true);
        } catch (Exception ignore) {}
        return "juridico/auditoria/exportacoes";
    }

    @GetMapping("/revisoes")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MASTER','ROLE_JURIDICO_GERENTE')")
    public String revisoes(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "Jurídico");
        model.addAttribute("titulo", "Auditoria Jurídico - Revisões Periódicas");
        model.addAttribute("categoria", "REVISAO");
        try {
            String usuario = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) { usuario = auth.getName(); }
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/juridico/auditoria/revisoes", usuario, ip, "Visualização da página Revisões Periódicas Jurídico", true);
        } catch (Exception ignore) {}
        return "juridico/auditoria/revisoes";
    }
}
