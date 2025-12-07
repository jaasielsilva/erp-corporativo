package com.jaasielsilva.portalceo.repository.treinamentos;

import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoTurma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreinamentoTurmaRepository extends JpaRepository<TreinamentoTurma, Long> {
    Page<TreinamentoTurma> findByStatus(String status, Pageable pageable);
}

