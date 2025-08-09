package com.jaasielsilva.portalceo.dto;

public class VendaPdvResponse {
    private Long vendaId;
    private String mensagem;
    private boolean sucesso;

    public VendaPdvResponse() {
    }

    public VendaPdvResponse(Long vendaId, String mensagem) {
        this.vendaId = vendaId;
        this.mensagem = mensagem;
        this.sucesso = true;
    }

    public VendaPdvResponse(String mensagem, boolean sucesso) {
        this.mensagem = mensagem;
        this.sucesso = sucesso;
    }

    public VendaPdvResponse(Long vendaId, String mensagem, boolean sucesso) {
        this.vendaId = vendaId;
        this.mensagem = mensagem;
        this.sucesso = sucesso;
    }

    // Getters e Setters
    public Long getVendaId() {
        return vendaId;
    }

    public void setVendaId(Long vendaId) {
        this.vendaId = vendaId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }
}