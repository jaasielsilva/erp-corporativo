package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rh_vale_transporte_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RhValeTransporteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "percentual_desconto", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentualDesconto;

    @Column(name = "valor_passagem", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorPassagem;

    @Column(name = "dias_uteis_mes", nullable = false)
    private Integer diasUteisMes;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_atualizacao", length = 100, nullable = false)
    private String usuarioAtualizacao;

    @Column(name = "versao", nullable = false)
    private Integer versao;
}
