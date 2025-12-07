package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoCandidatoFormacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecrutamentoCandidatoFormacaoRepository extends JpaRepository<RecrutamentoCandidatoFormacao, Long> {
    List<RecrutamentoCandidatoFormacao> findByCandidatoId(Long candidatoId);
}

