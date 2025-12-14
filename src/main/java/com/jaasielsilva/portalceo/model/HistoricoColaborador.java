package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.jaasielsilva.portalceo.model.Usuario;

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
    
    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    @Column(name = "ip_origem")
    private String ipOrigem;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "campo_alterado")
    private String campoAlterado;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_novo", columnDefinition = "TEXT")
    private String valorNovo;
    
    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();

}
