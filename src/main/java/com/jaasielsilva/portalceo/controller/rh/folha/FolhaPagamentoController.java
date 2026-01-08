package com.jaasielsilva.portalceo.controller.rh.folha;

import com.jaasielsilva.portalceo.model.FolhaPagamento;
import com.jaasielsilva.portalceo.model.Holerite;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.FolhaPagamentoService;
import com.jaasielsilva.portalceo.service.HoleriteService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.ResumoFolhaService;
import com.jaasielsilva.portalceo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.text.NumberFormat;
import java.util.Locale;
import java.awt.Color;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rh/folha-pagamento")
public class FolhaPagamentoController {

    private static final Logger logger = LoggerFactory.getLogger(FolhaPagamentoController.class);

    @Autowired
    private FolhaPagamentoService folhaPagamentoService;

    @Autowired
    private HoleriteService holeriteService;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ResumoFolhaService resumoFolhaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private com.jaasielsilva.portalceo.service.HoleriteEmailService holeriteEmailService;

    

    @Autowired
    private com.jaasielsilva.portalceo.service.ContaBancariaService contaBancariaService;

    /**
     * Página principal da folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping
    public String index(Model model) {
        model.addAttribute("folhasRecentes", folhaPagamentoService.buscarFolhasRecentes());
        
        // Dados do mês atual
        LocalDate hoje = LocalDate.now();
        Optional<FolhaPagamento> folhaAtual = folhaPagamentoService.buscarPorMesAno(hoje.getMonthValue(), hoje.getYear());
        model.addAttribute("folhaAtual", folhaAtual.orElse(null));
        if (folhaAtual.isPresent()) {
            com.jaasielsilva.portalceo.service.HoleriteService.ResumoFolhaDTO resumoAtual = holeriteService.calcularResumoFolha(folhaAtual.get().getId());
            model.addAttribute("resumoAtual", resumoAtual);
        }
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        
        return "rh/folha-pagamento/index";
    }

    /**
     * Formulário para gerar nova folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @GetMapping("/gerar")
    public String gerar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("nome").ascending());
        Page<com.jaasielsilva.portalceo.model.Colaborador> colaboradoresPage = colaboradorService.listarAtivosPaginado(pageable);
        model.addAttribute("colaboradores", colaboradoresPage);
        // Otimização: Usar projeção para dropdown
        model.addAttribute("departamentos", departamentoService.listarParaDropdown());
        
        // Verificar se já existe folha para o mês atual
        LocalDate hoje = LocalDate.now();
        boolean existeFolha = folhaPagamentoService.existeFolhaPorMesAno(hoje.getMonthValue(), hoje.getYear());
        model.addAttribute("existeFolhaAtual", existeFolha);
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        YearMonth ym = YearMonth.of(hoje.getYear(), hoje.getMonthValue());
        model.addAttribute("diasMesAtual", ym.lengthOfMonth());
        model.addAttribute("colaboradoresResumo", java.util.Collections.emptyList());
        model.addAttribute("currentPage", colaboradoresPage.getNumber());
        model.addAttribute("totalPages", colaboradoresPage.getTotalPages());
        model.addAttribute("totalElements", colaboradoresPage.getTotalElements());
        model.addAttribute("hasPrevious", colaboradoresPage.hasPrevious());
        model.addAttribute("hasNext", colaboradoresPage.hasNext());

        long totalAtivos = colaboradorService.contarAtivos();
        long totalClt = colaboradorService.contarCltAtivos();
        long totalPj = colaboradorService.contarPjAtivos();
        long totalEstagiario = colaboradorService.contarEstagiariosAtivos();
        model.addAttribute("totalAtivos", totalAtivos);
        model.addAttribute("totalClt", totalClt);
        model.addAttribute("totalPj", totalPj);
        model.addAttribute("totalEstagiario", totalEstagiario);

        return "rh/folha-pagamento/gerar";
    }

    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @GetMapping("/api/colaboradores-resumo")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> listarColaboradoresResumo(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("nome").ascending());
        Page<com.jaasielsilva.portalceo.model.Colaborador> colaboradoresPage = colaboradorService.listarAtivosPaginado(pageable);
        LocalDate hoje = LocalDate.now();
        YearMonth ym = YearMonth.of(hoje.getYear(), hoje.getMonthValue());
        java.util.List<com.jaasielsilva.portalceo.dto.ColaboradorResumoFolhaDTO> content = resumoFolhaService.criarResumoBatch(colaboradoresPage.getContent(), ym);

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", colaboradoresPage.getNumber());
        resp.put("totalPages", colaboradoresPage.getTotalPages());
        resp.put("totalElements", colaboradoresPage.getTotalElements());
        resp.put("hasPrevious", colaboradoresPage.hasPrevious());
        resp.put("hasNext", colaboradoresPage.hasNext());
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    /**
     * Processa geração da folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping("/processar")
    public String processar(@RequestParam Integer mes,
                           @RequestParam Integer ano,
                           @RequestParam(name = "departamento", required = false) Long departamentoId,
                           @RequestParam(name = "tipoFolha", defaultValue = "normal") String tipoFolha,
                           RedirectAttributes redirectAttributes) {
        try {
            // Obter usuário logado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmailLeve(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            FolhaPagamento folha = folhaPagamentoService.gerarFolhaPagamento(mes, ano, usuario, departamentoId, tipoFolha);
            
            redirectAttributes.addFlashAttribute("mensagem", 
                "Folha de pagamento " + mes + "/" + ano + " gerada com sucesso!");
            
            return "redirect:/rh/folha-pagamento/visualizar/" + folha.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao gerar folha: " + e.getMessage());
            return "redirect:/rh/folha-pagamento/gerar";
        }
    }

    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping(value = "/processar-async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> processarAsync(@RequestParam Integer mes,
                                                                        @RequestParam Integer ano,
                                                                        @RequestParam(name = "departamento", required = false) Long departamentoId,
                                                                        @RequestParam(name = "tipoFolha", defaultValue = "normal") String tipoFolha) {
        // Bloqueia início se já houver folha gerada/fechada para o mês/ano
        if (folhaPagamentoService.existeFolhaPorMesAno(mes, ano)) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("accepted", false);
            body.put("error", "Já existe folha gerada ou fechada para " + String.format("%02d/%d", mes, ano));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioService.buscarPorEmailLeve(auth.getName()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        String jobId = folhaPagamentoService.iniciarProcessamentoAsync(mes, ano, usuario, departamentoId, tipoFolha);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("accepted", true);
        resp.put("jobId", jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
    }

    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @GetMapping(value = "/status-processamento", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> statusProcessamento(@RequestParam String jobId) {
        java.util.Map<String, Object> status = folhaPagamentoService.obterStatusProcessamento(jobId);
        return ResponseEntity.ok(status);
    }

    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping(value = "/processar/pause")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> pauseJob(@RequestParam String jobId) {
        folhaPagamentoService.pause(jobId);
        return ResponseEntity.ok(java.util.Map.of("status", "paused"));
    }

    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping(value = "/processar/resume")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> resumeJob(@RequestParam String jobId) {
        folhaPagamentoService.resume(jobId);
        return ResponseEntity.ok(java.util.Map.of("status", "resumed"));
    }

    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping(value = "/processar/cancel")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> cancelJob(@RequestParam String jobId) {
        folhaPagamentoService.cancel(jobId);
        return ResponseEntity.ok(java.util.Map.of("status", "canceled"));
    }

    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping(value = "/metrics/processamento", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> registrarMetricas(@RequestBody java.util.Map<String, Object> body) {
        String jobId = String.valueOf(body.getOrDefault("jobId", ""));
        Object folhaId = body.get("folhaId");
        String status = String.valueOf(body.getOrDefault("status", ""));
        String mes = String.valueOf(body.getOrDefault("mes", ""));
        String ano = String.valueOf(body.getOrDefault("ano", ""));
        Object durationMsObj = body.get("durationMs");
        Long durationMs = null;
        try { if (durationMsObj != null) durationMs = Long.valueOf(String.valueOf(durationMsObj)); } catch (Exception ignored) {}
        String startedAt = String.valueOf(body.getOrDefault("startedAt", ""));
        String endedAt = String.valueOf(body.getOrDefault("endedAt", ""));
        String userAgent = String.valueOf(body.getOrDefault("userAgent", ""));
        String errMsg = String.valueOf(body.getOrDefault("errorMessage", ""));

        String msg = String.format(
                "Métricas Folha [jobId=%s, folhaId=%s, ref=%s/%s, status=%s] Tempo total=%.2fs (durationMs=%s) start=%s end=%s UA=%s%s",
                jobId,
                folhaId,
                mes,
                ano,
                status,
                durationMs != null ? (durationMs / 1000.0) : -1.0,
                String.valueOf(durationMs),
                startedAt,
                endedAt,
                userAgent,
                (errMsg != null && !errMsg.isBlank() ? (" | error=" + errMsg) : "")
        );
        logger.info(msg);
        return ResponseEntity.accepted().build();
    }

    /**
     * Visualiza uma folha de pagamento específica
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/visualizar/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        Optional<FolhaPagamento> folhaOpt = folhaPagamentoService.buscarPorId(id);
        if (folhaOpt.isEmpty()) {
            return "redirect:/rh/folha-pagamento";
        }
        
        FolhaPagamento folha = folhaOpt.get();
        model.addAttribute("folha", folha);
        model.addAttribute("holerites", java.util.Collections.emptyList());
        model.addAttribute("resumo", holeriteService.calcularResumoFolha(id));
        model.addAttribute("contasBancarias", contaBancariaService.listarContasAtivas());
        
        return "rh/folha-pagamento/visualizar";
    }

    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/api/folha/{id}/holerites")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> listarHoleritesFolha(
            @PathVariable Long id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "q", required = false) String q) {
        org.springframework.data.domain.Page<com.jaasielsilva.portalceo.repository.HoleriteRepository.HoleriteListProjection> pagina = holeriteService.listarPorFolhaPaginado(id, page, size, q);
        java.util.List<java.util.Map<String, Object>> content = pagina.getContent().stream().map(h -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", h.getId());
            m.put("nome", h.getColaboradorNome());
            m.put("departamento", h.getDepartamentoNome());
            m.put("salarioBase", h.getSalarioBase());
            m.put("totalProventos", h.getTotalProventos());
            m.put("totalDescontos", h.getTotalDescontos());
            m.put("salarioLiquido", h.getSalarioLiquido());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", pagina.getNumber());
        resp.put("totalPages", pagina.getTotalPages());
        resp.put("totalElements", pagina.getTotalElements());
        resp.put("hasPrevious", pagina.hasPrevious());
        resp.put("hasNext", pagina.hasNext());
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping(value = "/api/colaborador/{colaboradorId}/holerites", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> listarHoleritesPorColaborador(
            @PathVariable Long colaboradorId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "ano", required = false) Integer ano,
            @RequestParam(name = "mes", required = false) Integer mes) {
        org.springframework.data.domain.Page<com.jaasielsilva.portalceo.repository.HoleriteRepository.HoleriteColabListProjection> pagina = holeriteService.listarPorColaboradorPaginado(colaboradorId, page, size, ano, mes);
        java.util.List<java.util.Map<String, Object>> content = pagina.getContent().stream().map(h -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", h.getId());
            m.put("mes", h.getMesReferencia());
            m.put("ano", h.getAnoReferencia());
            m.put("salarioBase", h.getSalarioBase());
            m.put("totalProventos", h.getTotalProventos());
            m.put("totalDescontos", h.getTotalDescontos());
            m.put("salarioLiquido", h.getSalarioLiquido());
            m.put("descontoValeTransporte", h.getDescontoValeTransporte());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("content", content);
        resp.put("currentPage", pagina.getNumber());
        resp.put("totalPages", pagina.getTotalPages());
        resp.put("totalElements", pagina.getTotalElements());
        resp.put("hasPrevious", pagina.hasPrevious());
        resp.put("hasNext", pagina.hasNext());
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping(value = "/api/holerite/{id}/colaborador", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> obterColaboradorPorHolerite(@PathVariable Long id) {
        java.util.Optional<Holerite> holeriteOpt = holeriteService.buscarPorId(id);
        if (holeriteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("erro", "Holerite não encontrado"));
        }
        Holerite holerite = holeriteOpt.get();
        com.jaasielsilva.portalceo.model.Colaborador colab = holerite.getColaborador();
        if (colab == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("erro", "Colaborador não associado"));
        }
        return ResponseEntity.ok(java.util.Map.of("id", colab.getId(), "nome", colab.getNome()));
    }

    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping(value = "/api/folha/{id}/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> debugFolha(@PathVariable Long id) {
        com.jaasielsilva.portalceo.service.HoleriteService.ResumoFolhaDTO r = holeriteService.calcularResumoFolha(id);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("totalLiquido", r.getTotalLiquido());
        resp.put("totalProventos", r.getTotalProventos());
        resp.put("totalDescontos", r.getTotalDescontos());
        resp.put("quantidadeHolerites", r.getQuantidadeHolerites());
        return ResponseEntity.ok(resp);
    }

    /**
     * Lista todas as folhas de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) Integer ano, Model model) {
        if (ano != null) {
            model.addAttribute("folhas", folhaPagamentoService.buscarPorAno(ano));
            model.addAttribute("anoSelecionado", ano);
        } else {
            model.addAttribute("folhas", folhaPagamentoService.listarTodas());
        }
        
        return "rh/folha-pagamento/listar";
    }

    /**
     * Fecha uma folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping("/fechar/{id}")
    public String fechar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            folhaPagamentoService.fecharFolha(id, usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Folha de pagamento fechada com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao fechar folha: " + e.getMessage());
        }
        
        return "redirect:/rh/folha-pagamento/visualizar/" + id;
    }

    /**
     * Realiza o pagamento da folha
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarFinanceiro()")
    @PostMapping("/pagar")
    public String pagar(@RequestParam Long folhaId,
                        @RequestParam Long contaBancariaId,
                        RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmailLeve(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            folhaPagamentoService.pagarFolha(folhaId, contaBancariaId, usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Pagamento da folha realizado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao pagar folha: " + e.getMessage());
        }

        return "redirect:/rh/folha-pagamento/visualizar/" + folhaId;
    }

    /**
     * Cancela uma folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")
    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            folhaPagamentoService.cancelarFolha(id, usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Folha de pagamento cancelada com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar folha: " + e.getMessage());
        }
        
        return "redirect:/rh/folha-pagamento";
    }

    /**
     * Exibe holerite individual
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH() or @holeriteService.podeVerHolerite(#id)")
    @GetMapping("/holerite/{id}")
    public String holerite(@PathVariable Long id, Model model) {
        Optional<Holerite> holeriteOpt = holeriteService.buscarPorId(id);
        if (holeriteOpt.isEmpty()) {
            return "redirect:/rh/folha-pagamento";
        }
        
        Holerite holerite = holeriteOpt.get();
        model.addAttribute("holerite", holerite);
        model.addAttribute("numeroHolerite", holeriteService.gerarNumeroHolerite(holerite));
        model.addAttribute("periodoReferencia", holeriteService.gerarDescricaoPeriodo(holerite));
        model.addAttribute("dataReferencia", holeriteService.gerarDataReferencia(holerite));
        
        return "rh/folha-pagamento/holerite";
    }

    /**
     * Lista holerites por colaborador
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/holerites/colaborador/{colaboradorId}")
    public String holeritesPorColaborador(@PathVariable Long colaboradorId,
                                          @RequestParam(required = false) Integer ano,
                                          @RequestParam(required = false) Integer mes,
                                          Model model) {
        try {
            java.util.List<Holerite> holerites;
            if (ano != null) {
                holerites = holeriteService.listarPorColaboradorEAno(colaboradorId, ano);
            } else {
                holerites = holeriteService.listarPorColaborador(colaboradorId);
            }
            if (mes != null) {
                holerites = holerites.stream()
                        .filter(h -> h.getFolhaPagamento() != null && h.getFolhaPagamento().getMesReferencia() != null && h.getFolhaPagamento().getMesReferencia().equals(mes))
                        .collect(java.util.stream.Collectors.toList());
            }
            model.addAttribute("holerites", holerites);
            model.addAttribute("colaborador", colaboradorService.buscarPorId(colaboradorId));
            return "rh/folha-pagamento/holerites-colaborador";
        } catch (Exception e) {
            return "redirect:/rh/folha-pagamento";
        }
    }

    /**
     * Página de descontos
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/descontos")
    @io.micrometer.core.annotation.Timed(value = "rh.folha.descontos.page", histogram = true, percentiles = {0.5, 0.95})
    public String descontos(Model model) {
        return "rh/folha-pagamento/descontos";
    }

    /**
     * Relatórios da folha de pagamento
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/relatorios")
    public String relatorios(@RequestParam(name = "tipoFolha", required = false) String tipoFolha,
                             Model model) {
        // Otimização: Carregar apenas projeção de ID e Nome dos departamentos
        model.addAttribute("departamentos", departamentoService.listarParaDropdown());
        
        // Limitar a quantidade de folhas recentes buscadas para reduzir carga
        // Otimização: Usar projeção
        model.addAttribute("folhasRecentes", folhaPagamentoService.buscarFolhasRecentesResumo());
        
        // Dados do mês atual para resumo
        LocalDate hoje = LocalDate.now();
        Optional<FolhaPagamento> folhaAtual;
        String tipoKey = tipoFolha == null || tipoFolha.isBlank() ? "normal" : tipoFolha.toLowerCase();
        
        // Otimização: Evitar queries desnecessárias se não for o caso comum
        if ("ferias".equals(tipoKey) || "decimo_terceiro".equals(tipoKey)) {
            folhaAtual = folhaPagamentoService.buscarPorMesAnoTipo(hoje.getMonthValue(), hoje.getYear(), tipoKey);
        } else {
            folhaAtual = folhaPagamentoService.buscarPorMesAno(hoje.getMonthValue(), hoje.getYear());
        }
        
        // Otimização: calcularResumoFolha agora usa cache implementado no Service
        if (folhaAtual.isPresent()) {
            model.addAttribute("resumoAtual", holeriteService.calcularResumoFolha(folhaAtual.get().getId()));
        }
        model.addAttribute("tipoFolhaSelecionado", tipoKey);
        
        return "rh/folha-pagamento/relatorios";
    }

    /**
     * Exporta o holerite em PDF
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH() or @holeriteService.podeVerHolerite(#id)")
    @GetMapping("/holerite/{id}/pdf")
    public void holeritePdf(@PathVariable Long id, HttpServletResponse response) throws java.io.IOException {
        Optional<Holerite> holeriteOpt = holeriteService.buscarPorId(id);
        if (holeriteOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Holerite h = holeriteOpt.get();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=holerite_" + id + ".pdf");

        Context ctx = new Context(new Locale("pt", "BR"));
        ctx.setVariable("holerite", h);
        ctx.setVariable("numeroHolerite", holeriteService.gerarNumeroHolerite(h));
        ctx.setVariable("periodoReferencia", holeriteService.gerarDescricaoPeriodo(h));
        ctx.setVariable("dataReferencia", holeriteService.gerarDataReferencia(h));
        ctx.setVariable("pdf", true);

        String html = templateEngine.process("rh/folha-pagamento/holerite-pdf", ctx);

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(response.getOutputStream());
        try {
            builder.run();
        } catch (Exception e) {
            throw new java.io.IOException("Erro ao gerar PDF", e);
        }
    }
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH() or @holeriteService.podeVerHolerite(#id)")
    @PostMapping(value = "/holerite/{id}/email", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> enviarHoleriteEmail(@PathVariable Long id,
                                                 @RequestParam(required = false) String email) {
        Optional<Holerite> holeriteOpt = holeriteService.buscarPorId(id);
        if (holeriteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Holerite não encontrado");
        }

        Holerite h = holeriteOpt.get();
        try {
            holeriteEmailService.enviar(h, email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email do colaborador não disponível");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao gerar ou enviar PDF");
        }
        String destinatarioMsg = email != null && !email.isBlank() ? email : (h.getColaborador() != null ? h.getColaborador().getEmail() : "");
        return ResponseEntity.ok("Holerite enviado para " + destinatarioMsg);
    }

    private byte[] gerarPdfHolerite(Holerite h) throws java.io.IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        Context ctx = new Context(new Locale("pt", "BR"));
        ctx.setVariable("holerite", h);
        ctx.setVariable("numeroHolerite", holeriteService.gerarNumeroHolerite(h));
        ctx.setVariable("periodoReferencia", holeriteService.gerarDescricaoPeriodo(h));
        ctx.setVariable("dataReferencia", holeriteService.gerarDataReferencia(h));
        ctx.setVariable("pdf", true);

        String html = templateEngine.process("rh/folha-pagamento/holerite-pdf", ctx);

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        try {
            builder.run();
        } catch (Exception e) {
            throw new java.io.IOException("Erro ao gerar PDF", e);
        }
        return baos.toByteArray();
    }
    /**
     * Página de seleção de holerite
     */
    @PreAuthorize("@globalControllerAdvice.podeAcessarRH()")
    @GetMapping("/holerite")
    public String selecionarHolerite(Model model) {
        LocalDate hoje = LocalDate.now();
        model.addAttribute("mesAtual", hoje.getMonthValue());
        model.addAttribute("anoAtual", hoje.getYear());
        model.addAttribute("meses", java.util.stream.IntStream.rangeClosed(1, 12).boxed().toList());
        model.addAttribute("anos", java.util.stream.IntStream.rangeClosed(hoje.getYear() - 2, hoje.getYear() + 1).boxed().toList());
        return "rh/folha-pagamento/holerite-index";
    }
}
