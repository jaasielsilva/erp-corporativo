package com.jaasielsilva.portalceo.service.rh;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.SolicitacaoFerias;
import com.jaasielsilva.portalceo.model.SolicitacaoFerias.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.SolicitacaoFeriasRepository;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SolicitacaoFeriasService {

    @Autowired
    private SolicitacaoFeriasRepository repository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private AuditoriaRhLogService auditoriaRhLogService;

    @Transactional
    public SolicitacaoFerias solicitar(Long colaboradorId, LocalDate inicio, LocalDate fim, String observacoes,
                                       Usuario usuarioLogado, String ip) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Data fim não pode ser anterior à data início");
        }
        if (repository.existeConflitoPeriodo(colaborador, inicio, fim)) {
            throw new IllegalArgumentException("Já existe solicitação/aprovação em conflito para o período informado");
        }

        SolicitacaoFerias s = new SolicitacaoFerias();
        s.setColaborador(colaborador);
        s.setPeriodoInicio(inicio);
        s.setPeriodoFim(fim);
        s.setObservacoes(observacoes);
        s.setStatus(StatusSolicitacao.SOLICITADA);
        s.setDataSolicitacao(LocalDateTime.now());
        s.setUsuarioCriacao(usuarioLogado);

        SolicitacaoFerias saved = repository.save(s);

        String detalhes = "Solicitação de férias criada: colaborador=" + colaborador.getId() +
                " periodo=" + inicio + " a " + fim;
        auditoriaRhLogService.registrar("FERIAS", "SOLICITAR", "/api/rh/ferias/solicitacoes", 
                usuarioLogado != null ? usuarioLogado.getEmail() : null, ip, detalhes, true);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoFerias> listar(StatusSolicitacao status, LocalDate inicio, LocalDate fim, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return repository.pesquisar(status, inicio, fim, pageable);
    }

    @Transactional
    public SolicitacaoFerias aprovar(Long id, Usuario aprovador, String observacoes, String ip) {
        SolicitacaoFerias s = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        if (s.getStatus() != StatusSolicitacao.SOLICITADA) {
            throw new IllegalStateException("Somente solicitações em estado SOLICITADA podem ser aprovadas");
        }
        StatusSolicitacao anterior = s.getStatus();
        s.setStatus(StatusSolicitacao.APROVADA);
        s.setDataDecisao(LocalDateTime.now());
        s.setUsuarioAprovacao(aprovador);
        s.setObservacoes(observacoes);
        SolicitacaoFerias saved = repository.save(s);

        String detalhes = "Aprovação de férias: id=" + id + " status=" + anterior + "->APROVADA";
        auditoriaRhLogService.registrar("FERIAS", "APROVAR", "/api/rh/ferias/" + id + "/aprovar",
                aprovador != null ? aprovador.getEmail() : null, ip, detalhes, true);

        return saved;
    }

    @Transactional
    public SolicitacaoFerias reprovar(Long id, Usuario aprovador, String observacoes, String ip) {
        SolicitacaoFerias s = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        if (s.getStatus() != StatusSolicitacao.SOLICITADA) {
            throw new IllegalStateException("Somente solicitações em estado SOLICITADA podem ser reprovadas");
        }
        StatusSolicitacao anterior = s.getStatus();
        s.setStatus(StatusSolicitacao.REPROVADA);
        s.setDataDecisao(LocalDateTime.now());
        s.setUsuarioAprovacao(aprovador);
        s.setObservacoes(observacoes);
        SolicitacaoFerias saved = repository.save(s);

        String detalhes = "Reprovação de férias: id=" + id + " status=" + anterior + "->REPROVADA";
        auditoriaRhLogService.registrar("FERIAS", "REPROVAR", "/api/rh/ferias/" + id + "/reprovar",
                aprovador != null ? aprovador.getEmail() : null, ip, detalhes, true);

        return saved;
    }
}
