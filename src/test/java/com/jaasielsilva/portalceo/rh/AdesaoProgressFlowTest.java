package com.jaasielsilva.portalceo.rh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.portalceo.service.BeneficioAdesaoService;
import com.jaasielsilva.portalceo.service.AdesaoColaboradorService;
import com.jaasielsilva.portalceo.dto.AdesaoColaboradorDTO;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.service.AdesaoSecurityService;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdesaoProgressFlowTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeneficioAdesaoService beneficioAdesaoService;

    @MockBean
    AdesaoColaboradorService adesaoColaboradorService;

    @MockBean
    WorkflowAdesaoService workflowAdesaoService;

    @MockBean
    AdesaoSecurityService adesaoSecurityService;

    @MockBean
    DocumentoAdesaoService documentoAdesaoService;

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("ResumeFlow deve recuperar último estado e permitir retomada")
    void resumeFlowRecuperaEstado() throws Exception {
        org.mockito.Mockito.when(adesaoSecurityService.checkRateLimit(org.mockito.Mockito.anyString())).thenReturn(true);
        org.mockito.Mockito.when(adesaoSecurityService.validateDadosPessoais(org.mockito.Mockito.any(AdesaoColaboradorDTO.class)))
                .thenReturn(new AdesaoSecurityService.ValidationResult());
        org.mockito.Mockito.when(adesaoColaboradorService.existeCpf("390.533.447-05")).thenReturn(false);
        org.mockito.Mockito.when(adesaoColaboradorService.existeEmail("ok@example.com")).thenReturn(false);
        org.mockito.Mockito.when(adesaoColaboradorService.salvarDadosTemporarios(org.mockito.ArgumentMatchers.any(AdesaoColaboradorDTO.class)))
                .thenReturn("S_RESUME_1");

        org.mockito.Mockito.when(workflowAdesaoService.salvarProcesso(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap()))
                .thenReturn(new ProcessoAdesao("S_RESUME_1", "Teste Colaborador", "ok@example.com", "390.533.447-05"));
        Map<String, Object> payload = montarPayload("390.533.447-05", "ok@example.com");
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
        payload.put("bairro", "Centro");
        payload.put("cidade", "Sao Paulo");
        payload.put("estado", "SP");

        String sessionIdJson = mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        Map<?,?> resp = objectMapper.readValue(sessionIdJson, Map.class);
        String sid = (String) resp.get("sessionId");

        mockMvc.perform(get("/rh/colaboradores/adesao/api/progresso/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.flowId").value(sid))
                .andExpect(jsonPath("$.currentStep").value("DADOS_PESSOAIS"));
    }

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("Falha de rede em benefícios deve manter avanço com fallback")
    void falhaRedeBeneficiosComFallback() throws Exception {
        org.mockito.Mockito.when(adesaoSecurityService.checkRateLimit(org.mockito.Mockito.anyString())).thenReturn(true);
        doThrow(new RuntimeException(new java.net.SocketTimeoutException("timeout"))).when(beneficioAdesaoService).calcularCustoBeneficios(anyMap());

        org.mockito.Mockito.doNothing().when(workflowAdesaoService).salvarBeneficios(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap(), org.mockito.Mockito.anyDouble());
        org.mockito.Mockito.doNothing().when(workflowAdesaoService).atualizarEtapa(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString());

        Map<String, Object> beneficios = Map.of(
                "sessionId", "S2",
                "beneficios", Map.of("VALE_REFEICAO", Map.of("valor", "VR_300"))
        );

        mockMvc.perform(post("/rh/colaboradores/adesao/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beneficios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.proximaEtapa").value("revisao"));
    }

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("Timeout/Rate limit na validação de dados retorna 429")
    void timeoutValidacaoDadosRetorna429() throws Exception {
        org.mockito.Mockito.when(adesaoSecurityService.checkRateLimit(org.mockito.Mockito.anyString())).thenReturn(false);
        Map<String, Object> payload = montarPayload("390.533.447-05", "ok@example.com");
        mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("Interrupção de upload de documentos retorna 400")
    void interrupcaoUploadDocumentosRetorna400() throws Exception {
        org.mockito.Mockito.when(adesaoSecurityService.checkRateLimit(org.mockito.Mockito.anyString())).thenReturn(true);
        org.mockito.Mockito.when(documentoAdesaoService.verificarDocumentosObrigatorios(org.mockito.Mockito.anyString())).thenReturn(false);
        mockMvc.perform(post("/rh/colaboradores/adesao/documentos")
                        .param("sessionId", "S_DOC_FAIL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("Hash dos dados deve ser consistente após resume")
    void hashConsistenteAposResume() throws Exception {
        org.mockito.Mockito.when(adesaoSecurityService.checkRateLimit(org.mockito.Mockito.anyString())).thenReturn(true);
        org.mockito.Mockito.when(adesaoSecurityService.validateDadosPessoais(org.mockito.Mockito.any(AdesaoColaboradorDTO.class)))
                .thenReturn(new AdesaoSecurityService.ValidationResult());
        org.mockito.Mockito.when(adesaoColaboradorService.existeCpf("390.533.447-05")).thenReturn(false);
        org.mockito.Mockito.when(adesaoColaboradorService.existeEmail("ok@example.com")).thenReturn(false);
        org.mockito.Mockito.when(adesaoColaboradorService.salvarDadosTemporarios(org.mockito.ArgumentMatchers.any(AdesaoColaboradorDTO.class)))
                .thenReturn("S_HASH_1");
        org.mockito.Mockito.when(workflowAdesaoService.salvarProcesso(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap()))
                .thenReturn(new ProcessoAdesao("S_HASH_1", "Teste Colaborador", "ok@example.com", "390.533.447-05"));

        Map<String, Object> payload = montarPayload("390.533.447-05", "ok@example.com");

        String respStr = mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<?,?> resp = objectMapper.readValue(respStr, Map.class);
        String sid = (String) resp.get("sessionId");

        String resumeStr = mockMvc.perform(get("/rh/colaboradores/adesao/api/progresso/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        Map<?,?> resume = objectMapper.readValue(resumeStr, Map.class);
        String formDataJson = (String) resume.get("formData");

        java.util.Map<String,Object> original = objectMapper.readValue(objectMapper.writeValueAsBytes(payload), java.util.Map.class);
        java.util.Map<String,Object> resumed = objectMapper.readValue(formDataJson, java.util.Map.class);
        for (Map.Entry<String,Object> e : original.entrySet()) {
            org.junit.jupiter.api.Assertions.assertTrue(resumed.containsKey(e.getKey()));
            org.junit.jupiter.api.Assertions.assertEquals(String.valueOf(e.getValue()), String.valueOf(resumed.get(e.getKey())));
        }
    }

    @Test
    @WithMockUser(username = "master@sistema.com", roles = {"MASTER","ADMIN"})
    @DisplayName("Concorrência: múltiplas sessões não conflitam entre si")
    void concorrenciaMultiplasSessoes() throws Exception {
        org.mockito.Mockito.when(adesaoSecurityService.checkRateLimit(org.mockito.Mockito.anyString())).thenReturn(true);
        org.mockito.Mockito.when(adesaoSecurityService.validateDadosPessoais(org.mockito.Mockito.any(AdesaoColaboradorDTO.class)))
                .thenReturn(new AdesaoSecurityService.ValidationResult());
        org.mockito.Mockito.when(adesaoColaboradorService.existeCpf(org.mockito.Mockito.anyString())).thenReturn(false);
        org.mockito.Mockito.when(adesaoColaboradorService.existeEmail(org.mockito.Mockito.anyString())).thenReturn(false);
        org.mockito.Mockito.when(adesaoColaboradorService.salvarDadosTemporarios(org.mockito.ArgumentMatchers.any(AdesaoColaboradorDTO.class)))
                .thenReturn("S_CONC_1")
                .thenReturn("S_CONC_2");
        org.mockito.Mockito.when(workflowAdesaoService.salvarProcesso(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap()))
                .thenReturn(new ProcessoAdesao("S_CONC_1", "A", "a@example.com", "390.533.447-05"))
                .thenReturn(new ProcessoAdesao("S_CONC_2", "B", "b@example.com", "529.982.247-25"));

        Map<String, Object> p1 = montarPayload("390.533.447-05","a@example.com");
        Map<String, Object> p2 = montarPayload("529.982.247-25","b@example.com");

        mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isOk());

        String resumeStr = mockMvc.perform(get("/rh/colaboradores/adesao/api/progresso/resume"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<?,?> resume = objectMapper.readValue(resumeStr, Map.class);
        org.junit.jupiter.api.Assertions.assertTrue(((String)resume.get("flowId")).startsWith("S_CONC_"));
    }

    private Map<String, Object> montarPayload(String cpf, String email) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nome", "Teste Colaborador");
        payload.put("cpf", cpf);
        payload.put("email", email);
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
        payload.put("bairro", "Centro");
        payload.put("cidade", "Sao Paulo");
        payload.put("estado", "SP");
        return payload;
    }
}
