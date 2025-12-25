package com.jaasielsilva.portalceo.dto.whatchat;

import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemDirecao;
import com.jaasielsilva.portalceo.model.whatchat.ChatMensagemTipo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMensagemDTO {
    private Long id;
    private Long conversaId;
    private ChatMensagemDirecao direcao;
    private ChatMensagemTipo tipo;
    private String texto;
    private LocalDateTime dataMensagem;
    private String mediaFileName;
    private String mediaMimeType;
    private String mediaDownloadUrl;
    private String enviadaPorNome;
}

