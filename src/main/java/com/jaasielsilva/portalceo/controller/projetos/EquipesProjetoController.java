package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.projetos.EquipeProjeto;
import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.projetos.EquipeProjetoRepository;
import com.jaasielsilva.portalceo.repository.projetos.TarefaProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.HashSet;
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
    public String membros(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "9") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());

        Page<Colaborador> colaboradoresPage = colaboradorRepository.findByAtivoTrue(pageable);
        List<Colaborador> colaboradores = colaboradoresPage.getContent();

        // Calcular estatísticas de tarefas para colaboradores da página atual
        Map<Long, Map<String, Long>> estatisticasTarefas = new HashMap<>();
        for (Colaborador colaborador : colaboradores) {
            Map<String, Long> stats = new HashMap<>();

            long tarefasAtivas = tarefaRepository.countByAtribuidaAIdAndStatus(colaborador.getId(), TarefaProjeto.StatusTarefa.EM_ANDAMENTO)
                    + tarefaRepository.countByAtribuidaAIdAndStatus(colaborador.getId(), TarefaProjeto.StatusTarefa.PENDENTE);

            long tarefasConcluidas = tarefaRepository.countByAtribuidaAIdAndStatus(colaborador.getId(), TarefaProjeto.StatusTarefa.CONCLUIDA);

            stats.put("ativas", tarefasAtivas);
            stats.put("concluidas", tarefasConcluidas);
            stats.put("total", tarefaRepository.countByAtribuidaAId(colaborador.getId()));

            estatisticasTarefas.put(colaborador.getId(), stats);
        }

        // Totais gerais
        long totalMembros = colaboradoresPage.getTotalElements();

        // Disponíveis considerando todos ativos (menos de 5 tarefas ativas)
        List<Colaborador> todosAtivos = colaboradorRepository.findByAtivoTrue();
        long membrosDisponiveis = todosAtivos.stream().filter(c ->
                tarefaRepository.countByAtribuidaAIdAndStatus(c.getId(), TarefaProjeto.StatusTarefa.EM_ANDAMENTO)
                        + tarefaRepository.countByAtribuidaAIdAndStatus(c.getId(), TarefaProjeto.StatusTarefa.PENDENTE)
                        < 5
        ).count();

        model.addAttribute("pageTitle", "Membros da Equipe");
        model.addAttribute("equipes", equipeRepository.findAll());

        // Cards paginados
        model.addAttribute("colaboradoresPage", colaboradoresPage);
        model.addAttribute("estatisticasTarefas", estatisticasTarefas);

        // Lista completa para o modal de seleção
        model.addAttribute("colaboradoresAll", todosAtivos);

        model.addAttribute("totalMembros", totalMembros);
        model.addAttribute("membrosDisponiveis", membrosDisponiveis);

        return "projetos/equipes/membros";
    }

    // DTO para retornar dados dos membros
    static class MemberDTO {
        public Long id;
        public String nome;
        public MemberDTO(Long id, String nome) { 
            this.id = id; 
            this.nome = nome; 
        }
    }

    // Retorna os membros da equipe com id e nome para seleção no modal
    @GetMapping("/{equipeId}/membros")
    @ResponseBody
    public List<MemberDTO> listarMembrosPorEquipe(@PathVariable Long equipeId) {
        Optional<EquipeProjeto> equipeOpt = equipeRepository.findById(equipeId);
        return equipeOpt
                .map(EquipeProjeto::getMembros)
                .orElseGet(HashSet::new)
                .stream()
                .map(colaborador -> new MemberDTO(colaborador.getId(), colaborador.getNome()))
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

        equipe.setMembros(new HashSet<>(membrosSelecionados));
        equipeRepository.save(equipe);

        model.addAttribute("mensagem", "Membros atualizados com sucesso!");
        return "redirect:/projetos/equipes/membros";
    }
}