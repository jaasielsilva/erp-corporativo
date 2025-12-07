package com.jaasielsilva.portalceo.rh;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RhAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void auditoriaList_admin_ok() throws Exception {
        mockMvc.perform(get("/api/rh/auditoria/logs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"MASTER"})
    void auditoriaList_master_ok() throws Exception {
        mockMvc.perform(get("/api/rh/auditoria/logs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"RH_GERENTE"})
    void auditoriaList_rhGerente_ok() throws Exception {
        mockMvc.perform(get("/api/rh/auditoria/logs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USUARIO"})
    void auditoriaList_usuario_forbidden() throws Exception {
        mockMvc.perform(get("/api/rh/auditoria/logs")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void auditoriaCreate_admin_ok() throws Exception {
        mockMvc.perform(post("/api/rh/auditoria/logs")
                .param("categoria", "ACESSO")
                .param("acao", "TESTE"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"RH_GERENTE"})
    void auditoriaCreate_rhGerente_forbidden() throws Exception {
        mockMvc.perform(post("/api/rh/auditoria/logs")
                .param("categoria", "ACESSO")
                .param("acao", "TESTE"))
                .andExpect(status().isForbidden());
    }
}

