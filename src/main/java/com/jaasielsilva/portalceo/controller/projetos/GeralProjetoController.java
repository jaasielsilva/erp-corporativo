package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.projetos.Projeto;
import com.jaasielsilva.portalceo.repository.projetos.EquipeProjetoRepository;
import com.jaasielsilva.portalceo.service.projetos.ProjetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projetos/geral")
public class GeralProjetoController {

    @Autowired
    private ProjetoService projetoService;

    @Autowired
    private EquipeProjetoRepository equipeRepository;

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Projetos - Geral");
        model.addAttribute("projetos", projetoService.listarAtivos());
        model.addAttribute("totalEmAndamento", projetoService.countEmAndamento());
        model.addAttribute("totalConcluidos", projetoService.countConcluidos());
        model.addAttribute("totalAtrasados", projetoService.countAtrasados());
        return "projetos/geral/index";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Novo Projeto");
        model.addAttribute("equipes", equipeRepository.findAll());
        return "projetos/geral/novo";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) java.time.LocalDate prazo,
            @RequestParam(required = false) String prioridade,
            @RequestParam(required = false) Long gerente,
            @RequestParam(required = false) java.math.BigDecimal orcamento) {
        Projeto projeto = new Projeto();
        projeto.setNome(nome);
        projeto.setDescricao(descricao);
        projeto.setPrazo(prazo);
        // prioridade mapeada para status/plano simples
        projeto.setStatus(Projeto.StatusProjeto.EM_ANDAMENTO);
        projeto.setOrcamento(orcamento);
        projeto.setProgresso(0);

        Projeto salvo = projetoService.salvarComEquipe(projeto);
        return "redirect:/projetos/cronograma/visualizar/" + salvo.getId();
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Editar Projeto");
        projetoService.buscarPorId(id).ifPresent(p -> model.addAttribute("projeto", p));
        return "projetos/geral/novo";
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) java.time.LocalDate prazo,
            @RequestParam(required = false) java.math.BigDecimal orcamento) {
        Projeto projeto = projetoService.buscarPorId(id).orElseThrow();
        projeto.setNome(nome);
        projeto.setDescricao(descricao);
        projeto.setPrazo(prazo);
        projeto.setOrcamento(orcamento);
        projetoService.atualizar(projeto);
        return "redirect:/projetos/cronograma/visualizar/" + id;
    }
}
