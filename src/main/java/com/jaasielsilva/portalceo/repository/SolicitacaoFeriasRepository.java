package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.SolicitacaoFerias;
import com.jaasielsilva.portalceo.model.SolicitacaoFerias.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.Colaborador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SolicitacaoFeriasRepository extends JpaRepository<SolicitacaoFerias, Long> {
    Page<SolicitacaoFerias> findByStatus(StatusSolicitacao status, Pageable pageable);
    List<SolicitacaoFerias> findByColaboradorOrderByDataSolicitacaoDesc(Colaborador colaborador);

    @Query("SELECT s FROM SolicitacaoFerias s WHERE (:status IS NULL OR s.status = :status) " +
           "AND (:inicio IS NULL OR s.periodoInicio >= :inicio) " +
           "AND (:fim IS NULL OR s.periodoFim <= :fim) " +
           "ORDER BY s.dataSolicitacao DESC")
    Page<SolicitacaoFerias> pesquisar(@Param("status") StatusSolicitacao status,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fim") LocalDate fim,
                                      Pageable pageable);

    @Query("SELECT COUNT(s) > 0 FROM SolicitacaoFerias s WHERE s.colaborador = :colaborador " +
           "AND s.status IN ('SOLICITADA','APROVADA') " +
           "AND s.periodoInicio <= :fim AND s.periodoFim >= :inicio")
    boolean existeConflitoPeriodo(@Param("colaborador") Colaborador colaborador,
                                  @Param("inicio") LocalDate inicio,
                                  @Param("fim") LocalDate fim);
}
