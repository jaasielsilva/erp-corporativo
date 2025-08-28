package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "beneficios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Beneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do benefício é obrigatório")
    @Column(nullable = false, unique = true)
    private String nome;

    @OneToMany(mappedBy = "beneficio", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ColaboradorBeneficio> colaboradoresBeneficios;
}
