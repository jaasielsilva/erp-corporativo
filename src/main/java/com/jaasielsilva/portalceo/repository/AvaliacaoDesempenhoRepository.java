package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.AvaliacaoDesempenho;
import com.jaasielsilva.portalceo.model.AvaliacaoDesempenho.StatusAvaliacao;
import com.jaasielsilva.portalceo.model.Colaborador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AvaliacaoDesempenhoRepository extends JpaRepository<AvaliacaoDesempenho, Long> {

    Page<AvaliacaoDesempenho> findByStatus(StatusAvaliacao status, Pageable pageable);

    @Query("SELECT a FROM AvaliacaoDesempenho a WHERE (:status IS NULL OR a.status = :status) " +
           "AND (:inicio IS NULL OR a.periodoInicio >= :inicio) " +
           "AND (:fim IS NULL OR a.periodoFim <= :fim) " +
           "ORDER BY a.periodoInicio DESC")
    Page<AvaliacaoDesempenho> pesquisar(@Param("status") StatusAvaliacao status,
                                        @Param("inicio") LocalDate inicio,
                                        @Param("fim") LocalDate fim,
                                        Pageable pageable);

    @Query("SELECT COUNT(a) > 0 FROM AvaliacaoDesempenho a WHERE a.colaborador = :colaborador " +
           "AND a.periodoInicio <= :fim AND a.periodoFim >= :inicio")
    boolean existeAvaliacaoNoPeriodo(@Param("colaborador") Colaborador colaborador,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);
}

