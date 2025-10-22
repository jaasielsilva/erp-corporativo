package com.jaasielsilva.portalceo.controller.projetos;

import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.projetos.EquipeProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projetos/equipes")
public class EquipesProjetoController {

    @Autowired
    private EquipeProjetoRepository equipeRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @GetMapping("/membros")
    public String membros(Model model) {
        model.addAttribute("pageTitle", "Membros da Equipe");
        model.addAttribute("equipes", equipeRepository.findAll());
        model.addAttribute("colaboradores", colaboradorRepository.findByAtivoTrue());
        return "projetos/equipes/membros";
    }
}