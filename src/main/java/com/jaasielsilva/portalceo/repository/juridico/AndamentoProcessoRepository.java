package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.AndamentoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AndamentoProcessoRepository extends JpaRepository<AndamentoProcesso, Long> {
    java.util.List<AndamentoProcesso> findTop10ByOrderByDataHoraDesc();
}