package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessoJuridicoRepository extends JpaRepository<ProcessoJuridico, Long> {
    long countByStatus(ProcessoJuridico.StatusProcesso status);
    java.util.List<ProcessoJuridico> findByStatus(ProcessoJuridico.StatusProcesso status);
}