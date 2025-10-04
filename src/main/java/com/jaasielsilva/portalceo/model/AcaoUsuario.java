package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "acoes_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcaoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime data;

    private String acao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    public AcaoUsuario(LocalDateTime data, String acao, Usuario usuario, Usuario responsavel) {
        this.data = data;
        this.acao = acao;
        this.usuario = usuario;
        this.responsavel = responsavel;
    }
}
