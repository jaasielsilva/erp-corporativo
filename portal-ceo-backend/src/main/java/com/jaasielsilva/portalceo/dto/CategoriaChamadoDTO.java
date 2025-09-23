package com.jaasielsilva.portalceo.dto;

import java.util.List;

public class CategoriaChamadoDTO {
    private String codigo;
    private String nome;
    private List<SubcategoriaDTO> subcategorias;
    
    // Construtores
    public CategoriaChamadoDTO() {}
    
    public CategoriaChamadoDTO(String codigo, String nome, List<SubcategoriaDTO> subcategorias) {
        this.codigo = codigo;
        this.nome = nome;
        this.subcategorias = subcategorias;
    }
    
    // Getters e Setters
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public List<SubcategoriaDTO> getSubcategorias() {
        return subcategorias;
    }
    
    public void setSubcategorias(List<SubcategoriaDTO> subcategorias) {
        this.subcategorias = subcategorias;
    }
    
    public static class SubcategoriaDTO {
        private String codigo;
        private String nome;
        private String descricaoExemplo;
        
        // Construtores
        public SubcategoriaDTO() {}
        
        public SubcategoriaDTO(String codigo, String nome, String descricaoExemplo) {
            this.codigo = codigo;
            this.nome = nome;
            this.descricaoExemplo = descricaoExemplo;
        }
        
        // Getters e Setters
        public String getCodigo() {
            return codigo;
        }
        
        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }
        
        public String getNome() {
            return nome;
        }
        
        public void setNome(String nome) {
            this.nome = nome;
        }
        
        public String getDescricaoExemplo() {
            return descricaoExemplo;
        }
        
        public void setDescricaoExemplo(String descricaoExemplo) {
            this.descricaoExemplo = descricaoExemplo;
        }
    }
}