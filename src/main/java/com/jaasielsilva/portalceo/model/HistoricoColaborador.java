package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_colaboradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoColaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(nullable = false)
    private String evento; 
    // Exemplo: "Promoção", "Transferência", "Aumento Salarial", "Advertência"

    @Column(columnDefinition = "TEXT")
    private String descricao;  
    // Exemplo: "Promovido de Analista Jr para Analista Pleno no departamento de TI"

    private String cargoAnterior;
    private String cargoNovo;

    private BigDecimal salarioAnterior;
    private BigDecimal salarioNovo;

    private String departamentoAnterior;
    private String departamentoNovo;
    
    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();

}
