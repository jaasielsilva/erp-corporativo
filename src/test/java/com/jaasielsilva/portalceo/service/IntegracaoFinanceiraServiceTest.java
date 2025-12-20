package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.financeiro.IntegracaoFolhaDTO;
import com.jaasielsilva.portalceo.model.FolhaPagamento;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.AuditoriaRhLogRepository;
import com.jaasielsilva.portalceo.repository.ContaPagarRepository;
import com.jaasielsilva.portalceo.repository.FolhaPagamentoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IntegracaoFinanceiraServiceTest {

    @InjectMocks
    private IntegracaoFinanceiraService service;

    @Mock
    private FolhaPagamentoRepository folhaRepository;

    @Mock
    private ContaPagarRepository contaPagarRepository;

    @Mock
    private AuditoriaRhLogRepository auditoriaRepository;

    @Test
    public void deveIntegrarFolhaComSucesso() throws Exception {
        // Arrange
        FolhaPagamento folha = new FolhaPagamento();
        folha.setId(1L);
        folha.setStatus(FolhaPagamento.StatusFolha.PROCESSADA);
        folha.setTotalBruto(new BigDecimal("1000.00"));
        folha.setTotalDescontos(new BigDecimal("100.00"));
        folha.setTotalLiquido(new BigDecimal("900.00"));
        folha.setMesReferencia(12);
        folha.setAnoReferencia(2025);
        folha.setDataProcessamento(LocalDate.now());

        Usuario usuario = new Usuario();
        usuario.setEmail("teste@empresa.com");

        when(folhaRepository.findById(1L)).thenReturn(Optional.of(folha));

        // Act
        IntegracaoFolhaDTO result = service.enviarFolhaParaFinanceiro(1L, usuario);

        // Assert
        Assertions.assertNotNull(result.getHashIntegridade());
        Assertions.assertEquals(new BigDecimal("900.00"), result.getValorTotal());
        
        verify(contaPagarRepository, times(1)).save(any());
        verify(folhaRepository, times(1)).save(folha);
        verify(auditoriaRepository, times(1)).save(any());
        Assertions.assertEquals(FolhaPagamento.StatusFolha.ENVIADA_FINANCEIRO, folha.getStatus());
    }

    @Test
    public void deveFalharComDivergenciaDeValores() {
        // Arrange
        FolhaPagamento folha = new FolhaPagamento();
        folha.setId(1L);
        folha.setStatus(FolhaPagamento.StatusFolha.PROCESSADA);
        folha.setTotalBruto(new BigDecimal("1000.00"));
        folha.setTotalDescontos(new BigDecimal("100.00"));
        folha.setTotalLiquido(new BigDecimal("899.99")); // Errado!

        when(folhaRepository.findById(1L)).thenReturn(Optional.of(folha));

        // Act & Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            service.enviarFolhaParaFinanceiro(1L, new Usuario());
        });
        
        verify(contaPagarRepository, never()).save(any());
    }
}
