package com.jaasielsilva.portalceo.model.indicadores;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "view_roi_mensal")
public class ViewRoiMensal {

    @Id
    private Integer id;

    @Column(name = "roi_mensal")
    private BigDecimal roiMensal;

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getRoiMensal() {
        return roiMensal;
    }

    public void setRoiMensal(BigDecimal roiMensal) {
        this.roiMensal = roiMensal;
    }
}
