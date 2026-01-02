package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.PortalCeoApplication;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.service.PerfilService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(classes = PortalCeoApplication.class)
@AutoConfigureMockMvc
public class PerfilControllerApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PerfilService perfilService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarPaginaPerfis() throws Exception {
        mockMvc.perform(get("/perfis"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarDadosPageApi() throws Exception {
        mockMvc.perform(get("/perfis/api/page").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("relatorio")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deveNegarDadosQuandoSemPermissaoGerenciar() throws Exception {
        mockMvc.perform(get("/perfis/api/page").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAbrirPaginaNovoPerfil() throws Exception {
        mockMvc.perform(get("/perfis/novo"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAbrirDetalhesPerfilCriado() throws Exception {
        Perfil p = new Perfil();
        p.setNome("TESTE_DETALHES_" + System.currentTimeMillis());
        Perfil salvo = perfilService.salvar(p);
        mockMvc.perform(get("/perfis/detalhes/" + salvo.getId()))
                .andExpect(status().isOk());
    }
}
