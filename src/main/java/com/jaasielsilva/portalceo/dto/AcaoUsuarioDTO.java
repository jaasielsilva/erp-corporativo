package com.jaasielsilva.portalceo.dto;

import java.time.LocalDateTime;

public class AcaoUsuarioDTO {
    private LocalDateTime data;
    private String acao;
    private String usuario;
    private String responsavel;

    public AcaoUsuarioDTO(LocalDateTime data, String acao, String usuario, String responsavel) {
        this.data = data;
        this.acao = acao;
        this.usuario = usuario;
        this.responsavel = responsavel;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }
}
