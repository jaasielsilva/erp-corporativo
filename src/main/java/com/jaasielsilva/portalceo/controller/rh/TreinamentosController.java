package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;

@Controller
@RequestMapping("/rh/treinamentos")
public class TreinamentosController {
    @Autowired
    private AuditoriaRhLogService auditoriaService;

    @GetMapping({"", "/"})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String index(HttpServletRequest request) {
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos", usuario, ip, "Acesso ao índice de Treinamentos", true); } catch (Exception ignore) {}
        return "redirect:/rh/treinamentos/relatorios";
    }

    @GetMapping({"/cadastro", "/cadastro."})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String cadastro(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Cadastro");
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/cadastro", usuario, ip, "Acesso ao cadastro de Treinamentos", true); } catch (Exception ignore) {}
        return "rh/treinamentos/cadastro";
    }

    @GetMapping({"/certificado", "/certificado."})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String certificado(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Certificado");
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/certificado", usuario, ip, "Acesso à emissão de certificado", true); } catch (Exception ignore) {}
        return "rh/treinamentos/certificado";
    }

    @GetMapping({"/inscricao", "/inscricao."})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String inscricao(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Inscrição");
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/inscricao", usuario, ip, "Acesso à inscrição em Treinamentos", true); } catch (Exception ignore) {}
        return "rh/treinamentos/inscricao";
    }

    @GetMapping("/turmas/{id}")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String turmaDetalhe(@org.springframework.web.bind.annotation.PathVariable Long id, Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Detalhe da Turma");
        model.addAttribute("turmaId", id);
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/turmas/"+id, usuario, ip, "Acesso ao detalhe da turma", true); } catch (Exception ignore) {}
        return "rh/treinamentos/turma-detalhe";
    }
    @GetMapping("/cursos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String cursos(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Cursos");
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/cursos", usuario, ip, "Acesso à listagem de cursos", true); } catch (Exception ignore) {}
        return "rh/treinamentos/cursos";
    }

    @GetMapping("/instrutores")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String instrutores(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Instrutores");
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/instrutores", usuario, ip, "Acesso à listagem de instrutores", true); } catch (Exception ignore) {}
        return "rh/treinamentos/instrutores";
    }

    @GetMapping("/turmas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String turmas(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Turmas");
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/turmas", usuario, ip, "Acesso à listagem de turmas", true); } catch (Exception ignore) {}
        return "rh/treinamentos/turmas";
    }
    @GetMapping("/relatorios")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String relatorios(Model model, HttpServletRequest request) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Relatórios");
        try { String usuario = null; String ip = request != null ? request.getRemoteAddr() : null; auditoriaService.registrar("ACESSO", "ACESSO_PAGINA", "/rh/treinamentos/relatorios", usuario, ip, "Acesso aos relatórios de Treinamentos", true); } catch (Exception ignore) {}
        return "rh/treinamentos/relatorios";
    }
}
