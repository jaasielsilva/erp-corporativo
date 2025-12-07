package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoVaga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecrutamentoVagaRepository extends JpaRepository<RecrutamentoVaga, Long> {
    Page<RecrutamentoVaga> findByStatus(String status, Pageable pageable);
}

