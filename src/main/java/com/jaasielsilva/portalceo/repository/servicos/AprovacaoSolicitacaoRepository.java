package com.jaasielsilva.portalceo.repository.servicos;

import com.jaasielsilva.portalceo.model.servicos.AprovacaoSolicitacao;
import com.jaasielsilva.portalceo.model.servicos.AprovacaoSolicitacao.StatusAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AprovacaoSolicitacaoRepository extends JpaRepository<AprovacaoSolicitacao, Long> {
    List<AprovacaoSolicitacao> findByStatusOrderByCriadoEmAsc(StatusAprovacao status);
}