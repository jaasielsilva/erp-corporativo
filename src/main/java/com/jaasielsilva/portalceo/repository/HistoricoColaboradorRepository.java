package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.HistoricoColaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoColaboradorRepository extends JpaRepository<HistoricoColaborador, Long> {

    // Buscar todos os históricos de um colaborador específico
    List<HistoricoColaborador> findByColaboradorId(Long colaboradorId);

}
