package com.jaasielsilva.portalceo.dto;

import lombok.Data;

@Data
public class FornecedorDTO {

    private Long id;

    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String inscricaoEstadual;

    private String telefone;
    private String celular;
    private String email;
    private String site;

    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    private String observacoes;
}
