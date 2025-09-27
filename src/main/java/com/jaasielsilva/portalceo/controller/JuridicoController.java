package com.jaasielsilva.portalceo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/juridico")
@RequiredArgsConstructor
public class JuridicoController {

    // Página principal do Jurídico
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Jurídico - Dashboard");
        model.addAttribute("moduleCSS", "juridico");
        
        // Estatísticas do dashboard
        model.addAttribute("contratosAtivos", getContratosAtivos());
        model.addAttribute("processosAndamento", getProcessosAndamento());
        model.addAttribute("prazosVencendo", getPrazosVencendo());
        model.addAttribute("alertasCompliance", getAlertasCompliance());
        
        // Contratos próximos ao vencimento
        model.addAttribute("contratosVencimento", getContratosProximosVencimento());
        
        // Processos com prazos urgentes
        model.addAttribute("processosUrgentes", getProcessosUrgentes());
        
        // Últimas atividades
        model.addAttribute("ultimasAtividades", getUltimasAtividades());
        
        return "juridico/index";
    }

    // =============== CONTRATOS JURÍDICOS ===============
    
    @GetMapping("/contratos")
    public String contratos(Model model) {
        model.addAttribute("pageTitle", "Gestão de Contratos");
        model.addAttribute("moduleCSS", "juridico");
        
        // Lista de contratos por status
        model.addAttribute("contratosAtivos", getListaContratos("ATIVO"));
        model.addAttribute("contratosAnalise", getListaContratos("EM_ANALISE"));
        model.addAttribute("contratosVencidos", getListaContratos("VENCIDO"));
        
        // Estatísticas de contratos
        model.addAttribute("estatisticasContratos", getEstatisticasContratos());
        
        return "juridico/contratos";
    }
    
    @PostMapping("/contratos")
    @ResponseBody
    public ResponseEntity<?> criarContrato(@RequestParam String titulo,
                                          @RequestParam String tipo,
                                          @RequestParam String contraparte,
                                          @RequestParam String dataVencimento,
                                          @RequestParam String valor,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        // Simular criação de contrato
        Map<String, Object> contrato = new HashMap<>();
        contrato.put("id", System.currentTimeMillis());
        contrato.put("titulo", titulo);
        contrato.put("tipo", tipo);
        contrato.put("contraparte", contraparte);
        contrato.put("status", "EM_ANALISE");
        contrato.put("responsavel", userDetails.getUsername());
        contrato.put("dataCriacao", LocalDateTime.now());
        
        return ResponseEntity.ok(contrato);
    }

    // =============== PROCESSOS JURÍDICOS ===============
    
    @GetMapping("/processos")
    public String processos(Model model) {
        model.addAttribute("pageTitle", "Processos Jurídicos");
        model.addAttribute("moduleCSS", "juridico");
        
        // Processos por status
        model.addAttribute("processosAtivos", getListaProcessos("ATIVO"));
        model.addAttribute("processosArquivados", getListaProcessos("ARQUIVADO"));
        model.addAttribute("processosAndamento", getListaProcessos("EM_ANDAMENTO"));
        
        // Próximas audiências
        model.addAttribute("proximasAudiencias", getProximasAudiencias());
        
        // Prazos críticos
        model.addAttribute("prazosCriticos", getPrazosCriticos());
        
        return "juridico/processos";
    }
    
    @PostMapping("/processos")
    @ResponseBody
    public ResponseEntity<?> criarProcesso(@RequestParam String numero,
                                          @RequestParam String tipo,
                                          @RequestParam String tribunal,
                                          @RequestParam String parte,
                                          @RequestParam String assunto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        // Simular criação de processo
        Map<String, Object> processo = new HashMap<>();
        processo.put("id", System.currentTimeMillis());
        processo.put("numero", numero);
        processo.put("tipo", tipo);
        processo.put("tribunal", tribunal);
        processo.put("parte", parte);
        processo.put("assunto", assunto);
        processo.put("status", "EM_ANDAMENTO");
        processo.put("responsavel", userDetails.getUsername());
        processo.put("dataAbertura", LocalDateTime.now());
        
        return ResponseEntity.ok(processo);
    }

    // =============== COMPLIANCE ===============
    
    @GetMapping("/compliance")
    public String compliance(Model model) {
        model.addAttribute("pageTitle", "Compliance e Conformidade");
        model.addAttribute("moduleCSS", "juridico");
        
        // Status de compliance
        model.addAttribute("statusCompliance", getStatusCompliance());
        
        // Normas e regulamentações
        model.addAttribute("normasVigentes", getNormasVigentes());
        
        // Não conformidades
        model.addAttribute("naoConformidades", getNaoConformidades());
        
        // Auditorias
        model.addAttribute("auditorias", getAuditorias());
        
        return "juridico/compliance";
    }
    
    @PostMapping("/compliance/auditoria")
    @ResponseBody
    public ResponseEntity<?> criarAuditoria(@RequestParam String tipo,
                                           @RequestParam String escopo,
                                           @RequestParam String dataInicio,
                                           @RequestParam String auditor,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        // Simular criação de auditoria
        Map<String, Object> auditoria = new HashMap<>();
        auditoria.put("id", System.currentTimeMillis());
        auditoria.put("tipo", tipo);
        auditoria.put("escopo", escopo);
        auditoria.put("auditor", auditor);
        auditoria.put("status", "PLANEJADA");
        auditoria.put("responsavel", userDetails.getUsername());
        auditoria.put("dataCriacao", LocalDateTime.now());
        
        return ResponseEntity.ok(auditoria);
    }

    // =============== DOCUMENTOS JURÍDICOS ===============
    
    @GetMapping("/documentos")
    public String documentos(Model model) {
        model.addAttribute("pageTitle", "Biblioteca de Documentos");
        model.addAttribute("moduleCSS", "juridico");
        
        // Categorias de documentos
        model.addAttribute("categoriasDocumentos", getCategoriasDocumentos());
        
        // Documentos recentes
        model.addAttribute("documentosRecentes", getDocumentosRecentes());
        
        // Modelos de documentos
        model.addAttribute("modelosDocumentos", getModelosDocumentos());
        
        // Documentos pendentes de assinatura
        model.addAttribute("documentosPendentes", getDocumentosPendentesAssinatura());
        
        return "juridico/documentos";
    }
    
    @PostMapping("/documentos/upload")
    @ResponseBody
    public ResponseEntity<?> uploadDocumento(@RequestParam String titulo,
                                            @RequestParam String categoria,
                                            @RequestParam String descricao,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        // Simular upload de documento
        Map<String, Object> documento = new HashMap<>();
        documento.put("id", System.currentTimeMillis());
        documento.put("titulo", titulo);
        documento.put("categoria", categoria);
        documento.put("descricao", descricao);
        documento.put("status", "ATIVO");
        documento.put("autor", userDetails.getUsername());
        documento.put("dataUpload", LocalDateTime.now());
        
        return ResponseEntity.ok(documento);
    }

    // =============== APIs ===============
    
    @GetMapping("/api/dashboard/estatisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEstatisticasDashboard() {
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("contratosAtivos", getContratosAtivos());
        estatisticas.put("processosAndamento", getProcessosAndamento());
        estatisticas.put("prazosVencendo", getPrazosVencendo());
        estatisticas.put("alertasCompliance", getAlertasCompliance());
        estatisticas.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(estatisticas);
    }

    // =============== MÉTODOS AUXILIARES ===============
    
    private int getContratosAtivos() {
        return 45;
    }
    
    private int getProcessosAndamento() {
        return 12;
    }
    
    private int getPrazosVencendo() {
        return 8;
    }
    
    private int getAlertasCompliance() {
        return 3;
    }
    
    private List<Map<String, Object>> getContratosProximosVencimento() {
        List<Map<String, Object>> contratos = new ArrayList<>();
        
        Map<String, Object> contrato1 = new HashMap<>();
        contrato1.put("titulo", "Contrato de Prestação de Serviços - TechCorp");
        contrato1.put("contraparte", "TechCorp Ltda");
        contrato1.put("dataVencimento", LocalDate.now().plusDays(15));
        contrato1.put("valor", "R$ 50.000,00");
        contrato1.put("status", "ATIVO");
        contratos.add(contrato1);
        
        Map<String, Object> contrato2 = new HashMap<>();
        contrato2.put("titulo", "Contrato de Locação - Escritório Central");
        contrato2.put("contraparte", "Imobiliária ABC");
        contrato2.put("dataVencimento", LocalDate.now().plusDays(30));
        contrato2.put("valor", "R$ 15.000,00");
        contrato2.put("status", "ATIVO");
        contratos.add(contrato2);
        
        return contratos;
    }
    
    private List<Map<String, Object>> getProcessosUrgentes() {
        List<Map<String, Object>> processos = new ArrayList<>();
        
        Map<String, Object> processo1 = new HashMap<>();
        processo1.put("numero", "1234567-89.2023.8.26.0100");
        processo1.put("tipo", "Trabalhista");
        processo1.put("parte", "João Silva vs. Empresa");
        processo1.put("proximoPrazo", LocalDate.now().plusDays(3));
        processo1.put("acao", "Contestação");
        processos.add(processo1);
        
        return processos;
    }
    
    private List<Map<String, Object>> getUltimasAtividades() {
        List<Map<String, Object>> atividades = new ArrayList<>();
        
        Map<String, Object> atividade1 = new HashMap<>();
        atividade1.put("tipo", "Contrato");
        atividade1.put("descricao", "Contrato de prestação de serviços aprovado");
        atividade1.put("data", LocalDateTime.now().minusHours(2));
        atividade1.put("usuario", "Dr. Carlos Oliveira");
        atividades.add(atividade1);
        
        Map<String, Object> atividade2 = new HashMap<>();
        atividade2.put("tipo", "Processo");
        atividade2.put("descricao", "Audiência agendada para processo trabalhista");
        atividade2.put("data", LocalDateTime.now().minusHours(4));
        atividade2.put("usuario", "Dra. Ana Santos");
        atividades.add(atividade2);
        
        return atividades;
    }
    
    private List<Map<String, Object>> getListaContratos(String status) {
        // Simular lista de contratos por status
        return new ArrayList<>();
    }
    
    private Map<String, Object> getEstatisticasContratos() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAtivos", 45);
        stats.put("totalAnalise", 8);
        stats.put("totalVencidos", 3);
        stats.put("valorTotal", "R$ 2.450.000,00");
        return stats;
    }
    
    private List<Map<String, Object>> getListaProcessos(String status) {
        // Simular lista de processos por status
        return new ArrayList<>();
    }
    
    private List<Map<String, Object>> getProximasAudiencias() {
        List<Map<String, Object>> audiencias = new ArrayList<>();
        
        Map<String, Object> audiencia1 = new HashMap<>();
        audiencia1.put("processo", "1234567-89.2023.8.26.0100");
        audiencia1.put("tipo", "Instrução");
        audiencia1.put("data", LocalDateTime.now().plusDays(7));
        audiencia1.put("local", "Fórum Central");
        audiencias.add(audiencia1);
        
        return audiencias;
    }
    
    private List<Map<String, Object>> getPrazosCriticos() {
        List<Map<String, Object>> prazos = new ArrayList<>();
        
        Map<String, Object> prazo1 = new HashMap<>();
        prazo1.put("processo", "1234567-89.2023.8.26.0100");
        prazo1.put("acao", "Contestação");
        prazo1.put("dataLimite", LocalDate.now().plusDays(3));
        prazo1.put("responsavel", "Dr. Carlos Oliveira");
        prazos.add(prazo1);
        
        return prazos;
    }
    
    private Map<String, Object> getStatusCompliance() {
        Map<String, Object> status = new HashMap<>();
        status.put("conformidade", 92.5);
        status.put("naoConformidades", 3);
        status.put("auditoriasPendentes", 2);
        status.put("ultimaAuditoria", LocalDate.now().minusMonths(3));
        return status;
    }
    
    private List<Map<String, Object>> getNormasVigentes() {
        List<Map<String, Object>> normas = new ArrayList<>();
        
        Map<String, Object> norma1 = new HashMap<>();
        norma1.put("codigo", "LGPD");
        norma1.put("nome", "Lei Geral de Proteção de Dados");
        norma1.put("status", "VIGENTE");
        norma1.put("ultimaRevisao", LocalDate.now().minusMonths(6));
        normas.add(norma1);
        
        return normas;
    }
    
    private List<Map<String, Object>> getNaoConformidades() {
        List<Map<String, Object>> naoConformidades = new ArrayList<>();
        
        Map<String, Object> nc1 = new HashMap<>();
        nc1.put("codigo", "NC-001");
        nc1.put("descricao", "Falta de treinamento em LGPD");
        nc1.put("severidade", "MEDIA");
        nc1.put("status", "ABERTA");
        nc1.put("prazoCorrecao", LocalDate.now().plusDays(30));
        naoConformidades.add(nc1);
        
        return naoConformidades;
    }
    
    private List<Map<String, Object>> getAuditorias() {
        List<Map<String, Object>> auditorias = new ArrayList<>();
        
        Map<String, Object> auditoria1 = new HashMap<>();
        auditoria1.put("tipo", "Compliance LGPD");
        auditoria1.put("status", "CONCLUIDA");
        auditoria1.put("data", LocalDate.now().minusMonths(3));
        auditoria1.put("resultado", "APROVADO");
        auditorias.add(auditoria1);
        
        return auditorias;
    }
    
    private List<Map<String, Object>> getCategoriasDocumentos() {
        List<Map<String, Object>> categorias = new ArrayList<>();
        
        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("nome", "Contratos");
        cat1.put("quantidade", 45);
        categorias.add(cat1);
        
        Map<String, Object> cat2 = new HashMap<>();
        cat2.put("nome", "Processos");
        cat2.put("quantidade", 23);
        categorias.add(cat2);
        
        return categorias;
    }
    
    private List<Map<String, Object>> getDocumentosRecentes() {
        List<Map<String, Object>> documentos = new ArrayList<>();
        
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("titulo", "Contrato de Prestação de Serviços");
        doc1.put("categoria", "Contratos");
        doc1.put("dataUpload", LocalDateTime.now().minusHours(2));
        doc1.put("autor", "Dr. Carlos Oliveira");
        documentos.add(doc1);
        
        return documentos;
    }
    
    private List<Map<String, Object>> getModelosDocumentos() {
        List<Map<String, Object>> modelos = new ArrayList<>();
        
        Map<String, Object> modelo1 = new HashMap<>();
        modelo1.put("nome", "Modelo de Contrato de Prestação de Serviços");
        modelo1.put("categoria", "Contratos");
        modelo1.put("versao", "2.1");
        modelos.add(modelo1);
        
        return modelos;
    }
    
    private List<Map<String, Object>> getDocumentosPendentesAssinatura() {
        List<Map<String, Object>> pendentes = new ArrayList<>();
        
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("titulo", "Contrato de Locação - Filial Norte");
        doc1.put("signatarios", "2 de 3");
        doc1.put("prazoAssinatura", LocalDate.now().plusDays(5));
        pendentes.add(doc1);
        
        return pendentes;
    }
}