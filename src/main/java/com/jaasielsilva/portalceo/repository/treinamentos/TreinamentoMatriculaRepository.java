package com.jaasielsilva.portalceo.repository.treinamentos;

import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreinamentoMatriculaRepository extends JpaRepository<TreinamentoMatricula, Long> {
    List<TreinamentoMatricula> findByTurmaId(Long turmaId);
}

