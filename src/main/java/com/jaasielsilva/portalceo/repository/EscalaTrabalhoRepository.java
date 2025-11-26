package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.EscalaTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EscalaTrabalhoRepository extends JpaRepository<EscalaTrabalho, Long> {

    List<EscalaTrabalho> findByAtivoTrueOrderByNome();

    List<EscalaTrabalho> findByTipoAndAtivoTrueOrderByNome(EscalaTrabalho.TipoEscala tipo);

    @EntityGraph(attributePaths = {"usuarioCriacao"})
    @Query("SELECT e FROM EscalaTrabalho e WHERE e.ativo = true AND (e.dataVigenciaFim IS NULL OR e.dataVigenciaFim >= :data) AND (e.dataVigenciaInicio IS NULL OR e.dataVigenciaInicio <= :data) ORDER BY e.nome")
    List<EscalaTrabalho> findEscalasVigentes(@Param("data") LocalDate data);

    @Query("SELECT e FROM EscalaTrabalho e WHERE e.cargaHorariaDiaria BETWEEN :minutos1 AND :minutos2 AND e.ativo = true ORDER BY e.cargaHorariaDiaria")
    List<EscalaTrabalho> findByCargaHorariaDiariaBetween(@Param("minutos1") Integer minutos1, @Param("minutos2") Integer minutos2);

    boolean existsByNomeAndAtivoTrue(String nome);
}
