package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/configuracoes")
public class ConfiguracoesController {

    // Página principal de configurações
    @GetMapping
    public String configuracoes(Model model) {
        // TODO: Implementar lógica de configurações
        // Configurações do sistema, parâmetros globais
        // Configurações de módulos, integrações
        // Backup, logs, manutenção
        return "configuracoes/index";
    }
}