package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.model.ValeTransporte;
import com.jaasielsilva.portalceo.service.ValeTransporteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rh/beneficios/vale-transporte")
public class ValeTransporteController {

    private final ValeTransporteService service;

    public ValeTransporteController(ValeTransporteService service) {
        this.service = service;
    }

    // LISTAR
    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("vales", service.listarTodos());
        return "rh/beneficios/vale-transporte/listar";
    }

    // FORM NOVO
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("vale", new ValeTransporte());
        return "rh/beneficios/vale-transporte/form";
    }

    // SALVAR
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("vale") ValeTransporte vale, BindingResult result) {
        if (result.hasErrors()) {
            return "rh/beneficios/vale-transporte/form";
        }
        service.salvar(vale);
        return "redirect:/rh/beneficios/vale-transporte/listar";
    }

    // FORM EDITAR
    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        ValeTransporte vale = service.buscarPorId(id);
        model.addAttribute("vale", vale);
        return "rh/beneficios/vale-transporte/form";
    }

    // ATUALIZAR
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("vale") ValeTransporte vale,
                            BindingResult result) {
        if (result.hasErrors()) {
            return "rh/beneficios/vale-transporte/form";
        }
        vale.setId(id);
        service.salvar(vale);
        return "redirect:/rh/beneficios/vale-transporte/listar";
    }

    // DELETAR
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        service.deletar(id);
        return "redirect:/rh/beneficios/vale-transporte/listar";
    }
}
