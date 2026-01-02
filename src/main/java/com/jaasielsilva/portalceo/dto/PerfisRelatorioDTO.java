package com.jaasielsilva.portalceo.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PerfisRelatorioDTO {
    private Long totalPerfis;
    private Long totalUsuarios;
    private Map<String, Long> usuariosPorPerfil;
    private Map<String, Integer> permissoesPorPerfil;
}
