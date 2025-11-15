package com.jaasielsilva.portalceo.service.juridico;

import com.jaasielsilva.portalceo.model.juridico.Audiencia;
import com.jaasielsilva.portalceo.model.juridico.AndamentoProcesso;
import com.jaasielsilva.portalceo.model.juridico.PrazoJuridico;
import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import com.jaasielsilva.portalceo.repository.juridico.AudienciaRepository;
import com.jaasielsilva.portalceo.repository.juridico.AndamentoProcessoRepository;
import com.jaasielsilva.portalceo.repository.juridico.PrazoJuridicoRepository;
import com.jaasielsilva.portalceo.repository.juridico.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcessoJuridicoService {

    private final ProcessoJuridicoRepository processoRepo;
    private final AudienciaRepository audienciaRepo;
    private final PrazoJuridicoRepository prazoRepo;
    private final AndamentoProcessoRepository andamentoRepo;

    public ProcessoJuridicoService(ProcessoJuridicoRepository processoRepo,
                                   AudienciaRepository audienciaRepo,
                                   PrazoJuridicoRepository prazoRepo,
                                   AndamentoProcessoRepository andamentoRepo) {
        this.processoRepo = processoRepo;
        this.audienciaRepo = audienciaRepo;
        this.prazoRepo = prazoRepo;
        this.andamentoRepo = andamentoRepo;
    }

    @Transactional(readOnly = true)
    public int contarProcessosEmAndamento() {
        return (int) processoRepo.countByStatus(ProcessoJuridico.StatusProcesso.EM_ANDAMENTO);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obterProcessosUrgentes(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        return prazoRepo.findByCumpridoFalseAndDataLimiteBetween(hoje, limite)
                .stream()
                .map(p -> {
                    Optional<ProcessoJuridico> procOpt = processoRepo.findById(p.getProcessoId());
                    if (procOpt.isEmpty()) return null;
                    ProcessoJuridico proc = procOpt.get();
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", proc.getId());
                    m.put("numero", proc.getNumero());
                    m.put("parte", proc.getParte());
                    m.put("proximoPrazo", p.getDataLimite());
                    m.put("acao", p.getDescricao());
                    return m;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obterProximasAudiencias(int dias) {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = inicio.plusDays(dias);
        return audienciaRepo.findByDataHoraBetween(inicio, fim)
                .stream()
                .map(a -> {
                    Optional<ProcessoJuridico> procOpt = processoRepo.findById(a.getProcessoId());
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("processoId", a.getProcessoId());
                    m.put("dataHora", a.getDataHora());
                    m.put("tipo", a.getTipo());
                    m.put("observacoes", a.getObservacoes());
                    m.put("processoNumero", procOpt.map(ProcessoJuridico::getNumero).orElse(null));
                    m.put("parte", procOpt.map(ProcessoJuridico::getParte).orElse(null));
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obterPrazosCriticos(int dias) {
        return obterProcessosUrgentes(dias);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obterUltimasAtividades(int limit) {
        List<AndamentoProcesso> ultimos = andamentoRepo.findTop10ByOrderByDataHoraDesc();
        return ultimos.stream().limit(limit)
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("tipo", "Processo");
                    m.put("descricao", a.getDescricao());
                    m.put("data", a.getDataHora());
                    m.put("usuario", a.getUsuario());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorStatus(ProcessoJuridico.StatusProcesso status) {
        return processoRepo.findByStatus(status).stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("numero", p.getNumero());
                    m.put("tipo", p.getTipo());
                    m.put("tribunal", p.getTribunal());
                    m.put("parte", p.getParte());
                    m.put("assunto", p.getAssunto());
                    m.put("status", p.getStatus());
                    m.put("dataAbertura", p.getDataAbertura());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PrazoJuridico concluirPrazo(Long prazoId) {
        PrazoJuridico p = prazoRepo.findById(prazoId).orElseThrow(() -> new IllegalArgumentException("Prazo não encontrado"));
        p.setCumprido(true);
        return prazoRepo.save(p);
    }

    @Transactional(readOnly = true)
    public List<Audiencia> listarAudienciasDoProcesso(Long processoId) {
        return audienciaRepo.findByProcessoIdOrderByDataHoraAsc(processoId);
    }

    @Transactional(readOnly = true)
    public List<PrazoJuridico> listarPrazosDoProcesso(Long processoId) {
        return prazoRepo.findByProcessoIdOrderByDataLimiteAsc(processoId);
    }

    @Transactional
    public ProcessoJuridico atualizarStatus(Long id, ProcessoJuridico.StatusProcesso status) {
        ProcessoJuridico p = processoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
        p.setStatus(status);
        return processoRepo.save(p);
    }
}