package com.jaasielsilva.portalceo.model.juridico;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Audiencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "processo_id")
    private ProcessoJuridico processo;
    
    private LocalDateTime dataHora;
    private String tipo;
    private String observacoes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ProcessoJuridico getProcesso() { return processo; }
    public void setProcesso(ProcessoJuridico processo) { this.processo = processo; }
    
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}