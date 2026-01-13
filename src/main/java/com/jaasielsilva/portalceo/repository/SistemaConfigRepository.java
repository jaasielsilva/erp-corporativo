package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.SistemaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SistemaConfigRepository extends JpaRepository<SistemaConfig, String> {
    Optional<SistemaConfig> findByKey(String key);
}
