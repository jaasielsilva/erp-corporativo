package com.jaasielsilva.portalceo.controller.servicos;

import com.jaasielsilva.portalceo.dto.servicos.AprovacaoDTO;
import com.jaasielsilva.portalceo.dto.servicos.ServicoDTO;
import com.jaasielsilva.portalceo.dto.servicos.SolicitacaoServicoDTO;
import com.jaasielsilva.portalceo.model.servicos.Prioridade;
import com.jaasielsilva.portalceo.model.servicos.StatusSolicitacao;
import com.jaasielsilva.portalceo.service.servicos.AprovacaoService;
import com.jaasielsilva.portalceo.service.servicos.CatalogoService;
import com.jaasielsilva.portalceo.service.servicos.SolicitacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ServicosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServicosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogoService catalogoService;
    @MockBean
    private SolicitacaoService solicitacaoService;
    @MockBean
    private AprovacaoService aprovacaoService;

    @Test
    void deveRenderizarCatalogoEmServicosRaiz() throws Exception {
        when(catalogoService.listarCatalogo()).thenReturn(List.of(
                new ServicoDTO(1L, "Serviço A", "Desc A", "Infra", 4, 24, new BigDecimal("150"))));
        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos/catalogo"))
                .andExpect(model().attributeExists("servicos"));
    }

    @Test
    void deveRenderizarCatalogo() throws Exception {
        when(catalogoService.listarCatalogo()).thenReturn(List.of(
                new ServicoDTO(2L, "Serviço B", "Desc B", "Apps", 8, 48, new BigDecimal("250"))));
        mockMvc.perform(get("/servicos/catalogo"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos/catalogo"))
                .andExpect(model().attributeExists("servicos"));
    }

    @Test
    void deveRenderizarSolicitacoes() throws Exception {
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.CRIADA)).thenReturn(List.of(
                new SolicitacaoServicoDTO(10L, 1L, "Serviço A", "Acesso VPN", Prioridade.MEDIA,
                        StatusSolicitacao.CRIADA, LocalDateTime.now(), LocalDateTime.now())));
        mockMvc.perform(get("/servicos/solicitacoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos/solicitacoes"))
                .andExpect(model().attributeExists("solicitacoes"));
    }

    @Test
    void deveRenderizarAprovacoes() throws Exception {
        when(aprovacaoService.listarPendentes()).thenReturn(List.of(
                new AprovacaoDTO(20L, 10L, "Atualização", "Serviço B", "Gestor Demo",
                        "Aguardando janela", com.jaasielsilva.portalceo.model.servicos.AprovacaoSolicitacao.StatusAprovacao.EM_APROVACAO)));
        mockMvc.perform(get("/servicos/aprovacoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos/aprovacoes"))
                .andExpect(model().attributeExists("aprovacoes"));
    }

    @Test
    void deveRenderizarAvaliacoes() throws Exception {
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.CONCLUIDA)).thenReturn(List.of());
        mockMvc.perform(get("/servicos/avaliacoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos/avaliacoes"))
                .andExpect(model().attributeExists("concluidas"));
    }

    @Test
    void deveRenderizarRelatorios() throws Exception {
        when(catalogoService.listarCatalogo()).thenReturn(List.of());
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.CRIADA)).thenReturn(List.of());
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.CONCLUIDA)).thenReturn(List.of());
        when(aprovacaoService.listarPendentes()).thenReturn(List.of());
        mockMvc.perform(get("/servicos/relatorios"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos/relatorios"))
                .andExpect(model().attributeExists("catalogoCount"))
                .andExpect(model().attributeExists("solicitacoesCriadas"))
                .andExpect(model().attributeExists("solicitacoesConcluidas"))
                .andExpect(model().attributeExists("aprovacoesPendentes"));
    }

    @Test
    void deveRenderizarAdminCatalogo() throws Exception {
        when(catalogoService.listarCatalogo()).thenReturn(List.of());
        mockMvc.perform(get("/servicos/admin/catalogo"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos/admin/catalogo"))
                .andExpect(model().attributeExists("servicos"));
    }
}