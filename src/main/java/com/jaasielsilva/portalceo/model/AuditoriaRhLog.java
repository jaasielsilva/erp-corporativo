package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "rh_auditoria_logs", indexes = {
        @Index(name = "idx_rh_auditoria_categoria", columnList = "categoria"),
        @Index(name = "idx_rh_auditoria_usuario", columnList = "usuario"),
        @Index(name = "idx_rh_auditoria_recurso", columnList = "recurso"),
        @Index(name = "idx_rh_auditoria_data", columnList = "criadoEm")
})
@Getter
@Setter
public class AuditoriaRhLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40, nullable = false)
    private String categoria; // ACESSO, ALTERACAO, EXPORTACAO, REVISAO, CONFIGURACAO

    @Column(length = 120, nullable = false)
    private String acao; // ex: "LOGIN_SUCESSO", "ALTERACAO_COLABORADOR"

    @Column(length = 120)
    private String recurso; // ex: "/rh/configuracoes/politicas-ferias"

    @Column(length = 120)
    private String usuario; // email ou username

    @Column(length = 60)
    private String ipOrigem;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    private Boolean sucesso = Boolean.TRUE;

    private LocalDateTime criadoEm = LocalDateTime.now();
}

