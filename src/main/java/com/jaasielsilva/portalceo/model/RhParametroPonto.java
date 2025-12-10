package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_parametros_ponto")
@Getter
@Setter
public class RhParametroPonto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(0)
    @Max(120)
    private Integer toleranciaMinutos;

    @Min(0)
    @Max(120)
    private Integer arredondamentoMinutos;

    @Min(0)
    @Max(600)
    private Integer limiteHoraExtraDia;

    private Boolean exigeAprovacaoGerente;

    private Boolean permitirCorrecaoManual;
}
