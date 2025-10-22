package com.jaasielsilva.portalceo.repository.projetos;

import com.jaasielsilva.portalceo.model.projetos.EquipeProjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipeProjetoRepository extends JpaRepository<EquipeProjeto, Long> {
}