package com.jaasielsilva.portalceo.repository.indicadores;

import com.jaasielsilva.portalceo.model.indicadores.ViewInadimplencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewInadimplenciaRepository extends JpaRepository<ViewInadimplencia, Integer> {
}
