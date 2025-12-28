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

    @Query("SELECT ce FROM ColaboradorEscala ce JOIN FETCH ce.colaborador c JOIN FETCH ce.escalaTrabalho e WHERE ce.colaborador.id = :colaboradorId AND ce.ativo = true " +
           "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
           "AND ce.escalaTrabalho.ativo = true " +
           "AND (:data >= COALESCE(ce.escalaTrabalho.dataVigenciaInicio, :data)) " +
           "AND (ce.escalaTrabalho.dataVigenciaFim IS NULL OR :data <= ce.escalaTrabalho.dataVigenciaFim)")
    List<ColaboradorEscala> findVigenteByColaboradorAndData(@Param("colaboradorId") Long colaboradorId, @Param("data") LocalDate data);

    boolean existsByColaborador_IdAndAtivoTrue(Long colaboradorId);

    @Query("SELECT ce FROM ColaboradorEscala ce JOIN FETCH ce.colaborador c JOIN FETCH ce.escalaTrabalho e WHERE ce.ativo = true " +
           "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
           "AND ce.escalaTrabalho.ativo = true " +
           "AND (:data >= COALESCE(ce.escalaTrabalho.dataVigenciaInicio, :data)) " +
           "AND (ce.escalaTrabalho.dataVigenciaFim IS NULL OR :data <= ce.escalaTrabalho.dataVigenciaFim)")
    List<ColaboradorEscala> findVigentesByData(@Param("data") LocalDate data);

    @Query("SELECT ce FROM ColaboradorEscala ce JOIN FETCH ce.colaborador c JOIN FETCH ce.escalaTrabalho e WHERE ce.ativo = true " +
           "AND c.id IN :ids " +
           "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
           "AND e.ativo = true " +
           "AND (:data >= COALESCE(e.dataVigenciaInicio, :data)) " +
           "AND (e.dataVigenciaFim IS NULL OR :data <= e.dataVigenciaFim)")
    List<ColaboradorEscala> findVigentesByColaboradoresAndData(@Param("ids") List<Long> ids, @Param("data") LocalDate data);

    @Query("SELECT c.id FROM ColaboradorEscala ce JOIN ce.colaborador c JOIN ce.escalaTrabalho e WHERE ce.ativo = true " +
           "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
           "AND e.ativo = true AND e.id = :escalaId " +
           "AND (:data >= COALESCE(e.dataVigenciaInicio, :data)) " +
           "AND (e.dataVigenciaFim IS NULL OR :data <= e.dataVigenciaFim)")
    List<Long> findColaboradorIdsComEscalaNoDia(@Param("data") LocalDate data, @Param("escalaId") Long escalaId);

    @Query(value = "SELECT c.id, c.nome, d.nome, u.matricula, e.id, e.nome, e.horarioEntrada1, e.horarioSaida1, e.horarioEntrada2, e.horarioSaida2, e.tipo, ce.dataInicio " +
                "FROM ColaboradorEscala ce " +
                "JOIN ce.colaborador c " +
                "LEFT JOIN c.departamento d " +
                "LEFT JOIN c.usuario u " +
                "JOIN ce.escalaTrabalho e " +
                "WHERE ce.ativo = true " +
                "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
                "AND e.ativo = true " +
                "AND (:data >= COALESCE(e.dataVigenciaInicio, :data)) " +
                "AND (e.dataVigenciaFim IS NULL OR :data <= e.dataVigenciaFim) " +
                "AND (:departamentoId IS NULL OR (d IS NOT NULL AND d.id = :departamentoId)) " +
                "AND (:escalaId IS NULL OR e.id = :escalaId) " +
                "AND ( :search IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR (u IS NOT NULL AND LOWER(u.matricula) LIKE LOWER(CONCAT('%', :search, '%'))) ) " +
                "AND ( (:seg = true AND e.trabalhaSegunda = true) OR (:ter = true AND e.trabalhaTerca = true) OR (:qua = true AND e.trabalhaQuarta = true) OR (:qui = true AND e.trabalhaQuinta = true) OR (:sex = true AND e.trabalhaSexta = true) OR (:sab = true AND e.trabalhaSabado = true) OR (:dom = true AND e.trabalhaDomingo = true) ) " +
                "ORDER BY c.nome ASC",
            countQuery = "SELECT COUNT(ce) FROM ColaboradorEscala ce " +
            "JOIN ce.colaborador c " +
            "LEFT JOIN c.departamento d " +
            "LEFT JOIN c.usuario u " +
            "JOIN ce.escalaTrabalho e " +
            "WHERE ce.ativo = true " +
            "AND (:data >= ce.dataInicio AND (ce.dataFim IS NULL OR :data <= ce.dataFim)) " +
            "AND e.ativo = true " +
            "AND (:data >= COALESCE(e.dataVigenciaInicio, :data)) " +
            "AND (e.dataVigenciaFim IS NULL OR :data <= e.dataVigenciaFim) " +
            "AND (:departamentoId IS NULL OR (d IS NOT NULL AND d.id = :departamentoId)) " +
            "AND (:escalaId IS NULL OR e.id = :escalaId) " +
            "AND ( :search IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR (u IS NOT NULL AND LOWER(u.matricula) LIKE LOWER(CONCAT('%', :search, '%'))) ) " +
            "AND ( (:seg = true AND e.trabalhaSegunda = true) OR (:ter = true AND e.trabalhaTerca = true) OR (:qua = true AND e.trabalhaQuarta = true) OR (:qui = true AND e.trabalhaQuinta = true) OR (:sex = true AND e.trabalhaSexta = true) OR (:sab = true AND e.trabalhaSabado = true) OR (:dom = true AND e.trabalhaDomingo = true) )")
    org.springframework.data.domain.Page<Object[]> listarParaDia(
            @Param("data") LocalDate data,
            @Param("departamentoId") Long departamentoId,
            @Param("escalaId") Long escalaId,
            @Param("search") String search,
            @Param("seg") boolean seg,
            @Param("ter") boolean ter,
            @Param("qua") boolean qua,
            @Param("qui") boolean qui,
            @Param("sex") boolean sex,
            @Param("sab") boolean sab,
            @Param("dom") boolean dom,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT ce.id, c.id, c.nome, d.id, e.id, e.nome, e.tipo, e.trabalhaSegunda, e.trabalhaTerca, e.trabalhaQuarta, e.trabalhaQuinta, e.trabalhaSexta, e.trabalhaSabado, e.trabalhaDomingo, e.cargaHorariaDiaria, ce.dataInicio, ce.dataFim, e.dataVigenciaInicio, e.dataVigenciaFim " +
            "FROM ColaboradorEscala ce " +
            "JOIN ce.colaborador c " +
            "LEFT JOIN c.departamento d " +
            "JOIN ce.escalaTrabalho e " +
            "WHERE ce.ativo = true " +
            "AND e.ativo = true " +
            "AND ce.dataInicio <= :fim " +
            "AND (ce.dataFim IS NULL OR ce.dataFim >= :inicio) " +
            "AND (:departamentoId IS NULL OR (d IS NOT NULL AND d.id = :departamentoId)) " +
            "AND (:escalaId IS NULL OR e.id = :escalaId)" )
    java.util.List<Object[]> listarAtribuicoesNoPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("departamentoId") Long departamentoId,
            @Param("escalaId") Long escalaId);

    long countByEscalaTrabalho_Id(Long escalaId);
}
