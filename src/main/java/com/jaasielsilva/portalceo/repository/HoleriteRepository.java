package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.FolhaPagamento;
import com.jaasielsilva.portalceo.model.Holerite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}