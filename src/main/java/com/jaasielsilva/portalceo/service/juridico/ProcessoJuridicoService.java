package com.jaasielsilva.portalceo.service.juridico;

import com.jaasielsilva.portalceo.model.juridico.Audiencia;
import com.jaasielsilva.portalceo.model.juridico.AndamentoProcesso;
import com.jaasielsilva.portalceo.model.juridico.PrazoJuridico;
import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import com.jaasielsilva.portalceo.repository.juridico.AudienciaRepository;
import com.jaasielsilva.portalceo.repository.juridico.AndamentoProcessoRepository;
import com.jaasielsilva.portalceo.repository.juridico.PrazoJuridicoRepository;
import com.jaasielsilva.portalceo.repository.juridico.ProcessoJuridicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional
    public ProcessoJuridico salvar(ProcessoJuridico processo) {
        if (processo.getDataAbertura() == null) {
            processo.setDataAbertura(LocalDate.now());
        }
        if (processo.getStatus() == null) {
            processo.setStatus(ProcessoJuridico.StatusProcesso.EM_ANDAMENTO);
        }
        return processoRepo.save(processo);
    }
    
    @Transactional(readOnly = true)
    public Optional<ProcessoJuridico> buscarPorId(Long id) {
        return processoRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProcessoJuridico> listarPorCliente(Long clienteId, Pageable pageable) {
        return processoRepo.findByClienteId(clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public int contarProcessosEmAndamento() {
        return (int) processoRepo.countByStatus(ProcessoJuridico.StatusProcesso.EM_ANDAMENTO);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obterProcessosUrgentes(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        List<PrazoJuridico> prazos = prazoRepo.findByCumpridoFalseAndDataLimiteBetween(hoje, limite);
        
        Set<Long> processoIds = prazos.stream()
                .filter(p -> p.getProcesso() != null)
                .map(p -> p.getProcesso().getId())
                .collect(Collectors.toSet());
        
        Map<Long, ProcessoJuridico> processosMap = processoRepo.findAllById(processoIds).stream()
            .collect(Collectors.toMap(ProcessoJuridico::getId, p -> p));

        return prazos.stream()
                .map(p -> {
                    if (p.getProcesso() == null) return null;
                    ProcessoJuridico proc = processosMap.get(p.getProcesso().getId());
                    if (proc == null) return null;
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
        List<Audiencia> audiencias = audienciaRepo.findByDataHoraBetween(inicio, fim);

        Set<Long> processoIds = audiencias.stream()
                .filter(a -> a.getProcesso() != null)
                .map(a -> a.getProcesso().getId())
                .collect(Collectors.toSet());
                
        Map<Long, ProcessoJuridico> processosMap = processoRepo.findAllById(processoIds).stream()
            .collect(Collectors.toMap(ProcessoJuridico::getId, p -> p));

        return audiencias.stream()
                .map(a -> {
                    if (a.getProcesso() == null) return null;
                    ProcessoJuridico proc = processosMap.get(a.getProcesso().getId());
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("processoId", a.getProcesso().getId());
                    m.put("dataHora", a.getDataHora());
                    m.put("tipo", a.getTipo());
                    m.put("observacoes", a.getObservacoes());
                    m.put("processoNumero", proc != null ? proc.getNumero() : null);
                    m.put("parte", proc != null ? proc.getParte() : null);
                    return m;
                })
                .filter(Objects::nonNull)
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
        ProcessoJuridico proc = p.getProcesso();
        
        if (proc == null) {
            throw new IllegalArgumentException("Processo não encontrado");
        }
        
        if (proc.getStatus() == ProcessoJuridico.StatusProcesso.ENCERRADO) {
            throw new IllegalStateException("Não é possível modificar prazos de processos encerrados");
        }
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

    @Transactional(readOnly = true)
    public List<AndamentoProcesso> listarAndamentos(Long processoId) {
        return andamentoRepo.findByProcessoIdOrderByDataHoraDesc(processoId);
    }

    @Transactional
    public AndamentoProcesso adicionarAndamento(AndamentoProcesso andamento) {
        if (andamento.getProcesso() == null) {
            throw new IllegalArgumentException("Processo não informado");
        }
        ProcessoJuridico proc = andamento.getProcesso();
        
        if (proc.getStatus() == ProcessoJuridico.StatusProcesso.ENCERRADO) {
            throw new IllegalStateException("Não é possível adicionar eventos a processos encerrados");
        }
        return andamentoRepo.save(andamento);
    }

    @Transactional
    public AndamentoProcesso adicionarAndamento(Long processoId, String titulo, String descricao, String tipoEtapa) {
        ProcessoJuridico proc = processoRepo.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
        if (proc.getStatus() == ProcessoJuridico.StatusProcesso.ENCERRADO) {
            throw new IllegalStateException("Não é possível adicionar eventos a processos encerrados");
        }
        AndamentoProcesso a = new AndamentoProcesso();
        a.setProcesso(proc);
        a.setTitulo(titulo);
        a.setDescricao(descricao);
        try {
            a.setTipoEtapa(AndamentoProcesso.TipoEtapa.valueOf(tipoEtapa));
        } catch (IllegalArgumentException e) {
            a.setTipoEtapa(AndamentoProcesso.TipoEtapa.ANDAMENTO);
        }
        a.setDataHora(LocalDateTime.now());
        a.setUsuario("Sistema"); 
        return andamentoRepo.save(a);
    }

    @Transactional
    public PrazoJuridico adicionarPrazo(Long processoId, String dataLimite, String descricao, String responsabilidade) {
        ProcessoJuridico proc = processoRepo.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
        if (proc.getStatus() == ProcessoJuridico.StatusProcesso.ENCERRADO) {
            throw new IllegalStateException("Não é possível adicionar prazos a processos encerrados");
        }
        PrazoJuridico p = new PrazoJuridico();
        p.setProcesso(proc);
        p.setDescricao(descricao);
        p.setResponsabilidade(responsabilidade);
        try {
            p.setDataLimite(LocalDate.parse(dataLimite));
        } catch (Exception e) {
            p.setDataLimite(LocalDate.now().plusDays(15));
        }
        p.setCumprido(false);
        return prazoRepo.save(p);
    }

    @Transactional
    public Audiencia adicionarAudiencia(Long processoId, String dataHora, String tipo, String observacoes) {
        ProcessoJuridico proc = processoRepo.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
        if (proc.getStatus() == ProcessoJuridico.StatusProcesso.ENCERRADO) {
            throw new IllegalStateException("Não é possível agendar audiências em processos encerrados");
        }
        Audiencia a = new Audiencia();
        a.setProcesso(proc);
        a.setDataHora(LocalDateTime.parse(dataHora));
        a.setTipo(tipo);
        a.setObservacoes(observacoes);
        return audienciaRepo.save(a);
    }

    @Transactional
    public ProcessoJuridico atualizarStatus(Long id, ProcessoJuridico.StatusProcesso status) {
        ProcessoJuridico p = processoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
        p.setStatus(status);
        return processoRepo.save(p);
    }
}
