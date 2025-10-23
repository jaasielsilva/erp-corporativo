package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import com.jaasielsilva.portalceo.service.projetos.ProjetoService;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.projetos.TarefaProjetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto.StatusTarefa;
import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;



@Controller
@RequestMapping("/projetos/tarefas")
public class TarefaController {

    @Autowired
    private TarefaProjetoService tarefaService;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private ProjetoService projetoService;

    @GetMapping("/listar")
    public String listar(@RequestParam(name = "q", required = false) String q,
                         @RequestParam(name = "status", required = false) TarefaProjeto.StatusTarefa status,
                         Model model) {
        model.addAttribute("pageTitle", "Projetos - Tarefas");
        model.addAttribute("tarefas", tarefaService.buscarPorFiltro(q, status));
        model.addAttribute("totalPendentes", tarefaService.countPendentes());
        model.addAttribute("totalEmAndamento", tarefaService.countEmAndamento());
        model.addAttribute("totalConcluidas", tarefaService.countConcluidas());
        model.addAttribute("q", q);
        model.addAttribute("statusFilter", status != null ? status.name() : "");
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

    // Exibe formulário de cadastro de tarefa
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("pageTitle", "Cadastro de Tarefa");
        model.addAttribute("tarefa", new TarefaProjeto());
        model.addAttribute("projetos", projetoService.listarAtivos());
        model.addAttribute("colaboradores", colaboradorRepository.findByAtivoTrue());
        return "projetos/tarefas/cadastro";
    }

    // Salva nova tarefa
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute TarefaProjeto tarefa) {
        tarefaService.salvar(tarefa);
        return "redirect:/projetos/tarefas/listar";
    }

}
