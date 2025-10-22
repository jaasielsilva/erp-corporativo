package com.jaasielsilva.portalceo.model.projetos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import com.jaasielsilva.portalceo.model.Colaborador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "projeto_equipes")
public class EquipeProjeto extends BaseEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @ToString.Include
    private String nome;

    @ManyToMany
    @JoinTable(
        name = "projeto_equipe_membros",
        joinColumns = @JoinColumn(name = "equipe_id"),
        inverseJoinColumns = @JoinColumn(name = "colaborador_id")
    )
    private List<Colaborador> membros = new ArrayList<>();

    @Column(nullable = false)
    private Boolean ativa = true;
}
