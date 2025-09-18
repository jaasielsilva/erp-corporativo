package com.jaasielsilva.portalceo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
public class ChamadoServiceTest {

    @Autowired
    private ChamadoService chamadoService;

    @Test
    public void testCalcularSlaMedio() {
        try {
            Double slaMedio = chamadoService.calcularSlaMedio();
            System.out.println("SLA Médio: " + slaMedio);
            assert slaMedio != null;
        } catch (Exception e) {
            System.err.println("Erro ao calcular SLA médio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalcularTempoMedioResolucao() {
        try {
            Double tempoMedio = chamadoService.calcularTempoMedioResolucaoGeral();
            System.out.println("Tempo Médio de Resolução: " + tempoMedio);
            assert tempoMedio != null;
        } catch (Exception e) {
            System.err.println("Erro ao calcular tempo médio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalcularMetricasSLA() {
        try {
            Map<String, Object> metricas = chamadoService.calcularMetricasSLA();
            System.out.println("Métricas SLA: " + metricas);
            assert metricas != null;
        } catch (Exception e) {
            System.err.println("Erro ao calcular métricas SLA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}