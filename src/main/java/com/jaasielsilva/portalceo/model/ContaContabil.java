package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conta_contabil", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conta_contabil_codigo", columnNames = "codigo")
})
@EqualsAndHashCode(callSuper = true)
public class ContaContabil extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoConta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private GrupoConta grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id")
    private ContaBancaria contaBancaria;

    @Column(nullable = false)
    private Boolean ativa = true;

    public enum TipoConta {
        ATIVO,
        PASSIVO,
        PATRIMONIO_LIQUIDO,
        RECEITA,
        DESPESA
    }

    public enum GrupoConta {
        CAIXA_BANCOS,
        CONTAS_RECEBER,
        ESTOQUES,
        IMOBILIZADO,
        OUTROS_ATIVOS,
        CONTAS_PAGAR,
        FINANCIAMENTOS,
        OBRIGACOES_TRIBUTARIAS,
        CAPITAL_SOCIAL,
        RESULTADO_EXERCICIO,
        RECEITA_VENDAS,
        RECEITA_SERVICOS,
        OUTRAS_RECEITAS,
        CUSTOS,
        DESPESAS_OPERACIONAIS,
        DESPESAS_FINANCEIRAS,
        IMPOSTOS_SOBRE_VENDAS,
        IMPOSTOS_SOBRE_LUCRO
    }
}

