package com.jaasielsilva.portalceo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historico_conta_pagar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoContaPagar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conta_pagar_id", nullable = false)
    private ContaPagar conta;

    @Column(nullable = false)
    private String acao;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime data;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
