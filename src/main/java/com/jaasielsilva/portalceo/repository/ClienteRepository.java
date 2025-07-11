package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}

