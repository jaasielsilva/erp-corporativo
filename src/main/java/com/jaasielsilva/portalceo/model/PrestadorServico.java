package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prestador_servico")
public class PrestadorServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "cnpj_ou_cpf", nullable = false, unique = true)
    private String cnpjOuCpf;

    private String telefone;

    private String email;

    @Column(nullable = false)
    private boolean ativo = true;  // novo campo para indicar ativo/inativo

}
