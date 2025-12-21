package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.LancamentoContabil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LancamentoContabilRepository extends JpaRepository<LancamentoContabil, Long> {
    boolean existsByChaveIdempotencia(String chaveIdempotencia);
}

