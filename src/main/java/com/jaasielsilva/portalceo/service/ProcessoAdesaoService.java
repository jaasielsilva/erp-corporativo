package com.jaasielsilva.portalceo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.HistoricoProcessoAdesao;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.HistoricoProcessoAdesaoRepository;
import com.jaasielsilva.portalceo.repository.ProcessoAdesaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ProcessoAdesaoService {

    @Autowired
    private ProcessoAdesaoRepository processoRepository;

    @Autowired
    private HistoricoProcessoAdesaoRepository historicoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    private String formatCpf(String cpf) {
        if (cpf == null) return null;
        String trimmed = cpf.trim();
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.length() != 11) return trimmed;
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9, 11);
    }

    /**
     * Cria um novo processo de adesão
     */
    public ProcessoAdesao criarProcesso(String sessionId, Map<String, Object> dadosPessoais) {
        // Verifica se já existe processo para esta sessão
        Optional<ProcessoAdesao> processoExistente = processoRepository.findBySessionId(sessionId);
        if (processoExistente.isPresent()) {
            return processoExistente.get();
        }

        // Extrai dados pessoais
        String nomeColaborador = (String) dadosPessoais.get("nomeCompleto");
        String email = (String) dadosPessoais.get("email");
        String cpfRaw = (String) dadosPessoais.get("cpf");
        String cpf = formatCpf(cpfRaw);
        String cargo = (String) dadosPessoais.get("cargo");
        String dataAdmissaoStr = (String) dadosPessoais.get("dataAdmissao");

        // Verifica se já existe processo ativo para este CPF
        boolean existeAtivo = processoRepository.existeProcessoAtivoPorCpf(cpf);
        if (!existeAtivo && cpfRaw != null && cpf != null && !cpfRaw.equals(cpf)) {
            existeAtivo = processoRepository.existeProcessoAtivoPorCpf(cpfRaw);
        }
        if (existeAtivo) {
            throw new IllegalStateException("Já existe um processo de adesão ativo para este CPF");
        }

        // Cria novo processo
        ProcessoAdesao processo = new ProcessoAdesao();
        processo.setSessionId(sessionId);
        processo.setNomeColaborador(nomeColaborador); // correto
        processo.setEmailColaborador(email);
        processo.setCpfColaborador(cpf);
        processo.setCargo(cargo);

        // Converter dataAdmissao string -> LocalDateTime
        if (dataAdmissaoStr != null && !dataAdmissaoStr.isEmpty()) {
            LocalDateTime dataAdmissao = LocalDateTime.parse(dataAdmissaoStr);
            processo.setDataAdmissao(dataAdmissao);
        }

        processo.setStatus(ProcessoAdesao.StatusProcesso.INICIADO);
        processo.setEtapaAtual("dados-pessoais");

        try {
            processo.setDadosPessoaisJson(objectMapper.writeValueAsString(dadosPessoais));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar dados pessoais", e);
        }

        processo = processoRepository.save(processo);

        // Registra histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoInicioProcesso(processo);
        historicoRepository.save(historico);

        return processo;
    }

    /**
     * Atualiza etapa do processo
     */
    public ProcessoAdesao atualizarEtapa(String sessionId, String novaEtapa) {
        ProcessoAdesao processo = buscarPorSessionId(sessionId);
        String etapaAnterior = processo.getEtapaAtual();

        processo.atualizarEtapa(novaEtapa);
        processo = processoRepository.save(processo);

        // Registra histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoMudancaEtapa(
                processo, etapaAnterior, novaEtapa);
        historicoRepository.save(historico);

        return processo;
    }

    /**
     * Atualiza dados de documentos
     */
    public ProcessoAdesao atualizarDocumentos(String sessionId, Map<String, Object> dadosDocumentos) {
        ProcessoAdesao processo = buscarPorSessionId(sessionId);

        try {
            processo.setDocumentosJson(objectMapper.writeValueAsString(dadosDocumentos));
            processo.setDataAtualizacao(LocalDateTime.now());

            if (processo.getStatus() == ProcessoAdesao.StatusProcesso.INICIADO) {
                processo.setStatus(ProcessoAdesao.StatusProcesso.EM_ANDAMENTO);
            }

            return processoRepository.save(processo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar dados de documentos", e);
        }
    }

    /**
     * Atualiza dados de benefícios
     */
    public ProcessoAdesao atualizarBeneficios(String sessionId, Map<String, Object> dadosBeneficios,
            Double custoTotal) {
        ProcessoAdesao processo = buscarPorSessionId(sessionId);

        try {
            processo.setBeneficiosJson(objectMapper.writeValueAsString(dadosBeneficios));
            processo.setCustoTotalMensal(custoTotal);
            processo.setDataAtualizacao(LocalDateTime.now());

            return processoRepository.save(processo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar dados de benefícios", e);
        }
    }

    /**
     * Finaliza processo e envia para aprovação
     */
    public ProcessoAdesao finalizarProcesso(String sessionId, String observacoes) {
        ProcessoAdesao processo = buscarPorSessionId(sessionId);

        if (!processo.isFinalizavel()) {
            throw new IllegalStateException("Processo não pode ser finalizado no status atual");
        }

        processo.finalizar(observacoes);
        processo = processoRepository.save(processo);

        // Registra histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoFinalizacao(processo);
        historicoRepository.save(historico);

        return processo;
    }

    /**
     * Aprova processo
     */
    public ProcessoAdesao aprovarProcesso(Long processoId, String aprovadoPor, String observacoes) {
        ProcessoAdesao processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        if (!processo.isAprovavel()) {
            throw new IllegalStateException("Processo não pode ser aprovado no status atual");
        }

        processo.aprovar(aprovadoPor, observacoes);
        processo = processoRepository.save(processo);

        String cpfProcesso = processo.getCpfColaborador();
        Optional<Colaborador> colaboradorOpt = colaboradorRepository.findByCpf(cpfProcesso);
        if (colaboradorOpt.isEmpty()) {
            String cpfFmt = formatCpf(cpfProcesso);
            if (cpfFmt != null && cpfProcesso != null && !cpfFmt.equals(cpfProcesso)) {
                colaboradorOpt = colaboradorRepository.findByCpf(cpfFmt);
            }
        }
        if (colaboradorOpt.isPresent()) {
            Colaborador colaborador = colaboradorOpt.get();
            colaborador.setStatus(Colaborador.StatusColaborador.ATIVO);
            colaborador.setAtivo(true);
            colaboradorRepository.save(colaborador);
        }

        // Registra histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoAprovacao(processo, aprovadoPor);
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            historico.setObservacoes(observacoes);
        }
        historicoRepository.save(historico);

        return processo;
    }

    /**
     * Rejeita processo
     */
    public ProcessoAdesao rejeitarProcesso(Long processoId, String rejeitadoPor, String motivoRejeicao) {
        ProcessoAdesao processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        if (!processo.isAprovavel()) {
            throw new IllegalStateException("Processo não pode ser rejeitado no status atual");
        }

        processo.rejeitar(motivoRejeicao);
        processo = processoRepository.save(processo);

        // Registra histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoRejeicao(
                processo, motivoRejeicao, rejeitadoPor);
        historicoRepository.save(historico);

        return processo;
    }

    /**
     * Cancela processo
     */
    public ProcessoAdesao cancelarProcesso(String sessionId, String motivo) {
        ProcessoAdesao processo = buscarPorSessionId(sessionId);

        processo.cancelar(motivo);
        processo = processoRepository.save(processo);

        // Registra histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoCancelamento(processo, motivo);
        historicoRepository.save(historico);

        return processo;
    }

    /**
     * Busca processo por sessionId
     */
    public ProcessoAdesao buscarPorSessionId(String sessionId) {
        return processoRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado para esta sessão"));
    }

    /**
     * Busca processo por ID
     */
    public ProcessoAdesao buscarPorId(Long id) {
        return processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
    }

    /**
     * Lista processos aguardando aprovação
     */
    public List<ProcessoAdesao> listarProcessosAguardandoAprovacao() {
        return processoRepository.findProcessosAguardandoAprovacao();
    }

    /**
     * Lista processos por status
     */
    public List<ProcessoAdesao> listarProcessosPorStatus(ProcessoAdesao.StatusProcesso status) {
        return processoRepository.findByStatusOrderByDataCriacaoDesc(status);
    }

    /**
     * Busca histórico de um processo
     */
    public List<HistoricoProcessoAdesao> buscarHistoricoProcesso(Long processoId) {
        return historicoRepository.findByProcessoAdesaoIdOrderByDataEventoDesc(processoId);
    }

    /**
     * Busca histórico por sessionId
     */
    public List<HistoricoProcessoAdesao> buscarHistoricoPorSessionId(String sessionId) {
        return historicoRepository.findBySessionId(sessionId);
    }

    /**
     * Obtém estatísticas de processos
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> estatisticas = new HashMap<>();

        // Conta por status
        for (ProcessoAdesao.StatusProcesso status : ProcessoAdesao.StatusProcesso.values()) {
            long count = processoRepository.countByStatus(status);
            estatisticas.put(status.name().toLowerCase(), count);
        }

        // Processos hoje
        LocalDateTime inicioHoje = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimHoje = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<ProcessoAdesao> processosHoje = processoRepository.findByPeriodoCriacao(inicioHoje, fimHoje);
        estatisticas.put("processosHoje", processosHoje.size());

        // Últimos processos
        List<ProcessoAdesao> ultimosProcessos = processoRepository.findUltimosProcessos(PageRequest.of(0, 5));
        estatisticas.put("ultimosProcessos", ultimosProcessos);

        return estatisticas;
    }

    /**
     * Obtém dados completos do processo para revisão
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> obterDadosRevisao(String sessionId) {
        ProcessoAdesao processo = buscarPorSessionId(sessionId);
        Map<String, Object> dadosRevisao = new HashMap<>();

        try {
            // Dados pessoais
            if (processo.getDadosPessoaisJson() != null) {
                Map<String, Object> dadosPessoais = objectMapper.readValue(
                        processo.getDadosPessoaisJson(), Map.class);
                dadosRevisao.put("dadosPessoais", dadosPessoais);
            }

            // Dados de documentos
            if (processo.getDocumentosJson() != null) {
                Map<String, Object> dadosDocumentos = objectMapper.readValue(
                        processo.getDocumentosJson(), Map.class);
                dadosRevisao.put("dadosDocumentos", dadosDocumentos);
            }

            // Dados de benefícios
            if (processo.getBeneficiosJson() != null) {
                Map<String, Object> dadosBeneficios = objectMapper.readValue(
                        processo.getBeneficiosJson(), Map.class);
                dadosRevisao.put("dadosBeneficios", dadosBeneficios);
            }

            // Informações do processo
            dadosRevisao.put("processo", Map.of(
                    "id", processo.getId(),
                    "sessionId", processo.getSessionId(),
                    "status", processo.getStatus(),
                    "etapaAtual", processo.getEtapaAtual(),
                    "custoTotalMensal", processo.getCustoTotalMensal(),
                    "dataCriacao", processo.getDataCriacao(),
                    "dataAtualizacao", processo.getDataAtualizacao()));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao deserializar dados do processo", e);
        }

        return dadosRevisao;
    }

    /**
     * Limpa processos expirados (mais de 7 dias sem atualização)
     */
    @Transactional
    public void limparProcessosExpirados() {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(7);
        List<ProcessoAdesao> processosExpirados = processoRepository.findProcessosExpirados(dataLimite);

        for (ProcessoAdesao processo : processosExpirados) {
            cancelarProcesso(processo.getSessionId(), "Processo expirado por inatividade");
        }
    }
}
