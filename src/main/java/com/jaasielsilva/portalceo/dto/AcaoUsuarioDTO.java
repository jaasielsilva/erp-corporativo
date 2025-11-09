package com.jaasielsilva.portalceo.dto;

import java.time.LocalDateTime;

public class AcaoUsuarioDTO {
    private LocalDateTime data;
    private String acao;
    private String usuario;
    private String responsavel;
    private String ip;

    public AcaoUsuarioDTO(LocalDateTime data, String acao, String usuario, String responsavel, String ip) {
        this.data = data;
        this.acao = acao;
        this.usuario = usuario;
        this.responsavel = responsavel;
        this.ip = ip;
    }

    public LocalDateTime getData() {
        return data;
    }

    public String getAcao() {
        return acao;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getIp() {
        return ip;
    }
}
