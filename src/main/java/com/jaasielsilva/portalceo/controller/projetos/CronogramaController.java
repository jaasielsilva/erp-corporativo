package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.projetos.Projeto;
import com.jaasielsilva.portalceo.service.projetos.ProjetoService;
import com.jaasielsilva.portalceo.service.projetos.TarefaProjetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projetos/cronograma")
public class CronogramaController {

    @Autowired
    private ProjetoService projetoService;

    @Autowired
    private TarefaProjetoService tarefaService;

    @GetMapping("/visualizar")
    public String visualizar(Model model) {
        model.addAttribute("pageTitle", "Cronograma de Projetos");
        return "projetos/cronograma/visualizar";
    }

    @GetMapping("/visualizar/{id}")
    public String visualizarProjeto(@PathVariable Long id, Model model) {
        Projeto projeto = projetoService.buscarPorIdComEquipeMembros(id).orElse(null);
        model.addAttribute("pageTitle", "Cronograma de Projetos");
        model.addAttribute("projeto", projeto);
        if (projeto != null) {
            model.addAttribute("tarefas", tarefaService.listarPorProjeto(projeto.getId()));
            model.addAttribute("equipe", projeto.getEquipe());
        }
        return "projetos/cronograma/visualizar";
    }
}
