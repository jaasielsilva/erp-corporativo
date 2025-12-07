package com.jaasielsilva.portalceo.repository.treinamentos;

import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreinamentoAvaliacaoRepository extends JpaRepository<TreinamentoAvaliacao, Long> {
    List<TreinamentoAvaliacao> findByMatriculaTurmaId(Long turmaId);
}

