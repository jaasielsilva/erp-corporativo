package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ti")
public class TiController {

    // Página principal do TI
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Tecnologia da Informação");
        model.addAttribute("moduleCSS", "ti");
        return "ti/index";
    }

    // Sistemas
    @GetMapping("/sistemas")
    public String sistemas(Model model) {
        // TODO: Implementar lógica de sistemas
        // Monitoramento de sistemas, status de serviços
        // Logs de sistema, performance, uptime
        return "ti/sistemas";
    }

    // Suporte Técnico
    @GetMapping("/suporte")
    public String suporte(Model model) {
        // TODO: Implementar lógica de suporte
        // Tickets de suporte, chamados técnicos
        // SLA, prioridades, status de atendimento
        return "ti/suporte";
    }

    // Backup
    @GetMapping("/backup")
    public String backup(Model model) {
        // TODO: Implementar lógica de backup
        // Status de backups, agendamentos
        // Histórico, restaurações, políticas
        return "ti/backup";
    }

    // Segurança
    @GetMapping("/seguranca")
    public String seguranca(Model model) {
        // TODO: Implementar lógica de segurança
        // Logs de acesso, tentativas de invasão
        // Políticas de segurança, auditoria
        return "ti/seguranca";
    }
}