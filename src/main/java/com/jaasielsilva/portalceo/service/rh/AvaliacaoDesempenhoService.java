package com.jaasielsilva.portalceo.service.rh;

import com.jaasielsilva.portalceo.model.AvaliacaoDesempenho;
import com.jaasielsilva.portalceo.model.AvaliacaoDesempenho.StatusAvaliacao;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.AvaliacaoDesempenhoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
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
public class AvaliacaoDesempenhoService {

    @Autowired
    private AvaliacaoDesempenhoRepository repository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private AuditoriaRhLogService auditoriaRhLogService;

    @Transactional
    public AvaliacaoDesempenho abrir(Long colaboradorId, LocalDate inicio, LocalDate fim, Usuario usuarioLogado, String ip) {
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        if (inicio == null || fim == null) throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        if (fim.isBefore(inicio)) throw new IllegalArgumentException("Data fim não pode ser anterior à data início");
        if (repository.existeAvaliacaoNoPeriodo(colaborador, inicio, fim)) throw new IllegalArgumentException("Já existe avaliação no período informado");
        AvaliacaoDesempenho a = new AvaliacaoDesempenho();
        a.setColaborador(colaborador);
        a.setPeriodoInicio(inicio);
        a.setPeriodoFim(fim);
        a.setStatus(StatusAvaliacao.ABERTA);
        a.setUsuarioCriacao(usuarioLogado);
        AvaliacaoDesempenho saved = repository.save(a);
        auditoriaRhLogService.registrar("AVALIACOES", "ABRIR", "/api/rh/avaliacao/ciclos", usuarioLogado != null ? usuarioLogado.getEmail() : null, ip, "Avaliacao aberta id=" + saved.getId(), true);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoDesempenho> listar(StatusAvaliacao status, LocalDate inicio, LocalDate fim, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return repository.pesquisar(status, inicio, fim, pageable);
    }

    @Transactional
    public AvaliacaoDesempenho submeter(Long id, Double nota, String feedback, Usuario usuario, String ip) {
        AvaliacaoDesempenho a = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        if (a.getStatus() != StatusAvaliacao.ABERTA) throw new IllegalStateException("Somente avaliações ABERTAS podem ser submetidas");
        a.setNota(nota);
        a.setFeedback(feedback);
        a.setAvaliador(usuario);
        a.setStatus(StatusAvaliacao.SUBMETIDA);
        a.setDataSubmissao(LocalDateTime.now());
        AvaliacaoDesempenho saved = repository.save(a);
        auditoriaRhLogService.registrar("AVALIACOES", "SUBMETER", "/api/rh/avaliacao/" + id + "/submeter", usuario != null ? usuario.getEmail() : null, ip, "Submissão avaliacao id=" + id, true);
        return saved;
    }

    @Transactional
    public AvaliacaoDesempenho aprovar(Long id, Usuario aprovador, String observacoes, String ip) {
        AvaliacaoDesempenho a = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        if (a.getStatus() != StatusAvaliacao.SUBMETIDA) throw new IllegalStateException("Somente avaliações SUBMETIDAS podem ser aprovadas");
        a.setStatus(StatusAvaliacao.APROVADA);
        a.setDataDecisao(LocalDateTime.now());
        AvaliacaoDesempenho saved = repository.save(a);
        auditoriaRhLogService.registrar("AVALIACOES", "APROVAR", "/api/rh/avaliacao/" + id + "/aprovar", aprovador != null ? aprovador.getEmail() : null, ip, "Aprovação avaliacao id=" + id, true);
        return saved;
    }

    @Transactional
    public AvaliacaoDesempenho reprovar(Long id, Usuario aprovador, String observacoes, String ip) {
        AvaliacaoDesempenho a = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));
        if (a.getStatus() != StatusAvaliacao.SUBMETIDA) throw new IllegalStateException("Somente avaliações SUBMETIDAS podem ser reprovadas");
        a.setStatus(StatusAvaliacao.REPROVADA);
        a.setDataDecisao(LocalDateTime.now());
        AvaliacaoDesempenho saved = repository.save(a);
        auditoriaRhLogService.registrar("AVALIACOES", "REPROVAR", "/api/rh/avaliacao/" + id + "/reprovar", aprovador != null ? aprovador.getEmail() : null, ip, "Reprovação avaliacao id=" + id, true);
        return saved;
    }
}

