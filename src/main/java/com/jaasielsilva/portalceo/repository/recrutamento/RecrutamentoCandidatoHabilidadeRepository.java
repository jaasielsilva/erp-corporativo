package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoCandidatoHabilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecrutamentoCandidatoHabilidadeRepository extends JpaRepository<RecrutamentoCandidatoHabilidade, Long> {
    List<RecrutamentoCandidatoHabilidade> findByCandidatoId(Long candidatoId);
}

