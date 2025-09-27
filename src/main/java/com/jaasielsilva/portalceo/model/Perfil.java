package com.jaasielsilva.portalceo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "perfis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;  // ex: ADMIN, USUARIO, GERENTE

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "perfil_permissao",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id"))
    private Set<Permissao> permissoes;

    @ManyToMany(mappedBy = "perfis")
    @ToString.Exclude    // evita recursão no toString
    @EqualsAndHashCode.Exclude  // evita recursão em equals/hashCode
    @JsonIgnore  // evita referência circular no JSON
    private Set<Usuario> usuarios;
}
