package com.jaasielsilva.portalceo.controller.agenda;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jaasielsilva.portalceo.model.agenda.Evento;
import com.jaasielsilva.portalceo.service.agenda.AgendaService;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    @Autowired
    private AgendaService agendaService;

    @GetMapping
    public String agenda(Model model) {
        // TODO: Implementar lógica da agenda
        // Calendário de compromissos, reuniões
        // Agendamentos, lembretes, notificações
        // Integração com outros módulos (RH, vendas)
        model.addAttribute("eventosHoje", 0);
        model.addAttribute("eventosSemana", 0);
        model.addAttribute("proximosLembretes", 0);
        model.addAttribute("totalEventos", 0);
        model.addAttribute("proximosEventos", Collections.emptyList());
        model.addAttribute("eventos", Collections.emptyList());
        return "agenda/index";
    }

    @GetMapping("/eventos")
    public String listarEventos(Model model) {
        model.addAttribute("eventos", agendaService.listarTodos());
        return "agenda/visualizar-eventos";
    }

    @GetMapping("/novo-evento")
    public String novoEvento(Model model) {
        model.addAttribute("evento", new Evento());
        return "agenda/novo-evento";
    }

    @PostMapping("/salvar")
    public String salvarEvento(@ModelAttribute Evento evento) {
        agendaService.salvar(evento);
        return "redirect:/agenda/eventos";
    }

    @GetMapping("/editar/{id}")
    public String editarEvento(@PathVariable Long id, Model model) {
        model.addAttribute("evento", agendaService.buscarPorId(id));
        return "agenda/editar-evento";
    }

    @GetMapping("/deletar/{id}")
    public String deletarEvento(@PathVariable Long id) {
        agendaService.deletar(id);
        return "redirect:/agenda/eventos";
    }

    @GetMapping("/calendario")
    public String calendario(Model model) {
        model.addAttribute("eventos", agendaService.listarTodos());
        return "agenda/agenda";
    }

    @GetMapping("/lembretes")
    public String lembretes(Model model) {
        model.addAttribute("eventos", agendaService.listarTodos()
                .stream().filter(Evento::isLembrete).toList());
        return "agenda/lembretes";
    }

    @GetMapping("/configuracoes")
    public String configuracoes() {
        return "agenda/configuracoes";
    }
}