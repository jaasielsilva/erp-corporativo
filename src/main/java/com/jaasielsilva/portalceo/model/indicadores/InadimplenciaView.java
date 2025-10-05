package com.jaasielsilva.portalceo.model.indicadores;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "view_inadimplencia")
public class InadimplenciaView {

    @Id
    private Long id;
    private Double inadimplencia;

    public Long getId() {
        return id;
    }

    public Double getInadimplencia() {
        return inadimplencia;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInadimplencia(Double inadimplencia) {
        this.inadimplencia = inadimplencia;
    }
}
