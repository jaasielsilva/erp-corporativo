package com.jaasielsilva.portalceo.dto.rh.folhapagamento;

import java.math.BigDecimal;

public class ColaboradorFolhaDTO {

    private Long id;
    private String nome;
    private String cargo;
    private String departamento;
    private BigDecimal salarioBase;

    private Integer diasTrabalhados;
    private Integer diasMes;

    private Status status;

    public enum Status {
        INCLUIDO,
        FERIAS_PARCIAIS,
        AFASTADO
    }

    public ColaboradorFolhaDTO() {}

    public ColaboradorFolhaDTO(Long id, String nome, String cargo, String departamento,
                               BigDecimal salarioBase,
                               Integer diasTrabalhados, Integer diasMes,
                               Status status) {
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
        this.departamento = departamento;
        this.salarioBase = salarioBase;
        this.diasTrabalhados = diasTrabalhados;
        this.diasMes = diasMes;
        this.status = status;
    }

    // Getters e Setters...

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

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(BigDecimal salarioBase) {
        this.salarioBase = salarioBase;
    }

    public Integer getDiasTrabalhados() {
        return diasTrabalhados;
    }

    public void setDiasTrabalhados(Integer diasTrabalhados) {
        this.diasTrabalhados = diasTrabalhados;
    }

    public Integer getDiasMes() {
        return diasMes;
    }

    public void setDiasMes(Integer diasMes) {
        this.diasMes = diasMes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
