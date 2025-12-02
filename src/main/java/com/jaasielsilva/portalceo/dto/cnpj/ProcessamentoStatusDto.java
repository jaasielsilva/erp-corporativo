package com.jaasielsilva.portalceo.dto.cnpj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessamentoStatusDto {
    private boolean running;
    private int processed;
    private Long startedAt;
    private Long finishedAt;
    private Long durationMs;
    private int invalidCount;
    private int errorCount;
    private java.util.List<String> invalidSamples;
    private java.util.List<String> errorSamples;
}
