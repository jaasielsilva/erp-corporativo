package com.jaasielsilva.portalceo.service.servicos;

import com.jaasielsilva.portalceo.dto.servicos.SolicitacaoServicoDTO;
import com.jaasielsilva.portalceo.model.servicos.SolicitacaoServico;
import com.jaasielsilva.portalceo.model.servicos.StatusSolicitacao;
import com.jaasielsilva.portalceo.repository.servicos.SolicitacaoServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {
    private final SolicitacaoServicoRepository solicitacaoRepository;

    public SolicitacaoService(SolicitacaoServicoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    public List<SolicitacaoServicoDTO> listarDoUsuario(String nomeUsuario) {
        return solicitacaoRepository.findBySolicitanteNomeOrderByCriadoEmDesc(nomeUsuario)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<SolicitacaoServicoDTO> listarPorStatus(StatusSolicitacao status) {
        return solicitacaoRepository.findByStatusOrderByCriadoEmAsc(status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private SolicitacaoServicoDTO toDTO(SolicitacaoServico s) {
        return new SolicitacaoServicoDTO(
                s.getId(),
                s.getServico() != null ? s.getServico().getId() : null,
                s.getServico() != null ? s.getServico().getNome() : null,
                s.getTitulo(),
                s.getPrioridade(),
                s.getStatus(),
                s.getCriadoEm(),
                s.getAtualizadoEm()
        );
    }
}