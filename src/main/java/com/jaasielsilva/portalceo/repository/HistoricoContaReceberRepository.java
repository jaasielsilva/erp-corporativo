package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.HistoricoContaReceber;
import com.jaasielsilva.portalceo.model.ContaReceber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoContaReceberRepository extends JpaRepository<HistoricoContaReceber, Long> {

    List<HistoricoContaReceber> findByContaOrderByDataHoraDesc(ContaReceber conta);

    List<HistoricoContaReceber> findByContaIdOrderByDataHoraDesc(Long contaId);
}