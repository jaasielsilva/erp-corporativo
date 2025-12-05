package com.jaasielsilva.portalceo.model.ajuda;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuda_busca_log")
public class AjudaBuscaLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(nullable = false, length = 512)
    private String query;
    @Column(nullable = false)
    private Integer resultados;
    @Column(nullable = false)
    private Boolean resolveu = false;
    @Column(name = "criado_at", nullable = false)
    private LocalDateTime criadoAt;

    @PrePersist
    protected void onCreate() { if (criadoAt == null) criadoAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public Integer getResultados() { return resultados; }
    public void setResultados(Integer resultados) { this.resultados = resultados; }
    public Boolean getResolveu() { return resolveu; }
    public void setResolveu(Boolean resolveu) { this.resolveu = resolveu; }
    public LocalDateTime getCriadoAt() { return criadoAt; }
    public void setCriadoAt(LocalDateTime criadoAt) { this.criadoAt = criadoAt; }
}
