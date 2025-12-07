package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoCandidatoExperiencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecrutamentoCandidatoExperienciaRepository extends JpaRepository<RecrutamentoCandidatoExperiencia, Long> {
    List<RecrutamentoCandidatoExperiencia> findByCandidatoId(Long candidatoId);
}

