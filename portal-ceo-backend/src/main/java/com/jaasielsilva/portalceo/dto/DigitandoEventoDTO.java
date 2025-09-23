package com.jaasielsilva.portalceo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitandoEventoDTO {
    
    private Long usuarioId;
    private String usuarioNome;
    private Long conversaId;
    private boolean digitando;
    private long timestamp;
    
    // Método estático para criar evento de "digitando"
    public static DigitandoEventoDTO criarEventoDigitando(Long usuarioId, String usuarioNome, Long conversaId) {
        return DigitandoEventoDTO.builder()
                .usuarioId(usuarioId)
                .usuarioNome(usuarioNome)
                .conversaId(conversaId)
                .digitando(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // Método estático para criar evento de "parou de digitar"
    public static DigitandoEventoDTO criarEventoParouDeDigitar(Long usuarioId, String usuarioNome, Long conversaId) {
        return DigitandoEventoDTO.builder()
                .usuarioId(usuarioId)
                .usuarioNome(usuarioNome)
                .conversaId(conversaId)
                .digitando(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}