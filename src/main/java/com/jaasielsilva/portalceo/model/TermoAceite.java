package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "termo_aceites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"termo_id", "usuario_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoAceite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "termo_id", nullable = false)
    private Termo termo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_aceite", nullable = false)
    private LocalDateTime dataAceite;

    @Column(name = "ip_aceite", length = 45)
    private String ipAceite;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusAceite status = StatusAceite.ACEITO;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "versao_termo", length = 20)
    private String versaoTermo;

    @Column(name = "assinatura_digital", length = 500)
    private String assinaturaDigital;

    public enum StatusAceite {
        ACEITO("Aceito"),
        REJEITADO("Rejeitado"),
        PENDENTE("Pendente"),
        EXPIRADO("Expirado");

        private final String descricao;

        StatusAceite(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (dataAceite == null) {
            dataAceite = LocalDateTime.now();
        }
        if (termo != null) {
            versaoTermo = termo.getVersao();
        }
    }

    public boolean isValido() {
        return status == StatusAceite.ACEITO && 
               termo != null && 
               termo.isVigente();
    }
}