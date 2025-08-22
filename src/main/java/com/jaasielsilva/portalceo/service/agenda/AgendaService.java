package com.jaasielsilva.portalceo.service.agenda;

import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.agenda.Evento;
import com.jaasielsilva.portalceo.repository.agenda.EventoRepository;

import java.time.LocalDateTime;
import java.util.List;

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
}

