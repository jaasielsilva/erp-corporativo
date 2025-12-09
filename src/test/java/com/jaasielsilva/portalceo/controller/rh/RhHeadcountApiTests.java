package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.PortalCeoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(classes = PortalCeoApplication.class)
@AutoConfigureMockMvc
class RhHeadcountApiTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"RH","ADMIN"})
    void headcountJsonOk() throws Exception {
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusMonths(1).withDayOfMonth(1);
        mockMvc.perform(get("/api/rh/relatorios/headcount")
                        .param("inicio", inicio.toString())
                        .param("fim", fim.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @WithMockUser(roles = {"RH","ADMIN"})
    void headcountExcelOk() throws Exception {
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusMonths(1).withDayOfMonth(1);
        mockMvc.perform(get("/api/rh/relatorios/headcount/export")
                        .param("inicio", inicio.toString())
                        .param("fim", fim.toString())
                        .param("format", "excel"))
                .andExpect(status().isOk());
    }
}

