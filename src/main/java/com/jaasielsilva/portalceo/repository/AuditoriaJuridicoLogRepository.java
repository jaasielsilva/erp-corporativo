package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.AuditoriaJuridicoLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditoriaJuridicoLogRepository extends JpaRepository<AuditoriaJuridicoLog, Long> {

    Page<AuditoriaJuridicoLog> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    @Query("select a from AuditoriaJuridicoLog a where (:categoria is null or lower(a.categoria)=lower(:categoria)) " +
            "and (:usuario is null or lower(a.usuario) like lower(concat('%', :usuario, '%'))) " +
            "and (:recurso is null or lower(a.recurso) like lower(concat('%', :recurso, '%'))) " +
            "and (:inicio is null or a.criadoEm >= :inicio) and (:fim is null or a.criadoEm <= :fim) " +
            "order by a.criadoEm desc")
    Page<AuditoriaJuridicoLog> pesquisar(
            @Param("categoria") String categoria,
            @Param("usuario") String usuario,
            @Param("recurso") String recurso,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable);
}
