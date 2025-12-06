package com.jaasielsilva.portalceo.rh;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import com.jaasielsilva.portalceo.service.DocumentoAdesaoService;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService.DocumentoInfo;
import com.jaasielsilva.portalceo.service.AdesaoColaboradorService;
import com.jaasielsilva.portalceo.dto.AdesaoColaboradorDTO;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdesaoColaboradorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    DocumentoAdesaoService documentoAdesaoService;

    @MockBean
    AdesaoColaboradorService adesaoService;

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("Dados Pessoais: deve retornar 400 para nome inválido")
    void dadosPessoaisNomeInvalido() throws Exception {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("nome", "Teste Colaborador 2");
        payload.put("cpf", "529.982.247-25");
        payload.put("email", "negativo@example.com");
        payload.put("telefone", "(11) 98765-4321");
        payload.put("sexo", "MASCULINO");
        payload.put("dataNascimento", "1990-05-10");
        payload.put("estadoCivil", "SOLTEIRO");
        payload.put("rg", "12345678");
        payload.put("orgaoEmissorRg", "SSP-SP");
        payload.put("dataEmissaoRg", "2010-01-01");
        payload.put("dataAdmissao", "2025-12-01");
        payload.put("cargoId", 1);
        payload.put("departamentoId", 1);
        payload.put("salario", 3500.00);
        payload.put("tipoContrato", "CLT");
        payload.put("cargaHoraria", 40);
        payload.put("cep", "12345-678");
        payload.put("logradouro", "Rua Teste");
        payload.put("numero", "100");
        payload.put("complemento", "Apto 10");
        payload.put("bairro", "Centro");
        payload.put("cidade", "Sao Paulo");
        payload.put("estado", "SP");

        mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.nome[0]").exists());
    }

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("Fluxo feliz até benefícios e revisão")
    void fluxoBasicoBeneficiosRevisao() throws Exception {
        Map<String, Object> validPayload = new java.util.HashMap<>();
        validPayload.put("nome", "Teste Colaborador");
        validPayload.put("cpf", "390.533.447-05");
        validPayload.put("email", "ok@example.com");
        validPayload.put("telefone", "(11) 98765-4321");
        validPayload.put("sexo", "MASCULINO");
        validPayload.put("dataNascimento", "1990-05-10");
        validPayload.put("estadoCivil", "SOLTEIRO");
        validPayload.put("rg", "12345678");
        validPayload.put("orgaoEmissorRg", "SSP-SP");
        validPayload.put("dataEmissaoRg", "2010-01-01");
        validPayload.put("dataAdmissao", "2025-12-01");
        validPayload.put("cargoId", 1);
        validPayload.put("departamentoId", 1);
        validPayload.put("salario", 3500.00);
        validPayload.put("tipoContrato", "CLT");
        validPayload.put("cargaHoraria", 40);
        validPayload.put("cep", "12345-678");
        validPayload.put("logradouro", "Rua Teste");
        validPayload.put("numero", "100");
        validPayload.put("complemento", "Apto 10");
        validPayload.put("bairro", "Centro");
        validPayload.put("cidade", "Sao Paulo");
        validPayload.put("estado", "SP");

        // Mock de unicidade e sessão
        when(adesaoService.existeCpf("390.533.447-05")).thenReturn(false);
        when(adesaoService.existeEmail("ok@example.com")).thenReturn(false);
        when(adesaoService.salvarDadosTemporarios(org.mockito.ArgumentMatchers.any(AdesaoColaboradorDTO.class)))
                .thenReturn("S1");

        String sessionId = mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.proximaEtapa").value("documentos"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<?,?> resp = objectMapper.readValue(sessionId, Map.class);
        String sid = (String) resp.get("sessionId");

        when(documentoAdesaoService.verificarDocumentosObrigatorios(anyString())).thenReturn(true);
        
        DocumentoInfo docRg = new DocumentoInfo();
        docRg.setTipo("RG");
        docRg.setTipoInterno("documentoRg");
        docRg.setNomeArquivo("rg.png");
        docRg.setNomeArquivoSalvo("rg.png");
        docRg.setCaminhoArquivo("/tmp/rg.png");
        docRg.setTamanho("1.0 KB");
        docRg.setTamanhoBytes(1024);
        docRg.setContentType("image/png");
        docRg.setObrigatorio(true);
        docRg.setDataUpload(java.time.LocalDateTime.now());

        DocumentoInfo docCpf = new DocumentoInfo();
        docCpf.setTipo("CPF");
        docCpf.setTipoInterno("documentoCpf");
        docCpf.setNomeArquivo("cpf.jpg");
        docCpf.setNomeArquivoSalvo("cpf.jpg");
        docCpf.setCaminhoArquivo("/tmp/cpf.jpg");
        docCpf.setTamanho("2.0 KB");
        docCpf.setTamanhoBytes(2048);
        docCpf.setContentType("image/jpeg");
        docCpf.setObrigatorio(true);
        docCpf.setDataUpload(java.time.LocalDateTime.now());

        DocumentoInfo docEnd = new DocumentoInfo();
        docEnd.setTipo("Comprovante de Endereço");
        docEnd.setTipoInterno("comprovanteEndereco");
        docEnd.setNomeArquivo("end.png");
        docEnd.setNomeArquivoSalvo("end.png");
        docEnd.setCaminhoArquivo("/tmp/end.png");
        docEnd.setTamanho("4.0 KB");
        docEnd.setTamanhoBytes(4096);
        docEnd.setContentType("image/png");
        docEnd.setObrigatorio(true);
        docEnd.setDataUpload(java.time.LocalDateTime.now());

        when(documentoAdesaoService.listarDocumentos(anyString())).thenReturn(List.of(docRg, docCpf, docEnd));

        // Mock para revisão
        when(adesaoService.existeSessao(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        AdesaoColaboradorDTO dto = new AdesaoColaboradorDTO();
        dto.setNome("Teste Colaborador");
        dto.setCpf("390.533.447-05");
        dto.setEmail("ok@example.com");
        dto.setTelefone("(11) 98765-4321");
        dto.setSexo("MASCULINO");
        dto.setDataNascimento(java.time.LocalDate.parse("1990-05-10"));
        dto.setEstadoCivil("SOLTEIRO");
        dto.setRg("12345678");
        dto.setOrgaoEmissorRg("SSP-SP");
        dto.setDataEmissaoRg(java.time.LocalDate.parse("2010-01-01"));
        dto.setDataAdmissao(java.time.LocalDate.parse("2025-12-01"));
        dto.setCargoId(1L);
        dto.setDepartamentoId(1L);
        dto.setSalario(java.math.BigDecimal.valueOf(3500.00));
        dto.setTipoContrato("CLT");
        dto.setCargaHoraria(40);
        dto.setCep("12345-678");
        dto.setLogradouro("Rua Teste");
        dto.setNumero("100");
        dto.setComplemento("Apto 10");
        dto.setBairro("Centro");
        dto.setCidade("Sao Paulo");
        dto.setEstado("SP");
        when(adesaoService.obterDadosCompletos(org.mockito.ArgumentMatchers.anyString())).thenReturn(dto);
        when(adesaoService.obterBeneficiosSessao(org.mockito.ArgumentMatchers.anyString())).thenReturn(java.util.Collections.emptyMap());

        mockMvc.perform(post("/rh/colaboradores/adesao/documentos")
                        .param("sessionId", sid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.proximaEtapa").value("beneficios"));

        Map<String, Object> beneficios = Map.of(
                "sessionId", sid,
                "beneficios", Map.of(
                        "VALE_REFEICAO", Map.of("valor", "VR_300"),
                        "VALE_TRANSPORTE", Map.of("valor", "VT_200")
                )
        );

        mockMvc.perform(post("/rh/colaboradores/adesao/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beneficios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.proximaEtapa").value("revisao"));

        mockMvc.perform(get("/rh/colaboradores/adesao/revisao/" + sid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
