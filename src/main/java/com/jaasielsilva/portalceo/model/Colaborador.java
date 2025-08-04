package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "colaboradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String cpf;

    private String email;

    private String telefone;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    private LocalDate dataNascimento;

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;

    @Enumerated(EnumType.STRING)
    private StatusColaborador status = StatusColaborador.ATIVO;

    private Boolean ativo = true;

    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    private LocalDateTime dataUltimaEdicao;

    @Column(name = "rg")
    private String rg;

    @Column(precision = 10, scale = 2)
    private BigDecimal salario;

    // Campos de endereço
    private String cep;
    
    private String logradouro;
    
    private String numero;
    
    private String complemento;
    
    private String bairro;
    
    private String cidade;
    
    private String estado;
    
    private String pais;
    
    private String observacoes;
    
    private String tipoContrato;
    
    private Integer cargaHoraria;
    
    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Colaborador supervisor;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null)
            ativo = true;
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Enums internas ou separadas em arquivos próprios
    public enum Sexo {
        MASCULINO, FEMININO, OUTRO
    }

    public enum EstadoCivil {
        SOLTEIRO, CASADO, DIVORCIADO, VIUVO
    }

    public enum StatusColaborador {
        ATIVO, INATIVO, SUSPENSO
    }
}
