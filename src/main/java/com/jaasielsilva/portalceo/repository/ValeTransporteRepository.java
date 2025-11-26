package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ValeTransporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query("SELECT v FROM ValeTransporte v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.colaborador.id IN :ids")
    List<ValeTransporte> findByMesAnoAndColaboradorIdIn(@Param("mes") Integer mes, @Param("ano") Integer ano, @Param("ids") java.util.Collection<Long> ids);

    List<ValeTransporte> findByStatusOrderByColaborador_Nome(ValeTransporte.StatusValeTransporte status);

    @Query("SELECT v FROM ValeTransporte v WHERE v.colaborador.ativo = true AND v.status = :status ORDER BY v.colaborador.nome")
    List<ValeTransporte> findByStatusAndColaboradorAtivo(@Param("status") ValeTransporte.StatusValeTransporte status);

    @Query("SELECT SUM(v.valorTotalMes) FROM ValeTransporte v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = 'ATIVO'")
    Double sumValorTotalByMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query("SELECT SUM(v.valorDesconto) FROM ValeTransporte v WHERE v.mesReferencia = :mes AND v.anoReferencia = :ano AND v.status = 'ATIVO'")
    Double sumValorDescontoByMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);

    boolean existsByColaboradorAndMesReferenciaAndAnoReferencia(
            Colaborador colaborador, 
            Integer mesReferencia, 
            Integer anoReferencia
    );
}
