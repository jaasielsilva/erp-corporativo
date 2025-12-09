package com.jaasielsilva.portalceo.rh;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoCandidato;
import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoCandidatura;
import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoVaga;
import com.jaasielsilva.portalceo.service.rh.recrutamento.RecrutamentoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
public class RecrutamentoServiceTests {
    @Autowired private RecrutamentoService service;

    @Test
    void fluxoBasico() {
        RecrutamentoCandidato c = service.criarCandidato("Candidato Teste","candidato@example.com","(11)99999-9999","Masculino", LocalDate.of(1990,1,1));
        Assertions.assertNotNull(c.getId());
        RecrutamentoVaga v = service.criarVaga(
                "Dev Java","Back-end","TI","Pleno","Remoto","CLT",
                "Desenvolver APIs","Spring Boot","Docker diferencial","Plano de saúde"
        );
        Assertions.assertNotNull(v.getId());
        RecrutamentoCandidatura cad = service.candidatar(c.getId(), v.getId(), "Interno");
        Assertions.assertEquals("TRIAGEM", cad.getEtapa());
        var ent = service.agendarEntrevista(cad.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), "Google Meet","Remoto");
        Assertions.assertNotNull(ent.getId());
        var alt = service.alterarEtapa(cad.getId(), "ENTREVISTA");
        Assertions.assertEquals("ENTREVISTA", alt.getEtapa());
    }
}
