package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_processo_adesao")
public class HistoricoProcessoAdesao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_adesao_id", nullable = false)
    private ProcessoAdesao processoAdesao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento;
    
    @Column(name = "descricao", nullable = false)
    private String descricao;
    
    @Column(name = "etapa_anterior")
    private String etapaAnterior;
    
    @Column(name = "etapa_atual")
    private String etapaAtual;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior")
    private ProcessoAdesao.StatusProcesso statusAnterior;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_atual")
    private ProcessoAdesao.StatusProcesso statusAtual;
    
    @Column(name = "usuario_responsavel")
    private String usuarioResponsavel;
    
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
    
    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
    
    @Column(name = "dados_adicionais", columnDefinition = "TEXT")
    private String dadosAdicionais;
    
    // Construtores
    public HistoricoProcessoAdesao() {
        this.dataEvento = LocalDateTime.now();
    }
    
    public HistoricoProcessoAdesao(ProcessoAdesao processoAdesao, TipoEvento tipoEvento, String descricao) {
        this();
        this.processoAdesao = processoAdesao;
        this.tipoEvento = tipoEvento;
        this.descricao = descricao;
    }
    
    // Métodos estáticos para criação de eventos específicos
    public static HistoricoProcessoAdesao criarEventoInicioProcesso(ProcessoAdesao processo) {
        HistoricoProcessoAdesao historico = new HistoricoProcessoAdesao(
            processo, 
            TipoEvento.INICIO_PROCESSO, 
            "Processo de adesão iniciado"
        );
        historico.setStatusAtual(processo.getStatus());
        historico.setEtapaAtual(processo.getEtapaAtual());
        return historico;
    }
    
    public static HistoricoProcessoAdesao criarEventoMudancaEtapa(
            ProcessoAdesao processo, 
            String etapaAnterior, 
            String etapaAtual) {
        HistoricoProcessoAdesao historico = new HistoricoProcessoAdesao(
            processo, 
            TipoEvento.MUDANCA_ETAPA, 
            String.format("Etapa alterada de '%s' para '%s'", etapaAnterior, etapaAtual)
        );
        historico.setEtapaAnterior(etapaAnterior);
        historico.setEtapaAtual(etapaAtual);
        historico.setStatusAtual(processo.getStatus());
        return historico;
    }
    
    public static HistoricoProcessoAdesao criarEventoMudancaStatus(
            ProcessoAdesao processo, 
            ProcessoAdesao.StatusProcesso statusAnterior, 
            ProcessoAdesao.StatusProcesso statusAtual,
            String usuarioResponsavel) {
        HistoricoProcessoAdesao historico = new HistoricoProcessoAdesao(
            processo, 
            TipoEvento.MUDANCA_STATUS, 
            String.format("Status alterado de '%s' para '%s'", 
                statusAnterior.getDescricao(), statusAtual.getDescricao())
        );
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusAtual(statusAtual);
        historico.setEtapaAtual(processo.getEtapaAtual());
        historico.setUsuarioResponsavel(usuarioResponsavel);
        return historico;
    }
    
    public static HistoricoProcessoAdesao criarEventoAprovacao(
            ProcessoAdesao processo, 
            String aprovadoPor) {
        HistoricoProcessoAdesao historico = new HistoricoProcessoAdesao(
            processo, 
            TipoEvento.APROVACAO, 
            "Processo de adesão aprovado"
        );
        historico.setStatusAnterior(ProcessoAdesao.StatusProcesso.AGUARDANDO_APROVACAO);
        historico.setStatusAtual(ProcessoAdesao.StatusProcesso.APROVADO);
        historico.setEtapaAtual(processo.getEtapaAtual());
        historico.setUsuarioResponsavel(aprovadoPor);
        return historico;
    }
    
    public static HistoricoProcessoAdesao criarEventoRejeicao(
            ProcessoAdesao processo, 
            String motivoRejeicao, 
            String usuarioResponsavel) {
        HistoricoProcessoAdesao historico = new HistoricoProcessoAdesao(
            processo, 
            TipoEvento.REJEICAO, 
            "Processo de adesão rejeitado"
        );
        historico.setStatusAnterior(ProcessoAdesao.StatusProcesso.AGUARDANDO_APROVACAO);
        historico.setStatusAtual(ProcessoAdesao.StatusProcesso.REJEITADO);
        historico.setEtapaAtual(processo.getEtapaAtual());
        historico.setUsuarioResponsavel(usuarioResponsavel);
        historico.setObservacoes(motivoRejeicao);
        return historico;
    }
    
    public static HistoricoProcessoAdesao criarEventoCancelamento(
            ProcessoAdesao processo, 
            String motivo) {
        HistoricoProcessoAdesao historico = new HistoricoProcessoAdesao(
            processo, 
            TipoEvento.CANCELAMENTO, 
            "Processo de adesão cancelado"
        );
        historico.setStatusAnterior(processo.getStatus());
        historico.setStatusAtual(ProcessoAdesao.StatusProcesso.CANCELADO);
        historico.setEtapaAtual(processo.getEtapaAtual());
        historico.setObservacoes(motivo);
        return historico;
    }
    
    public static HistoricoProcessoAdesao criarEventoFinalizacao(ProcessoAdesao processo) {
        HistoricoProcessoAdesao historico = new HistoricoProcessoAdesao(
            processo, 
            TipoEvento.FINALIZACAO, 
            "Processo de adesão finalizado e enviado para aprovação"
        );
        historico.setStatusAnterior(ProcessoAdesao.StatusProcesso.EM_ANDAMENTO);
        historico.setStatusAtual(ProcessoAdesao.StatusProcesso.AGUARDANDO_APROVACAO);
        historico.setEtapaAnterior("revisao");
        historico.setEtapaAtual("finalizado");
        return historico;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProcessoAdesao getProcessoAdesao() {
        return processoAdesao;
    }
    
    public void setProcessoAdesao(ProcessoAdesao processoAdesao) {
        this.processoAdesao = processoAdesao;
    }
    
    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }
    
    public void setTipoEvento(TipoEvento tipoEvento) {
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
    
    public String getDadosAdicionais() {
        return dadosAdicionais;
    }
    
    public void setDadosAdicionais(String dadosAdicionais) {
        this.dadosAdicionais = dadosAdicionais;
    }
    
    // Enum para Tipo de Evento
    public enum TipoEvento {
        INICIO_PROCESSO("Início do Processo"),
        MUDANCA_ETAPA("Mudança de Etapa"),
        MUDANCA_STATUS("Mudança de Status"),
        FINALIZACAO("Finalização"),
        APROVACAO("Aprovação"),
        REJEICAO("Rejeição"),
        CANCELAMENTO("Cancelamento"),
        OBSERVACAO("Observação");
        
        private final String descricao;
        
        TipoEvento(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
}