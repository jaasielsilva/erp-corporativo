package com.jaasielsilva.portalceo.repository.indicadores;

import com.jaasielsilva.portalceo.model.indicadores.ViewMargemLucro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para leitura da view 'view_margem_lucro'.
 * Normalmente possui apenas 1 registro (id = 1) com o valor da margem consolidada.
 */
@Repository
public interface ViewMargemLucroRepository extends JpaRepository<ViewMargemLucro, Integer> {
}
