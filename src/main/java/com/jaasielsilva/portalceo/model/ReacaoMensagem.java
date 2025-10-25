package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reacoes_mensagens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReacaoMensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensagem_id", nullable = false)
    private Mensagem mensagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 10)
    private String emoji;

    @Column(name = "data_reacao", nullable = false)
    private LocalDateTime dataReacao = LocalDateTime.now();
}