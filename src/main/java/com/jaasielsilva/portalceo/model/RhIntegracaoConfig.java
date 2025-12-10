package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_integracoes_config")
@Getter
@Setter
public class RhIntegracaoConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100)
    private String folhaProvider;

    @Size(max = 200)
    private String apiEndpointFolha;

    @Size(max = 200)
    private String beneficiosProvider;

    @Size(max = 200)
    private String apiEndpointBeneficios;

    @Email
    @Size(max = 120)
    private String emailNotificacoes;

    private Boolean habilitarNotificacoes;
}
