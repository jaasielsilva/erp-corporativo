package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
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

    private Integer diasPorAno = 30;

    private Boolean permitirVenda = Boolean.TRUE;

    @Column(length = 120)
    private String periodosBlackout; // ex: "12-20;07-05" (mes-dia;mes-dia)

    private Boolean exigeAprovacaoGerente = Boolean.TRUE;
}

