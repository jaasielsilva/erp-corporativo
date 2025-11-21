package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ABERTO;

    @Column(precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    private LocalDateTime dataCriacao;

    public enum Status {
        ABERTO,
        FATURADO,
        CANCELADO
    }

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        if (this.total == null) {
            this.total = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = Status.ABERTO;
        }
    }

    public BigDecimal calcularTotal() {
        BigDecimal t = BigDecimal.ZERO;
        for (PedidoItem item : itens) {
            t = t.add(item.getSubtotal());
        }
        this.total = t;
        return t;
    }
}