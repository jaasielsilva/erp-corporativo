package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ContratoLegalService;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/juridico")
@RequiredArgsConstructor
public class JuridicoController {

    private final ContratoLegalService contratoLegalService;
    private final UsuarioRepository usuarioRepository;

    // Página principal do Jurídico
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Jurídico - Dashboard");
        model.addAttribute("moduleCSS", "juridico");
        
        // Estatísticas do dashboard (dados reais quando disponíveis)
        Map<ContratoLegal.StatusContrato, Long> estatisticasStatus = contratoLegalService.getEstatisticasPorStatus();
        long ativos = estatisticasStatus.getOrDefault(ContratoLegal.StatusContrato.ATIVO, 0L);
        model.addAttribute("contratosAtivos", ativos);

        // Ainda não modelado: processos e compliance reais
        model.addAttribute("processosAndamento", getProcessosAndamento());
        
        // Prazos/vencimentos a partir de contratos com vencimento próximo (próximos 30 dias)
        List<ContratoLegal> proximosVencimentos = contratoLegalService.buscarContratosVencendoEm(30);
        model.addAttribute("prazosVencendo", proximosVencimentos != null ? proximosVencimentos.size() : 0);

        // Alertas de compliance permanecem como placeholder até integração
        model.addAttribute("alertasCompliance", getAlertasCompliance());

        // Contratos próximos ao vencimento (renderização simplificada)
        List<Map<String, Object>> contratosVencimentoVm = new ArrayList<>();
        if (proximosVencimentos != null) {
            for (ContratoLegal c : proximosVencimentos) {
                Map<String, Object> item = new HashMap<>();
                item.put("titulo", c.getTitulo());
                // usar nome da contraparte quando disponível
                item.put("contraparte", c.getNomeContraparte());
                item.put("dataVencimento", c.getDataVencimento());
                item.put("valor", c.getValorContrato() != null ? c.getValorContrato() : c.getValorMensal());
                item.put("status", c.getStatus());
                contratosVencimentoVm.add(item);
            }
        }
        model.addAttribute("contratosVencimento", contratosVencimentoVm);
        
        // Processos com prazos urgentes
        model.addAttribute("processosUrgentes", getProcessosUrgentes());
        
        // Últimas atividades
        model.addAttribute("ultimasAtividades", getUltimasAtividades());
        
