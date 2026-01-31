package com.jaasielsilva.portalceo.service.agenda;

import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.agenda.Evento;
import com.jaasielsilva.portalceo.repository.agenda.EventoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AgendaService {

    private final EventoRepository eventoRepository;

    public AgendaService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Evento salvar(Evento evento) {
        if (evento.getTitulo() == null || evento.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        if (evento.getDataHoraInicio() == null) {
            evento.setDataHoraInicio(agora);
        }
        if (evento.getDataHoraFim() == null || !evento.getDataHoraFim().isAfter(evento.getDataHoraInicio())) {
            evento.setDataHoraFim(evento.getDataHoraInicio().plusHours(1));
        }
        if (evento.getDescricao() != null && evento.getDescricao().length() > 500) {
            evento.setDescricao(evento.getDescricao().substring(0, 500));
        }
        if (evento.getTitulo() != null) {
            evento.setTitulo(evento.getTitulo().trim());
        }
        if (evento.getLocal() != null) {
            evento.setLocal(evento.getLocal().trim());
        }
        return eventoRepository.save(evento);
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id).orElse(null);
    }

    public void deletar(Long id) {
        eventoRepository.deleteById(id);
    }

    public List<Evento> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return eventoRepository.findByDataHoraInicioBetween(inicio, fim);
    }

    public long contarTotalEventos() {
        return eventoRepository.count();
    }

    public long contarEventosHoje() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);
        return listarPorPeriodo(inicio, fim).size();
    }

    public long contarEventosSemana() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(java.time.DayOfWeek.MONDAY);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        return listarPorPeriodo(inicioSemana.atStartOfDay(), fimSemana.atTime(LocalTime.MAX)).size();
    }

    public long contarProximosLembretes(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate ate = hoje.plusDays(dias);
        return listarPorPeriodo(hoje.atStartOfDay(), ate.atTime(LocalTime.MAX))
                .stream()
                .filter(Evento::isLembrete)
                .count();
    }

    public List<Evento> listarProximosEventos(int limite) {
        LocalDateTime agora = LocalDateTime.now();
        List<Evento> futuros = eventoRepository.findByDataHoraInicioBetween(agora.minusYears(1), agora.plusYears(5));
        futuros.sort((a, b) -> a.getDataHoraInicio().compareTo(b.getDataHoraInicio()));
        return futuros.stream().filter(e -> e.getDataHoraInicio().isAfter(agora)).limit(limite).toList();
    }
}

