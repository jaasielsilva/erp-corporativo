package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_politicas_ferias")
@Getter
@Setter
public class RhPoliticaFerias extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(40)
    private Integer diasPorAno = 30;

    private Boolean permitirVenda = Boolean.TRUE;

    @Column(length = 120)
    private String periodosBlackout;

    private Boolean exigeAprovacaoGerente = Boolean.TRUE;
}