        return "juridico/index";
    }

    // =============== CONTRATOS JURÍDICOS ===============
    
    @GetMapping("/contratos")
    public String contratos(
            @RequestParam(value = "status", required = false) ContratoLegal.StatusContrato status,
            @RequestParam(value = "tipo", required = false) ContratoLegal.TipoContrato tipo,
            @RequestParam(value = "numero", required = false) String numero,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        model.addAttribute("pageTitle", "Gestão de Contratos");
        model.addAttribute("moduleCSS", "juridico");

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ContratoLegal> contratosPage = contratoLegalService.buscarContratosComFiltros(numero, status, tipo, pageable);

            model.addAttribute("listaContratos", contratosPage.getContent());
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("totalElements", contratosPage.getTotalElements());
            model.addAttribute("totalPages", contratosPage.getTotalPages());

            model.addAttribute("statusContrato", ContratoLegal.StatusContrato.values());
            model.addAttribute("tiposContrato", ContratoLegal.TipoContrato.values());

            // Estatísticas reais de contratos
            model.addAttribute("estatisticasStatus", contratoLegalService.getEstatisticasPorStatus());
            model.addAttribute("valorTotalAtivos", contratoLegalService.calcularValorTotalAtivos());
            model.addAttribute("receitaMensal", contratoLegalService.calcularReceitaMensalRecorrente());
        } catch (Exception e) {
            // Fallback seguro para evitar quebras de renderização
            model.addAttribute("listaContratos", java.util.Collections.emptyList());
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("totalElements", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("statusContrato", ContratoLegal.StatusContrato.values());
            model.addAttribute("tiposContrato", ContratoLegal.TipoContrato.values());
            model.addAttribute("estatisticasStatus", java.util.Collections.emptyMap());
            model.addAttribute("valorTotalAtivos", java.math.BigDecimal.ZERO);
            model.addAttribute("receitaMensal", java.math.BigDecimal.ZERO);
            model.addAttribute("erroCarregamento", e.getMessage());
        }

        return "juridico/contratos";
    }
    
    @PostMapping("/contratos")
    @ResponseBody
    public ResponseEntity<?> criarContrato(@RequestParam String titulo,
                                           @RequestParam ContratoLegal.TipoContrato tipo,
                                           @RequestParam(required = false) String descricao,
                                           @RequestParam String dataInicio,
                                           @RequestParam(required = false, defaultValue = "12") Integer duracaoMeses,
                                           @RequestParam(required = false) String valorMensal,
                                           @RequestParam(required = false) String valorContrato,
                                           @RequestParam(required = false, defaultValue = "false") Boolean renovacaoAutomatica,
                                           @RequestParam(required = false, defaultValue = "30") Integer prazoNotificacao,
                                           @RequestParam(required = false) String numeroContrato,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Obter usuário logado
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);

            // Mapear dados do contrato
            ContratoLegal contrato = new ContratoLegal();
            contrato.setTitulo(titulo);
            contrato.setTipo(tipo);
            contrato.setDescricao(descricao);
            contrato.setDataInicio(LocalDate.parse(dataInicio));
            contrato.setDuracaoMeses(duracaoMeses);
            contrato.setRenovacaoAutomatica(Boolean.TRUE.equals(renovacaoAutomatica));
            contrato.setPrazoNotificacao(prazoNotificacao);

            if (numeroContrato != null && !numeroContrato.isBlank()) {
                contrato.setNumeroContrato(numeroContrato.trim());
            }

            if (valorMensal != null && !valorMensal.isBlank()) {
                String vm = valorMensal.replace(".", "").replace(",", ".");
                contrato.setValorMensal(new BigDecimal(vm));
            }
            if (valorContrato != null && !valorContrato.isBlank()) {
                String vc = valorContrato.replace(".", "").replace(",", ".");
                contrato.setValorContrato(new BigDecimal(vc));
            }

            // Associar usuário
            contrato.setUsuarioCriacao(usuario);
            contrato.setUsuarioResponsavel(usuario);

            // Persistir usando service (gera número se vazio e cria alertas)
            ContratoLegal salvo = contratoLegalService.salvarContrato(contrato);

            Map<String, Object> payload = new HashMap<>();
            payload.put("id", salvo.getId());
            payload.put("numeroContrato", salvo.getNumeroContrato());
            payload.put("status", salvo.getStatus());
            payload.put("titulo", salvo.getTitulo());
            payload.put("tipo", salvo.getTipo());
            payload.put("dataInicio", salvo.getDataInicio());
            payload.put("valorMensal", salvo.getValorMensal());
            payload.put("valorContrato", salvo.getValorContrato());

            return ResponseEntity.ok(payload);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao criar contrato", "detalhes", e.getMessage()));
        }
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
        Map<ContratoLegal.StatusContrato, Long> estatisticasStatus = contratoLegalService.getEstatisticasPorStatus();
        long ativos = estatisticasStatus.getOrDefault(ContratoLegal.StatusContrato.ATIVO, 0L);
        estatisticas.put("contratosAtivos", ativos);
        estatisticas.put("processosAndamento", getProcessosAndamento());
        List<ContratoLegal> proximos = contratoLegalService.buscarContratosVencendoEm(30);
        estatisticas.put("prazosVencendo", proximos != null ? proximos.size() : 0);
        estatisticas.put("alertasCompliance", getAlertasCompliance());
        estatisticas.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/api/contratos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarContratosApi(
            @RequestParam(value = "status", required = false) ContratoLegal.StatusContrato status,
            @RequestParam(value = "tipo", required = false) ContratoLegal.TipoContrato tipo,
            @RequestParam(value = "numero", required = false) String numero,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContratoLegal> contratosPage = contratoLegalService.buscarContratosComFiltros(numero, status, tipo, pageable);

        Map<String, Object> payload = new HashMap<>();
        payload.put("content", contratosPage.getContent());
        payload.put("totalElements", contratosPage.getTotalElements());
        payload.put("page", contratosPage.getNumber());
        payload.put("size", contratosPage.getSize());
        payload.put("totalPages", contratosPage.getTotalPages());

        return ResponseEntity.ok(payload);
    }

    // =============== Endpoints de Ações de Contrato ===============

    @PutMapping("/contratos/{id}/enviar-analise")
    @ResponseBody
    public ResponseEntity<?> enviarParaAnalise(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.enviarParaAnalise(id, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato enviado para análise"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao enviar para análise", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/aprovar")
    @ResponseBody
    public ResponseEntity<?> aprovarContrato(@PathVariable Long id,
                                             @RequestBody(required = false) Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String observacoes = body != null ? String.valueOf(body.getOrDefault("observacoes", "")) : "";
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.aprovarContrato(id, observacoes, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato aprovado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao aprovar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/assinar")
    @ResponseBody
    public ResponseEntity<?> assinarContrato(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String dataStr = String.valueOf(body.getOrDefault("dataAssinatura", ""));
            LocalDate dataAssinatura = LocalDate.parse(dataStr);
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.assinarContrato(id, dataAssinatura, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato assinado"
            ));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao assinar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/ativar")
    @ResponseBody
    public ResponseEntity<?> ativarContrato(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.ativarContrato(id, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato ativado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao ativar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/suspender")
    @ResponseBody
    public ResponseEntity<?> suspenderContrato(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String motivo = String.valueOf(body.getOrDefault("motivo", ""));
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.suspenderContrato(id, motivo, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato suspenso"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao suspender contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/reativar")
    @ResponseBody
    public ResponseEntity<?> reativarContrato(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.reativarContrato(id, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato reativado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao reativar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/rescindir")
    @ResponseBody
    public ResponseEntity<?> rescindirContrato(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String motivo = String.valueOf(body.getOrDefault("motivo", ""));
            String dataStr = String.valueOf(body.getOrDefault("dataRescisao", ""));
            LocalDate dataRescisao = LocalDate.parse(dataStr);
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.rescindirContrato(id, motivo, dataRescisao, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato rescindido"
            ));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao rescindir contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/renovar")
    @ResponseBody
    public ResponseEntity<?> renovarContrato(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer novasDuracaoMeses = body.get("novasDuracaoMeses") != null ? Integer.parseInt(String.valueOf(body.get("novasDuracaoMeses"))) : null;
            String valorStr = String.valueOf(body.getOrDefault("novoValor", "0"));
            // aceitar tanto "1500,00" quanto "1500.00"
            valorStr = valorStr.replace(".", "").replace(",", ".");
            BigDecimal novoValor = new BigDecimal(valorStr);

            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.renovarContrato(id, novasDuracaoMeses, novoValor, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato renovado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao renovar contrato", "detalhes", e.getMessage()));
        }
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