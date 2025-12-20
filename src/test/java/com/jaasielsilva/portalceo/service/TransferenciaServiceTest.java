package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.financeiro.TransferenciaDTO;
import com.jaasielsilva.portalceo.model.ContaBancaria;
import com.jaasielsilva.portalceo.model.FluxoCaixa;
import com.jaasielsilva.portalceo.model.Transferencia;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.ContaBancariaRepository;
import com.jaasielsilva.portalceo.repository.FluxoCaixaRepository;
import com.jaasielsilva.portalceo.repository.TransferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private ContaBancariaRepository contaBancariaRepository;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private FluxoCaixaRepository fluxoCaixaRepository;

    @InjectMocks
    private TransferenciaService transferenciaService;

    private ContaBancaria contaOrigem;
    private ContaBancaria contaDestino;
    private Usuario usuario;
    private TransferenciaDTO dto;

    @BeforeEach
    void setUp() {
        contaOrigem = new ContaBancaria();
        contaOrigem.setId(1L);
        contaOrigem.setNome("Banco A");
        contaOrigem.setSaldo(new BigDecimal("1000.00"));

        contaDestino = new ContaBancaria();
        contaDestino.setId(2L);
        contaDestino.setNome("Banco B");
        contaDestino.setSaldo(new BigDecimal("500.00"));

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Admin");

        dto = new TransferenciaDTO();
        dto.setContaOrigemId(1L);
        dto.setContaDestinoId(2L);
        dto.setValor(new BigDecimal("200.00"));
        dto.setDataTransferencia(LocalDate.now());
        dto.setDescricao("Transferência Teste");
    }

    @Test
    void realizarTransferencia_Sucesso() {
        when(contaBancariaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findById(2L)).thenReturn(Optional.of(contaDestino));
        when(transferenciaRepository.save(any(Transferencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferenciaService.realizarTransferencia(dto, usuario);

        // Verifica saldos atualizados
        assertEquals(new BigDecimal("800.00"), contaOrigem.getSaldo());
        assertEquals(new BigDecimal("700.00"), contaDestino.getSaldo());

        // Verifica chamadas aos repositórios
        verify(contaBancariaRepository, times(2)).save(any(ContaBancaria.class));
        verify(transferenciaRepository).save(any(Transferencia.class));
        verify(fluxoCaixaRepository, times(2)).save(any(FluxoCaixa.class)); // 1 Saída + 1 Entrada
    }

    @Test
    void realizarTransferencia_SaldoInsuficiente() {
        dto.setValor(new BigDecimal("2000.00")); // Valor maior que saldo (1000)

        when(contaBancariaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
        when(contaBancariaRepository.findById(2L)).thenReturn(Optional.of(contaDestino));

        assertThrows(IllegalArgumentException.class, () -> {
            transferenciaService.realizarTransferencia(dto, usuario);
        });

        // Garante que nada foi salvo
        verify(contaBancariaRepository, never()).save(any(ContaBancaria.class));
        verify(transferenciaRepository, never()).save(any(Transferencia.class));
    }

    @Test
    void realizarTransferencia_MesmaConta() {
        dto.setContaDestinoId(1L); // Origem = Destino

        when(contaBancariaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
        // Nota: O serviço chama findById duas vezes, uma para origem e outra para destino.
        // Se passarmos o mesmo ID, o mock retorna a mesma conta.

        assertThrows(IllegalArgumentException.class, () -> {
            transferenciaService.realizarTransferencia(dto, usuario);
        });
        
        verify(transferenciaRepository, never()).save(any(Transferencia.class));
    }
}
