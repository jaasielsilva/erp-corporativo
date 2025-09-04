package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.service.ProcessoAdesaoService;
import com.jaasielsilva.portalceo.service.RhRelatorioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller principal do módulo de Recursos Humanos (RH).
 * Responsável por gerenciar as rotas principais e redirecionamentos do módulo RH.
 */
@Controller
@RequestMapping("/rh")
public class RhController {

    private static final Logger logger = LoggerFactory.getLogger(RhController.class);
    
    @Autowired
    private ProcessoAdesaoService processoAdesaoService;
    
    @Autowired
    private RhRelatorioService rhRelatorioService;
    
    /**
     * API REST: Obter estatísticas dos processos de adesão
     */
    @GetMapping("/api/processos-adesao/estatisticas")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> obterEstatisticasProcessos() {
        try {
            Map<String, Object> estatisticas = processoAdesaoService.obterEstatisticas();
            
            // Formatar resposta para compatibilidade com frontend
            Map<String, Object> response = new HashMap<>();
            response.put("processosAguardandoAprovacao", estatisticas.get("aguardando_aprovacao"));
            response.put("processosAprovados", estatisticas.get("aprovado"));
            response.put("processosRejeitados", estatisticas.get("rejeitado"));
            response.put("processosHoje", estatisticas.get("processosHoje"));
            response.put("success", true);
            
            logger.info("Estatísticas API carregadas: {} processos aguardando aprovação", estatisticas.get("aguardando_aprovacao"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas via API", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erro ao carregar estatísticas: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Página principal do módulo RH
     * Dashboard central com opções de navegação
     */
    @GetMapping
    public String moduloRh(Model model) {
        logger.info("Acessando dashboard principal do módulo RH");
        
        try {
            // Carregar estatísticas dos processos de adesão
            Map<String, Object> estatisticas = processoAdesaoService.obterEstatisticas();
            model.addAttribute("estatisticas", estatisticas);
            
            // Extrair valores específicos para facilitar o uso no template
            model.addAttribute("processosAguardandoAprovacao", estatisticas.get("aguardando_aprovacao"));
            model.addAttribute("processosAprovados", estatisticas.get("aprovado"));
            model.addAttribute("processosRejeitados", estatisticas.get("rejeitado"));
            model.addAttribute("processosHoje", estatisticas.get("processosHoje"));
            
            logger.info("Estatísticas carregadas: {} processos aguardando aprovação", estatisticas.get("aguardando_aprovacao"));
            
        } catch (Exception e) {
            logger.error("Erro ao carregar estatísticas do dashboard RH", e);
            // Valores padrão em caso de erro
            model.addAttribute("processosAguardandoAprovacao", 0);
            model.addAttribute("processosAprovados", 0);
            model.addAttribute("processosRejeitados", 0);
            model.addAttribute("processosHoje", 0);
        }
        
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recursos Humanos - Central");
        return "rh/dashboard";
    }

    /**
     * Dashboard do módulo RH
     */
    @GetMapping("/dashboard")
    public String dashboardRh(Model model) {
        logger.info("Acessando dashboard do módulo RH");
        
        try {
            // Carregar estatísticas dos processos de adesão
            Map<String, Object> estatisticas = processoAdesaoService.obterEstatisticas();
            model.addAttribute("estatisticas", estatisticas);
            
            // Extrair valores específicos para facilitar o uso no template
            model.addAttribute("processosAguardandoAprovacao", estatisticas.get("aguardando_aprovacao"));
            model.addAttribute("processosAprovados", estatisticas.get("aprovado"));
            model.addAttribute("processosRejeitados", estatisticas.get("rejeitado"));
            model.addAttribute("processosHoje", estatisticas.get("processosHoje"));
            
            logger.info("Estatísticas carregadas: {} processos aguardando aprovação", estatisticas.get("aguardando_aprovacao"));
            
        } catch (Exception e) {
            logger.error("Erro ao carregar estatísticas do dashboard RH", e);
            // Valores padrão em caso de erro
            model.addAttribute("processosAguardandoAprovacao", 0);
            model.addAttribute("processosAprovados", 0);
            model.addAttribute("processosRejeitados", 0);
            model.addAttribute("processosHoje", 0);
        }
        
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Dashboard - Recursos Humanos");
        return "rh/dashboard";
    }
    
    // ===============================
    // ENDPOINTS PARA RELATÓRIOS DETALHADOS
    // ===============================
    
    /**
     * Gera relatório analítico da folha de pagamento
     */
    @GetMapping("/api/relatorios/folha-analitica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    @ResponseBody
    public ResponseEntity<?> gerarRelatorioFolhaAnalitica(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long departamentoId) {
        try {
            RhRelatorioService.RelatorioFolhaAnalitica relatorio = 
                rhRelatorioService.gerarRelatorioFolhaAnalitica(inicio, fim, departamentoId);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório analítico da folha", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao gerar relatório"));
        }
    }
    
    /**
     * Gera relatório gerencial resumido
     */
    @GetMapping("/api/relatorios/gerencial")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    @ResponseBody
    public ResponseEntity<?> gerarRelatorioGerencial(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            RhRelatorioService.RelatorioGerencial relatorio = 
                rhRelatorioService.gerarRelatorioGerencial(inicio, fim);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório gerencial", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao gerar relatório"));
        }
    }
    
