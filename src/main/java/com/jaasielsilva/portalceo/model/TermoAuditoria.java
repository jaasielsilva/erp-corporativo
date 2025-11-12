package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "termo_auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "termo_id", nullable = false)
    private Termo termo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private Termo.StatusTermo statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", length = 20)
    private Termo.StatusTermo statusNovo;

    @Column(name = "usuario_email", length = 150)
    private String usuarioEmail;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "operacao", length = 50)
    private String operacao;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @PrePersist
    protected void onCreate() {
        if (dataEvento == null) {
            dataEvento = LocalDateTime.now();
        }
    }
}