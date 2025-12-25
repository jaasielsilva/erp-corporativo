package com.jaasielsilva.portalceo.dto.whatchat;

import com.jaasielsilva.portalceo.model.whatchat.ChatConversaStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversaResumoDTO {
    private Long id;
    private String waId;
    private String nomeContato;
    private ChatConversaStatus status;
    private LocalDateTime ultimaMensagemEm;
    private Long clienteId;
    private String clienteNome;
    private Long processoPrevidenciarioId;
    private String atendenteNome;
}

