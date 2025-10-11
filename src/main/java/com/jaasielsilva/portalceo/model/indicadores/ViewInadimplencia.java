package com.jaasielsilva.portalceo.model.indicadores;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "view_inadimplencia")
public class ViewInadimplencia {

    @Id
    private Long id;

    @Column(name = "taxa_inadimplencia")
    private BigDecimal taxaInadimplencia;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTaxaInadimplencia() {
        return taxaInadimplencia;
    }

    public void setTaxaInadimplencia(BigDecimal taxaInadimplencia) {
        this.taxaInadimplencia = taxaInadimplencia;
    }
}
