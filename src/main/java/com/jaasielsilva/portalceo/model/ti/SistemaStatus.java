package com.jaasielsilva.portalceo.model.ti;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ti_sistema_status")
public class SistemaStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String status;
    private String uptime;
    private LocalDateTime ultimaVerificacao;

    public SistemaStatus() {}

    public SistemaStatus(Long id, String nome, String status, String uptime, LocalDateTime ultimaVerificacao) {
        this.id = id;
        this.nome = nome;
        this.status = status;
        this.uptime = uptime;
        this.ultimaVerificacao = ultimaVerificacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUptime() { return uptime; }
    public void setUptime(String uptime) { this.uptime = uptime; }
    public LocalDateTime getUltimaVerificacao() { return ultimaVerificacao; }
    public void setUltimaVerificacao(LocalDateTime ultimaVerificacao) { this.ultimaVerificacao = ultimaVerificacao; }
}