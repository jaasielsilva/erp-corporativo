package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoCandidatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecrutamentoCandidaturaRepository extends JpaRepository<RecrutamentoCandidatura, Long> {
    List<RecrutamentoCandidatura> findByVagaId(Long vagaId);
    List<RecrutamentoCandidatura> findByEtapa(String etapa);
}

