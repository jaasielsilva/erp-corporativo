package com.jaasielsilva.portalceo.repository.agenda;


import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.portalceo.model.agenda.Evento;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByDataHoraInicioBetween(LocalDateTime inicio, LocalDateTime fim);
}

