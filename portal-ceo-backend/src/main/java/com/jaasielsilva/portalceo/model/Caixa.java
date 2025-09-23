package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "caixa")
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "valor_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "valor_final", precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "total_vendas", precision = 10, scale = 2)
    private BigDecimal totalVendas = BigDecimal.ZERO;

    @Column(name = "total_dinheiro", precision = 10, scale = 2)
    private BigDecimal totalDinheiro = BigDecimal.ZERO;

    @Column(name = "total_cartao", precision = 10, scale = 2)
    private BigDecimal totalCartao = BigDecimal.ZERO;

    @Column(name = "total_pix", precision = 10, scale = 2)
    private BigDecimal totalPix = BigDecimal.ZERO;

    @Column(name = "quantidade_vendas")
    private Integer quantidadeVendas = 0;

    @Column(name = "status", length = 20)
    private String status = "ABERTO"; // ABERTO, FECHADO

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "usuario_abertura_id")
    private Usuario usuarioAbertura;

    @ManyToOne
    @JoinColumn(name = "usuario_fechamento_id")
    private Usuario usuarioFechamento;

    @PrePersist
    public void prePersist() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
        if (status == null) {
            status = "ABERTO";
        }
    }

    // Métodos utilitários
    public void adicionarVenda(BigDecimal valor, String formaPagamento) {
        this.totalVendas = this.totalVendas.add(valor);
        this.quantidadeVendas++;
        
        switch (formaPagamento.toLowerCase()) {
            case "dinheiro":
                this.totalDinheiro = this.totalDinheiro.add(valor);
                break;
            case "pix":
                this.totalPix = this.totalPix.add(valor);
                break;
            case "cartão de débito":
            case "cartão de crédito":
                this.totalCartao = this.totalCartao.add(valor);
                break;
        }
    }

    public BigDecimal calcularTotalEsperado() {
        return valorInicial.add(totalVendas);
    }

    public BigDecimal calcularDiferenca() {
        if (valorFinal == null) {
            return BigDecimal.ZERO;
        }
        return valorFinal.subtract(calcularTotalEsperado());
    }
}