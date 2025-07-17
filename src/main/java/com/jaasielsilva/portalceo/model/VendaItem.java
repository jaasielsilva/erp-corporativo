package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relação com produto
    @ManyToOne
    private Produto produto;

    private int quantidade;

    private double precoUnitario;

    // Relação com venda
    @ManyToOne
    private Venda venda;

    // Construtores
    public VendaItem() {}

    public VendaItem(Produto produto, int quantidade, double precoUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }
    public double getSubtotal() {
        return precoUnitario * quantidade;
    }
}