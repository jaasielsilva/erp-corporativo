package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.PortalCeoApplication;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PortalCeoApplication.class)
@AutoConfigureMockMvc
public class PdfGenerationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteService clienteService;

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER"})
    void deveGerarPdfProcuracaoComSucesso() throws Exception {
        // Create a temporary client for the test
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste PDF");
        cliente.setTipoCliente("PF");
        cliente.setCpfCnpj("123.456.789-00");
        cliente.setEmail("teste@cliente.com");
        cliente.setStatus("Ativo");
        
        // Save client to get an ID
        Cliente salvo = clienteService.salvar(cliente);

        try {
            // Perform the request
            MvcResult result = mockMvc.perform(get("/juridico/api/templates/procuracao-ad-judicia/pdf")
                            .param("clienteId", salvo.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andReturn();

            // Save PDF to file for manual verification
            byte[] pdfBytes = result.getResponse().getContentAsByteArray();
            File outputFile = new File("teste_procuracao.pdf");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(pdfBytes);
            }
            
            System.out.println("PDF gerado com sucesso em: " + outputFile.getAbsolutePath());
            System.out.println("Tamanho do PDF: " + pdfBytes.length + " bytes");

        } finally {
            // Cleanup
            clienteService.excluir(salvo.getId());
        }
    }
}
