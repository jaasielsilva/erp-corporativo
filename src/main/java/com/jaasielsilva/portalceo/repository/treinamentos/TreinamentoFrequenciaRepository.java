package com.jaasielsilva.portalceo.repository.treinamentos;

import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoFrequencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreinamentoFrequenciaRepository extends JpaRepository<TreinamentoFrequencia, Long> {
    List<TreinamentoFrequencia> findByMatriculaId(Long matriculaId);
}

