package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.projetos.TarefaProjetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projetos/tarefas")
public class TarefaController {

    @Autowired
    private TarefaProjetoService tarefaService;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Projetos - Tarefas");
        model.addAttribute("tarefas", tarefaService.listarTodas());
        model.addAttribute("totalPendentes", tarefaService.countPendentes());
        model.addAttribute("totalEmAndamento", tarefaService.countEmAndamento());
        model.addAttribute("totalConcluidas", tarefaService.countConcluidas());
        return "projetos/tarefas/listar";
    }

    @GetMapping("/atribuicoes")
    public String atribuicoes(Model model) {
        model.addAttribute("pageTitle", "Atribuições de Tarefas");
        model.addAttribute("tarefas", tarefaService.listarTodas());
        model.addAttribute("colaboradores", colaboradorRepository.findByAtivoTrue());
        return "projetos/tarefas/atribuicoes";
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id, @RequestParam Long colaboradorId) {
        tarefaService.atribuir(id, colaboradorId);
        return "redirect:/projetos/tarefas/atribuicoes";
    }
}
