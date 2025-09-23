package com.jaasielsilva.portalceo.dto;

import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioPDFDTO {
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    private LocalDate dataAdmissao;
    private LocalDate dataDesligamento;
    private String enderecoCompleto;
    private String genero;
    private String nivelAcesso;
    private String departamento;
    private String cargo;
    private String status;
    private Set<String> perfis;

}

