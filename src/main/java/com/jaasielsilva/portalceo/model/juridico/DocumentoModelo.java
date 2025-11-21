package com.jaasielsilva.portalceo.model.juridico;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DocumentoModelo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String categoria;
    private String versao;
    @Enumerated(EnumType.STRING)
    private ModeloStatus status;
    private String arquivoModelo;
    @Column(length = 2000)
    private String changelog;
    @Column(length = 1000)
    private String observacoes;
    private String criadoPor;
    private LocalDateTime dataCriacao;
    private String aprovadoPor;
    private LocalDateTime dataPublicacao;
    private LocalDateTime deprecadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getVersao() { return versao; }
    public void setVersao(String versao) { this.versao = versao; }
    public ModeloStatus getStatus() { return status; }
    public void setStatus(ModeloStatus status) { this.status = status; }
    public String getArquivoModelo() { return arquivoModelo; }
    public void setArquivoModelo(String arquivoModelo) { this.arquivoModelo = arquivoModelo; }
    public String getChangelog() { return changelog; }
    public void setChangelog(String changelog) { this.changelog = changelog; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public String getCriadoPor() { return criadoPor; }
    public void setCriadoPor(String criadoPor) { this.criadoPor = criadoPor; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getAprovadoPor() { return aprovadoPor; }
    public void setAprovadoPor(String aprovadoPor) { this.aprovadoPor = aprovadoPor; }
    public LocalDateTime getDataPublicacao() { return dataPublicacao; }
    public void setDataPublicacao(LocalDateTime dataPublicacao) { this.dataPublicacao = dataPublicacao; }
    public LocalDateTime getDeprecadoEm() { return deprecadoEm; }
    public void setDeprecadoEm(LocalDateTime deprecadoEm) { this.deprecadoEm = deprecadoEm; }
}