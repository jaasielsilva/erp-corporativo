package com.jaasielsilva.portalceo.model.ajuda;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuda_conversa_ia")
public class AjudaConversaIa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String pergunta;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String resposta;
    @Column
    private Double confianca;
    @Column(nullable = false)
    private Boolean escalonado = false;
    @Column(name = "criado_at", nullable = false)
    private LocalDateTime criadoAt;

    @PrePersist
    protected void onCreate() { if (criadoAt == null) criadoAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getPergunta() { return pergunta; }
    public void setPergunta(String pergunta) { this.pergunta = pergunta; }
    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }
    public Double getConfianca() { return confianca; }
    public void setConfianca(Double confianca) { this.confianca = confianca; }
    public Boolean getEscalonado() { return escalonado; }
    public void setEscalonado(Boolean escalonado) { this.escalonado = escalonado; }
    public LocalDateTime getCriadoAt() { return criadoAt; }
    public void setCriadoAt(LocalDateTime criadoAt) { this.criadoAt = criadoAt; }
}
