package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.HistoricoColaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoColaboradorRepository extends JpaRepository<HistoricoColaborador, Long> {

    // Buscar todos os históricos de um colaborador específico
    List<HistoricoColaborador> findByColaboradorId(Long colaboradorId);

    long countByEventoAndDataRegistroBetween(String evento, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    List<HistoricoColaborador> findByEventoAndDataRegistroBetween(String evento, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    @org.springframework.data.jpa.repository.Query("SELECT YEAR(h.dataRegistro) as ano, MONTH(h.dataRegistro) as mes, COUNT(h) as qtd " +
            "FROM HistoricoColaborador h " +
            "WHERE h.evento = 'Desligamento' AND h.dataRegistro BETWEEN :inicio AND :fim " +
            "AND (:departamentoNome IS NULL OR (h.colaborador.departamento IS NOT NULL AND LOWER(h.colaborador.departamento.nome) = LOWER(:departamentoNome))) " +
            "AND (:cargoNome IS NULL OR (h.colaborador.cargo IS NOT NULL AND LOWER(h.colaborador.cargo.nome) = LOWER(:cargoNome))) " +
            "AND (:tipoMovimento IS NULL OR (:tipoMovimento = 'VOLUNTARIO' AND LOWER(h.descricao) LIKE '%volunt%') OR (:tipoMovimento = 'INVOLUNTARIO' AND LOWER(h.descricao) LIKE '%involunt%')) " +
            "GROUP BY YEAR(h.dataRegistro), MONTH(h.dataRegistro) " +
            "ORDER BY YEAR(h.dataRegistro), MONTH(h.dataRegistro)")
    java.util.List<Object[]> contarDesligamentosPorMes(
            @org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
            @org.springframework.data.repository.query.Param("fim") java.time.LocalDateTime fim,
            @org.springframework.data.repository.query.Param("departamentoNome") String departamentoNome,
            @org.springframework.data.repository.query.Param("cargoNome") String cargoNome,
            @org.springframework.data.repository.query.Param("tipoMovimento") String tipoMovimento);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(h.colaborador.departamento.nome, 'Sem departamento') as nome, COUNT(h) as qtd " +
            "FROM HistoricoColaborador h " +
            "WHERE h.evento = 'Desligamento' AND h.dataRegistro BETWEEN :inicio AND :fim " +
            "GROUP BY h.colaborador.departamento.nome " +
            "ORDER BY nome")
    java.util.List<Object[]> contarDesligamentosPorDepartamento(
            @org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
            @org.springframework.data.repository.query.Param("fim") java.time.LocalDateTime fim);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(h.colaborador.cargo.nome, 'Sem cargo') as nome, COUNT(h) as qtd " +
            "FROM HistoricoColaborador h " +
            "WHERE h.evento = 'Desligamento' AND h.dataRegistro BETWEEN :inicio AND :fim " +
            "GROUP BY h.colaborador.cargo.nome " +
            "ORDER BY nome")
    java.util.List<Object[]> contarDesligamentosPorCargo(
            @org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
            @org.springframework.data.repository.query.Param("fim") java.time.LocalDateTime fim);
}
