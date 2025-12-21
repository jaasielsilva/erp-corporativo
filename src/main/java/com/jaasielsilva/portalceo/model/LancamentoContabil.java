package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lancamento_contabil", indexes = {
        @Index(name = "idx_lancamento_contabil_data", columnList = "data"),
        @Index(name = "idx_lancamento_contabil_chave", columnList = "chaveIdempotencia")
})
@EqualsAndHashCode(callSuper = true)
public class LancamentoContabil extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(length = 120, unique = true)
    private String chaveIdempotencia;

    @Column(length = 60)
    private String referenciaTipo;

    @Column
    private Long referenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusLancamento status = StatusLancamento.LANCADO;

    @OneToMany(mappedBy = "lancamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LancamentoContabilItem> itens = new ArrayList<>();

    public enum StatusLancamento {
        LANCADO,
        ESTORNADO
    }
}

