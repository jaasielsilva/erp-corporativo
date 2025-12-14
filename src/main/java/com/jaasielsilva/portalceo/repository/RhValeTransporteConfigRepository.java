package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.RhValeTransporteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RhValeTransporteConfigRepository extends JpaRepository<RhValeTransporteConfig, Long> {
    RhValeTransporteConfig findFirstByOrderByDataAtualizacaoDesc();
}
