package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razaoSocial;
    private String nomeFantasia;
    @Column(nullable = false, unique = true)
    private String cnpj;

    private String inscricaoEstadual;

    private String telefone;
    private String celular;
    private String email;
    private String site;

    // Endereço
    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    @Column(length = 1000)
    private String observacoes;

    @NotNull(message = "O status é obrigatório.")
    @Column(length = 20)
    private String status;

}
