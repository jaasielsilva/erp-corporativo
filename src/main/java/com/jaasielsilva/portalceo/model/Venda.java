package com.jaasielsilva.portalceo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_venda", unique = true)
    private String numeroVenda;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "desconto", precision = 10, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;
    
    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;
    
    @Column(name = "parcelas")
    private Integer parcelas = 1;
    
    @Column(name = "valor_pago", precision = 10, scale = 2)
    private BigDecimal valorPago;
    
    @Column(name = "troco", precision = 10, scale = 2)
    private BigDecimal troco = BigDecimal.ZERO;

    @Column(name = "data_venda")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataVenda;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    @Column(name = "status", length = 20)
    private String status = "FINALIZADA";

    @Column(name = "observacoes", length = 500)
    private String observacoes;
    
    @Column(name = "cupom_fiscal", length = 100)
    private String cupomFiscal;

    @PrePersist
    public void prePersist() {
        if (dataVenda == null) {
            dataVenda = LocalDateTime.now();
        }
        if (numeroVenda == null) {
            numeroVenda = gerarNumeroVenda();
        }
        if (status == null) {
            status = "FINALIZADA";
        }
    }

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendaItem> itens = new ArrayList<>();
    
    // Métodos utilitários
    private String gerarNumeroVenda() {
        return "VND" + System.currentTimeMillis();
    }
    
    public BigDecimal calcularTotal() {
        BigDecimal total = itens.stream()
            .map(VendaItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.subtract(desconto != null ? desconto : BigDecimal.ZERO);
    }
    
    public Integer getQuantidadeItens() {
        return itens.stream()
            .mapToInt(VendaItem::getQuantidade)
            .sum();
    }
}
