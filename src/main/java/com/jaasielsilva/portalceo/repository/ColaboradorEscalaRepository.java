package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ColaboradorEscala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ColaboradorEscalaRepository extends JpaRepository<ColaboradorEscala, Long> {

    @Query("SELECT ce FROM ColaboradorEscala ce WHERE ce.colaborador.id = :colaboradorId AND ce.ativo = true " +
           "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
           "AND ce.escalaTrabalho.ativo = true " +
           "AND (:data >= COALESCE(ce.escalaTrabalho.dataVigenciaInicio, :data)) " +
           "AND (ce.escalaTrabalho.dataVigenciaFim IS NULL OR :data <= ce.escalaTrabalho.dataVigenciaFim)")
    List<ColaboradorEscala> findVigenteByColaboradorAndData(@Param("colaboradorId") Long colaboradorId, @Param("data") LocalDate data);

    boolean existsByColaborador_IdAndAtivoTrue(Long colaboradorId);

    @Query("SELECT ce FROM ColaboradorEscala ce WHERE ce.ativo = true " +
           "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
           "AND ce.escalaTrabalho.ativo = true " +
           "AND (:data >= COALESCE(ce.escalaTrabalho.dataVigenciaInicio, :data)) " +
           "AND (ce.escalaTrabalho.dataVigenciaFim IS NULL OR :data <= ce.escalaTrabalho.dataVigenciaFim)")
    List<ColaboradorEscala> findVigentesByData(@Param("data") LocalDate data);
}