package com.jaasielsilva.portalceo.dto;

public class MemberDTO {
    private Long id;
    private String nome;

    public MemberDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
