package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Classe base para entidades que precisam de auditoria automática.
 * Fornece campos padrão para rastreamento de criação e modificação.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * Data e hora de criação do registro
     */
    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    /**
     * Data e hora da última modificação do registro
     */
    @LastModifiedDate
    @Column(name = "data_ultima_edicao")
    private LocalDateTime dataUltimaEdicao;

    /**
     * Usuário que criou o registro
     */
    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criacao_id", updatable = false)
    private Usuario usuarioCriacao;

    /**
     * Usuário que fez a última modificação do registro
     */
    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_ultima_edicao_id")
    private Usuario usuarioUltimaEdicao;

    /**
     * Método executado antes da persistência para garantir
     * que a data de criação seja definida se não estiver presente
     */
    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        dataUltimaEdicao = LocalDateTime.now();
    }

    /**
     * Método executado antes da atualização para garantir
     * que a data de última edição seja atualizada
     */
    @PreUpdate
    protected void onUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }
}