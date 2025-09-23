package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "colaborador_escala")
public class ColaboradorEscala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_trabalho_id", nullable = false)
    private EscalaTrabalho escalaTrabalho;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column
    private LocalDate dataFim;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
        if (dataInicio == null) {
            dataInicio = LocalDate.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    public boolean isVigente() {
        if (!ativo) return false;
        
        LocalDate hoje = LocalDate.now();
        boolean inicioOk = dataInicio == null || !hoje.isBefore(dataInicio);
        boolean fimOk = dataFim == null || !hoje.isAfter(dataFim);
        
        return inicioOk && fimOk;
    }

    public boolean isVigenteEm(LocalDate data) {
        if (!ativo) return false;
        
        boolean inicioOk = dataInicio == null || !data.isBefore(dataInicio);
        boolean fimOk = dataFim == null || !data.isAfter(dataFim);
        
        return inicioOk && fimOk;
    }

    public String getStatusDescricao() {
        if (!ativo) {
            return "Inativa";
        }
        
        LocalDate hoje = LocalDate.now();
        
        if (dataInicio != null && hoje.isBefore(dataInicio)) {
            return "Aguardando início";
        }
        
        if (dataFim != null && hoje.isAfter(dataFim)) {
            return "Expirada";
        }
        
        return "Vigente";
    }

    public String getStatusCor() {
        String status = getStatusDescricao();
        switch (status) {
            case "Vigente":
                return "success";
            case "Aguardando início":
                return "warning";
            case "Expirada":
            case "Inativa":
                return "danger";
            default:
                return "secondary";
        }
    }
}