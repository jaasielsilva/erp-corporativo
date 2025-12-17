package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.PlanoSaude;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PlanoSaudeRepositoryTest {

    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;

    private PlanoSaude novoPlanoMinimoValido(String nome, String operadora, String codigo, PlanoSaude.TipoPlano tipo) {
        PlanoSaude plano = new PlanoSaude();
        plano.setNome(nome);
        plano.setOperadora(operadora);
        plano.setCodigo(codigo);
        plano.setTipo(tipo);
        plano.setValorTitular(BigDecimal.valueOf(350));
        plano.setValorDependente(BigDecimal.valueOf(200));
        // percentualEmpresa/percentualColaborador e ativo são preenchidos em @PrePersist
        return plano;
    }

    @Test
    @DisplayName("Salvar e recuperar PlanoSaude mínimo válido")
    void salvarRecuperarPlanoMinimoValido() {
        PlanoSaude plano = novoPlanoMinimoValido(
                "Plano de Saúde - Amil",
                "Amil",
                "basico",
                PlanoSaude.TipoPlano.BASICO
        );

        PlanoSaude salvo = planoSaudeRepository.save(plano);

        assertNotNull(salvo.getId(), "ID deve ser gerado ao salvar");
        assertEquals("Plano de Saúde - Amil", salvo.getNome());
        assertEquals("Amil", salvo.getOperadora());
        assertEquals(PlanoSaude.TipoPlano.BASICO, salvo.getTipo());
        assertEquals(BigDecimal.valueOf(350), salvo.getValorTitular());
        assertEquals(BigDecimal.valueOf(200), salvo.getValorDependente());

        // Verificar defaults aplicados pelo @PrePersist
        assertTrue(Boolean.TRUE.equals(salvo.getAtivo()), "Plano deve ficar ativo por padrão");
        assertNotNull(salvo.getPercentualEmpresa(), "Percentual empresa deve ser definido");
        assertNotNull(salvo.getPercentualColaborador(), "Percentual colaborador deve ser definido");

        // Consultas do repository
        assertTrue(planoSaudeRepository.findByNome("Plano de Saúde - Amil").isPresent(), "findByNome deve localizar");
        assertTrue(planoSaudeRepository.existsByCodigo("basico"), "existsByCodigo deve retornar true para código salvo");
    }
}