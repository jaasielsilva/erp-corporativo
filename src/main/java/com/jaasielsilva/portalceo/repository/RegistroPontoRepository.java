package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    Optional<RegistroPonto> findByColaboradorAndData(Colaborador colaborador, LocalDate data);

    List<RegistroPonto> findByColaboradorAndDataBetweenOrderByDataDesc(
            Colaborador colaborador, 
            LocalDate dataInicio, 
            LocalDate dataFim
    );

    List<RegistroPonto> findByDataBetweenOrderByColaborador_NomeAscDataDesc(
            LocalDate dataInicio, 
            LocalDate dataFim
    );

    List<RegistroPonto> findByColaboradorAndDataBetweenAndStatusOrderByDataDesc(
            Colaborador colaborador, 
            LocalDate dataInicio, 
            LocalDate dataFim,
            RegistroPonto.StatusPonto status
    );

    @Query("SELECT r FROM RegistroPonto r WHERE r.data = :data ORDER BY r.colaborador.nome")
    List<RegistroPonto> findByData(@Param("data") LocalDate data);

    @Query("SELECT r FROM RegistroPonto r WHERE r.colaborador.departamento.id = :departamentoId AND r.data BETWEEN :dataInicio AND :dataFim ORDER BY r.colaborador.nome, r.data DESC")
    List<RegistroPonto> findByDepartamentoAndPeriodo(
            @Param("departamentoId") Long departamentoId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT r FROM RegistroPonto r WHERE r.status = :status AND r.data BETWEEN :dataInicio AND :dataFim ORDER BY r.data DESC, r.colaborador.nome")
    List<RegistroPonto> findByStatusAndPeriodo(
            @Param("status") RegistroPonto.StatusPonto status,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT SUM(r.totalMinutosTrabalhados) FROM RegistroPonto r WHERE r.colaborador.id = :colaboradorId AND r.data BETWEEN :dataInicio AND :dataFim AND r.falta = false")
    Long sumMinutosTrabalhadosByColaboradorAndPeriodo(
            @Param("colaboradorId") Long colaboradorId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT SUM(r.minutosHoraExtra) FROM RegistroPonto r WHERE r.colaborador.id = :colaboradorId AND r.data BETWEEN :dataInicio AND :dataFim")
    Long sumMinutosHoraExtraByColaboradorAndPeriodo(
            @Param("colaboradorId") Long colaboradorId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT COUNT(r) FROM RegistroPonto r WHERE r.colaborador.id = :colaboradorId AND r.data BETWEEN :dataInicio AND :dataFim AND r.falta = true")
    Long countFaltasByColaboradorAndPeriodo(
            @Param("colaboradorId") Long colaboradorId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT COUNT(r) FROM RegistroPonto r WHERE r.colaborador.id = :colaboradorId AND r.data BETWEEN :dataInicio AND :dataFim AND r.minutosAtraso > 0")
    Long countAtrasosByColaboradorAndPeriodo(
            @Param("colaboradorId") Long colaboradorId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    boolean existsByColaboradorAndData(Colaborador colaborador, LocalDate data);

    @Query("SELECT r FROM RegistroPonto r WHERE r.colaborador.ativo = true AND r.data = :data ORDER BY r.colaborador.nome")
    List<RegistroPonto> findByDataAndColaboradorAtivo(@Param("data") LocalDate data);

    @Query("SELECT r FROM RegistroPonto r WHERE r.data BETWEEN :dataInicio AND :dataFim AND r.status IN :statuses ORDER BY r.data DESC, r.colaborador.nome")
    List<RegistroPonto> findByPeriodoAndStatusIn(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("statuses") List<RegistroPonto.StatusPonto> statuses
    );

    List<RegistroPonto> findByColaboradorAndDataOrderByEntrada1Desc(
        Colaborador colaborador, LocalDate data
);

    // Buscar os últimos registros ordenados por data de criação
    List<RegistroPonto> findTop10ByOrderByDataCriacaoDesc();
    
}