package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.PrazoJuridico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrazoJuridicoRepository extends JpaRepository<PrazoJuridico, Long> {
    java.util.List<PrazoJuridico> findByCumpridoFalseAndDataLimiteBetween(java.time.LocalDate inicio, java.time.LocalDate fim);
    java.util.List<PrazoJuridico> findByProcessoIdOrderByDataLimiteAsc(Long processoId);
}