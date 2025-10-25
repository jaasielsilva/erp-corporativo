package com.jaasielsilva.portalceo.dto;

public class DigitacaoNotificationDTO {
    private Long remetenteId;
    private boolean digitando;

    public DigitacaoNotificationDTO(Long remetenteId, boolean digitando) {
        this.remetenteId = remetenteId;
        this.digitando = digitando;
    }

    public Long getRemetenteId() {
        return remetenteId;
    }

    public void setRemetenteId(Long remetenteId) {
        this.remetenteId = remetenteId;
    }

    public boolean isDigitando() {
        return digitando;
    }

    public void setDigitando(boolean digitando) {
        this.digitando = digitando;
    }
}