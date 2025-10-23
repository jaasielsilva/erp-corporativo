package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.projetos.EquipeProjeto;
import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.projetos.EquipeProjetoRepository;
import com.jaasielsilva.portalceo.repository.projetos.TarefaProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projetos/equipes")
public class EquipesProjetoController {

    @Autowired
    private EquipeProjetoRepository equipeRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private TarefaProjetoRepository tarefaRepository;

    // Listagem de equipes
    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Equipes de Projetos");
        model.addAttribute("equipes", equipeRepository.findAll());
        return "projetos/equipes/listar";
    }

    // Página de cadastro de equipe
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("pageTitle", "Cadastro de Equipe");
        model.addAttribute("equipe", new EquipeProjeto());
        return "projetos/equipes/cadastro";
    }

    // Salvar equipe
    @PostMapping("/salvar")
    public String salvar(@RequestParam(required = false) Long id, @RequestParam String nome, @RequestParam(defaultValue = "true") boolean ativa) {
        EquipeProjeto equipe = new EquipeProjeto();
        equipe.setId(id);
        equipe.setNome(nome);
        equipe.setAtiva(ativa);
        equipeRepository.save(equipe);
        return "redirect:/projetos/equipes/listar";
    }

    @GetMapping("/membros")
    public String membros(Model model) {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        
        // Calcular estatísticas de tarefas para cada colaborador
        Map<Long, Map<String, Long>> estatisticasTarefas = new HashMap<>();
        for (Colaborador colaborador : colaboradores) {
            Map<String, Long> stats = new HashMap<>();
            
            // Contar tarefas por status
            long tarefasAtivas = tarefaRepository.findByAtribuidaAId(colaborador.getId())
                    .stream()
                    .filter(t -> t.getStatus() == TarefaProjeto.StatusTarefa.EM_ANDAMENTO || 
                               t.getStatus() == TarefaProjeto.StatusTarefa.PENDENTE)
                    .count();
            
            long tarefasConcluidas = tarefaRepository.findByAtribuidaAId(colaborador.getId())
                    .stream()
                    .filter(t -> t.getStatus() == TarefaProjeto.StatusTarefa.CONCLUIDA)
                    .count();
            
            stats.put("ativas", tarefasAtivas);
            stats.put("concluidas", tarefasConcluidas);
            stats.put("total", tarefasAtivas + tarefasConcluidas);
            
            estatisticasTarefas.put(colaborador.getId(), stats);
        }
        
        // Calcular totais gerais
        long totalMembros = colaboradores.size();
        long membrosDisponiveis = colaboradores.stream()
                .filter(c -> estatisticasTarefas.get(c.getId()).get("ativas") < 5) // Considerando disponível se tem menos de 5 tarefas ativas
                .count();
        
        model.addAttribute("pageTitle", "Membros da Equipe");
        model.addAttribute("equipes", equipeRepository.findAll());
        model.addAttribute("colaboradores", colaboradores);
        model.addAttribute("estatisticasTarefas", estatisticasTarefas);
        model.addAttribute("totalMembros", totalMembros);
        model.addAttribute("membrosDisponiveis", membrosDisponiveis);
        
        return "projetos/equipes/membros";
    }

    // Retorna os IDs dos membros da equipe para seleção no modal
    @GetMapping("/{equipeId}/membros")
    @ResponseBody
    public List<Long> listarMembrosPorEquipe(@PathVariable Long equipeId) {
        Optional<EquipeProjeto> equipeOpt = equipeRepository.findById(equipeId);
        return equipeOpt
                .map(EquipeProjeto::getMembros)
                .orElseGet(List::of)
                .stream()
                .map(Colaborador::getId)
                .collect(Collectors.toList());
    }

    // Salva os membros selecionados para a equipe
    @PostMapping("/membros/salvar")
    public String salvarMembros(
            @RequestParam("equipeId") Long equipeId,
            @RequestParam(value = "colaboradoresIds", required = false) List<Long> colaboradoresIds,
            Model model
    ) {
        EquipeProjeto equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + equipeId));

        List<Colaborador> membrosSelecionados = colaboradoresIds == null || colaboradoresIds.isEmpty()
                ? List.of()
                : colaboradorRepository.findAllById(colaboradoresIds);

        equipe.setMembros(membrosSelecionados);
        equipeRepository.save(equipe);

        model.addAttribute("mensagem", "Membros atualizados com sucesso!");
        return "redirect:/projetos/equipes/membros";
    }
}