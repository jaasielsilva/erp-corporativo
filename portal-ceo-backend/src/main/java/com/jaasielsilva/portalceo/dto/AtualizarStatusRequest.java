package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.validation.ValidAtualizacaoStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de atualização de status de chamados
 * Inclui validações robustas para garantir integridade dos dados
 */
@ValidAtualizacaoStatus
public class AtualizarStatusRequest {

    @NotBlank(message = "A ação é obrigatória e não pode estar vazia")
    @Pattern(regexp = "^(iniciar|resolver|fechar|reabrir)$", 
             message = "Ação deve ser uma das seguintes: iniciar, resolver, fechar, reabrir")
    private String acao;

    @Size(max = 100, message = "Nome do técnico deve ter no máximo 100 caracteres")
    private String tecnicoResponsavel;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;

    @Size(max = 500, message = "Motivo da reabertura deve ter no máximo 500 caracteres")
    private String motivoReabertura;

    // Construtores
    public AtualizarStatusRequest() {}

    public AtualizarStatusRequest(String acao, String tecnicoResponsavel) {
        this.acao = acao;
        this.tecnicoResponsavel = tecnicoResponsavel;
    }

    // Getters e Setters
    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getTecnicoResponsavel() {
        return tecnicoResponsavel;
    }

    public void setTecnicoResponsavel(String tecnicoResponsavel) {
        this.tecnicoResponsavel = tecnicoResponsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getMotivoReabertura() {
        return motivoReabertura;
    }

    public void setMotivoReabertura(String motivoReabertura) {
        this.motivoReabertura = motivoReabertura;
    }

    /**
     * Valida se a ação é válida
     */
    public boolean isAcaoValida() {
        if (acao == null) return false;
        return acao.matches("^(iniciar|resolver|fechar|reabrir)$");
    }

    /**
     * Converte ação para StatusChamado correspondente
     */
    public StatusChamado getStatusDestino() {
        if (acao == null) return null;
        
        switch (acao.toLowerCase()) {
            case "iniciar":
                return StatusChamado.EM_ANDAMENTO;
            case "resolver":
                return StatusChamado.RESOLVIDO;
            case "fechar":
                return StatusChamado.FECHADO;
            case "reabrir":
                return StatusChamado.ABERTO;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "AtualizarStatusRequest{" +
                "acao='" + acao + '\'' +
                ", tecnicoResponsavel='" + tecnicoResponsavel + '\'' +
                ", observacoes='" + observacoes + '\'' +
                ", motivoReabertura='" + motivoReabertura + '\'' +
                '}';
    }
}