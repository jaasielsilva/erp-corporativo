package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.HistoricoContaPagar;
import com.jaasielsilva.portalceo.model.ContaPagar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoContaPagarRepository extends JpaRepository<HistoricoContaPagar, Long> {

    // Ajustado para o nome correto da propriedade da entidade
    List<HistoricoContaPagar> findByContaOrderByDataHoraDesc(ContaPagar conta);

    // Também ajustado para buscar por id da conta
    List<HistoricoContaPagar> findByContaIdOrderByDataHoraDesc(Long contaId);
}
