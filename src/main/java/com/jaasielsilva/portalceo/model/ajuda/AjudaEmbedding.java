package com.jaasielsilva.portalceo.model.ajuda;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuda_embedding")
public class AjudaEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "conteudo_id")
    private AjudaConteudo conteudo;
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;
    @Lob
    @Column(nullable = false)
    private byte[] embedding;
    @Column(nullable = false)
    private Integer dim;
    @Column
    private String area;
    @Column(name = "criado_at", nullable = false)
    private LocalDateTime criadoAt;

    @PrePersist
    protected void onCreate() { if (criadoAt == null) criadoAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AjudaConteudo getConteudo() { return conteudo; }
    public void setConteudo(AjudaConteudo conteudo) { this.conteudo = conteudo; }
    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }
    public byte[] getEmbedding() { return embedding; }
    public void setEmbedding(byte[] embedding) { this.embedding = embedding; }
    public Integer getDim() { return dim; }
    public void setDim(Integer dim) { this.dim = dim; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public LocalDateTime getCriadoAt() { return criadoAt; }
    public void setCriadoAt(LocalDateTime criadoAt) { this.criadoAt = criadoAt; }
}
