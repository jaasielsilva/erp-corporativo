package com.jaasielsilva.portalceo.model.ajuda;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuda_conteudo")
public class AjudaConteudo {
    public enum Tipo { ARTIGO, FAQ, VIDEO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private AjudaCategoria categoria;
    @Column(nullable = false)
    private String titulo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String corpo;
    @Column
    private String tags;
    @Column(nullable = false)
    private boolean publicado = false;
    @Column(name = "criado_at", nullable = false)
    private LocalDateTime criadoAt;
    @Column
    private Integer visualizacoes;

    @PrePersist
    protected void onCreate() { if (criadoAt == null) criadoAt = LocalDateTime.now(); if (visualizacoes == null) visualizacoes = 0; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AjudaCategoria getCategoria() { return categoria; }
    public void setCategoria(AjudaCategoria categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public String getCorpo() { return corpo; }
    public void setCorpo(String corpo) { this.corpo = corpo; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public boolean isPublicado() { return publicado; }
    public void setPublicado(boolean publicado) { this.publicado = publicado; }
    public LocalDateTime getCriadoAt() { return criadoAt; }
    public void setCriadoAt(LocalDateTime criadoAt) { this.criadoAt = criadoAt; }
    public Integer getVisualizacoes() { return visualizacoes; }
    public void setVisualizacoes(Integer visualizacoes) { this.visualizacoes = visualizacoes; }
}
