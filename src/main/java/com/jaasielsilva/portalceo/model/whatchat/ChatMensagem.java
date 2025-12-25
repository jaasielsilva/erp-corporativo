package com.jaasielsilva.portalceo.model.whatchat;

import com.jaasielsilva.portalceo.model.BaseEntity;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "whatchat_mensagens", indexes = {
        @Index(name = "idx_whatchat_msg_conversa_data", columnList = "conversa_id,data_mensagem"),
        @Index(name = "idx_whatchat_msg_wa_msg_id", columnList = "whatsapp_message_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatMensagem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversa_id", nullable = false)
    private ChatConversa conversa;

    @Enumerated(EnumType.STRING)
    @Column(name = "direcao", nullable = false, length = 20)
    private ChatMensagemDirecao direcao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private ChatMensagemTipo tipo;

    @Lob
    @Column(name = "texto", columnDefinition = "TEXT")
    private String texto;

    @Column(name = "whatsapp_message_id", length = 80)
    private String whatsappMessageId;

    @Column(name = "media_id", length = 120)
    private String mediaId;

    @Column(name = "media_mime_type", length = 120)
    private String mediaMimeType;

    @Column(name = "media_file_name", length = 255)
    private String mediaFileName;

    @Column(name = "media_caminho", length = 800)
    private String mediaCaminho;

    @Column(name = "media_sha256", length = 120)
    private String mediaSha256;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviada_por_usuario_id")
    private Usuario enviadaPor;

    @Column(name = "data_mensagem", nullable = false)
    private LocalDateTime dataMensagem;

    @Lob
    @Column(name = "payload_bruto", columnDefinition = "LONGTEXT")
    private String payloadBruto;
}

