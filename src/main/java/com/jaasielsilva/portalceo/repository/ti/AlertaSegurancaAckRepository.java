package com.jaasielsilva.portalceo.repository.ti;

import com.jaasielsilva.portalceo.model.ti.AlertaSegurancaAck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlertaSegurancaAckRepository extends JpaRepository<AlertaSegurancaAck, Long> {

    @Query("SELECT a FROM AlertaSegurancaAck a WHERE a.alertaId = :alertaId AND a.usuarioId = :usuarioId")
    Optional<AlertaSegurancaAck> findByAlertaAndUsuario(@Param("alertaId") Long alertaId,
                                                        @Param("usuarioId") Long usuarioId);
}