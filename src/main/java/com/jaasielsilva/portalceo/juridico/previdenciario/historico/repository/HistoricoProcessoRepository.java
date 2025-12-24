package com.jaasielsilva.portalceo.juridico.previdenciario.historico.repository;

import com.jaasielsilva.portalceo.juridico.previdenciario.historico.entity.HistoricoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface HistoricoProcessoRepository extends JpaRepository<HistoricoProcesso, Long> {
    @EntityGraph(attributePaths = {"usuario"})
    List<HistoricoProcesso> findByProcessoPrevidenciario_IdOrderByDataEventoDesc(Long processoId);
}
