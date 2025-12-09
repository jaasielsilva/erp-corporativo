package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.service.RhRelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RhRelatoriosApiController.class)
class RhRelatoriosApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RhRelatorioService service;

    @Test
    @WithMockUser(roles = {"RH"})
    void turnoverEndpointReturnsOk() throws Exception {
        var rel = new RhRelatorioService.RelatorioTurnover(
                LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"),
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        given(service.gerarRelatorioTurnover(any(), any())).willReturn(rel);
        mockMvc.perform(get("/api/rh/relatorios/turnover")
                        .param("inicio", "2025-01-01")
                        .param("fim", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("turnoverPorMes")));
    }

    @Test
    @WithMockUser(roles = {"RH"})
    void turnoverDetalhadoEndpointReturnsOk() throws Exception {
        var rel = new RhRelatorioService.RelatorioTurnoverDetalhado(
                LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"),
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), 5.0);
        given(service.gerarRelatorioTurnoverDetalhado(any(), any(), any(), any(), any(), any(), any(Boolean.class))).willReturn(rel);
        mockMvc.perform(get("/api/rh/relatorios/turnover/detalhado")
                        .param("inicio", "2025-01-01")
                        .param("fim", "2025-12-31")
                        .param("departamento", "TI")
                        .param("cargo", "Desenvolvedor")
                        .param("tipoMovimento", "VOLUNTARIO")
                        .param("meta", "5.0")
                        .param("comparar", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("metaTurnover")));
    }

    @Test
    @WithMockUser(roles = {"RH"})
    void admissoesDemissoesEndpointReturnsOk() throws Exception {
        var rel = new RhRelatorioService.RelatorioAdmissoesDemissoes(
                LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"), 0,0,0.0,
                java.util.List.of(), java.util.List.of());
        given(service.gerarRelatorioAdmissoesDemissoes(any(), any())).willReturn(rel);
        mockMvc.perform(get("/api/rh/relatorios/admissoes-demissoes")
                        .param("inicio", "2025-01-01")
                        .param("fim", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("totalAdmissoes")));
    }

    @Test
    @WithMockUser(roles = {"RH"})
    void feriasBeneficiosEndpointReturnsOk() throws Exception {
        var beneficios = new RhRelatorioService.RelatorioBeneficios(LocalDate.now(), LocalDate.now(), new LinkedHashMap<>(), java.math.BigDecimal.ZERO);
        var rel = new RhRelatorioService.RelatorioFeriasBeneficios(LocalDate.now(), LocalDate.now(), 0,0,0, beneficios);
        given(service.gerarRelatorioFeriasBeneficios(any(), any())).willReturn(rel);
        mockMvc.perform(get("/api/rh/relatorios/ferias-beneficios")
                        .param("inicio", "2025-01-01")
                        .param("fim", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("beneficios")));
    }

    @Test
    @WithMockUser(roles = {"RH"})
    void orcamentoFeriasBeneficiosEndpointReturnsOk() throws Exception {
        var rel = new RhRelatorioService.OrcamentoFeriasBeneficios(LocalDate.now(), LocalDate.now(), new LinkedHashMap<>(), new java.util.LinkedHashMap<>(), new LinkedHashMap<>(), new java.util.LinkedHashMap<>(), new LinkedHashMap<>());
        given(service.gerarOrcamentoFeriasBeneficios(any(), any())).willReturn(rel);
        mockMvc.perform(get("/api/rh/relatorios/ferias-beneficios/orcamento")
                        .param("inicio", "2025-01-01")
                        .param("fim", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("feriasPorTrimestre")));
    }

    @Test
    @WithMockUser(roles = {"RH"})
    void indicadoresEndpointReturnsOk() throws Exception {
        var rel = new RhRelatorioService.RelatorioIndicadoresDesempenho(LocalDate.now(), LocalDate.now(), 0,0,0, 0.0);
        given(service.gerarRelatorioIndicadores(any(), any())).willReturn(rel);
        mockMvc.perform(get("/api/rh/relatorios/indicadores")
                        .param("inicio", "2025-01-01")
                        .param("fim", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("mediaNotas")));
    }

    @Test
    @WithMockUser(roles = {"RH"})
    void indicadoresSerieEndpointReturnsOk() throws Exception {
        var serie = new LinkedHashMap<String, RhRelatorioService.IndicadorMensal>();
        var rel = new RhRelatorioService.SerieIndicadores12Meses(LocalDate.now().minusMonths(11), LocalDate.now(), serie, "KPIs por mês (12M)", "AvaliacoesDesempenho");
        given(service.gerarSerieIndicadores12Meses(any())).willReturn(rel);
        mockMvc.perform(get("/api/rh/relatorios/indicadores/serie-12m")
                        .param("fim", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("metodologia")));
    }
}
