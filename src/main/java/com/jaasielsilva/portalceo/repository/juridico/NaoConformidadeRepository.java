package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.NaoConformidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NaoConformidadeRepository extends JpaRepository<NaoConformidade, Long> {
    long countByResolvida(boolean resolvida);
}