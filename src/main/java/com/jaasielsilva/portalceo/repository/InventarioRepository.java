package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
}
