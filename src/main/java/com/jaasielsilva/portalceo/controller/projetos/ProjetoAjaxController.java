package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.projetos.Projeto;
import com.jaasielsilva.portalceo.service.projetos.ProjetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projetos")
public class ProjetoAjaxController {

    @Autowired
    private ProjetoService projetoService;

    static class MemberDTO {
        public Long id;
        public String nome;
        public MemberDTO(Long id, String nome) { this.id = id; this.nome = nome; }
    }

    @GetMapping("/{projetoId}/equipe/membros")
    public List<MemberDTO> membrosDaEquipeDoProjeto(@PathVariable Long projetoId) {
        Projeto projeto = projetoService.buscarPorId(projetoId).orElse(null);
        if (projeto == null || projeto.getEquipe() == null) {
            return List.of();
        }
        return projeto.getEquipe().getMembros()
                .stream()
                .map((Colaborador c) -> new MemberDTO(c.getId(), c.getNome()))
                .collect(Collectors.toList());
    }
}