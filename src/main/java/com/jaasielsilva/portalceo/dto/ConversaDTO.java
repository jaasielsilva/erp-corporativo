package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.Conversa;
import com.jaasielsilva.portalceo.model.Usuario;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversaDTO {
    
    private Long id;
    private Long outroUsuarioId;
    private String outroUsuarioNome;
    private String outroUsuarioFoto;
    private boolean outroUsuarioOnline;
    private String ultimaMensagem;
    private LocalDateTime ultimaAtividade;
    private String ultimaAtividadeFormatada;
    private long mensagensNaoLidas;
    private boolean ativa;
    
    // Construtor a partir da entidade Conversa
    public static ConversaDTO fromEntity(Conversa conversa, Usuario usuarioLogado) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Usuario outroUsuario = conversa.getOutroParticipante(usuarioLogado);
        
        String ultimaMensagemTexto = "";
        if (conversa.getUltimaMensagem() != null) {
            String conteudo = conversa.getUltimaMensagem().getConteudo();
            ultimaMensagemTexto = conteudo.length() > 50 ? 
                                 conteudo.substring(0, 50) + "..." : 
                                 conteudo;
        }
        
        return ConversaDTO.builder()
                .id(conversa.getId())
                .outroUsuarioId(outroUsuario.getId())
                .outroUsuarioNome(outroUsuario.getNome())
                .outroUsuarioFoto(outroUsuario.getFotoPerfil() != null ? 
                                 "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(outroUsuario.getFotoPerfil()) : 
                                 null)
                .outroUsuarioOnline(outroUsuario.isOnline())
                .ultimaMensagem(ultimaMensagemTexto)
                .ultimaAtividade(conversa.getUltimaAtividade())
                .ultimaAtividadeFormatada(conversa.getUltimaAtividade() != null ? 
                                         conversa.getUltimaAtividade().format(formatter) : 
                                         "")
                .mensagensNaoLidas(conversa.contarMensagensNaoLidas(usuarioLogado))
                .ativa(conversa.isAtiva())
                .build();
    }
}