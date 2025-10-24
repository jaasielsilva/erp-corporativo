package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.controller.projetos.ProjetoAjaxController.MemberDTO;
import com.jaasielsilva.portalceo.model.projetos.Projeto;
import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import com.jaasielsilva.portalceo.service.projetos.ProjetoService;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.projetos.TarefaProjetoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public String listar(@RequestParam(name = "projetoId", required = false) Long projetoId,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) TarefaProjeto.StatusTarefa status,
            Model model) {

        model.addAttribute("pageTitle", "Projetos - Tarefas");
        model.addAttribute("tarefas", tarefaService.buscarPorFiltro(q, status));
        model.addAttribute("totalPendentes", tarefaService.countPendentes());
        model.addAttribute("totalEmAndamento", tarefaService.countEmAndamento());
        model.addAttribute("totalConcluidas", tarefaService.countConcluidas());
        model.addAttribute("q", q);
        model.addAttribute("statusFilter", status != null ? status.name() : "");
        model.addAttribute("projetos", projetoService.buscarTodosAtivos());

        if (projetoId != null) {
            model.addAttribute("projeto", projetoService.buscarPorId(projetoId));
        }

        model.addAttribute("membros", colaboradorRepository.findByAtivoTrue());
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
    public String atribuir(@PathVariable Long id, @RequestParam Long colaboradorId, @RequestParam Long projetoId) {
        tarefaService.atribuir(id, colaboradorId, projetoId);
        return "redirect:/projetos/tarefas/atribuicoes";
    }

    // Exibe formulário de cadastro de tarefa
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("pageTitle", "Cadastro de Tarefa");
        model.addAttribute("tarefa", new TarefaProjeto());
        model.addAttribute("projetos", projetoService.buscarTodosAtivos());

        // Padronizar o nome como "colaboradoresAll" (para o Thymeleaf)
        model.addAttribute("colaboradoresAll", colaboradorRepository.findByAtivoTrue());

        return "projetos/tarefas/cadastro";
    }

    // Salva nova tarefa
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute TarefaProjeto tarefa) {
        if (tarefa.getProjeto() == null || tarefa.getProjeto().getId() == null) {
            throw new RuntimeException("Projeto não informado");
        }
        Projeto projeto = projetoService.buscarPorId(tarefa.getProjeto().getId())
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
        tarefa.setProjeto(projeto);
        tarefaService.salvar(tarefa);
        return "redirect:/projetos/tarefas/listar";
    }

    @GetMapping("/projetos/{projetoId}/equipe/membros")
    @ResponseBody
    public List<MemberDTO> listarMembrosPorProjeto(@PathVariable Long projetoId) {
        return projetoService.buscarPorIdComEquipeMembros(projetoId)
                .map(projeto -> {
                    if (projeto.getEquipe() == null)
                        return List.<MemberDTO>of();
                    return projeto.getEquipe().getMembros().stream()
                            .map(colab -> new MemberDTO(colab.getId(), colab.getNome()))
                            .collect(Collectors.toList());
                })
                .orElse(List.of());
    }


    // metodo que retornar tarefas não atribuídas de um projeto
    @GetMapping("/projetos/{projetoId}/tarefas/disponiveis")
    @ResponseBody
    public List<Map<String, Object>> listarTarefasDisponiveis(@PathVariable Long projetoId) {
        return tarefaService.listarPorProjeto(projetoId).stream()
                .filter(t -> t.getAtribuidaA() == null) // somente tarefas sem responsável
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("nome", t.getNome());
                    return map;
                })
                .collect(Collectors.toList());
    }

}
