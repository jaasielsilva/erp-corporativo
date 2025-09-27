package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Produto produto;

    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo; // ENTRADA, SAIDA, AJUSTE

    private String motivo;

    private LocalDateTime dataHora;

    private String usuarioResponsavel; // para auditoria básica

    @PrePersist
    public void prePersist() {
        this.dataHora = LocalDateTime.now();
    }

    public String getDataFormatada() {
    if (this.dataHora == null) {
        return "";
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    return this.dataHora.format(formatter);
}
}

