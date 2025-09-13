package com.jaasielsilva.portalceo.service.rh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.HistoricoProcessoAdesao;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.HistoricoProcessoAdesaoRepository;
import com.jaasielsilva.portalceo.repository.ProcessoAdesaoRepository;

import com.jaasielsilva.portalceo.service.NotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkflowAdesaoService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowAdesaoService.class);

    @Autowired
    private ProcessoAdesaoRepository processoRepository;

    @Autowired
    @Lazy
    private NotificacaoService notificacaoService;

    @Autowired
    private HistoricoProcessoAdesaoRepository historicoRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Salva processo de adesão no banco de dados
     */
    /**
     * Salva processo de adesão no banco de dados
     */
    public ProcessoAdesao salvarProcesso(String sessionId, Map<String, Object> dadosPessoais) {
        try {
            // Verificar se já existe processo para esta sessão
            Optional<ProcessoAdesao> processoExistente = processoRepository.findBySessionId(sessionId);

            ProcessoAdesao processo;
            if (processoExistente.isPresent()) {
                processo = processoExistente.get();
                processo.setDataAtualizacao(LocalDateTime.now());
            } else {
                // Criar novo processo
                String nome = dadosPessoais.get("nome") != null ? dadosPessoais.get("nome").toString() : null;
                String email = dadosPessoais.get("email") != null ? dadosPessoais.get("email").toString() : null;
                String cpf = dadosPessoais.get("cpf") != null ? dadosPessoais.get("cpf").toString() : null;

                // Verificar se já existe processo ativo para este CPF
                if (processoRepository.existeProcessoAtivoPorCpf(cpf)) {
                    throw new WorkflowException("Já existe um processo de adesão ativo para este CPF");
                }

                processo = new ProcessoAdesao(sessionId, nome, email, cpf);

                // Cargo
                if (dadosPessoais.get("cargo") != null) {
                    processo.setCargo(dadosPessoais.get("cargo").toString());
                }

                // Converter data de admissão se presente
                if (dadosPessoais.get("dataAdmissao") != null) {
                    String dataAdmissaoStr = dadosPessoais.get("dataAdmissao").toString();
                    processo.setDataAdmissao(LocalDateTime.parse(dataAdmissaoStr + "T00:00:00"));
                }

                // Salário (se existir e for número)
                if (dadosPessoais.get("salario") != null) {
                    Object salarioObj = dadosPessoais.get("salario");
                    if (salarioObj instanceof Number) {
                        processo.setSalario(BigDecimal.valueOf(((Number) salarioObj).doubleValue()));
                    } else {
                        // caso venha como string
                        processo.setSalario(new BigDecimal(salarioObj.toString()));
                    }
                }
            }

            // Salvar dados pessoais como JSON
            processo.setDadosPessoaisJson(objectMapper.writeValueAsString(dadosPessoais));
            processo.setStatus(ProcessoAdesao.StatusProcesso.EM_ANDAMENTO);

            processo = processoRepository.save(processo);

            // Registrar no histórico
            if (!processoExistente.isPresent()) {
                HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoInicioProcesso(processo);
                historicoRepository.save(historico);
            }

            return processo;

        } catch (JsonProcessingException e) {
            throw new WorkflowException("Erro ao processar dados pessoais: " + e.getMessage());
        }
    }

    /**
     * Atualiza etapa do processo
     */
    public void atualizarEtapa(String sessionId, String novaEtapa) {
        ProcessoAdesao processo = buscarProcessoPorSessionId(sessionId);

        if (!processo.isEditavel()) {
            throw new WorkflowException(
                    "Processo não pode ser editado no status atual: " + processo.getStatus().getDescricao());
        }

        String etapaAnterior = processo.getEtapaAtual();
        processo.atualizarEtapa(novaEtapa);

        processoRepository.save(processo);

        // Registrar mudança de etapa no histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoMudancaEtapa(
                processo, etapaAnterior, novaEtapa);
        historicoRepository.save(historico);
    }

    /**
     * Salva documentos do processo
     */
    public void salvarDocumentos(String sessionId, Map<String, Object> documentos) {
        try {
            ProcessoAdesao processo = buscarProcessoPorSessionId(sessionId);

            if (!processo.isEditavel()) {
                throw new WorkflowException("Processo não pode ser editado no status atual");
            }

            processo.setDocumentosJson(objectMapper.writeValueAsString(documentos));
            processo.setDataAtualizacao(LocalDateTime.now());

            processoRepository.save(processo);

        } catch (JsonProcessingException e) {
            throw new WorkflowException("Erro ao processar documentos: " + e.getMessage());
        }
    }

    /**
     * Salva benefícios do processo
     */
    public void salvarBeneficios(String sessionId, Map<String, Object> beneficios, Double custoTotal) {
        try {
            ProcessoAdesao processo = buscarProcessoPorSessionId(sessionId);

            if (!processo.isEditavel()) {
                throw new WorkflowException("Processo não pode ser editado no status atual");
            }

            processo.setBeneficiosJson(objectMapper.writeValueAsString(beneficios));
            processo.setCustoTotalMensal(custoTotal);
            processo.setDataAtualizacao(LocalDateTime.now());

            processoRepository.save(processo);

        } catch (JsonProcessingException e) {
            throw new WorkflowException("Erro ao processar benefícios: " + e.getMessage());
        }
    }

    /**
     * Finaliza processo e envia para aprovação
     */
    public void finalizarProcesso(String sessionId, String observacoes) {
        ProcessoAdesao processo = buscarProcessoPorSessionId(sessionId);

        if (!processo.isFinalizavel()) {
            throw new WorkflowException("Processo não pode ser finalizado no status atual");
        }

        // Validar se todas as etapas foram concluídas
        validarProcessoCompleto(processo);

        processo.setObservacoes(observacoes);
        processo.finalizar(observacoes);

        processoRepository.save(processo);

        // Registrar finalização no histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoFinalizacao(processo);
        historicoRepository.save(historico);

        // Enviar notificação
        notificacaoService.notificarNovoProcessoAdesao(processo);
    }

    /**
     * Aprova processo de adesão
     */
    public void aprovarProcesso(Long processoId, String aprovadoPor, String observacoes) {
        ProcessoAdesao processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new WorkflowException("Processo não encontrado"));

        if (!processo.isAprovavel()) {
            throw new WorkflowException("Processo não pode ser aprovado no status atual");
        }

        // Chamada correta com dois argumentos
        processo.aprovar(aprovadoPor, observacoes != null ? observacoes : "");

        processoRepository.save(processo);

        // ATIVAR COLABORADOR APÓS APROVAÇÃO
        ativarColaboradorAprovado(processo);

        // Registrar aprovação no histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoAprovacao(processo, aprovadoPor);
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            historico.setObservacoes(observacoes);
        }
        historicoRepository.save(historico);

        // Enviar notificação
        notificacaoService.notificarProcessoAprovado(processo);
    }

    /**
     * Rejeita processo de adesão
     */
    public void rejeitarProcesso(Long processoId, String motivoRejeicao, String usuarioResponsavel) {
        ProcessoAdesao processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new WorkflowException("Processo não encontrado"));

        if (!processo.isAprovavel()) {
            throw new WorkflowException("Processo não pode ser rejeitado no status atual");
        }

        processo.rejeitar(motivoRejeicao);
        processoRepository.save(processo);

        // Registrar rejeição no histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoRejeicao(
                processo, motivoRejeicao, usuarioResponsavel);
        historicoRepository.save(historico);

        // Enviar notificação
        notificacaoService.notificarProcessoRejeitado(processo);
    }

    /**
     * Cancela processo de adesão
     */
    public void cancelarProcesso(String sessionId, String motivo) {
        ProcessoAdesao processo = buscarProcessoPorSessionId(sessionId);

        if (!processo.isEditavel()) {
            throw new WorkflowException("Processo não pode ser cancelado no status atual");
        }

        // Passar o motivo para o método cancelar
        processo.cancelar(motivo);
        processoRepository.save(processo);

        // Registrar cancelamento no histórico
        HistoricoProcessoAdesao historico = HistoricoProcessoAdesao.criarEventoCancelamento(processo, motivo);
        historicoRepository.save(historico);
    }

    /**
     * Lista processos aguardando aprovação
     */
    public List<ProcessoAdesaoInfo> listarProcessosAguardandoAprovacao() {
        List<ProcessoAdesao> processos = processoRepository.findProcessosAguardandoAprovacao();
        return processos.stream()
                .map(this::converterParaInfo)
                .collect(Collectors.toList());
    }

    /**
     * Lista processos por status com paginação
     */
    public Page<ProcessoAdesaoInfo> listarProcessosPorStatus(ProcessoAdesao.StatusProcesso status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataCriacao").descending());
        return processoRepository.findByStatus(status, pageable)
                .map(this::converterParaInfo);
    }

    /**
     * Busca processos com filtro por status e texto (nome ou CPF)
     */
    public Page<ProcessoAdesaoInfo> buscarProcessosComFiltro(ProcessoAdesao.StatusProcesso status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataCriacao").descending());
        return processoRepository.buscarPorStatusETexto(status, search, pageable)
                .map(this::converterParaInfo);
    }

    /**
     * Busca processo por ID
     */
    public ProcessoAdesaoInfo buscarProcessoPorId(Long id) {
        ProcessoAdesao processo = processoRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Processo não encontrado"));
        return converterParaInfoCompleta(processo);
    }

    /**
     * Busca histórico de um processo
     */
    public List<HistoricoEventoInfo> buscarHistoricoProcesso(Long processoId) {
        List<HistoricoProcessoAdesao> historico = historicoRepository.findByProcessoAdesaoId(processoId);
        return historico.stream()
                .map(this::converterHistoricoParaInfo)
                .collect(Collectors.toList());
    }

    /**
     * Estatísticas do dashboard
     */
    public DashboardEstatisticas obterEstatisticas() {
        DashboardEstatisticas stats = new DashboardEstatisticas();

        // Contar processos por status
        List<Object[]> contadores = processoRepository.contarProcessosPorStatus();
        Map<String, Long> statusCount = new HashMap<>();

        for (Object[] row : contadores) {
            ProcessoAdesao.StatusProcesso status = (ProcessoAdesao.StatusProcesso) row[0];
            Long count = (Long) row[1];
            statusCount.put(status.name(), count);
        }

        stats.setProcessosPorStatus(statusCount);
        stats.setProcessosAguardandoAprovacao(statusCount.getOrDefault("AGUARDANDO_APROVACAO", 0L));
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fimHoje = inicioHoje.plusDays(1).minusSeconds(1);
        stats.setProcessosHoje(processoRepository.findProcessosHoje(inicioHoje, fimHoje).size());

        return stats;
    }

    /**
     * Compatibilidade para dashboard
     */
    public DashboardEstatisticas obterEstatisticasDashboard() {
        return obterEstatisticas(); // reutiliza seu método existente
    }

    // Métodos auxiliares

    public ProcessoAdesao buscarProcessoPorSessionId(String sessionId) {
        return processoRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new WorkflowException("Processo não encontrado para esta sessão"));
    }

    private void validarProcessoCompleto(ProcessoAdesao processo) {
        if (processo.getDadosPessoaisJson() == null || processo.getDadosPessoaisJson().trim().isEmpty()) {
            throw new WorkflowException("Dados pessoais não foram preenchidos");
        }

        if (processo.getDocumentosJson() == null || processo.getDocumentosJson().trim().isEmpty()) {
            throw new WorkflowException("Documentos não foram enviados");
        }

        // Benefícios são opcionais, mas se houver, deve ter custo calculado
        if (processo.getBeneficiosJson() != null && !processo.getBeneficiosJson().trim().isEmpty()) {
            if (processo.getCustoTotalMensal() == null) {
                throw new WorkflowException("Custo total dos benefícios não foi calculado");
            }
        }
    }

    private ProcessoAdesaoInfo converterParaInfo(ProcessoAdesao processo) {
        ProcessoAdesaoInfo info = new ProcessoAdesaoInfo();
        info.setId(processo.getId());
        info.setNomeColaborador(processo.getNomeColaborador());
        info.setEmailColaborador(processo.getEmailColaborador());
        info.setCpfColaborador(processo.getCpfColaborador());
        info.setCargo(processo.getCargo());
        info.setStatus(processo.getStatus());
        info.setEtapaAtual(processo.getEtapaAtual());
        info.setDataCriacao(processo.getDataCriacao());
        info.setDataFinalizacao(processo.getDataFinalizacao());
        info.setCustoTotalMensal(processo.getCustoTotalMensal());
        return info;
    }

    private ProcessoAdesaoInfo converterParaInfoCompleta(ProcessoAdesao processo) {
        ProcessoAdesaoInfo info = converterParaInfo(processo);
        info.setDataAtualizacao(processo.getDataAtualizacao());
        info.setAprovadoPor(processo.getAprovadoPor());
        info.setDataAprovacao(processo.getDataAprovacao());
        info.setMotivoRejeicao(processo.getMotivoRejeicao());
        info.setObservacoes(processo.getObservacoes());

        // Converter JSONs para Maps
        try {
            if (processo.getDadosPessoaisJson() != null) {
                info.setDadosPessoais(objectMapper.readValue(processo.getDadosPessoaisJson(), Map.class));
            }
            if (processo.getDocumentosJson() != null) {
                info.setDocumentos(objectMapper.readValue(processo.getDocumentosJson(), Map.class));
            }
            if (processo.getBeneficiosJson() != null) {
                info.setBeneficios(objectMapper.readValue(processo.getBeneficiosJson(), Map.class));
            }
        } catch (JsonProcessingException e) {
            // Log error but don't fail
            System.err.println("Erro ao converter JSON: " + e.getMessage());
        }

        return info;
    }

    private HistoricoEventoInfo converterHistoricoParaInfo(HistoricoProcessoAdesao historico) {
        HistoricoEventoInfo info = new HistoricoEventoInfo();
        info.setId(historico.getId());
        info.setTipoEvento(historico.getTipoEvento());
        info.setDescricao(historico.getDescricao());
        info.setEtapaAnterior(historico.getEtapaAnterior());
        info.setEtapaAtual(historico.getEtapaAtual());
        info.setStatusAnterior(historico.getStatusAnterior());
        info.setStatusAtual(historico.getStatusAtual());
        info.setUsuarioResponsavel(historico.getUsuarioResponsavel());
        info.setObservacoes(historico.getObservacoes());
        info.setDataEvento(historico.getDataEvento());
        return info;
    }

    // Classes internas para DTOs

    public static class ProcessoAdesaoInfo {
        private Long id;
        private String nomeColaborador;
        private String emailColaborador;
        private String cpfColaborador;
        private String cargo;
        private ProcessoAdesao.StatusProcesso status;
        private String etapaAtual;
        private LocalDateTime dataCriacao;
        private LocalDateTime dataAtualizacao;
        private LocalDateTime dataFinalizacao;
        private String aprovadoPor;
        private LocalDateTime dataAprovacao;
        private String motivoRejeicao;
        private Double custoTotalMensal;
        private String observacoes;
        private Map<String, Object> dadosPessoais;
        private Map<String, Object> documentos;
        private Map<String, Object> beneficios;

        // Getters e Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNomeColaborador() {
            return nomeColaborador;
        }

        public void setNomeColaborador(String nomeColaborador) {
            this.nomeColaborador = nomeColaborador;
        }

        public String getEmailColaborador() {
            return emailColaborador;
        }

        public void setEmailColaborador(String emailColaborador) {
            this.emailColaborador = emailColaborador;
        }

        public String getCpfColaborador() {
            return cpfColaborador;
        }

        public void setCpfColaborador(String cpfColaborador) {
            this.cpfColaborador = cpfColaborador;
        }

        public String getCargo() {
            return cargo;
        }

        public void setCargo(String cargo) {
            this.cargo = cargo;
        }

        public ProcessoAdesao.StatusProcesso getStatus() {
            return status;
        }

        public void setStatus(ProcessoAdesao.StatusProcesso status) {
            this.status = status;
        }

        public String getEtapaAtual() {
            return etapaAtual;
        }

        public void setEtapaAtual(String etapaAtual) {
            this.etapaAtual = etapaAtual;
        }

        public LocalDateTime getDataCriacao() {
            return dataCriacao;
        }

        public void setDataCriacao(LocalDateTime dataCriacao) {
            this.dataCriacao = dataCriacao;
        }

        public LocalDateTime getDataAtualizacao() {
            return dataAtualizacao;
        }

        public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
            this.dataAtualizacao = dataAtualizacao;
        }

        public LocalDateTime getDataFinalizacao() {
            return dataFinalizacao;
        }

        public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
            this.dataFinalizacao = dataFinalizacao;
        }

        public String getAprovadoPor() {
            return aprovadoPor;
        }

        public void setAprovadoPor(String aprovadoPor) {
            this.aprovadoPor = aprovadoPor;
        }

        public LocalDateTime getDataAprovacao() {
            return dataAprovacao;
        }

        public void setDataAprovacao(LocalDateTime dataAprovacao) {
            this.dataAprovacao = dataAprovacao;
        }

        public String getMotivoRejeicao() {
            return motivoRejeicao;
        }

        public void setMotivoRejeicao(String motivoRejeicao) {
            this.motivoRejeicao = motivoRejeicao;
        }

        public Double getCustoTotalMensal() {
            return custoTotalMensal;
        }

        public void setCustoTotalMensal(Double custoTotalMensal) {
            this.custoTotalMensal = custoTotalMensal;
        }

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        public Map<String, Object> getDadosPessoais() {
            return dadosPessoais;
        }

        public void setDadosPessoais(Map<String, Object> dadosPessoais) {
            this.dadosPessoais = dadosPessoais;
        }

        public Map<String, Object> getDocumentos() {
            return documentos;
        }

        public void setDocumentos(Map<String, Object> documentos) {
            this.documentos = documentos;
        }

        public Map<String, Object> getBeneficios() {
            return beneficios;
        }

        public void setBeneficios(Map<String, Object> beneficios) {
            this.beneficios = beneficios;
        }

        public String getDataCriacaoFormatada() {
            return dataCriacao != null ? dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        }

        public String getDataFinalizacaoFormatada() {
            return dataFinalizacao != null ? dataFinalizacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "";
        }

        public String getCustoTotalFormatado() {
            return custoTotalMensal != null ? String.format("R$ %.2f", custoTotalMensal).replace(".", ",") : "R$ 0,00";
        }
    }

    public static class HistoricoEventoInfo {
        private Long id;
        private HistoricoProcessoAdesao.TipoEvento tipoEvento;
        private String descricao;
        private String etapaAnterior;
        private String etapaAtual;
        private ProcessoAdesao.StatusProcesso statusAnterior;
        private ProcessoAdesao.StatusProcesso statusAtual;
        private String usuarioResponsavel;
        private String observacoes;
        private LocalDateTime dataEvento;

        // Getters e Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public HistoricoProcessoAdesao.TipoEvento getTipoEvento() {
            return tipoEvento;
        }

        public void setTipoEvento(HistoricoProcessoAdesao.TipoEvento tipoEvento) {
            this.tipoEvento = tipoEvento;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public String getEtapaAnterior() {
            return etapaAnterior;
        }

        public void setEtapaAnterior(String etapaAnterior) {
            this.etapaAnterior = etapaAnterior;
        }

        public String getEtapaAtual() {
            return etapaAtual;
        }

        public void setEtapaAtual(String etapaAtual) {
            this.etapaAtual = etapaAtual;
        }

        public ProcessoAdesao.StatusProcesso getStatusAnterior() {
            return statusAnterior;
        }

        public void setStatusAnterior(ProcessoAdesao.StatusProcesso statusAnterior) {
            this.statusAnterior = statusAnterior;
        }

        public ProcessoAdesao.StatusProcesso getStatusAtual() {
            return statusAtual;
        }

        public void setStatusAtual(ProcessoAdesao.StatusProcesso statusAtual) {
            this.statusAtual = statusAtual;
        }

        public String getUsuarioResponsavel() {
            return usuarioResponsavel;
        }

        public void setUsuarioResponsavel(String usuarioResponsavel) {
            this.usuarioResponsavel = usuarioResponsavel;
        }

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        public LocalDateTime getDataEvento() {
            return dataEvento;
        }

        public void setDataEvento(LocalDateTime dataEvento) {
            this.dataEvento = dataEvento;
        }

        public String getDataEventoFormatada() {
            return dataEvento != null ? dataEvento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";
        }
    }

    public static class DashboardEstatisticas {
        private Map<String, Long> processosPorStatus;
        private Long processosAguardandoAprovacao;
        private Integer processosHoje;

        // Getters e Setters
        public Map<String, Long> getProcessosPorStatus() {
            return processosPorStatus;
        }

        public void setProcessosPorStatus(Map<String, Long> processosPorStatus) {
            this.processosPorStatus = processosPorStatus;
        }

        public Long getProcessosAguardandoAprovacao() {
            return processosAguardandoAprovacao;
        }

        public void setProcessosAguardandoAprovacao(Long processosAguardandoAprovacao) {
            this.processosAguardandoAprovacao = processosAguardandoAprovacao;
        }

        public Integer getProcessosHoje() {
            return processosHoje;
        }

        public void setProcessosHoje(Integer processosHoje) {
            this.processosHoje = processosHoje;
        }
    }

    /**
     * Ativa colaborador após aprovação do processo
     */
    private void ativarColaboradorAprovado(ProcessoAdesao processo) {
        try {
            // Buscar colaborador pelo CPF do processo
            Optional<Colaborador> colaboradorOpt = colaboradorRepository.findByCpf(processo.getCpfColaborador());
            
            if (colaboradorOpt.isPresent()) {
                Colaborador colaborador = colaboradorOpt.get();
                
                // Ativar colaborador
                colaborador.setStatus(Colaborador.StatusColaborador.ATIVO);
                colaborador.setAtivo(true);
                
                // Salvar alterações
                colaboradorRepository.save(colaborador);
                
                logger.info("Colaborador ativado após aprovação do processo - CPF: {}, Nome: {}", 
                           processo.getCpfColaborador(), processo.getNomeColaborador());
            } else {
                logger.warn("Colaborador não encontrado para ativação - CPF: {}, Processo ID: {}", 
                           processo.getCpfColaborador(), processo.getId());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao ativar colaborador após aprovação - Processo ID: {}, CPF: {}", 
                        processo.getId(), processo.getCpfColaborador(), e);
            // Não propagar erro para não interromper o fluxo de aprovação
        }
    }

    public static class WorkflowException extends RuntimeException {
        public WorkflowException(String message) {
            super(message);
        }

        public WorkflowException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}