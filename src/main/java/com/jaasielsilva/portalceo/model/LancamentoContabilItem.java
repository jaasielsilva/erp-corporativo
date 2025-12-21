package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lancamento_contabil_item", indexes = {
        @Index(name = "idx_lanc_item_conta", columnList = "conta_contabil_id"),
        @Index(name = "idx_lanc_item_lanc", columnList = "lancamento_id")
})
@EqualsAndHashCode(callSuper = true)
public class LancamentoContabilItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lancamento_id", nullable = false)
    private LancamentoContabil lancamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_contabil_id", nullable = false)
    private ContaContabil contaContabil;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal debito = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal credito = BigDecimal.ZERO;
}

