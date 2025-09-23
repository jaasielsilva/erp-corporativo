package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO para resposta de operações de status de chamados
 * Fornece informações estruturadas sobre o resultado da operação
 */
public class ChamadoStatusResponse {

    private boolean sucesso;
    private String mensagem;
    private Long chamadoId;
    private String numero;
    private StatusChamado status;
    private String statusDescricao;
    private Long slaRestante;
    private String tecnicoResponsavel;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataUltimaAtualizacao;

    // Construtores
    public ChamadoStatusResponse() {}

    public ChamadoStatusResponse(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    /**
     * Cria uma resposta de sucesso a partir de um chamado
     */
    public static ChamadoStatusResponse sucesso(Chamado chamado, String mensagem) {
        ChamadoStatusResponse response = new ChamadoStatusResponse();
        response.sucesso = true;
        response.mensagem = mensagem;
        response.chamadoId = chamado.getId();
        response.numero = chamado.getNumero();
        response.status = chamado.getStatus();
        response.statusDescricao = chamado.getStatus().getDescricao();
        response.slaRestante = chamado.getSlaRestante();
        response.tecnicoResponsavel = chamado.getTecnicoResponsavel();
        response.dataUltimaAtualizacao = LocalDateTime.now();
        return response;
    }

    /**
     * Cria uma resposta de erro
     */
    public static ChamadoStatusResponse erro(String mensagem) {
        return new ChamadoStatusResponse(false, mensagem);
    }

    /**
     * Cria uma resposta de erro com detalhes do chamado
     */
    public static ChamadoStatusResponse erro(Long chamadoId, String mensagem) {
        ChamadoStatusResponse response = new ChamadoStatusResponse(false, mensagem);
        response.chamadoId = chamadoId;
        return response;
    }

    // Getters e Setters
    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getChamadoId() {
        return chamadoId;
    }

    public void setChamadoId(Long chamadoId) {
        this.chamadoId = chamadoId;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public String getStatusDescricao() {
        return statusDescricao;
    }

    public void setStatusDescricao(String statusDescricao) {
        this.statusDescricao = statusDescricao;
    }

    public Long getSlaRestante() {
        return slaRestante;
    }

    public void setSlaRestante(Long slaRestante) {
        this.slaRestante = slaRestante;
    }

    public String getTecnicoResponsavel() {
        return tecnicoResponsavel;
    }

    public void setTecnicoResponsavel(String tecnicoResponsavel) {
        this.tecnicoResponsavel = tecnicoResponsavel;
    }

    public LocalDateTime getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(LocalDateTime dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    @Override
    public String toString() {
        return "ChamadoStatusResponse{" +
                "sucesso=" + sucesso +
                ", mensagem='" + mensagem + '\'' +
                ", chamadoId=" + chamadoId +
                ", numero='" + numero + '\'' +
                ", status=" + status +
                ", slaRestante=" + slaRestante +
                ", tecnicoResponsavel='" + tecnicoResponsavel + '\'' +
                '}';
    }
}