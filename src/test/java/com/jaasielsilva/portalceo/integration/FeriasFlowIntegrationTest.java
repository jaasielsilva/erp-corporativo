package com.jaasielsilva.portalceo.integration;

import com.jaasielsilva.portalceo.model.SolicitacaoFerias;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.SolicitacaoFeriasRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.service.rh.SolicitacaoFeriasService;
import com.jaasielsilva.portalceo.model.RhPoliticaFerias;
import com.jaasielsilva.portalceo.repository.RhPoliticaFeriasRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.model.Colaborador;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class FeriasFlowIntegrationTest {

    @Autowired
    private SolicitacaoFeriasService feriasService;

    @Autowired
    private SolicitacaoFeriasRepository feriasRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RhPoliticaFeriasRepository politicaRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Test
    void fluxoCompletoFeriasColaborador1() {
        Usuario master = usuarioRepository.findByEmail("master@sistema.com").orElseThrow();
        RhPoliticaFerias p = politicaRepository.findAll().stream().findFirst().orElseGet(() -> politicaRepository.save(new RhPoliticaFerias()));
        p.setDiasPorAno(30);
        p.setExigeAprovacaoGerente(Boolean.TRUE);
        politicaRepository.save(p);

        Colaborador col = colaboradorRepository.findById(1L).orElseThrow();
        java.util.List<com.jaasielsilva.portalceo.model.SolicitacaoFerias> historico = feriasRepository.findByColaboradorOrderByDataSolicitacaoDesc(col);
        LocalDate base = LocalDate.now().plusYears(5);
        if (!historico.isEmpty()) {
            LocalDate ultimoFim = historico.get(0).getPeriodoFim();
            base = ultimoFim.plusDays(30);
        }
        LocalDate inicio = base.withDayOfMonth(10);
        LocalDate fim = inicio.plusDays(5);

        SolicitacaoFerias s = feriasService.solicitar(1L, inicio, fim, "Teste automático", master, "127.0.0.1");
        Assertions.assertNotNull(s.getId());
        Assertions.assertEquals(1L, s.getColaborador().getId());
        Assertions.assertEquals(inicio, s.getPeriodoInicio());
        Assertions.assertEquals(fim, s.getPeriodoFim());

        if (s.getStatus() == SolicitacaoFerias.StatusSolicitacao.SOLICITADA) {
            s = feriasService.aprovar(s.getId(), master, "Aprovada em teste", "127.0.0.1");
        }

        SolicitacaoFerias persisted = feriasRepository.findById(s.getId()).orElseThrow();
        Assertions.assertEquals(SolicitacaoFerias.StatusSolicitacao.APROVADA, persisted.getStatus());
        Assertions.assertEquals(inicio, persisted.getPeriodoInicio());
        Assertions.assertEquals(fim, persisted.getPeriodoFim());
    }

    @Test
    void fluxoReprovarFeriasColaborador1() {
        Usuario master = usuarioRepository.findByEmail("master@sistema.com").orElseThrow();
        RhPoliticaFerias p = politicaRepository.findAll().stream().findFirst().orElseGet(() -> politicaRepository.save(new RhPoliticaFerias()));
        p.setDiasPorAno(30);
        p.setExigeAprovacaoGerente(Boolean.TRUE);
        politicaRepository.save(p);

        Colaborador col = colaboradorRepository.findById(1L).orElseThrow();
        java.util.List<com.jaasielsilva.portalceo.model.SolicitacaoFerias> historico = feriasRepository.findByColaboradorOrderByDataSolicitacaoDesc(col);
        LocalDate base = LocalDate.now().plusYears(6);
        if (!historico.isEmpty()) {
            base = historico.get(0).getPeriodoFim().plusDays(45);
        }
        LocalDate inicio = base.withDayOfMonth(8);
        LocalDate fim = inicio.plusDays(4);

        SolicitacaoFerias s = feriasService.solicitar(1L, inicio, fim, "Fluxo reprovação", master, "127.0.0.1");
        Assertions.assertEquals(SolicitacaoFerias.StatusSolicitacao.SOLICITADA, s.getStatus());

        s = feriasService.reprovar(s.getId(), master, "Reprovada em teste", "127.0.0.1");
        Assertions.assertEquals(SolicitacaoFerias.StatusSolicitacao.REPROVADA, s.getStatus());

        SolicitacaoFerias persisted = feriasRepository.findById(s.getId()).orElseThrow();
        Assertions.assertEquals(SolicitacaoFerias.StatusSolicitacao.REPROVADA, persisted.getStatus());
    }

    @Test
    void validacaoBlackoutBloqueiaSolicitacao() {
        RhPoliticaFerias p = politicaRepository.findAll().stream().findFirst().orElseGet(() -> {
            RhPoliticaFerias novo = new RhPoliticaFerias();
            novo.setDiasPorAno(30);
            novo.setExigeAprovacaoGerente(Boolean.TRUE);
            return politicaRepository.save(novo);
        });
        p.setPeriodosBlackout("12-15;12-25");
        politicaRepository.save(p);

        Usuario master = usuarioRepository.findByEmail("master@sistema.com").orElseThrow();
        LocalDate inicio = LocalDate.of(LocalDate.now().getYear(), 12, 20);
        LocalDate fim = inicio.plusDays(3);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                feriasService.solicitar(1L, inicio, fim, "Teste blackout", master, "127.0.0.1")
        );
    }

    @Test
    void aprovacaoAutomaticaQuandoPoliticaNaoExigeGerente() {
        RhPoliticaFerias p = politicaRepository.findAll().stream().findFirst().orElseGet(() -> politicaRepository.save(new RhPoliticaFerias()));
        p.setExigeAprovacaoGerente(Boolean.FALSE);
        p.setDiasPorAno(30);
        politicaRepository.save(p);

        Usuario master = usuarioRepository.findByEmail("master@sistema.com").orElseThrow();
        Colaborador col = colaboradorRepository.findById(1L).orElseThrow();
        java.util.List<com.jaasielsilva.portalceo.model.SolicitacaoFerias> historico = feriasRepository.findByColaboradorOrderByDataSolicitacaoDesc(col);
        LocalDate base = LocalDate.now().plusYears(7);
        if (!historico.isEmpty()) {
            base = historico.get(0).getPeriodoFim().plusDays(60);
        }
        LocalDate inicio = base.withDayOfMonth(10);
        LocalDate fim = inicio.plusDays(5);

        SolicitacaoFerias s = feriasService.solicitar(1L, inicio, fim, "Autoaprovação pela política", master, "127.0.0.1");
        Assertions.assertEquals(SolicitacaoFerias.StatusSolicitacao.APROVADA, s.getStatus());
    }

    @Test
    void limiteAnualDiasImpedeSolicitacaoAcimaDoPermitido() {
        RhPoliticaFerias p = politicaRepository.findAll().stream().findFirst().orElseGet(() -> politicaRepository.save(new RhPoliticaFerias()));
        p.setDiasPorAno(3);
        p.setExigeAprovacaoGerente(Boolean.TRUE);
        politicaRepository.save(p);

        Usuario master = usuarioRepository.findByEmail("master@sistema.com").orElseThrow();
        LocalDate inicio = LocalDate.now().plusMonths(9).withDayOfMonth(5);
        LocalDate fim = inicio.plusDays(5);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                feriasService.solicitar(1L, inicio, fim, "Acima do limite anual", master, "127.0.0.1")
        );
    }
}
