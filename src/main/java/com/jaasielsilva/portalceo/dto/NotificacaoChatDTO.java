package com.jaasielsilva.portalceo.dto;

import lombok.*;
import java.time.LocalDateTime;


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
}