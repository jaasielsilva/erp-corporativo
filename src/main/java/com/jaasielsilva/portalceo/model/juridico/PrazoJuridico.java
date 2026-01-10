package com.jaasielsilva.portalceo.model.juridico;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PrazoJuridico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "processo_id")
    private ProcessoJuridico processo;
    
    private LocalDate dataLimite;
    private String descricao;
    private String responsabilidade;
    private boolean cumprido;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ProcessoJuridico getProcesso() { return processo; }
    public void setProcesso(ProcessoJuridico processo) { this.processo = processo; }
    
    public LocalDate getDataLimite() { return dataLimite; }
    public void setDataLimite(LocalDate dataLimite) { this.dataLimite = dataLimite; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getResponsabilidade() { return responsabilidade; }
    public void setResponsabilidade(String responsabilidade) { this.responsabilidade = responsabilidade; }
    public boolean isCumprido() { return cumprido; }
    public void setCumprido(boolean cumprido) { this.cumprido = cumprido; }
}