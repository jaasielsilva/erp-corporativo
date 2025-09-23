package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "forma_pagamento")
public class FormaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(length = 100)
    private String descricao;

    @Column(name = "aceita_parcelas")
    private Boolean aceitaParcelas = false;

    @Column(name = "max_parcelas")
    private Integer maxParcelas = 1;

    @Column(name = "taxa_juros")
    private Double taxaJuros = 0.0;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;

    // Construtor para facilitar criação
    public FormaPagamento(String nome, String descricao, Boolean aceitaParcelas, Integer maxParcelas) {
        this.nome = nome;
        this.descricao = descricao;
        this.aceitaParcelas = aceitaParcelas;
        this.maxParcelas = maxParcelas;
        this.ativo = true;
    }
}