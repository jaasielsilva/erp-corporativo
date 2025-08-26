package com.jaasielsilva.portalceo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "permissoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome; // ex: ROLE_ADMIN, ROLE_USUARIO

    @Column
    private String categoria; // categoria da permissão

    @ManyToMany(mappedBy = "permissoes")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore  // evita referência circular no JSON
    private Set<Perfil> perfis;
}
