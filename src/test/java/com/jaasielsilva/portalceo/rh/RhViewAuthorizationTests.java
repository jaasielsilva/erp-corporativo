package com.jaasielsilva.portalceo.rh;

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
class RhViewAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void configuracoesIndex_admin_ok() throws Exception {
        mockMvc.perform(get("/rh/configuracoes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"RH_GERENTE"})
    void configuracoesIndex_rhGerente_ok() throws Exception {
        mockMvc.perform(get("/rh/configuracoes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USUARIO"})
    void configuracoesIndex_usuario_forbidden() throws Exception {
        mockMvc.perform(get("/rh/configuracoes")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void auditoriaIndex_admin_ok() throws Exception {
        mockMvc.perform(get("/rh/auditoria")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"RH_GERENTE"})
    void auditoriaExportacoes_rhGerente_ok() throws Exception {
        mockMvc.perform(get("/rh/auditoria/exportacoes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USUARIO"})
    void auditoriaIndex_usuario_forbidden() throws Exception {
        mockMvc.perform(get("/rh/auditoria")).andExpect(status().isForbidden());
    }
}

