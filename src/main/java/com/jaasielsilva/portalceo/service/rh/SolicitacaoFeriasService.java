package com.jaasielsilva.portalceo.service.rh;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.SolicitacaoFerias;
import com.jaasielsilva.portalceo.model.SolicitacaoFerias.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.RhPoliticaFeriasRepository;
import com.jaasielsilva.portalceo.model.RhPoliticaFerias;
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
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitacaoFeriasService {

    @Autowired
    private SolicitacaoFeriasRepository repository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private AuditoriaRhLogService auditoriaRhLogService;

    @Autowired
    private RhPoliticaFeriasRepository feriasRepository;

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

        RhPoliticaFerias politica = feriasRepository.findAll().stream().findFirst().orElse(null);
        if (politica != null) {
            long diasSolicitados = java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) + 1;
            Integer limite = politica.getDiasPorAno();
            if (limite != null && limite > 0 && diasSolicitados > limite) {
                throw new IllegalArgumentException("Quantidade de dias solicitados excede o limite anual configurado");
            }

            String blackout = politica.getPeriodosBlackout();
            if (blackout != null && !blackout.isBlank()) {
                List<MonthDay[]> periodos = parsePeriodosBlackout(blackout);
                if (existeIntersecaoComBlackout(inicio, fim, periodos)) {
                    throw new IllegalArgumentException("Período solicitado coincide com período de blackout definido nas políticas");
                }
            }
        }

        SolicitacaoFerias s = new SolicitacaoFerias();
        s.setColaborador(colaborador);
        s.setPeriodoInicio(inicio);
        s.setPeriodoFim(fim);
        s.setObservacoes(observacoes);
        if (politica != null && Boolean.FALSE.equals(politica.getExigeAprovacaoGerente())) {
            s.setStatus(StatusSolicitacao.APROVADA);
        } else {
            s.setStatus(StatusSolicitacao.SOLICITADA);
        }
        s.setDataSolicitacao(LocalDateTime.now());
        s.setUsuarioCriacao(usuarioLogado);

        SolicitacaoFerias saved = repository.save(s);

        String detalhes = "Solicitação de férias criada: colaborador=" + colaborador.getId() +
                " periodo=" + inicio + " a " + fim;
        auditoriaRhLogService.registrar("FERIAS", "SOLICITAR", "/api/rh/ferias/solicitacoes", 
                usuarioLogado != null ? usuarioLogado.getEmail() : null, ip, detalhes, true);

        return saved;
    }

    private List<MonthDay[]> parsePeriodosBlackout(String value) {
        List<MonthDay[]> lista = new ArrayList<>();
        String[] tokens = value.split(";\s*");
        for (int i = 0; i + 1 < tokens.length; i += 2) {
            MonthDay inicio = parseMonthDay(tokens[i]);
            MonthDay fim = parseMonthDay(tokens[i + 1]);
            if (inicio != null && fim != null) {
                lista.add(new MonthDay[] { inicio, fim });
            }
        }
        return lista;
    }

    private MonthDay parseMonthDay(String s) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
            java.time.LocalDate d = java.time.LocalDate.parse(s, fmt);
            return MonthDay.of(d.getMonthValue(), d.getDayOfMonth());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean existeIntersecaoComBlackout(LocalDate inicio, LocalDate fim, List<MonthDay[]> periodos) {
        if (periodos == null || periodos.isEmpty()) return false;
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fim)) {
            MonthDay md = MonthDay.of(cursor.getMonthValue(), cursor.getDayOfMonth());
            for (MonthDay[] p : periodos) {
                MonthDay pInicio = p[0];
                MonthDay pFim = p[1];
                boolean wrap = pFim.isBefore(pInicio);
                boolean dentro;
                if (!wrap) {
                    dentro = (md.equals(pInicio) || md.equals(pFim) || (md.isAfter(pInicio) && md.isBefore(pFim)));
                } else {
                    dentro = md.isAfter(pInicio) || md.isBefore(pFim) || md.equals(pInicio) || md.equals(pFim);
                }
                if (dentro) return true;
            }
            cursor = cursor.plusDays(1);
        }
        return false;
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
