package com.jaasielsilva.portalceo.repository.ti;

import com.jaasielsilva.portalceo.model.ti.SistemaMetricas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SistemaMetricasRepository extends JpaRepository<SistemaMetricas, Long> {
    SistemaMetricas findTopByOrderByCreatedAtDesc();
    List<SistemaMetricas> findTop20ByOrderByCreatedAtDesc();
}