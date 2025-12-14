package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ValeTransporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ValeTransporteRepository extends JpaRepository<ValeTransporte, Long> {

    List<ValeTransporte> findByColaboradorAndStatusOrderByAnoReferenciaDescMesReferenciaDesc(
            Colaborador colaborador, 
            ValeTransporte.StatusValeTransporte status
    );

    Optional<ValeTransporte> findByColaboradorAndMesReferenciaAndAnoReferencia(
            Colaborador colaborador, 
            Integer mesReferencia, 
            Integer anoReferencia
    );

    List<ValeTransporte> findByMesReferenciaAndAnoReferenciaOrderByColaborador_Nome(
            Integer mesReferencia, 
            Integer anoReferencia
    );

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT v FROM ValeTransporte v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.colaborador.id IN :ids")
    List<ValeTransporte> findByMesAnoAndColaboradorIdIn(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("ids") java.util.Collection<Long> ids);

    List<ValeTransporte> findByStatusOrderByColaborador_Nome(ValeTransporte.StatusValeTransporte status);

    @Query("SELECT v FROM ValeTransporte v WHERE v.colaborador.ativo = true AND v.status = :status ORDER BY v.colaborador.nome")
    List<ValeTransporte> findByStatusAndColaboradorAtivo(@Param("status") ValeTransporte.StatusValeTransporte status);

    @Query("SELECT SUM(v.valorTotalMes) FROM ValeTransporte v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = 'ATIVO'")
    Double sumValorTotalByMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query("SELECT SUM(v.valorDesconto) FROM ValeTransporte v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = 'ATIVO'")
    Double sumValorDescontoByMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query("SELECT COUNT(v) FROM ValeTransporte v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = :status")
    Long countByMesAnoAndStatus(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("status") ValeTransporte.StatusValeTransporte status);

    boolean existsByColaboradorAndMesReferenciaAndAnoReferencia(
            Colaborador colaborador, 
            Integer mesReferencia, 
            Integer anoReferencia
    );

    boolean existsByColaboradorAndMesReferenciaAndAnoReferenciaAndStatus(
            Colaborador colaborador,
            Integer mesReferencia,
            Integer anoReferencia,
            ValeTransporte.StatusValeTransporte status
    );

    List<ValeTransporte> findByMesReferenciaAndAnoReferenciaAndStatus(
            Integer mesReferencia,
            Integer anoReferencia,
            ValeTransporte.StatusValeTransporte status
    );

    @Query(
        "SELECT new com.jaasielsilva.portalceo.dto.ValeTransporteListDTO(" +
        " v.id, c.nome, CAST(c.id AS string), COALESCE(d.nome, 'N/A'), v.linhaOnibus, v.viagensDia, v.diasUteis, " +
        " v.valorTotalMes, v.valorDesconto, v.valorSubsidioEmpresa, v.status, v.enderecoOrigem, v.enderecoDestino) " +
        "FROM ValeTransporte v " +
        "LEFT JOIN v.colaborador c " +
        "LEFT JOIN c.departamento d " +
        "WHERE (:mes IS NULL OR v.mesReferencia = :mes) " +
        "AND (:ano IS NULL OR v.anoReferencia = :ano) " +
        "AND (:status IS NULL OR v.status = :status) " +
        "AND (:colabId IS NULL OR c.id = :colabId) " +
        "AND (:depId IS NULL OR d.id = :depId) " +
        "AND (:q IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :q, '%')))"
    )
    Page<com.jaasielsilva.portalceo.dto.ValeTransporteListDTO> listarPaginado(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            @Param("status") ValeTransporte.StatusValeTransporte status,
            @Param("colabId") Long colaboradorId,
            @Param("depId") Long departamentoId,
            @Param("q") String q,
            Pageable pageable);
}
