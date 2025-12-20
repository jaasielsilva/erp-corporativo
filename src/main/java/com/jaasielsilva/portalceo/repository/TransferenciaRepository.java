package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {
    List<Transferencia> findByDataTransferenciaBetween(LocalDate inicio, LocalDate fim);
}
