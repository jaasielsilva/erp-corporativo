package com.jaasielsilva.portalceo.model.indicadores;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "view_roi_mensal")
public class RoiMensalView {

    @Id
    private Long id;
    private Double roiMensal;

    public Long getId() {
        return id;
    }

    public Double getRoiMensal() {
        return roiMensal;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoiMensal(Double roiMensal) {
        this.roiMensal = roiMensal;
    }

    //Retorno sobre investimento (ROI) no mês corrente.
    //(Simula ROI = Lucro / Custo Total × 100)
}
