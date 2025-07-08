package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime expiracao;

    @Column(nullable = false)
    private boolean usado = false;  // novo campo para indicar se o token já foi usado

    public PasswordResetToken() {}

    public PasswordResetToken(String token, Usuario usuario, LocalDateTime expiracao) {
        this.token = token;
        this.usuario = usuario;
        this.expiracao = expiracao;
        this.usado = false;
    }

}
