package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.dto.ConfiguracaoValeTransporteDTO;
import com.jaasielsilva.portalceo.dto.ResumoValeTransporteDTO;
import com.jaasielsilva.portalceo.dto.ValeTransporteListDTO;
import com.jaasielsilva.portalceo.model.ValeTransporte;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.ValeTransporteService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;    
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import com.jaasielsilva.portalceo.dto.ConfiguracaoValeTransporteDTO;

@Controller
@RequestMapping("/rh/beneficios/vale-transporte")
public class ValeTransporteController {

    private static final Logger logger = LoggerFactory.getLogger(ValeTransporteController.class);

    private final ValeTransporteService service;
    
    @Autowired
    private ColaboradorService colaboradorService;
    @Autowired
    private DepartamentoService departamentoService;
    @Autowired
    private AuditoriaRhLogService auditoriaService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private com.jaasielsilva.portalceo.service.RhValeTransporteConfigService vtConfigService;

    public ValeTransporteController(ValeTransporteService service) {
        this.service = service;
    }

    // LISTAR - COM DADOS REAIS
    @GetMapping("/listar")
    public String listar(Model model,
                        @RequestParam(value = "mes", required = false) Integer mes,
                        @RequestParam(value = "ano", required = false) Integer ano,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "departamento", required = false) String departamento) {
        
        logger.debug("Listando vale transporte - Filtros: mes={}, ano={}, status={}, departamento={}", 
                    mes, ano, status, departamento);
        
        try {
            // Definir mês/ano padrão se não informado
            LocalDate hoje = LocalDate.now();
            if (mes == null) mes = hoje.getMonthValue();
            if (ano == null) ano = hoje.getYear();
            
            // Gerar resumo mensal
            ResumoValeTransporteDTO resumo = service.gerarResumoMensal(mes, ano);
            
            // Adicionar dados ao modelo
            model.addAttribute("resumo", resumo);
            model.addAttribute("mesAtual", mes);
            model.addAttribute("anoAtual", ano);
            model.addAttribute("statusFiltro", status);
            model.addAttribute("departamentoFiltro", departamento);
            
            ConfiguracaoValeTransporteDTO config = vtConfigService.obterConfiguracaoAtual();
            model.addAttribute("configuracao", config);
            
            return "rh/beneficios/vale-transporte/listar";
            
        } catch (Exception e) {
            logger.error("Erro ao listar vale transporte: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar dados: " + e.getMessage());
            return "rh/beneficios/vale-transporte/listar";
        }
    }

    @GetMapping("/api/config")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public org.springframework.http.ResponseEntity<ConfiguracaoValeTransporteDTO> obterConfig() {
        return org.springframework.http.ResponseEntity.ok(vtConfigService.obterConfiguracaoAtual());
    }

    @PostMapping("/api/config")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public org.springframework.http.ResponseEntity<java.util.Map<String,Object>> salvarConfig(@RequestBody ConfiguracaoValeTransporteDTO body,
                                                                                             org.springframework.security.core.Authentication auth,
                                                                                             jakarta.servlet.http.HttpServletRequest request) {
        String usuario = auth != null ? auth.getName() : "sistema";
        if (body == null || !body.isPercentualValido() || !body.isValorPassagemValido() || !body.isDiasUteisValido()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("sucesso", false, "mensagem", "Dados inválidos"));
        }
        java.util.Map<String,Object> result = vtConfigService.salvarConfiguracao(body, usuario);
        try { auditoriaService.registrar("CONFIGURACAO", "SALVAR_VT_CONFIG", "/rh/beneficios/vale-transporte/api/config", usuario, request!=null?request.getRemoteAddr():null, "Atualizacao de configuração VT", Boolean.TRUE.equals(result.get("sucesso"))); } catch (Exception ignore) {}
        return org.springframework.http.ResponseEntity.ok(result);
    }

    // FORM NOVO - COM COLABORADORES
    @GetMapping("/novo")
    public String novo(Model model) {
        try {
            model.addAttribute("vale", new ValeTransporte());
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            
            // Valores padrão
            LocalDate hoje = LocalDate.now();
            model.addAttribute("mesAtual", hoje.getMonthValue());
            model.addAttribute("anoAtual", hoje.getYear());
            
            return "rh/beneficios/vale-transporte/form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário novo: {}", e.getMessage(), e);
            return "redirect:/rh/beneficios/vale-transporte/listar";
        }
    }

    // SALVAR - COM VALIDAÇÕES
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("vale") ValeTransporte vale, 
                        BindingResult result, 
                        RedirectAttributes redirectAttributes,
                        Model model,
                        org.springframework.security.core.Authentication auth) {
        
        logger.debug("Salvando vale transporte para colaborador: {}", 
                    vale.getColaborador() != null ? vale.getColaborador().getId() : "N/A");
        
        if (result.hasErrors()) {
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
        
        try {
            if (vale.getUsuarioCriacao() == null && auth != null && auth.getName() != null) {
                var usuarioOpt = usuarioService.buscarPorEmail(auth.getName());
                usuarioOpt.ifPresent(vale::setUsuarioCriacao);
            }
            service.salvar(vale);
            redirectAttributes.addFlashAttribute("mensagem", "Vale transporte salvo com sucesso!");
            return "redirect:/rh/beneficios/vale-transporte/listar";
        } catch (Exception e) {
            logger.error("Erro ao salvar vale transporte: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
    }

    // FORM EDITAR
    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        try {
            ValeTransporte vale = service.buscarPorId(id);
            model.addAttribute("vale", vale);
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de edição: {}", e.getMessage(), e);
            return "redirect:/rh/beneficios/vale-transporte/listar";
        }
    }

    // ATUALIZAR
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("vale") ValeTransporte vale,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
        
        try {
            vale.setId(id);
            service.salvar(vale);
            redirectAttributes.addFlashAttribute("mensagem", "Vale transporte atualizado com sucesso!");
            return "redirect:/rh/beneficios/vale-transporte/listar";
        } catch (Exception e) {
            logger.error("Erro ao atualizar vale transporte: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
    }

    // DELETAR
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Vale transporte removido com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao deletar vale transporte: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-transporte/listar";
    }

    // === ENDPOINTS AJAX/API ===

    /**
     * API para buscar estatísticas em tempo real
     */
    @GetMapping("/api/estatisticas")
    @ResponseBody
    public ResponseEntity<ResumoValeTransporteDTO> getEstatisticas(
            @RequestParam(defaultValue = "0") Integer mes,
            @RequestParam(defaultValue = "0") Integer ano) {
        
        try {
            LocalDate hoje = LocalDate.now();
            if (mes == 0) mes = hoje.getMonthValue();
            if (ano == 0) ano = hoje.getYear();
            
            ResumoValeTransporteDTO resumo = service.gerarResumoMensal(mes, ano);
            return ResponseEntity.ok(resumo);
        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Processar cálculo mensal em lote
     */
    @PostMapping("/api/calcular-mes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calcularMes(
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            @RequestBody ConfiguracaoValeTransporteDTO config,
            org.springframework.security.core.Authentication auth) {
        
        logger.info("Iniciando cálculo mensal para {}/{}", mes, ano);
        Map<String, Object> response = new HashMap<>();
        
        try {
            com.jaasielsilva.portalceo.model.Usuario usuario = null;
            if (auth != null && auth.getName() != null) {
                usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            }
            int processados = service.calcularValesMensais(mes, ano, config, usuario);
            
            response.put("sucesso", true);
            response.put("processados", processados);
            response.put("mensagem", String.format("Processados %d vales transporte para %d/%d", 
                        processados, mes, ano));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro no cálculo mensal: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Suspender vale transporte
     */
    @PostMapping("/api/suspender/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suspender(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            service.suspender(id, motivo);
            response.put("sucesso", true);
            response.put("mensagem", "Vale transporte suspenso com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao suspender vale: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Reativar vale transporte
     */
    @PostMapping("/api/reativar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reativar(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            service.reativar(id);
            response.put("sucesso", true);
            response.put("mensagem", "Vale transporte reativado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao reativar vale: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cancelar vale transporte
     */
    @PostMapping("/api/cancelar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelar(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            service.cancelar(id, motivo);
            response.put("sucesso", true);
            response.put("mensagem", "Vale transporte cancelado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao cancelar vale: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Exportar relatório de Vale Transporte
     */
    @GetMapping("/api/relatorio")
    public ResponseEntity<Map<String, Object>> exportarRelatorio(
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "formato", defaultValue = "json") String formato) {
        
        try {
            LocalDate hoje = LocalDate.now();
            if (mes == null) mes = hoje.getMonthValue();
            if (ano == null) ano = hoje.getYear();
            
            // Buscar dados para o relatório
            List<ValeTransporteListDTO> vales = mes != null && ano != null ? 
                service.listarPorMesAno(mes, ano) : service.listarComColaboradores();
            
            ResumoValeTransporteDTO resumo = service.gerarResumoMensal(mes, ano);
            
            Map<String, Object> relatorio = new HashMap<>();
            relatorio.put("titulo", "Relatório de Vale Transporte");
            relatorio.put("periodo", mes + "/" + ano);
            relatorio.put("dataGeracao", LocalDate.now().toString());
            relatorio.put("resumo", resumo);
            relatorio.put("vales", vales);
            relatorio.put("totalRegistros", vales.size());
            
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/listar")
    @ResponseBody
    @Timed(value = "rh.beneficios.vt.list.api", histogram = true)
    public ResponseEntity<Map<String, Object>> listarApi(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long colaboradorId,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = switch (sort) {
            case "nome" -> "colaborador.nome";
            case "departamento" -> "colaborador.departamento.nome";
            case "valorTotal" -> "valorTotalMes";
            case "status" -> "status";
            default -> "id";
        };
        Page<ValeTransporteListDTO> pageData = service.listarPaginado(mes, ano, status, colaboradorId, departamentoId, q, page, size, Sort.by(direction, sortField));
        Map<String, Object> resp = new HashMap<>();
        resp.put("content", pageData.getContent());
        resp.put("currentPage", pageData.getNumber());
        resp.put("totalPages", pageData.getTotalPages());
        resp.put("totalElements", pageData.getTotalElements());
        resp.put("hasPrevious", pageData.hasPrevious());
        resp.put("hasNext", pageData.hasNext());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-store, no-cache, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return new ResponseEntity<>(resp, headers, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/api/departamentos")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> departamentosApi() {
        List<Map<String, Object>> deps = departamentoService.listarTodos().stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("nome", d.getNome());
            return m;
        }).toList();
        return ResponseEntity.ok(deps);
    }

    @GetMapping("/api/detalhe/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detalhe(@PathVariable Long id) {
        try {
            ValeTransporte v = service.buscarPorId(id);
            Map<String, Object> m = new HashMap<>();
            m.put("id", v.getId());
            m.put("colaborador", v.getColaborador() != null ? v.getColaborador().getNome() : null);
            m.put("mes", v.getMesReferencia());
            m.put("ano", v.getAnoReferencia());
            m.put("status", v.getStatus() != null ? v.getStatus().name() : null);
            m.put("linhaOnibus", v.getLinhaOnibus());
            m.put("enderecoOrigem", v.getEnderecoOrigem());
            m.put("enderecoDestino", v.getEnderecoDestino());
            m.put("valorPassagem", v.getValorPassagem());
            m.put("valorTotalMes", v.getValorTotalMes());
            m.put("valorDesconto", v.getValorDesconto());
            m.put("valorSubsidioEmpresa", v.getValorSubsidioEmpresa());
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("sucesso", false);
            err.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping(value = "/api/export/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Timed(value = "rh.beneficios.vt.export.excel", histogram = true)
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long colaboradorId,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String q,
            HttpServletRequest request) {
        Page<ValeTransporteListDTO> pageData = service.listarPaginado(mes, ano, status, colaboradorId, departamentoId, q, 0, 1000, Sort.by(Sort.Direction.DESC, "id"));
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("ValeTransporte");
            Row r0 = sh.createRow(0);
            r0.createCell(0).setCellValue("ERP Corporativo - RH Benefícios");
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Relatório Vale Transporte");
            Row r2 = sh.createRow(2);
            r2.createCell(0).setCellValue("Gerado em: " + java.time.LocalDateTime.now());
            Row r3 = sh.createRow(3);
            r3.createCell(0).setCellValue("Filtros: mes=" + (mes!=null?mes:"") + ", ano=" + (ano!=null?ano:"") + ", status=" + (status!=null?status:"") + ", colaboradorId=" + (colaboradorId!=null?colaboradorId:"") + ", departamentoId=" + (departamentoId!=null?departamentoId:"") + ", q=" + (q!=null?q:""));
            Row h = sh.createRow(5);
            String[] cols = {"Colaborador","Departamento","Viagens/Dia","Dias Úteis","Valor Total","Desconto do Colaborador","Subsidio","Status","Origem","Destino"};
            for (int i = 0; i < cols.length; i++) { Cell c = h.createCell(i); c.setCellValue(cols[i]); }
            int r = 6;
            double totVal = 0, totDesc = 0, totSubs = 0;
            for (ValeTransporteListDTO v : pageData.getContent()) {
                Row row = sh.createRow(r++);
                row.createCell(0).setCellValue(v.getNomeColaborador());
                row.createCell(1).setCellValue(v.getDepartamento());
                row.createCell(2).setCellValue(v.getViagensDia() != null ? v.getViagensDia() : 0);
                row.createCell(3).setCellValue(v.getDiasUteis() != null ? v.getDiasUteis() : 0);
                double vTot = v.getValorTotalMes() != null ? v.getValorTotalMes().doubleValue() : 0;
                double vDesc = v.getValorDesconto() != null ? v.getValorDesconto().doubleValue() : 0;
                double vSubs = v.getValorSubsidioEmpresa() != null ? v.getValorSubsidioEmpresa().doubleValue() : 0;
                row.createCell(4).setCellValue(vTot);
                row.createCell(5).setCellValue(vDesc);
                row.createCell(6).setCellValue(vSubs);
                totVal += vTot; totDesc += vDesc; totSubs += vSubs;
                row.createCell(7).setCellValue(v.getStatus() != null ? v.getStatus().name() : "");
                row.createCell(8).setCellValue(v.getEnderecoOrigem() != null ? v.getEnderecoOrigem() : "");
                row.createCell(9).setCellValue(v.getEnderecoDestino() != null ? v.getEnderecoDestino() : "");
            }
            Row t = sh.createRow(r);
            t.createCell(0).setCellValue("TOTAIS");
            t.createCell(4).setCellValue(totVal);
            t.createCell(5).setCellValue(totDesc);
            t.createCell(6).setCellValue(totSubs);
            for (int i = 0; i < cols.length; i++) sh.autoSizeColumn(i);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            byte[] bytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vale-transporte.xlsx");
            headers.setContentLength(bytes.length);
            String ip = request != null ? request.getRemoteAddr() : null;
            String detalhes = "excel;registros=" + pageData.getTotalElements() + ";filtros=" + (mes!=null?mes:"") + "/" + (ano!=null?ano:"") + ";status=" + (status!=null?status:"") + ";colab=" + (colaboradorId!=null?colaboradorId:"") + ";dep=" + (departamentoId!=null?departamentoId:"") + ";q=" + (q!=null?q:"");
            auditoriaService.registrar("EXPORTACAO","EXPORT_EXCEL","/rh/beneficios/vale-transporte/api/export/excel",null,ip,detalhes,true);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().contentType(MediaType.TEXT_PLAIN).body(("Falha ao gerar Excel: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping(value = "/api/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Timed(value = "rh.beneficios.vt.export.pdf", histogram = true)
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long colaboradorId,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String q,
            HttpServletRequest request) {
        Page<ValeTransporteListDTO> pageData = service.listarPaginado(mes, ano, status, colaboradorId, departamentoId, q, 0, 1000, Sort.by(Sort.Direction.DESC, "id"));
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\"><style>body{font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#222}table{width:100%;border-collapse:collapse}th,td{border:1px solid #ddd;padding:6px}th{background:#f5f5f5;text-align:left}.tot{font-weight:bold}</style></head><body>");
        html.append("<h1>ERP Corporativo - RH Benefícios</h1>");
        html.append("<h2>Relatório Vale Transporte</h2>");
        html.append("<p>Gerado em: ").append(java.time.LocalDateTime.now()).append("</p>");
        html.append("<p>Filtros: mes=").append(mes!=null?mes:"").append(", ano=").append(ano!=null?ano:"").append(", status=").append(status!=null?status:"").append(", colaboradorId=").append(colaboradorId!=null?colaboradorId:"").append(", departamentoId=").append(departamentoId!=null?departamentoId:"").append(", q=").append(q!=null?q:"").append("</p>");
        html.append("<table><thead><tr><th>Colaborador</th><th>Departamento</th><th>Viagens/Dia</th><th>Dias Úteis</th><th>Valor Total</th><th>Desconto do Colaborador</th><th>Subsidio</th><th>Status</th></tr></thead><tbody>");
        double totVal = 0, totDesc = 0, totSubs = 0;
        for (ValeTransporteListDTO v : pageData.getContent()) {
            html.append("<tr>")
                .append("<td>").append(v.getNomeColaborador()!=null?v.getNomeColaborador():"").append("</td>")
                .append("<td>").append(v.getDepartamento()!=null?v.getDepartamento():"").append("</td>")
                .append("<td>").append(v.getViagensDia()!=null?v.getViagensDia():0).append("</td>")
                .append("<td>").append(v.getDiasUteis()!=null?v.getDiasUteis():0).append("</td>")
                .append("<td>").append(v.getValorTotalMes()!=null?v.getValorTotalMes():java.math.BigDecimal.ZERO).append("</td>")
                .append("<td>").append(v.getValorDesconto()!=null?v.getValorDesconto():java.math.BigDecimal.ZERO).append("</td>")
                .append("<td>").append(v.getValorSubsidioEmpresa()!=null?v.getValorSubsidioEmpresa():java.math.BigDecimal.ZERO).append("</td>")
                .append("<td>").append(v.getStatus()!=null?v.getStatus().name():"").append("</td>")
                .append("</tr>");
            double vTot = v.getValorTotalMes()!=null?v.getValorTotalMes().doubleValue():0;
            double vDesc = v.getValorDesconto()!=null?v.getValorDesconto().doubleValue():0;
            double vSubs = v.getValorSubsidioEmpresa()!=null?v.getValorSubsidioEmpresa().doubleValue():0;
            totVal += vTot; totDesc += vDesc; totSubs += vSubs;
        }
        html.append("<tr class=\"tot\"><td colspan=\"4\"><strong>TOTAIS</strong></td><td>").append(totVal).append("</td><td>").append(totDesc).append("</td><td>").append(totSubs).append("</td><td></td></tr>");
        html.append("</tbody></table></body></html>");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html.toString(), null);
        builder.toStream(baos);
        try { builder.run(); } catch (Exception e) {
            return ResponseEntity.internalServerError().contentType(MediaType.TEXT_PLAIN).body(("Falha ao gerar PDF: " + e.getMessage()).getBytes());
        }
        byte[] pdf = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vale-transporte.pdf");
        headers.setContentLength(pdf.length);
        String ip = request != null ? request.getRemoteAddr() : null;
        String detalhes = "pdf;registros=" + pageData.getTotalElements() + ";filtros=" + (mes!=null?mes:"") + "/" + (ano!=null?ano:"") + ";status=" + (status!=null?status:"") + ";colab=" + (colaboradorId!=null?colaboradorId:"") + ";dep=" + (departamentoId!=null?departamentoId:"") + ";q=" + (q!=null?q:"");
        auditoriaService.registrar("EXPORTACAO","EXPORT_PDF","/rh/beneficios/vale-transporte/api/export/pdf",null,ip,detalhes,true);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping(value = "/api/solicitacao", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> criarSolicitacao(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Object colabIdObj = body.get("colaboradorId");
            Object dataInicioObj = body.get("dataInicio");
            Object enderecoResidenciaObj = body.get("enderecoResidencia");
            Object tipoTransporte1Obj = body.get("tipoTransporte1");
            Object linha1Obj = body.get("linha1");
            Object temIntegracaoObj = body.get("temIntegracao");
            Object viagensDiaObj = body.get("viagensDia");
            Object observacoesObj = body.get("observacoes");
            if (colabIdObj == null || dataInicioObj == null || enderecoResidenciaObj == null || tipoTransporte1Obj == null || viagensDiaObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Campos obrigatórios ausentes"));
            }
            Long colaboradorId = Long.valueOf(String.valueOf(colabIdObj));
            java.time.LocalDate dataInicio = java.time.LocalDate.parse(String.valueOf(dataInicioObj), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            java.time.LocalDate hoje = java.time.LocalDate.now();
            if (dataInicio.isBefore(hoje)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Data de início não pode ser anterior a hoje"));
            }
            com.jaasielsilva.portalceo.model.Colaborador colab = colaboradorService.buscarPorId(colaboradorId);
            if (service.existeValeAtivoNoPeriodo(colab, hoje.getMonthValue(), hoje.getYear())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Colaborador já possui vale transporte ativo para o período atual"));
            }
            ValeTransporte vt = new ValeTransporte();
            vt.setColaborador(colab);
            vt.setMesReferencia(hoje.getMonthValue());
            vt.setAnoReferencia(hoje.getYear());
            vt.setDataAdesao(dataInicio);
            vt.setEnderecoOrigem(String.valueOf(enderecoResidenciaObj));
            vt.setEnderecoDestino("Trabalho");
            vt.setLinhaOnibus(String.valueOf(linha1Obj));
            int viagensDia = Integer.valueOf(String.valueOf(viagensDiaObj));
            if (viagensDia < 1 || viagensDia > 8) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Quantidade de viagens por dia deve ser entre 1 e 8"));
            }
            vt.setViagensDia(viagensDia);
            ConfiguracaoValeTransporteDTO cfg = new ConfiguracaoValeTransporteDTO();
            Object diasUteisObj = body.get("diasUteis");
            vt.setDiasUteis(diasUteisObj != null ? Integer.valueOf(String.valueOf(diasUteisObj)) : cfg.getDiasUteisPadrao());
            vt.setStatus(ValeTransporte.StatusValeTransporte.ATIVO);
            vt.setObservacoes(observacoesObj != null ? String.valueOf(observacoesObj) : null);
            Object valorPassagemObj = body.get("valorPassagem");
            Object percentualDescontoObj = body.get("percentualDesconto");
            java.math.BigDecimal valorPassagem = valorPassagemObj != null ? new java.math.BigDecimal(String.valueOf(valorPassagemObj)) : cfg.getValorPassagem();
            java.math.BigDecimal percentualDesconto = percentualDescontoObj != null ? new java.math.BigDecimal(String.valueOf(percentualDescontoObj)) : cfg.getPercentualDesconto();
            vt.setValorPassagem(valorPassagem);
            vt.setPercentualDesconto(percentualDesconto);
            service.salvar(vt);
            resp.put("success", true);
            resp.put("id", vt.getId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/duplicados/suspender", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Timed(value = "rh.beneficios.vt.duplicados.suspender", histogram = true)
    public ResponseEntity<Map<String,Object>> suspenderDuplicados(@RequestParam Integer mes,
                                                                  @RequestParam Integer ano,
                                                                  @RequestParam(required = false) String motivo,
                                                                  HttpServletRequest request) {
        Map<String,Object> r = new HashMap<>();
        try {
            int qtd = service.suspenderDuplicadosAtivos(mes, ano, motivo);
            String ip = request != null ? request.getRemoteAddr() : null;
            auditoriaService.registrar("BENEFICIO","VT_DUPLICADOS_SUSPENSOS","/rh/beneficios/vale-transporte/api/duplicados/suspender",null,ip,"mes="+mes+";ano="+ano+";qtd="+qtd,true);
            r.put("sucesso", true);
            r.put("suspensos", qtd);
            r.put("mensagem", "Registros duplicados suspensos: " + qtd);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            r.put("sucesso", false);
            r.put("erro", e.getMessage());
            return ResponseEntity.status(500).body(r);
        }
    }
}
