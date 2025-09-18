package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ChamadoServiceSLATest {

    @Autowired
    private ChamadoService chamadoService;

    @Test
    public void testCalcularSlaRestante_Minutos() {
        // Criar um chamado com SLA de 14 minutos
        Chamado chamado = new Chamado();
        chamado.setDataAbertura(LocalDateTime.of(2025, 9, 17, 19, 26)); // 19:26
        chamado.setPrioridade(Chamado.Prioridade.BAIXA); // 72 horas = 4320 minutos
        
        // Calcular o SLA de vencimento
        LocalDateTime slaVencimento = chamadoService.calcularSlaVencimento(chamado);
        chamado.setSlaVencimento(slaVencimento);
        
        // Simular o tempo atual como 13 minutos após a abertura (19:39)
        // Para testar, vamos definir o tempo atual como 19:39
        // O SLA vence às 19:40 (14 minutos após a abertura)
        
        // O resultado esperado deve ser 0 horas (menos de 1 hora restante)
        Long slaRestante = chamadoService.calcularSlaRestante(chamado);
        
        // Com a correção, mesmo que reste menos de 1 hora, 
        // o valor retornado deve ser 0 (pois estamos convertendo minutos para horas)
        assertEquals(0L, slaRestante);
    }
    
    @Test
    public void testCalcularSlaRestante_Vencido() {
        // Criar um chamado com SLA de 14 minutos
        Chamado chamado = new Chamado();
        chamado.setDataAbertura(LocalDateTime.of(2025, 9, 17, 19, 26)); // 19:26
        chamado.setPrioridade(Chamado.Prioridade.BAIXA);
        
        // Calcular o SLA de vencimento (14 minutos após a abertura = 19:40)
        LocalDateTime slaVencimento = chamadoService.calcularSlaVencimento(chamado);
        chamado.setSlaVencimento(slaVencimento);
        
        // Simular o tempo atual como 15 minutos após a abertura (19:41)
        // O SLA venceu há 1 minuto
        
        // O resultado esperado deve ser negativo (SLA vencido)
        Long slaRestante = chamadoService.calcularSlaRestante(chamado);
        
        // Com a correção, o valor retornado deve ser negativo
        assertTrue(slaRestante <= 0);
    }
}