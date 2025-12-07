package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rh/treinamentos")
public class TreinamentosController {
    @GetMapping({"", "/"})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String index() {
        return "redirect:/rh/treinamentos/relatorios";
    }

    @GetMapping({"/cadastro", "/cadastro."})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String cadastro(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Cadastro");
        return "rh/treinamentos/cadastro";
    }

    @GetMapping({"/certificado", "/certificado."})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String certificado(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Certificado");
        return "rh/treinamentos/certificado";
    }

    @GetMapping({"/inscricao", "/inscricao."})
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String inscricao(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Inscrição");
        return "rh/treinamentos/inscricao";
    }

    @GetMapping("/turmas/{id}")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String turmaDetalhe(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Detalhe da Turma");
        model.addAttribute("turmaId", id);
        return "rh/treinamentos/turma-detalhe";
    }
    @GetMapping("/cursos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String cursos(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Cursos");
        return "rh/treinamentos/cursos";
    }

    @GetMapping("/instrutores")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String instrutores(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Instrutores");
        return "rh/treinamentos/instrutores";
    }

    @GetMapping("/turmas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String turmas(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Turmas");
        return "rh/treinamentos/turmas";
    }
    @GetMapping("/relatorios")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String relatorios(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Relatórios");
        return "rh/treinamentos/relatorios";
    }
}
