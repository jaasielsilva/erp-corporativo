package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.FolhaPagamento;
import com.jaasielsilva.portalceo.model.Holerite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoleriteRepository extends JpaRepository<Holerite, Long> {

    List<Holerite> findByColaboradorOrderByFolhaPagamento_AnoReferenciaDescFolhaPagamento_MesReferenciaDesc(Colaborador colaborador);

    List<Holerite> findByFolhaPagamento(FolhaPagamento folhaPagamento);

    Optional<Holerite> findByColaboradorAndFolhaPagamento(Colaborador colaborador, FolhaPagamento folhaPagamento);

    @Query("SELECT h FROM Holerite h WHERE h.colaborador.id = :colaboradorId AND h.folhaPagamento.anoReferencia = :ano ORDER BY h.folhaPagamento.mesReferencia DESC")
    List<Holerite> findByColaboradorAndAno(@Param("colaboradorId") Long colaboradorId, @Param("ano") Integer ano);

    @Query("SELECT h FROM Holerite h WHERE h.folhaPagamento.mesReferencia = :mes AND h.folhaPagamento.anoReferencia = :ano")
    List<Holerite> findByMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query("SELECT h FROM Holerite h WHERE h.colaborador.departamento.id = :departamentoId AND h.folhaPagamento.id = :folhaId")
    List<Holerite> findByDepartamentoAndFolha(@Param("departamentoId") Long departamentoId, @Param("folhaId") Long folhaId);

    @Query("SELECT SUM(h.salarioLiquido) FROM Holerite h WHERE h.folhaPagamento.id = :folhaId")
    Double sumSalarioLiquidoByFolha(@Param("folhaId") Long folhaId);

    @Query("SELECT SUM(h.totalProventos) FROM Holerite h WHERE h.folhaPagamento.id = :folhaId")
    Double sumTotalProventosByFolha(@Param("folhaId") Long folhaId);

    @Query("SELECT SUM(h.totalDescontos) FROM Holerite h WHERE h.folhaPagamento.id = :folhaId")
    Double sumTotalDescontosByFolha(@Param("folhaId") Long folhaId);

    @Query("SELECT COUNT(h) FROM Holerite h WHERE h.folhaPagamento.id = :folhaId")
    Long countByFolha(@Param("folhaId") Long folhaId);

    boolean existsByColaboradorAndFolhaPagamento(Colaborador colaborador, FolhaPagamento folhaPagamento);

    @Query("SELECT h FROM Holerite h WHERE h.colaborador.ativo = true AND h.folhaPagamento.id = :folhaId ORDER BY h.colaborador.nome")
    List<Holerite> findByFolhaAndColaboradorAtivo(@Param("folhaId") Long folhaId);

    @EntityGraph(attributePaths = {"colaborador","colaborador.departamento"})
    @Query("SELECT h FROM Holerite h WHERE h.folhaPagamento.id = :folhaId AND (:q IS NULL OR LOWER(h.colaborador.nome) LIKE LOWER(CONCAT('%', :q, '%')))" )
    Page<Holerite> findByFolhaPaginado(@Param("folhaId") Long folhaId, @Param("q") String q, Pageable pageable);

    public static interface HoleriteListProjection {
        Long getId();
        String getColaboradorNome();
        String getDepartamentoNome();
        java.math.BigDecimal getSalarioBase();
        java.math.BigDecimal getTotalProventos();
        java.math.BigDecimal getTotalDescontos();
        java.math.BigDecimal getSalarioLiquido();
    }

    public static interface HoleriteColabListProjection {
        Long getId();
        Integer getMesReferencia();
        Integer getAnoReferencia();
        java.math.BigDecimal getSalarioBase();
        java.math.BigDecimal getTotalProventos();
        java.math.BigDecimal getTotalDescontos();
        java.math.BigDecimal getSalarioLiquido();
    }

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT h.id as id, c.nome as colaboradorNome, d.nome as departamentoNome, h.salarioBase as salarioBase, h.totalProventos as totalProventos, h.totalDescontos as totalDescontos, h.salarioLiquido as salarioLiquido FROM Holerite h JOIN h.colaborador c LEFT JOIN c.departamento d WHERE h.folhaPagamento.id = :folhaId AND (:q IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY c.nome ASC" )
    Page<HoleriteListProjection> findListByFolhaPaginado(@Param("folhaId") Long folhaId, @Param("q") String q, Pageable pageable);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT h.id as id, f.mesReferencia as mesReferencia, f.anoReferencia as anoReferencia, h.salarioBase as salarioBase, h.totalProventos as totalProventos, h.totalDescontos as totalDescontos, h.salarioLiquido as salarioLiquido FROM Holerite h JOIN h.folhaPagamento f WHERE h.colaborador.id = :colaboradorId AND (:ano IS NULL OR f.anoReferencia = :ano) AND (:mes IS NULL OR f.mesReferencia = :mes) ORDER BY f.anoReferencia DESC, f.mesReferencia DESC")
    Page<HoleriteColabListProjection> findListByColaboradorPaginado(@Param("colaboradorId") Long colaboradorId, @Param("ano") Integer ano, @Param("mes") Integer mes, Pageable pageable);
}
