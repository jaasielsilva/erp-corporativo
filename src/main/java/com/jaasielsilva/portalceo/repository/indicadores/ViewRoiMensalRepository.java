package com.jaasielsilva.portalceo.repository.indicadores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jaasielsilva.portalceo.model.indicadores.ViewRoiMensal;

@Repository
public interface ViewRoiMensalRepository extends JpaRepository<ViewRoiMensal, Integer> {
}
