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
    private String titulo;
    private String descricao;
    @Enumerated(EnumType.STRING)
    private TipoEtapa tipoEtapa;
    private String usuario;

    public enum TipoEtapa {
        INICIAL,        // Abertura, Distribuição
        ANDAMENTO,      // Movimentação comum
        IMPORTANTE,     // Decisões, Prazos, Perícias
        AUDIENCIA,      // Audiências
        FINAL           // Sentença, Baixa, Arquivamento
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProcessoId() { return processoId; }
    public void setProcessoId(Long processoId) { this.processoId = processoId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public TipoEtapa getTipoEtapa() { return tipoEtapa; }
    public void setTipoEtapa(TipoEtapa tipoEtapa) { this.tipoEtapa = tipoEtapa; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}