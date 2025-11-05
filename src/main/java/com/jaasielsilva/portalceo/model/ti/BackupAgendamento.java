package com.jaasielsilva.portalceo.model.ti;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ti_backup_agendamento")
public class BackupAgendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // FULL, INCREMENTAL
    private String periodicidade; // DIARIO, SEMANAL, MENSAL
    private LocalDateTime proximaExecucao;
    private boolean ativo;

    public BackupAgendamento() {}

    public BackupAgendamento(Long id, String tipo, String periodicidade, LocalDateTime proximaExecucao, boolean ativo) {
        this.id = id;
        this.tipo = tipo;
        this.periodicidade = periodicidade;
        this.proximaExecucao = proximaExecucao;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getPeriodicidade() { return periodicidade; }
    public void setPeriodicidade(String periodicidade) { this.periodicidade = periodicidade; }
    public LocalDateTime getProximaExecucao() { return proximaExecucao; }
    public void setProximaExecucao(LocalDateTime proximaExecucao) { this.proximaExecucao = proximaExecucao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}