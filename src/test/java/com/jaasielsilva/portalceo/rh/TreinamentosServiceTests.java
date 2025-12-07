package com.jaasielsilva.portalceo.rh;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoCurso;
import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoMatricula;
import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoTurma;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.rh.treinamentos.TreinamentoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
public class TreinamentosServiceTests {
    @Autowired
    private TreinamentoService service;
    @Autowired
    private ColaboradorRepository colabRepo;

    @Test
    void criarFluxoBasico() {
        TreinamentoCurso c = service.criarCurso("LGPD", "Treinamento LGPD", "Compliance", 8);
        Assertions.assertNotNull(c.getId());
        TreinamentoTurma t = service.criarTurma(c.getId(), null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                "Sala 1", 2);
        Assertions.assertNotNull(t.getId());
        Colaborador col = new Colaborador();
        col.setNome("Participante");
        String[] cpfs = new String[]{"529.982.247-25","111.444.777-35"};
        String chosen = cpfs[0];
        if (colabRepo.existsByCpf(chosen)) { chosen = cpfs[1]; }
        if (colabRepo.existsByCpf(chosen)) { chosen = "935.411.347-80"; }
        col.setCpf(chosen);
        col.setEmail("p" + System.currentTimeMillis() + "@example.com");
        col.setDataAdmissao(LocalDate.now());
        col = colabRepo.save(col);
        TreinamentoMatricula m = service.matricular(t.getId(), col.getId());
        Assertions.assertNotNull(m.getId());
    }
}
