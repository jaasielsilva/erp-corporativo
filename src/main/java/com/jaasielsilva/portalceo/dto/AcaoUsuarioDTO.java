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

    public String getAcao() {
        return acao;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getResponsavel() {
        return responsavel;
    }
}
