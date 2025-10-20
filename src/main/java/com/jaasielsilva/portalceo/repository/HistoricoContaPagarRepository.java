package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.HistoricoContaPagar;
import com.jaasielsilva.portalceo.model.ContaPagar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoContaPagarRepository extends JpaRepository<HistoricoContaPagar, Long> {

    List<HistoricoContaPagar> findByContaOrderByDataDesc(ContaPagar conta);

    // Se quiser por id da conta
    List<HistoricoContaPagar> findByContaIdOrderByDataDesc(Long contaId);
}
