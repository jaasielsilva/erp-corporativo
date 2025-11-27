package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ValeRefeicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValeRefeicaoRepository extends JpaRepository<ValeRefeicao, Long> {

    List<ValeRefeicao> findByStatus(ValeRefeicao.StatusValeRefeicao status);

    List<ValeRefeicao> findByColaboradorOrderByAnoReferenciaDescMesReferenciaDesc(Colaborador colaborador);

    Optional<ValeRefeicao> findByColaboradorAndStatus(Colaborador colaborador, ValeRefeicao.StatusValeRefeicao status);

    Optional<ValeRefeicao> findByColaboradorAndMesReferenciaAndAnoReferencia(Colaborador colaborador, Integer mesReferencia, Integer anoReferencia);

    List<ValeRefeicao> findByMesReferenciaAndAnoReferencia(Integer mesReferencia, Integer anoReferencia);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT v FROM ValeRefeicao v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.colaborador.id IN :ids")
    List<ValeRefeicao> findByMesAnoAndColaboradorIdIn(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("ids") java.util.Collection<Long> ids);

    List<ValeRefeicao> findByAnoReferenciaOrderByMesReferenciaDesc(Integer anoReferencia);

    @Query("SELECT v FROM ValeRefeicao v WHERE v.colaborador.id = :colaboradorId AND v.status = :status ORDER BY v.anoReferencia DESC, v.mesReferencia DESC")
    List<ValeRefeicao> findByColaboradorAndStatusOrdered(@Param("colaboradorId") Long colaboradorId, @Param("status") ValeRefeicao.StatusValeRefeicao status);

    @Query("SELECT v FROM ValeRefeicao v WHERE v.colaborador.departamento.id = :departamentoId AND v.mesReferencia = :mes AND v.anoReferencia = :ano")
    List<ValeRefeicao> findByDepartamentoAndMesAno(@Param("departamentoId") Long departamentoId, @Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query("SELECT SUM(v.valorTotalMes) FROM ValeRefeicao v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = :status")
    Double sumValorTotalByMesAnoAndStatus(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("status") ValeRefeicao.StatusValeRefeicao status);

    @Query("SELECT SUM(v.valorSubsidioEmpresa) FROM ValeRefeicao v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = :status")
    Double sumSubsidioEmpresaByMesAnoAndStatus(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("status") ValeRefeicao.StatusValeRefeicao status);

    @Query("SELECT SUM(v.valorDesconto) FROM ValeRefeicao v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = :status")
    Double sumDescontoByMesAnoAndStatus(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("status") ValeRefeicao.StatusValeRefeicao status);

    @Query("SELECT COUNT(v) FROM ValeRefeicao v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = :status")
    Long countByMesAnoAndStatus(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("status") ValeRefeicao.StatusValeRefeicao status);

    boolean existsByColaboradorAndMesReferenciaAndAnoReferencia(Colaborador colaborador, Integer mesReferencia, Integer anoReferencia);

    @Query("SELECT v FROM ValeRefeicao v WHERE v.colaborador.ativo = true AND v.status = :status ORDER BY v.colaborador.nome")
    List<ValeRefeicao> findByColaboradorAtivoAndStatus(@Param("status") ValeRefeicao.StatusValeRefeicao status);

    @Query("SELECT v FROM ValeRefeicao v WHERE v.operadora = :operadora AND v.status = :status")
    List<ValeRefeicao> findByOperadoraAndStatus(@Param("operadora") String operadora, @Param("status") ValeRefeicao.StatusValeRefeicao status);

    @Query("SELECT v FROM ValeRefeicao v WHERE v.tipo = :tipo AND v.mesReferencia = :mes AND v.anoReferencia = :ano")
    List<ValeRefeicao> findByTipoAndMesAno(@Param("tipo") ValeRefeicao.TipoVale tipo, @Param("mes") Integer mes, @Param("ano") Integer ano);
}
