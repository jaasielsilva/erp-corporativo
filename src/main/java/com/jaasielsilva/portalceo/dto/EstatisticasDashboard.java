package com.jaasielsilva.portalceo.dto;

public class EstatisticasDashboard {
    private int aguardandoAprovacao;
    private int emAndamento;
    private int finalizados;

    // Getters e Setters
    public int getAguardandoAprovacao() {
        return aguardandoAprovacao;
    }

    public void setAguardandoAprovacao(int aguardandoAprovacao) {
        this.aguardandoAprovacao = aguardandoAprovacao;
    }

    public int getEmAndamento() {
        return emAndamento;
    }

    public void setEmAndamento(int emAndamento) {
        this.emAndamento = emAndamento;
    }

    public int getFinalizados() {
        return finalizados;
    }

    public void setFinalizados(int finalizados) {
        this.finalizados = finalizados;
    }
}

