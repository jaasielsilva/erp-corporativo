package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoEntrevista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecrutamentoEntrevistaRepository extends JpaRepository<RecrutamentoEntrevista, Long> {
    List<RecrutamentoEntrevista> findByCandidaturaId(Long candidaturaId);
}

