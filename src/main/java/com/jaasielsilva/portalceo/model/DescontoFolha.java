package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "desconto_folha")
public class DescontoFolha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holerite_id", nullable = false)
    private Holerite holerite;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDesconto tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentual;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum TipoDesconto {
        EMPRESTIMO_CONSIGNADO,
        PENSAO_ALIMENTICIA,
        SINDICATO,
        SEGURO_VIDA,
        PLANO_DENTAL,
        FARMACIA,
        ADIANTAMENTO,
        MULTA,
        DANO_PATRIMONIO,
        OUTROS
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    public String getTipoDescricao() {
        switch (tipo) {
            case EMPRESTIMO_CONSIGNADO:
                return "Empréstimo Consignado";
            case PENSAO_ALIMENTICIA:
                return "Pensão Alimentícia";
            case SINDICATO:
                return "Contribuição Sindical";
            case SEGURO_VIDA:
                return "Seguro de Vida";
            case PLANO_DENTAL:
                return "Plano Dental";
            case FARMACIA:
                return "Farmácia";
            case ADIANTAMENTO:
                return "Adiantamento";
            case MULTA:
                return "Multa";
            case DANO_PATRIMONIO:
                return "Dano ao Patrimônio";
            case OUTROS:
                return "Outros";
            default:
                return tipo.toString();
        }
    }
}