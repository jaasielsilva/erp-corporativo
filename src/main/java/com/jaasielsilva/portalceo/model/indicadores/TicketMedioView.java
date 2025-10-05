package com.jaasielsilva.portalceo.model.indicadores;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "view_ticket_medio")
public class TicketMedioView {

    @Id
    private Long id; // campo fictício só pra JPA aceitar a view
    private Double ticketMedio;

    public Long getId() {
        return id;
    }

    public Double getTicketMedio() {
        return ticketMedio;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTicketMedio(Double ticketMedio) {
        this.ticketMedio = ticketMedio;
    }
}
