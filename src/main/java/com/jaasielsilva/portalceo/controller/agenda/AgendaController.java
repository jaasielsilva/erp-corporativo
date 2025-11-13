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
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaasielsilva.portalceo.model.agenda.Evento;
import com.jaasielsilva.portalceo.service.agenda.AgendaService;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    @Autowired
    private AgendaService agendaService;

    @GetMapping
    public String agenda(Model model) {
        var eventos = agendaService.listarTodos();

        var hoje = java.time.LocalDate.now();
        var agora = java.time.LocalDateTime.now();

        int eventosHoje = (int) eventos.stream()
                .filter(e -> e.getDataHoraInicio() != null && e.getDataHoraInicio().toLocalDate().equals(hoje))
                .count();

        // Semana atual (segunda a domingo)
        var primeiroDiaSemana = hoje.with(java.time.DayOfWeek.MONDAY);
        var ultimoDiaSemana = hoje.with(java.time.DayOfWeek.SUNDAY);
        int eventosSemana = (int) eventos.stream()
                .filter(e -> e.getDataHoraInicio() != null)
                .filter(e -> {
                    var data = e.getDataHoraInicio().toLocalDate();
                    return (data.isEqual(primeiroDiaSemana) || data.isAfter(primeiroDiaSemana))
                            && (data.isEqual(ultimoDiaSemana) || data.isBefore(ultimoDiaSemana));
                })
                .count();

        int proximosLembretes = (int) eventos.stream()
                .filter(Evento::isLembrete)
                .filter(e -> e.getDataHoraInicio() != null && e.getDataHoraInicio().isAfter(agora))
                .count();

        var proximosEventos = eventos.stream()
                .filter(e -> e.getDataHoraInicio() != null && e.getDataHoraInicio().isAfter(agora))
                .sorted(java.util.Comparator.comparing(Evento::getDataHoraInicio))
                .limit(5)
                .toList();

        model.addAttribute("eventosHoje", eventosHoje);
        model.addAttribute("eventosSemana", eventosSemana);
        model.addAttribute("proximosLembretes", proximosLembretes);
        model.addAttribute("totalEventos", eventos.size());
        model.addAttribute("proximosEventos", proximosEventos);
        model.addAttribute("eventos", eventos);
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

    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable Long id, @ModelAttribute Evento evento) {
        evento.setId(id);
        agendaService.salvar(evento);
        return "redirect:/agenda/eventos";
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

    @GetMapping("/api/eventos")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> apiEventos() {
        var eventos = agendaService.listarTodos();
        var lista = new java.util.ArrayList<java.util.Map<String, Object>>();
        for (var e : eventos) {
            if (e.getDataHoraInicio() == null) continue;
            var map = new java.util.HashMap<String, Object>();
            map.put("id", e.getId());
            map.put("title", e.getTitulo());
            map.put("start", e.getDataHoraInicio());
            if (e.getDataHoraFim() != null) {
                map.put("end", e.getDataHoraFim());
            }
            lista.add(map);
        }
        return lista;
    }

    @PostMapping("/salvar-rascunho")
    @ResponseBody
    public java.util.Map<String, String> salvarRascunho() {
        return java.util.Map.of("status", "ok");
    }
}