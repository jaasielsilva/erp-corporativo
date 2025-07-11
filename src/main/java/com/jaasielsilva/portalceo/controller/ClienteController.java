package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listarTodos());
        return "clientes/lista";
    }
    

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Cliente cliente) {
        service.salvar(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        var cliente = service.buscarPorId(id);
        model.addAttribute("cliente", cliente.orElse(new Cliente()));
        return "clientes/form";
    }

    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model) {
        var cliente = service.buscarPorId(id);
        model.addAttribute("cliente", cliente.orElse(null));
        return "clientes/detalhes";
    }

    @GetMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/clientes";
    }
}
