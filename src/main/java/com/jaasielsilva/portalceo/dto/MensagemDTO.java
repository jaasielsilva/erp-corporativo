package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Mensagem;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
        
        return MensagemDTO.builder()
                .id(mensagem.getId())
                .remetenteId(mensagem.getRemetente().getId())
                .remetenteNome(mensagem.getRemetente().getNome())
                .remetenteFoto(mensagem.getRemetente().getFotoPerfil() != null ? 
                              "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(mensagem.getRemetente().getFotoPerfil()) : 
                              null)
                .destinatarioId(mensagem.getDestinatario().getId())
                .destinatarioNome(mensagem.getDestinatario().getNome())
                .conversaId(mensagem.getConversa().getId())
                .conteudo(mensagem.getConteudo())
                .dataEnvio(mensagem.getDataEnvio())
                .dataEnvioFormatada(mensagem.getDataEnvio().format(formatter))
                .lida(mensagem.isLida())
                .dataLeitura(mensagem.getDataLeitura())
                .isMinhaMensagem(mensagem.getRemetente().getId().equals(usuarioLogadoId))
                .build();
    }
    
    // Sobrecarga do método fromEntity sem usuarioLogadoId
    public static MensagemDTO fromEntity(Mensagem mensagem) {
        return fromEntity(mensagem, null);
    }
}