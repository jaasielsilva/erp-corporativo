package com.jaasielsilva.portalceo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.service.ContratoService;

import java.util.List;

@Controller
@RequestMapping("/contratos")
public class ContratosController {

    @Autowired
    private ContratoService contratoService;

    // 1. Lista todos os contratos
    @GetMapping
    public String listarTodos(Model model) {
        List<Contrato> contratos = contratoService.findAll();
        model.addAttribute("contratos", contratos);
        return "contrato/listar";
    }

    // 2. Formulário para novo contrato
    @GetMapping("/novo")
    public String novoContrato(Model model) {
        model.addAttribute("contrato", new Contrato());
        return "contrato/contrato-form";
    }

    // 3. Salva contrato (novo ou edição)
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Contrato contrato) {
        contratoService.save(contrato);
        return "redirect:/contratos";
    }

    // 4. Edita contrato existente
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        model.addAttribute("contrato", contrato);
        return "contrato/form";
    }

    // 5. Visualizar detalhes do contrato
    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        model.addAttribute("contrato", contrato);
        return "contrato/detalhes";
    }

    // 6. Tela para renovar contrato
    @GetMapping("/{id}/renovar")
    public String renovar(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        // Exemplo: Pode duplicar ou gerar nova vigência
        model.addAttribute("contrato", contrato);
        return "contrato/renovar";
    }

    // 7. Anexar documentos
    @GetMapping("/{id}/documentos")
    public String documentos(@PathVariable Long id, Model model) {
        Contrato contrato = contratoService.findById(id);
        model.addAttribute("contrato", contrato);
        // Listagem de anexos pode ser adicionada aqui
        return "contrato/documentos";
    }
}
