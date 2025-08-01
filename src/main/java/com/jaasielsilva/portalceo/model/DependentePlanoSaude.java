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
@Table(name = "dependente_plano_saude")
public class DependentePlanoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adesao_plano_saude_id", nullable = false)
    private AdesaoPlanoSaude adesaoPlanoSaude;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 14)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoParentesco parentesco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Column(length = 20)
    private String rg;

    @Column(length = 20)
    private String cartaoSus;

    @Column(nullable = false)
    private LocalDate dataInclusao;

    @Column
    private LocalDate dataExclusao;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 500)
    private String observacoes;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum TipoParentesco {
        CONJUGE,
        FILHO,
        FILHA,
        PAI,
        MAE,
        ENTEADO,
        ENTEADA,
        COMPANHEIRO,
        COMPANHEIRA,
        OUTROS
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
        if (dataInclusao == null) {
            dataInclusao = LocalDate.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    public String getParentescoDescricao() {
        switch (parentesco) {
            case CONJUGE:
                return "Cônjuge";
            case FILHO:
                return "Filho";
            case FILHA:
                return "Filha";
            case PAI:
                return "Pai";
            case MAE:
                return "Mãe";
            case ENTEADO:
                return "Enteado";
            case ENTEADA:
                return "Enteada";
            case COMPANHEIRO:
                return "Companheiro";
            case COMPANHEIRA:
                return "Companheira";
            case OUTROS:
                return "Outros";
            default:
                return parentesco.toString();
        }
    }

    public int getIdade() {
        if (dataNascimento != null) {
            return LocalDate.now().getYear() - dataNascimento.getYear();
        }
        return 0;
    }

    public boolean isMenorIdade() {
        return getIdade() < 18;
    }
}