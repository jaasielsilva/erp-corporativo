package com.jaasielsilva.portalceo.model.whatchat;

import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.model.BaseEntity;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "whatchat_conversas", indexes = {
        @Index(name = "idx_whatchat_conversa_wa_id", columnList = "wa_id"),
        @Index(name = "idx_whatchat_conversa_status", columnList = "status"),
        @Index(name = "idx_whatchat_conversa_ultima_msg", columnList = "ultima_mensagem_em")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatConversa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wa_id", nullable = false, length = 32, unique = true)
    private String waId;

    @Column(name = "nome_contato", length = 120)
    private String nomeContato;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ChatConversaStatus status = ChatConversaStatus.ABERTA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_previdenciario_id")
    private ProcessoPrevidenciario processoPrevidenciario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id")
    private Usuario atendente;

    @Column(name = "ultima_mensagem_em")
    private LocalDateTime ultimaMensagemEm;
}