    /**
     * Gera relatório de obrigações trabalhistas
     */
    @GetMapping("/api/relatorios/obrigacoes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    @ResponseBody
    public ResponseEntity<?> gerarRelatorioObrigacoes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            RhRelatorioService.RelatorioObrigacoes relatorio = 
                rhRelatorioService.gerarRelatorioObrigacoes(inicio, fim);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de obrigações", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao gerar relatório"));
        }
    }
    
    /**
     * Gera relatório por departamento
     */
    @GetMapping("/api/relatorios/departamento")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    @ResponseBody
    public ResponseEntity<?> gerarRelatorioDepartamento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam Long departamentoId) {
        try {
            RhRelatorioService.RelatorioDepartamento relatorio = 
                rhRelatorioService.gerarRelatorioDepartamento(inicio, fim, departamentoId);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório por departamento", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao gerar relatório"));
        }
    }
    
    /**
     * Gera relatório comparativo mensal
     */
    @GetMapping("/api/relatorios/comparativo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    @ResponseBody
    public ResponseEntity<?> gerarRelatorioComparativo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            RhRelatorioService.RelatorioComparativo relatorio = 
                rhRelatorioService.gerarRelatorioComparativo(inicio, fim);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório comparativo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao gerar relatório"));
        }
    }
    
    /**
     * Gera relatório de benefícios
     */
    @GetMapping("/api/relatorios/beneficios")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    @ResponseBody
    public ResponseEntity<?> gerarRelatorioBeneficios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            RhRelatorioService.RelatorioBeneficios relatorio = 
                rhRelatorioService.gerarRelatorioBeneficios(inicio, fim);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de benefícios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao gerar relatório"));
        }
    }
    
    /**
     * Exporta relatório em formato CSV
     */
    @GetMapping("/api/relatorios/exportar/csv")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<String> exportarRelatorioCSV(
            @RequestParam String tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long departamentoId) {
        try {
            String csvContent = gerarCSVPorTipo(tipo, inicio, fim, departamentoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", 
                "relatorio_" + tipo + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
        } catch (Exception e) {
            logger.error("Erro ao exportar relatório CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao gerar relatório CSV");
        }
    }
    
    /**
     * Exporta relatório em formato Excel (simulado como CSV)
     */
    @GetMapping("/api/relatorios/exportar/excel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<String> exportarRelatorioExcel(
            @RequestParam String tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long departamentoId) {
        try {
            String csvContent = gerarCSVPorTipo(tipo, inicio, fim, departamentoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDispositionFormData("attachment", 
                "relatorio_" + tipo + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xls");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
        } catch (Exception e) {
            logger.error("Erro ao exportar relatório Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao gerar relatório Excel");
        }
    }
    
    /**
     * Exporta relatório em formato PDF (simulado como texto)
     */
    @GetMapping("/api/relatorios/exportar/pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<String> exportarRelatorioPDF(
            @RequestParam String tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long departamentoId) {
        try {
            String pdfContent = gerarPDFPorTipo(tipo, inicio, fim, departamentoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "relatorio_" + tipo + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
        } catch (Exception e) {
            logger.error("Erro ao exportar relatório PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao gerar relatório PDF");
        }
    }
    
    // ===============================
    // MÉTODOS AUXILIARES PARA EXPORTAÇÃO
    // ===============================
    
    private String gerarCSVPorTipo(String tipo, LocalDate inicio, LocalDate fim, Long departamentoId) {
        StringBuilder csv = new StringBuilder();
        
        switch (tipo.toLowerCase()) {
            case "folha-analitica":
                RhRelatorioService.RelatorioFolhaAnalitica folhaAnalitica = 
                    rhRelatorioService.gerarRelatorioFolhaAnalitica(inicio, fim, departamentoId);
                csv.append("Nome,Departamento,Cargo,Salário Base,INSS,IRRF,Total Descontos,Salário Líquido\n");
                folhaAnalitica.getItens().forEach(item -> {
                    csv.append(String.format("%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                        item.getNomeColaborador(), item.getDepartamento(), item.getCargo(),
                        item.getSalarioBase(), item.getInss(), item.getIrrf(),
                        item.getTotalDescontos(), item.getSalarioLiquido()));
                });
                break;
                
            case "gerencial":
                RhRelatorioService.RelatorioGerencial gerencial = 
                    rhRelatorioService.gerarRelatorioGerencial(inicio, fim);
                csv.append("Departamento,Colaboradores,Custo Total,Média Salarial\n");
                gerencial.getResumoPorDepartamento().values().forEach(resumo -> {
                    csv.append(String.format("%s,%d,%.2f,%.2f\n",
                        resumo.getDepartamento(), resumo.getColaboradores(),
                        resumo.getCustoTotal(), resumo.getMediaSalarial()));
                });
                break;
                
            case "obrigacoes":
                RhRelatorioService.RelatorioObrigacoes obrigacoes = 
                    rhRelatorioService.gerarRelatorioObrigacoes(inicio, fim);
                csv.append("Nome,Salário Base,INSS,IRRF,FGTS\n");
                obrigacoes.getItens().forEach(item -> {
                    csv.append(String.format("%s,%.2f,%.2f,%.2f,%.2f\n",
                        item.getNomeColaborador(), item.getSalarioBase(),
                        item.getInss(), item.getIrrf(), item.getFgts()));
                });
                break;
                
            case "beneficios":
                RhRelatorioService.RelatorioBeneficios beneficios = 
                    rhRelatorioService.gerarRelatorioBeneficios(inicio, fim);
                csv.append("Benefício,Colaboradores Atendidos,Custo Total\n");
                beneficios.getBeneficiosPorTipo().values().forEach(item -> {
                    csv.append(String.format("%s,%d,%.2f\n",
                        item.getNomeBeneficio(), item.getColaboradoresAtendidos(),
                        item.getCustoTotal()));
                });
                break;
                
            default:
                csv.append("Tipo de relatório não suportado\n");
        }
        
        return csv.toString();
    }
    
    private String gerarPDFPorTipo(String tipo, LocalDate inicio, LocalDate fim, Long departamentoId) {
        // Simulação de conteúdo PDF como texto formatado
        StringBuilder pdf = new StringBuilder();
        pdf.append("RELATÓRIO - ").append(tipo.toUpperCase()).append("\n");
        pdf.append("Período: ").append(inicio).append(" a ").append(fim).append("\n\n");
        
        // Adicionar conteúdo baseado no CSV
        String csvContent = gerarCSVPorTipo(tipo, inicio, fim, departamentoId);
        pdf.append(csvContent.replace(",", " | "));
        
        return pdf.toString();
    }
}