package com.jaasielsilva.portalceo.service.servicos;

import com.jaasielsilva.portalceo.dto.servicos.AprovacaoDTO;
import com.jaasielsilva.portalceo.model.servicos.AprovacaoSolicitacao;
import com.jaasielsilva.portalceo.model.servicos.AprovacaoSolicitacao.StatusAprovacao;
import com.jaasielsilva.portalceo.repository.servicos.AprovacaoSolicitacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AprovacaoService {
    private final AprovacaoSolicitacaoRepository aprovacaoRepository;

    public AprovacaoService(AprovacaoSolicitacaoRepository aprovacaoRepository) {
        this.aprovacaoRepository = aprovacaoRepository;
    }

    public List<AprovacaoDTO> listarPendentes() {
        return aprovacaoRepository.findByStatusOrderByCriadoEmAsc(StatusAprovacao.EM_APROVACAO)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AprovacaoDTO toDTO(AprovacaoSolicitacao a) {
        return new AprovacaoDTO(
                a.getId(),
                a.getSolicitacao() != null ? a.getSolicitacao().getId() : null,
                a.getSolicitacao() != null ? a.getSolicitacao().getTitulo() : null,
                a.getSolicitacao() != null && a.getSolicitacao().getServico() != null ? a.getSolicitacao().getServico().getNome() : null,
                a.getGestorNome(),
                a.getJustificativa(),
                a.getStatus()
        );
    }
}