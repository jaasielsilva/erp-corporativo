package com.jaasielsilva.portalceo.dto;

public class ColaboradorSimpleDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String cargo;
    private String departamento;
    private java.time.LocalDate dataAdmissao;

    public ColaboradorSimpleDTO() {}

    public ColaboradorSimpleDTO(Long id, String nome, String email, String cpf, String cargo, String departamento, java.time.LocalDate dataAdmissao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.cargo = cargo;
        this.departamento = departamento;
        this.dataAdmissao = dataAdmissao;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public java.time.LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(java.time.LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }
}