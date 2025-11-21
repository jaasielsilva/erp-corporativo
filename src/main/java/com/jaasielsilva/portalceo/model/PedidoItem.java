package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Pedido pedido;

    @ManyToOne(optional = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    public BigDecimal getSubtotal() {
        BigDecimal p = precoUnitario != null ? precoUnitario : BigDecimal.ZERO;
        int q = quantidade != null ? quantidade : 0;
        return p.multiply(BigDecimal.valueOf(q));
    }
}