package com.jaasielsilva.portalceo.model.ti;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ti_alerta_seguranca")
public class AlertaSeguranca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String severidade;
    private LocalDateTime data;
    private String origem;
    @Column(length = 2000)
    private String detalhes;

    public AlertaSeguranca() {}

    public AlertaSeguranca(Long id, String titulo, String severidade, LocalDateTime data, String origem, String detalhes) {
        this.id = id;
        this.titulo = titulo;
        this.severidade = severidade;
        this.data = data;
        this.origem = origem;
        this.detalhes = detalhes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getSeveridade() { return severidade; }
    public void setSeveridade(String severidade) { this.severidade = severidade; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
}