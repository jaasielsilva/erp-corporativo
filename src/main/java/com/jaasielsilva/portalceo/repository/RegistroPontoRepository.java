package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
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

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT r.colaborador.id, r.entrada1, r.saida1, r.entrada2, r.saida2, r.falta, r.minutosAtraso, r.totalMinutosTrabalhados " +
           "FROM RegistroPonto r WHERE r.data = :data")
    List<Object[]> findCamposDia(@Param("data") LocalDate data);

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

    /**
     * Projeção agregada para resumo mensal de ponto por colaborador
     */
    public static interface PontoResumoProjection {
        Long getFaltas();
        Long getAtrasos();
        Long getMinutosTrabalhados();
        Long getMinutosExtras();
        Long getDiasComRegistro();
    }

    /**
     * Projeção agregada mensal de ponto agrupada por colaborador, herdando o resumo base
     */
    public static interface PontoResumoPorColaboradorProjection extends PontoResumoProjection {
        Long getColaboradorId();
    }

    /**
     * Consulta única agregada para reduzir múltiplas queries por colaborador
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT " +
           "SUM(CASE WHEN r.falta = true THEN 1 ELSE 0 END) AS faltas, " +
           "SUM(CASE WHEN r.minutosAtraso > 0 THEN 1 ELSE 0 END) AS atrasos, " +
           "SUM(COALESCE(r.totalMinutosTrabalhados, 0)) AS minutosTrabalhados, " +
           "SUM(COALESCE(r.minutosHoraExtra, 0)) AS minutosExtras, " +
           "COUNT(DISTINCT r.data) AS diasComRegistro " +
           "FROM RegistroPonto r WHERE r.colaborador.id = :colaboradorId " +
           "AND r.data >= :dataInicio AND r.data <= :dataFim")
    PontoResumoProjection aggregateResumoByColaboradorAndPeriodo(
            @Param("colaboradorId") Long colaboradorId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    /**
     * Consulta única para o período, agregando por colaborador (reduz N consultas para 1)
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT " +
           "r.colaborador.id AS colaboradorId, " +
           "SUM(CASE WHEN r.falta = true THEN 1 ELSE 0 END) AS faltas, " +
           "SUM(CASE WHEN r.minutosAtraso > 0 THEN 1 ELSE 0 END) AS atrasos, " +
           "SUM(COALESCE(r.totalMinutosTrabalhados, 0)) AS minutosTrabalhados, " +
           "SUM(COALESCE(r.minutosHoraExtra, 0)) AS minutosExtras, " +
           "COUNT(DISTINCT r.data) AS diasComRegistro " +
           "FROM RegistroPonto r " +
           "WHERE r.data >= :dataInicio AND r.data <= :dataFim " +
           "GROUP BY r.colaborador.id")
    List<PontoResumoPorColaboradorProjection> aggregateResumoPorPeriodoGroupByColaborador(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    public static interface PontoMensalAggregationProjection {
        Integer getAno();
        Integer getMes();
        Long getFaltas();
        Long getAtrasos();
        Long getMinutos();
    }

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT YEAR(r.data) as ano, MONTH(r.data) as mes, " +
           "SUM(CASE WHEN (r.falta = true OR r.status IN :statuses) THEN 1 ELSE 0 END) as faltas, " +
           "SUM(CASE WHEN r.minutosAtraso > 0 THEN 1 ELSE 0 END) as atrasos, " +
           "SUM(r.totalMinutosTrabalhados) as minutos " +
           "FROM RegistroPonto r " +
           "WHERE r.data BETWEEN :dataInicio AND :dataFim " +
           "AND (:departamentoNome IS NULL OR (r.colaborador.departamento IS NOT NULL AND LOWER(r.colaborador.departamento.nome) = LOWER(:departamentoNome))) " +
           "AND (:cargoNome IS NULL OR (r.colaborador.cargo IS NOT NULL AND LOWER(r.colaborador.cargo.nome) = LOWER(:cargoNome))) " +
           "GROUP BY YEAR(r.data), MONTH(r.data) " +
           "ORDER BY YEAR(r.data), MONTH(r.data)")
    List<PontoMensalAggregationProjection> aggregateMensal(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("departamentoNome") String departamentoNome,
            @Param("cargoNome") String cargoNome,
            @Param("statuses") java.util.List<com.jaasielsilva.portalceo.model.RegistroPonto.StatusPonto> statuses
    );

}
