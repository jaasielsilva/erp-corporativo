package com.jaasielsilva.portalceo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RHSecurityControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"RH"})
    void feriasAprovar_deveNegarParaRH() throws Exception {
        mockMvc.perform(get("/rh/ferias/aprovar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"GERENCIAL"})
    void feriasAprovar_devePermitirParaGerencial() throws Exception {
        mockMvc.perform(get("/rh/ferias/aprovar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"RH"})
    void avaliacaoRelatorios_devePermitirParaRH() throws Exception {
        mockMvc.perform(get("/rh/avaliacao/relatorios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void avaliacaoRelatorios_deveNegarParaUser() throws Exception {
        mockMvc.perform(get("/rh/avaliacao/relatorios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void recomendacoes_devePermitirParaAdmin() throws Exception {
        mockMvc.perform(get("/recomendacoes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void recomendados_antigaRotaDeveRetornar404() throws Exception {
        mockMvc.perform(get("/recomendados"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processoDetalhes_semAutenticacaoRedirecionaLogin() throws Exception {
        mockMvc.perform(get("/rh/processo-detalhes"))
                .andExpect(status().isFound());
    }
}
