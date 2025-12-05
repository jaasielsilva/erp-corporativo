package com.jaasielsilva.portalceo.model.ajuda;

import jakarta.persistence.*;

@Entity
@Table(name = "ajuda_categoria")
public class AjudaCategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String slug;
    @Column(nullable = false)
    private String nome;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private AjudaCategoria parent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public AjudaCategoria getParent() { return parent; }
    public void setParent(AjudaCategoria parent) { this.parent = parent; }
}
