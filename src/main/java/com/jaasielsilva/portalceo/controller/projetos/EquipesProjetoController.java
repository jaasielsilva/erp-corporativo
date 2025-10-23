package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.projetos.EquipeProjeto;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.projetos.EquipeProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projetos/equipes")
public class EquipesProjetoController {

    @Autowired
    private EquipeProjetoRepository equipeRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

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
        model.addAttribute("pageTitle", "Membros da Equipe");
        model.addAttribute("equipes", equipeRepository.findAll());
        model.addAttribute("colaboradores", colaboradorRepository.findByAtivoTrue());
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