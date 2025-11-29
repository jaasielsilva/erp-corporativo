package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.FolhaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolhaPagamentoRepository extends JpaRepository<FolhaPagamento, Long> {

    List<FolhaPagamento> findByAnoReferenciaOrderByMesReferenciaDesc(Integer anoReferencia);

    List<FolhaPagamento> findByMesReferenciaAndAnoReferencia(Integer mesReferencia, Integer anoReferencia);

    Optional<FolhaPagamento> findByMesReferenciaAndAnoReferenciaAndStatus(
            Integer mesReferencia, 
            Integer anoReferencia, 
            FolhaPagamento.StatusFolha status
    );

    List<FolhaPagamento> findByStatusOrderByAnoReferenciaDescMesReferenciaDesc(FolhaPagamento.StatusFolha status);

    @Query("SELECT f FROM FolhaPagamento f WHERE f.anoReferencia = :ano AND f.mesReferencia = :mes")
    Optional<FolhaPagamento> findFolhaByMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query("SELECT f FROM FolhaPagamento f WHERE f.anoReferencia = :ano AND f.mesReferencia = :mes AND LOWER(COALESCE(f.tipoFolha, 'normal')) = LOWER(:tipo)")
    Optional<FolhaPagamento> findFolhaByMesAnoAndTipo(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("tipo") String tipo);

    @Query("SELECT f FROM FolhaPagamento f WHERE f.status IN :statuses ORDER BY f.anoReferencia DESC, f.mesReferencia DESC")
    List<FolhaPagamento> findByStatusIn(@Param("statuses") List<FolhaPagamento.StatusFolha> statuses);

    @Query("SELECT COUNT(f) FROM FolhaPagamento f WHERE f.anoReferencia = :ano")
    Long countByAno(@Param("ano") Integer ano);

    @Query("SELECT f FROM FolhaPagamento f WHERE f.anoReferencia >= :anoInicio ORDER BY f.anoReferencia DESC, f.mesReferencia DESC")
    List<FolhaPagamento> findFolhasRecentes(@Param("anoInicio") Integer anoInicio);

    boolean existsByMesReferenciaAndAnoReferencia(Integer mesReferencia, Integer anoReferencia);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM FolhaPagamento f WHERE f.anoReferencia = :ano AND f.mesReferencia = :mes AND LOWER(COALESCE(f.tipoFolha, 'normal')) = LOWER(:tipo)")
    boolean existsByMesAnoAndTipo(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("tipo") String tipo);
}
