package com.jaasielsilva.portalceo.rh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;
import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.model.Colaborador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest({
        com.jaasielsilva.portalceo.controller.rh.colaborador.AdesaoColaboradorController.class,
        com.jaasielsilva.portalceo.controller.rh.colaborador.DocumentoAdesaoController.class
})
class AdesaoFullFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CargoRepository cargoRepository;

    @MockBean
    DepartamentoRepository departamentoRepository;

    @MockBean
    ColaboradorRepository colaboradorRepository;

    // Evitar envio real de e-mails em testes
    @MockBean
    com.jaasielsilva.portalceo.service.EmailService emailService;

    @MockBean com.jaasielsilva.portalceo.service.AdesaoColaboradorService adesaoService;
    @MockBean com.jaasielsilva.portalceo.service.AdesaoSecurityService securityService;
    @MockBean com.jaasielsilva.portalceo.service.AuditService auditService;
    @MockBean com.jaasielsilva.portalceo.service.CargoService cargoService;
    @MockBean com.jaasielsilva.portalceo.service.DepartamentoService departamentoService;
    @MockBean com.jaasielsilva.portalceo.service.BeneficioService beneficioService;
    @MockBean com.jaasielsilva.portalceo.service.DocumentoAdesaoService documentoService;
    @MockBean com.jaasielsilva.portalceo.service.BeneficioAdesaoService beneficioAdesaoService;
    @MockBean com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService workflowService;
    @MockBean com.jaasielsilva.portalceo.service.ColaboradorService colaboradorService;
    @MockBean com.jaasielsilva.portalceo.service.NotificationService notificationService;
    @MockBean com.jaasielsilva.portalceo.service.UsuarioService usuarioService;
    @MockBean com.jaasielsilva.portalceo.service.TermoService termoService;
    @MockBean com.jaasielsilva.portalceo.repository.UsuarioRepository usuarioRepository;

    @BeforeEach
    void seedBasicData() {
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        cargo.setNome("Desenvolvedor");
        cargo.setAtivo(true);
        Departamento dep = new Departamento();
        dep.setId(1L);
        dep.setNome("Tecnologia");

        org.mockito.Mockito.when(cargoRepository.findByNome(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.Optional.of(cargo));
        org.mockito.Mockito.when(departamentoRepository.findByNome(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.Optional.of(dep));

        org.mockito.Mockito.when(cargoService.listarAtivos()).thenReturn(java.util.List.of(cargo));
        org.mockito.Mockito.when(departamentoService.listarTodos()).thenReturn(java.util.List.of(dep));
        org.mockito.Mockito.when(beneficioService.listarTodos()).thenReturn(java.util.List.of());

        org.mockito.Mockito.when(securityService.checkRateLimit(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        com.jaasielsilva.portalceo.service.AdesaoSecurityService.ValidationResult validResult =
                new com.jaasielsilva.portalceo.service.AdesaoSecurityService.ValidationResult();
        org.mockito.Mockito.when(securityService.validateDadosPessoais(org.mockito.ArgumentMatchers.any()))
                .thenReturn(validResult);
        org.mockito.Mockito.when(securityService.validateFileUpload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new com.jaasielsilva.portalceo.service.AdesaoSecurityService.ValidationResult());
        org.mockito.Mockito.when(securityService.isSessionBlocked(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(false);
        org.mockito.Mockito.when(adesaoService.existeCpf(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        org.mockito.Mockito.when(adesaoService.existeEmail(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        org.mockito.Mockito.when(adesaoService.salvarDadosTemporarios(org.mockito.ArgumentMatchers.any()))
                .thenReturn("sess-123");

        org.mockito.Mockito.when(documentoService.verificarDocumentosObrigatorios(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        org.mockito.Mockito.when(documentoService.listarDocumentos(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.List.of());

        com.jaasielsilva.portalceo.model.ProcessoAdesao processo = new com.jaasielsilva.portalceo.model.ProcessoAdesao();
        processo.setId(100L);
        processo.setSessionId("sess-123");
        processo.setStatus(com.jaasielsilva.portalceo.model.ProcessoAdesao.StatusProcesso.AGUARDANDO_APROVACAO);
        processo.setEtapaAtual("finalizado");
        org.mockito.Mockito.when(workflowService.buscarProcessoPorSessionId(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(processo);
        org.mockito.Mockito.when(workflowService.salvarProcesso(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(processo);
        org.mockito.Mockito.doNothing().when(workflowService).atualizarEtapa(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        org.mockito.Mockito.doNothing().when(workflowService).finalizarProcesso(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        org.mockito.Mockito.doNothing().when(workflowService).salvarDocumentos(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap());
        org.mockito.Mockito.doNothing().when(workflowService).salvarBeneficios(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap(), org.mockito.ArgumentMatchers.anyDouble());

        com.jaasielsilva.portalceo.model.Colaborador col = new com.jaasielsilva.portalceo.model.Colaborador();
        col.setId(10L);
        col.setNome("Teste Colaborador");
        col.setEmail("colaborador@empresa.com");
        col.setCpf("390.533.447-05");
        col.setStatus(com.jaasielsilva.portalceo.model.Colaborador.StatusColaborador.ATIVO);
        col.setAtivo(true);
        org.mockito.Mockito.when(adesaoService.finalizarAdesao(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(col);

        org.mockito.Mockito.when(colaboradorRepository.findByCpf(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.Optional.of(col));

        com.jaasielsilva.portalceo.model.Notification notif = new com.jaasielsilva.portalceo.model.Notification(
                "hr_admission", "Novo Colaborador", "Mensagem", com.jaasielsilva.portalceo.model.Notification.Priority.HIGH);
        org.mockito.Mockito.when(notificationService
                .notifyNewEmployeeAdmission(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(notif);

        org.mockito.Mockito.when(usuarioService.buscarUsuariosComPermissaoGerenciarRH()).thenReturn(java.util.List.of());
        org.mockito.Mockito.when(beneficioAdesaoService.validarSelecaoBeneficios(org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(java.util.List.of());
        com.jaasielsilva.portalceo.service.BeneficioAdesaoService.CalculoBeneficio calc = new com.jaasielsilva.portalceo.service.BeneficioAdesaoService.CalculoBeneficio();
        org.mockito.Mockito.when(beneficioAdesaoService.calcularCustoBeneficios(org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(calc);

        com.jaasielsilva.portalceo.model.TermoAceite aceite = new com.jaasielsilva.portalceo.model.TermoAceite();
        org.mockito.Mockito.when(termoService.aceitarTermo(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(aceite);
        org.mockito.Mockito.when(termoService.buscarPorTipo(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());
        org.mockito.Mockito.when(usuarioService.buscarPorEmail(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.doNothing().when(workflowService).aprovarProcesso(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        org.mockito.Mockito.when(workflowService.buscarProcessoPorId(org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService.ProcessoAdesaoInfo());
    }

    @Test
    @WithMockUser(username = "colaborador@empresa.com", roles = {"COLABORADOR"})
    @DisplayName("Fluxo completo de adesão ativa colaborador após aprovação")
    void fullAdesaoFlowAtivaColaborador() throws Exception {
        Long cargoId = 1L;
        Long departamentoId = 1L;

        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", "Teste Colaborador");
        dados.put("cpf", "390.533.447-05");
        dados.put("email", "colaborador@empresa.com");
        dados.put("telefone", "(11) 98765-4321");
        dados.put("sexo", "MASCULINO");
        dados.put("dataNascimento", LocalDate.of(1990, 5, 10).toString());
        dados.put("estadoCivil", "SOLTEIRO");
        dados.put("rg", "12345678");
        dados.put("orgaoEmissorRg", "SSP-SP");
        dados.put("dataEmissaoRg", LocalDate.of(2010, 1, 1).toString());
        dados.put("dataAdmissao", LocalDate.now().toString());
        dados.put("cargoId", cargoId);
        dados.put("departamentoId", departamentoId);
        dados.put("salario", 3500.00);
        dados.put("tipoContrato", "CLT");
        dados.put("cargaHoraria", 40);
        dados.put("cep", "12345-678");
        dados.put("logradouro", "Rua Teste");
        dados.put("numero", "100");
        dados.put("bairro", "Centro");
        dados.put("cidade", "Sao Paulo");
        dados.put("estado", "SP");

        MvcResult resultDados = mockMvc.perform(post("/rh/colaboradores/adesao/dados-pessoais").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        Map<?,?> respDados = objectMapper.readValue(resultDados.getResponse().getContentAsString(), Map.class);
        String sessionId = (String) respDados.get("sessionId");
        assertNotNull(sessionId);

        byte[] pdfBytes = "%PDF-1.4".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/rh/colaboradores/adesao/documentos/upload")
                        .file(new MockMultipartFile("arquivo", "rg.pdf", "application/pdf", pdfBytes))
                        .param("tipoDocumento", "rg")
                        .param("sessionId", sessionId)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/rh/colaboradores/adesao/documentos/upload")
                        .file(new MockMultipartFile("arquivo", "cpf.pdf", "application/pdf", pdfBytes))
                        .param("tipoDocumento", "cpf")
                        .param("sessionId", sessionId)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/rh/colaboradores/adesao/documentos/upload")
                        .file(new MockMultipartFile("arquivo", "endereco.pdf", "application/pdf", pdfBytes))
                        .param("tipoDocumento", "endereco")
                        .param("sessionId", sessionId)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/rh/colaboradores/adesao/documentos").with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.proximaEtapa").value("beneficios"));

        Map<String, Object> beneficios = new HashMap<>();
        beneficios.put("VALE_REFEICAO", Map.of("valor", "VR_300"));
        beneficios.put("VALE_TRANSPORTE", Map.of("valor", "VT_200"));

        Map<String, Object> beneficiosReq = new HashMap<>();
        beneficiosReq.put("sessionId", sessionId);
        beneficiosReq.put("beneficios", beneficios);

        mockMvc.perform(post("/rh/colaboradores/adesao/beneficios").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beneficiosReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.proximaEtapa").value("revisao"));

        mockMvc.perform(post("/rh/colaboradores/adesao/finalizar").with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("sessionId", sessionId)
                        .param("aceitarTermos", "true")
                        .param("autorizarDesconto", "true")
                        .param("confirmarDados", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.protocolo").exists());

        MvcResult statusResult = mockMvc.perform(get("/rh/colaboradores/adesao/api/status/" + sessionId))
                .andExpect(status().isOk())
                .andReturn();
        Map<?,?> statusResp = objectMapper.readValue(statusResult.getResponse().getContentAsString(), Map.class);
        Number processoId = (Number) statusResp.get("processoId");
        assertNotNull(processoId);

        mockMvc.perform(post("/rh/colaboradores/adesao/api/aprovar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "processoId", String.valueOf(processoId.longValue()),
                                "aprovadoPor", "admin@sistema.com",
                                "observacoes", "OK"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Colaborador colaborador = colaboradorRepository.findByCpf("390.533.447-05").orElse(null);
        assertNotNull(colaborador);
        assertTrue(Boolean.TRUE.equals(colaborador.getAtivo()));
        assertEquals(Colaborador.StatusColaborador.ATIVO, colaborador.getStatus());
    }
}
