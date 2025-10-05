package com.jaasielsilva.portalceo.model.indicadores;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "view_margem_lucro")
public class MargemLucroView {

    @Id
    private Long id;
    private Double margemLucro;

    public Long getId() {
        return id;
    }

    public Double getMargemLucro() {
        return margemLucro;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMargemLucro(Double margemLucro) {
        this.margemLucro = margemLucro;
    }
}
