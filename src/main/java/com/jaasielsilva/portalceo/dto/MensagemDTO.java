package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Mensagem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MensagemDTO {
    
    private Long id;
    private Long remetenteId;
    private String remetenteNome;
    private String remetenteFoto;
    private Long destinatarioId;
    private String destinatarioNome;
    private Long conversaId;
    private String conteudo;
    private LocalDateTime dataEnvio;
    private String dataEnvioFormatada;
    private boolean lida;
    private LocalDateTime dataLeitura;
    private String tipoMensagem;
    private boolean isMinhaMensagem;

    // Construtor a partir da entidade Mensagem
    public static MensagemDTO fromEntity(Mensagem mensagem, Long usuarioLogadoId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        MensagemDTO dto = new MensagemDTO();
        dto.setId(mensagem.getId());
        dto.setRemetenteId(mensagem.getRemetente().getId());
        dto.setRemetenteNome(mensagem.getRemetente().getNome());
        dto.setRemetenteFoto(mensagem.getRemetente().getFotoPerfil() != null ? "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(mensagem.getRemetente().getFotoPerfil()) : null);
        // Mensagens no chat são enviadas para a conversa, não para destinatário específico
        dto.setDestinatarioId(null);
        dto.setDestinatarioNome(null);
        dto.setConversaId(mensagem.getConversa().getId());
        dto.setConteudo(mensagem.getConteudo());
        dto.setDataEnvio(mensagem.getEnviadaEm());
        dto.setDataEnvioFormatada(mensagem.getEnviadaEm().format(formatter));
        dto.setLida(mensagem.isLida());
        dto.setDataLeitura(mensagem.getLidaEm());
        dto.setTipoMensagem(mensagem.getTipo().name());
        dto.setMinhaMensagem(usuarioLogadoId != null && mensagem.getRemetente().getId().equals(usuarioLogadoId));
        
        return dto;
    }
    
    // Sobrecarga do método fromEntity sem usuarioLogadoId
    public static MensagemDTO fromEntity(Mensagem mensagem) {
        return fromEntity(mensagem, null);
    }
}