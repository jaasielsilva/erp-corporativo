package com.jaasielsilva.portalceo.controller.agenda;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        long eventosHoje = agendaService.contarEventosHoje();
        long eventosSemana = agendaService.contarEventosSemana();
        long proximosLembretes = agendaService.contarProximosLembretes(7);
        long totalEventos = agendaService.contarTotalEventos();
        model.addAttribute("eventosHoje", eventosHoje);
        model.addAttribute("eventosSemana", eventosSemana);
        model.addAttribute("proximosLembretes", proximosLembretes);
        model.addAttribute("totalEventos", totalEventos);
        model.addAttribute("proximosEventos", agendaService.listarProximosEventos(10));
        model.addAttribute("eventos", agendaService.listarTodos());
        return "agenda/index";
    }

    @GetMapping("/eventos")
    public String listarEventos(Model model) {
        model.addAttribute("eventos", agendaService.listarTodos());
        return "agenda/visualizar-eventos";
    }

    @GetMapping("/novo-evento")
    public String novoEvento(Model model, @RequestParam(value = "data", required = false) String data) {
        Evento evento = new Evento();
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        if (data != null && !data.isBlank()) {
            try {
                java.time.LocalDate d = java.time.LocalDate.parse(data);
                evento.setDataHoraInicio(d.atTime(9, 0));
                evento.setDataHoraFim(d.atTime(10, 0));
            } catch (Exception e) {
                evento.setDataHoraInicio(agora);
                evento.setDataHoraFim(agora.plusHours(1));
            }
        } else {
            evento.setDataHoraInicio(agora);
            evento.setDataHoraFim(agora.plusHours(1));
        }
        model.addAttribute("evento", evento);
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

    @GetMapping("/api/eventos")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> apiEventos() {
        return agendaService.listarTodos().stream().map(e -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitulo());
            m.put("start", e.getDataHoraInicio());
            if (e.getDataHoraFim() != null) {
                m.put("end", e.getDataHoraFim());
            }
            java.util.Map<String, Object> ext = new java.util.HashMap<>();
            ext.put("description", e.getDescricao());
            ext.put("local", e.getLocal());
            ext.put("lembrete", e.isLembrete());
            m.put("extendedProps", ext);
            return m;
        }).toList();
    }

    @GetMapping("/api/estatisticas")
    @ResponseBody
    public java.util.Map<String, Object> estatisticas() {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("eventosHoje", agendaService.contarEventosHoje());
        m.put("eventosSemana", agendaService.contarEventosSemana());
        m.put("proximosLembretes", agendaService.contarProximosLembretes(7));
        m.put("totalEventos", agendaService.contarTotalEventos());
        return m;
    }

    @GetMapping("/api/evento/{id}")
    @ResponseBody
    public Map<String, Object> apiEventoPorId(@PathVariable Long id) {
        Evento e = agendaService.buscarPorId(id);
        if (e == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("titulo", e.getTitulo());
        m.put("descricao", e.getDescricao());
        m.put("dataHoraInicio", e.getDataHoraInicio());
        m.put("dataHoraFim", e.getDataHoraFim());
        m.put("lembrete", e.isLembrete());
        m.put("local", e.getLocal());
        return m;
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<Void> excluirEventoAjax(@PathVariable Long id) {
        agendaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
