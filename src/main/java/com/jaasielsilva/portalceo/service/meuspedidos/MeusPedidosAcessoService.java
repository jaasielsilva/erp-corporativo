package com.jaasielsilva.portalceo.service.meuspedidos;

import com.jaasielsilva.portalceo.model.SolicitacaoAcesso;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.StatusSolicitacao;
import com.jaasielsilva.portalceo.repository.SolicitacaoAcessoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MeusPedidosAcessoService {

    private final SolicitacaoAcessoRepository solicitacaoAcessoRepository;

    public MeusPedidosAcessoService(SolicitacaoAcessoRepository solicitacaoAcessoRepository) {
        this.solicitacaoAcessoRepository = solicitacaoAcessoRepository;
    }

    public Page<SolicitacaoAcesso> listarTodas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return solicitacaoAcessoRepository.findAllWithFetch(pageable);
    }

    public Page<SolicitacaoAcesso> listarPorStatus(StatusSolicitacao status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return solicitacaoAcessoRepository.findByStatusWithFetch(status, pageable);
    }

    public Page<SolicitacaoAcesso> listarDoSolicitante(String emailSolicitante, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Reutiliza busca por texto para simplificar quando só temos email
        return solicitacaoAcessoRepository.findByTextoWithFetch(emailSolicitante, pageable);
    }

    public Optional<SolicitacaoAcesso> buscarPorId(Long id) {
        return solicitacaoAcessoRepository.findById(id);
    }

    public Optional<SolicitacaoAcesso> buscarPorProtocolo(String protocolo) {
        return solicitacaoAcessoRepository.findByProtocolo(protocolo);
    }

    public SolicitacaoAcesso criarSolicitacaoBasica(SolicitacaoAcesso solicitacao) {
        // Defaults mínimos
        if (solicitacao.getStatus() == null) {
            solicitacao.setStatus(StatusSolicitacao.PENDENTE);
        }
        if (solicitacao.getDataSolicitacao() == null) {
            solicitacao.setDataSolicitacao(LocalDateTime.now());
        }
        // Garantir coerência de prazo
        if (solicitacao.getPrazoAcesso() == SolicitacaoAcesso.PrazoAcesso.TEMPORARIO) {
            if (solicitacao.getDataInicio() == null) {
                solicitacao.setDataInicio(LocalDate.now());
            }
        } else {
            solicitacao.setDataInicio(null);
            solicitacao.setDataFim(null);
        }
        return solicitacaoAcessoRepository.save(solicitacao);
    }

    public long contarPorStatus(StatusSolicitacao status) {
        return solicitacaoAcessoRepository.countByStatus(status);
    }

    public List<SolicitacaoAcesso> listarPendentesUrgentesOuPrazoProximo(LocalDate dataLimite) {
        return solicitacaoAcessoRepository.findSolicitacoesQueNecessitamAtencao(dataLimite);
    }
}