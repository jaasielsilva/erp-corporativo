package com.jaasielsilva.portalceo.model.juridico;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AndamentoProcesso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long processoId;
    private LocalDateTime dataHora;
    private String descricao;
    private String usuario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProcessoId() { return processoId; }
    public void setProcessoId(Long processoId) { this.processoId = processoId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}