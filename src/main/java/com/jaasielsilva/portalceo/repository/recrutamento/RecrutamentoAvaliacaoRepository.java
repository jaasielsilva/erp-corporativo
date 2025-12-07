package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecrutamentoAvaliacaoRepository extends JpaRepository<RecrutamentoAvaliacao, Long> {
    List<RecrutamentoAvaliacao> findByCandidaturaId(Long candidaturaId);
}

