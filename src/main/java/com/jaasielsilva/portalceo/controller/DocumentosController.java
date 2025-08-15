package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/documentos")
public class DocumentosController {

    // Documentos
    @GetMapping
    public String documentos(Model model) {
        // TODO: Implementar lógica dos documentos
        // Repositório de documentos da empresa
        // Upload, download, versionamento
        // Controle de acesso, assinatura digital
        // Categorização, busca, metadados
        return "documentos/index";
    }
}