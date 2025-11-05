package com.jaasielsilva.portalceo.model.ti;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ti_backup_registro")
public class BackupRegistro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // FULL, INCREMENTAL
    private String status; // SUCESSO, FALHA, EM_ANDAMENTO
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    @Column(length = 1000)
    private String descricao;
    private Double tamanhoMb;

    public BackupRegistro() {}

    public BackupRegistro(Long id, String tipo, String status, LocalDateTime dataInicio, LocalDateTime dataFim, String descricao, Double tamanhoMb) {
        this.id = id;
        this.tipo = tipo;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.descricao = descricao;
        this.tamanhoMb = tamanhoMb;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Double getTamanhoMb() { return tamanhoMb; }
    public void setTamanhoMb(Double tamanhoMb) { this.tamanhoMb = tamanhoMb; }
}