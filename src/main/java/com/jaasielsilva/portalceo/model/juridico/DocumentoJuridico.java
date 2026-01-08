package com.jaasielsilva.portalceo.model.juridico;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DocumentoJuridico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String categoria;
    private String descricao;
    private String caminhoArquivo;
    private LocalDateTime criadoEm;
    private String autor;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] conteudo;
    private String contentType;
    private String originalFilename;
    private Long tamanho;

    @ElementCollection
    private java.util.Set<String> tags = new java.util.HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private ProcessoJuridico processo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public byte[] getConteudo() {
        return conteudo;
    }

    public void setConteudo(byte[] conteudo) {
        this.conteudo = conteudo;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getTamanho() {
        return tamanho;
    }

    public void setTamanho(Long tamanho) {
        this.tamanho = tamanho;
    }

    public java.util.Set<String> getTags() {
        return tags;
    }

    public void setTags(java.util.Set<String> tags) {
        this.tags = tags;
    }

    public ProcessoJuridico getProcesso() {
        return processo;
    }

    public void setProcesso(ProcessoJuridico processo) {
        this.processo = processo;
    }
}
