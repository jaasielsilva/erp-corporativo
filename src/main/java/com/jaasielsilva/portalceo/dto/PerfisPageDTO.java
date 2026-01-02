package com.jaasielsilva.portalceo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfisPageDTO {
    private List<com.jaasielsilva.portalceo.model.Perfil> perfis;
    private PerfisRelatorioDTO relatorio;
}
