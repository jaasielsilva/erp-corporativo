package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.NotificacaoChat;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoChatDTO {
    
    private Long id;
    private Long usuarioId;
    private Long remetenteId;
    private String remetenteNome;
    private String remetenteFoto;
    private Long mensagemId;
    private String titulo;
    private String conteudo;
    private boolean lida;
    private LocalDateTime dataCriacao;
    private String dataFormatada;
    private String tipo;
    private String prioridade;
    
    // Construtor a partir da entidade NotificacaoChat
    public static NotificacaoChatDTO fromEntity(NotificacaoChat notificacao) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        return NotificacaoChatDTO.builder()
                .id(notificacao.getId())
                .usuarioId(notificacao.getUsuario().getId())
                .remetenteId(notificacao.getRemetente().getId())
                .remetenteNome(notificacao.getRemetente().getNome())
                .remetenteFoto(notificacao.getRemetente().getFotoPerfil() != null ? 
                              "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(notificacao.getRemetente().getFotoPerfil()) : 
                              null)
                .mensagemId(notificacao.getMensagem().getId())
                .titulo(notificacao.getTitulo())
                .conteudo(notificacao.getConteudo())
                .lida(notificacao.isLida())
                .dataCriacao(notificacao.getDataCriacao())
                .dataFormatada(notificacao.getDataCriacao().format(formatter))
                .tipo(notificacao.getTipo().name())
                .prioridade(notificacao.getPrioridade().name())
                .build();
    }
}