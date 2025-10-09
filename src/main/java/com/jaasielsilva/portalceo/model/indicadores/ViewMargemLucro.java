package com.jaasielsilva.portalceo.model.indicadores;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidade que representa a view SQL 'view_margem_lucro'.
 * Essa view deve retornar o valor consolidado da margem de lucro da empresa.
 */
@Entity
@Table(name = "view_margem_lucro")
public class ViewMargemLucro {

    @Id
    private Integer id;

    private BigDecimal margemLucro;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getMargemLucro() {
        return margemLucro;
    }

    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
    }
}
