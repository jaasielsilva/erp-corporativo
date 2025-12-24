package com.jaasielsilva.portalceo.juridico.previdenciario.processo.repository;

import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface ProcessoPrevidenciarioRepository extends JpaRepository<ProcessoPrevidenciario, Long> {
    @EntityGraph(attributePaths = { "cliente", "responsavel" })
    List<ProcessoPrevidenciario> findAllByOrderByDataAberturaDesc();
}
