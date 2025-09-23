package com.jaasielsilva.portalceo.model;

public enum TipoContrato {
    FORNECEDOR("Fornecedor"),
    CLIENTE("Cliente"),
    LOCACAO("Locação"),
    PRESTADOR_SERVICO("Prestador de Serviço"),
    EMPREGO_COLABORADOR("Emprego de Colaborador"),
    CONFIDENCIALIDADE_NDA("Confidencialidade (NDA)"),
    CONTRATO_COMERCIAL("Contrato Comercial"),
    TRABALHISTA("Trabalhista");

    private final String descricao;

    TipoContrato(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
