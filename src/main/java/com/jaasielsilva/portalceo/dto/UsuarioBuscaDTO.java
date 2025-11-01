package com.jaasielsilva.portalceo.dto;

public class UsuarioBuscaDTO {
    private Long id;
    private String nome;
    private String departamento;
    private Boolean online;

    public UsuarioBuscaDTO(Long id, String nome, String departamento, Boolean online) {
        this.id = id;
        this.nome = nome;
        this.departamento = departamento;
        this.online = online;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDepartamento() { return departamento; }
    public Boolean getOnline() { return online; }
}