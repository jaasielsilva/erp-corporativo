package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.Audiencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudienciaRepository extends JpaRepository<Audiencia, Long> {
    java.util.List<Audiencia> findByDataHoraBetween(java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
    java.util.List<Audiencia> findByProcessoIdOrderByDataHoraAsc(Long processoId);
}