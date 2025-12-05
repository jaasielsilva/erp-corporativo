package com.jaasielsilva.portalceo.model.ajuda;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuda_feedback")
public class AjudaFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "conteudo_id")
    private AjudaConteudo conteudo;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(nullable = false)
    private boolean upvote;
    @Column
    private String comentario;
    @Column(name = "criado_at", nullable = false)
    private LocalDateTime criadoAt;

    @PrePersist
    protected void onCreate() { if (criadoAt == null) criadoAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AjudaConteudo getConteudo() { return conteudo; }
    public void setConteudo(AjudaConteudo conteudo) { this.conteudo = conteudo; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public boolean isUpvote() { return upvote; }
    public void setUpvote(boolean upvote) { this.upvote = upvote; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getCriadoAt() { return criadoAt; }
    public void setCriadoAt(LocalDateTime criadoAt) { this.criadoAt = criadoAt; }
}
