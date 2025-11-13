package com.jaasielsilva.portalceo.repository.servicos;

import com.jaasielsilva.portalceo.model.servicos.SolicitacaoServico;
import com.jaasielsilva.portalceo.model.servicos.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoServicoRepository extends JpaRepository<SolicitacaoServico, Long> {
    List<SolicitacaoServico> findBySolicitanteNomeOrderByCriadoEmDesc(String solicitanteNome);
    List<SolicitacaoServico> findByStatusOrderByCriadoEmAsc(StatusSolicitacao status);
}