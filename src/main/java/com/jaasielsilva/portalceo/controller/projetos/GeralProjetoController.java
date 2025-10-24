package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.projetos.EquipeProjeto;
import com.jaasielsilva.portalceo.model.projetos.Projeto;
import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.projetos.EquipeProjetoRepository;
import com.jaasielsilva.portalceo.service.projetos.ProjetoService;
import com.jaasielsilva.portalceo.service.projetos.TarefaProjetoService;
import jakarta.validation.Valid;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projetos/geral")
public class GeralProjetoController {

    @Autowired
    private ProjetoService projetoService;

    @Autowired
    private EquipeProjetoRepository equipeRepository;

    @Autowired
    private TarefaProjetoService tarefaProjetoService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Projetos - Geral");
        model.addAttribute("projetos", projetoService.buscarTodosAtivos());
        model.addAttribute("colaboradores", colaboradorRepository.findAll());
        model.addAttribute("totalEmAndamento", projetoService.countEmAndamento());
        model.addAttribute("totalConcluidos", projetoService.countConcluidos());
        model.addAttribute("totalAtrasados", projetoService.countAtrasados());
        return "projetos/geral/index";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Novo Projeto");
        model.addAttribute("equipes", equipeRepository.findAll());

        // Busca colaboradores com cargo que contenha "Gerente" ou "Coordenador"
        List<Colaborador> gerentes = colaboradorRepository.findByAtivoTrueAndCargoNomeContainingIgnoreCase("Gerente");
        List<Colaborador> coordenadores = colaboradorRepository
                .findByAtivoTrueAndCargoNomeContainingIgnoreCase("Coordenador");

        // Une as duas listas
        gerentes.addAll(coordenadores);

        model.addAttribute("colaboradores", gerentes);

        return "projetos/geral/novo";
    }

    @PostMapping("/salvar")
    @Transactional
    public String salvar(@Valid Projeto projeto, BindingResult result,
            @RequestParam(value = "equipeId", required = false) Long equipeId,
            @RequestParam(value = "tarefas", required = false) List<String> tarefas, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("equipes", equipeRepository.findAll());
            return "projetos/geral/novo";
        }

        // Validação: um projeto deve ter uma equipe antes de permitir a criação de
        // tarefas
        if (equipeId == null && tarefas != null && !tarefas.isEmpty()) {
            result.rejectValue("equipe", "equipe.notnull", "Um projeto deve ter uma equipe antes de criar tarefas.");
            model.addAttribute("equipes", equipeRepository.findAll());
            return "projetos/geral/novo";
        }

        // Associar equipe se selecionada
        if (equipeId != null) {
            EquipeProjeto equipe = equipeRepository.findById(equipeId).orElse(null);
            projeto.setEquipe(equipe);
        }

        try {
            System.out.println("Diagnóstico Controller - Projeto antes de salvar: Nome=" + projeto.getNome()
                    + ", Prazo=" + projeto.getPrazo()); // Novo Diagnóstico
            Projeto salvo = projetoService.salvar(projeto);
            System.out.println("ID do Projeto salvo: " + (salvo.getId() != null ? salvo.getId() : "NULO")); // Diagnóstico
            entityManager.flush(); // Força o flush para garantir que o ID do projeto esteja disponível

            // Obtém uma referência gerenciada do projeto para associar às tarefas
            Projeto managedProjetoReference = entityManager.getReference(Projeto.class, salvo.getId());

            // Salvar tarefas
            if (tarefas != null && !tarefas.isEmpty()) {
                for (String nomeTarefa : tarefas) {
                    if (!nomeTarefa.trim().isEmpty()) {
                        TarefaProjeto tarefa = new TarefaProjeto();
                        tarefa.setNome(nomeTarefa.trim());
                        // Associa a tarefa diretamente ao projeto salvo e gerenciado
                        tarefa.setProjeto(managedProjetoReference);
                        tarefaProjetoService.salvar(tarefa);
                    }
                }
            }
            return "redirect:/projetos/geral/listar";
        } catch (Exception e) {
            System.err.println("Erro ao salvar projeto ou tarefas: " + e.getMessage());
            e.printStackTrace();
            // Você pode adicionar um redirecionamento para uma página de erro ou exibir uma
            // mensagem na tela
            return "redirect:/projetos/geral/novo"; // Redireciona de volta para o formulário de criação
        }
    }

    @GetMapping("/listar-ativos")
    @ResponseBody
    public List<Projeto> listarProjetosAtivos() {
        return projetoService.buscarTodosAtivos();
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Editar Projeto");
        projetoService.buscarPorId(id).ifPresent(p -> model.addAttribute("projeto", p));
        return "projetos/geral/novo";
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id, @Valid Projeto projeto, BindingResult result,
            @RequestParam(value = "equipeId", required = false) Long equipeId, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("equipes", equipeRepository.findAll());
            model.addAttribute("projeto", projeto);
            return "projetos/geral/editar";
        }

        projeto.setId(id);

        if (equipeId != null) {
            EquipeProjeto equipe = equipeRepository.findById(equipeId).orElse(null);
            projeto.setEquipe(equipe);
        } else {
            projeto.setEquipe(null);
        }

        projetoService.atualizar(projeto);
        return "redirect:/projetos/cronograma/visualizar/" + id;
    }
}
