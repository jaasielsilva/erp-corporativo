package com.jaasielsilva.portalceo.controller.marketing;

import com.jaasielsilva.portalceo.model.CampanhaMarketing;
import com.jaasielsilva.portalceo.service.CampanhaMarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/marketing/relatorios")
@RequiredArgsConstructor
@PreAuthorize("@globalControllerAdvice.podeAcessarMarketing()")
public class RelatorioMarketingController {

    private final CampanhaMarketingService campanhaService;

    @GetMapping
    public String relatorios(Model model,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        if (dataInicio == null) dataInicio = LocalDate.now().withDayOfMonth(1);
        if (dataFim == null) dataFim = LocalDate.now();

        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("performancePorTipo", campanhaService.getPerformancePorTipo(dataInicio, dataFim));
        model.addAttribute("campanhasMaisLucrativas", campanhaService.findCampanhasMaisLucrativas());
        model.addAttribute("roiTotal", campanhaService.calcularROITotal());
        return "marketing/relatorios";
    }

    // ENDPOINTS API (opcional mover para @RestController)
    @GetMapping("/api/estatisticas-status")
    @ResponseBody
    public ResponseEntity<Map<CampanhaMarketing.StatusCampanha, Long>> getEstatisticasStatus() {
        return ResponseEntity.ok(campanhaService.getEstatisticasPorStatus());
    }
}
