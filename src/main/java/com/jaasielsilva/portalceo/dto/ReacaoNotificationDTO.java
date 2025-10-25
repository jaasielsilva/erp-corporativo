package com.jaasielsilva.portalceo.dto;

public class ReacaoNotificationDTO {
    private Long mensagemId;
    private Long reacaoId;
    private Long usuarioId;
    private String emoji;
    private String tipo;

    public ReacaoNotificationDTO(Long mensagemId, Long reacaoId, Long usuarioId, String emoji, String tipo) {
        this.mensagemId = mensagemId;
        this.reacaoId = reacaoId;
        this.usuarioId = usuarioId;
        this.emoji = emoji;
        this.tipo = tipo;
    }

    public Long getMensagemId() {
        return mensagemId;
    }

    public void setMensagemId(Long mensagemId) {
        this.mensagemId = mensagemId;
    }

    public Long getReacaoId() {
        return reacaoId;
    }

    public void setReacaoId(Long reacaoId) {
        this.reacaoId = reacaoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}