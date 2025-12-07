package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoDivulgacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecrutamentoDivulgacaoRepository extends JpaRepository<RecrutamentoDivulgacao, Long> {
    List<RecrutamentoDivulgacao> findByVagaId(Long vagaId);
}

