package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String telefone;
    private String celular;
    private String cpfCnpj;
    private String tipoCliente; // PF ou PJ

    // Endereço separado
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    private LocalDate dataCadastro;
    private String status;
    private String pessoaContato;
    private String observacoes;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAlteracao;
    private String nomeFantasia;
    private String inscricaoMunicipal;
    private String inscricaoEstadual;
    private String representanteLegal;

    @ManyToOne
    @JoinColumn(name = "usuario_exclusao_id")
    private Usuario usuarioExclusao;

    private Boolean ativo = true;

    private LocalDateTime dataExclusao;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        dataCadastro = LocalDate.now();
        if (status == null) {
            status = "Ativo";
        }
        if (ativo == null) {
            ativo = true;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataAlteracao = LocalDateTime.now();
    }
}
