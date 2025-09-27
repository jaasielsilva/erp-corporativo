package com.jaasielsilva.portalceo.controller.rh.ponto;

import com.jaasielsilva.portalceo.service.ColaboradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rh/ponto-escalas")
public class PontoEscalaController {

    @Autowired
    private ColaboradorService colaboradorService;

    @GetMapping("/registros")
    public String registros(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/registros";
    }

    @GetMapping("/correcoes")
    public String correcoes(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/correcoes";
    }

    @GetMapping("/escalas")
    public String escalas(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/escalas";
    }
}
