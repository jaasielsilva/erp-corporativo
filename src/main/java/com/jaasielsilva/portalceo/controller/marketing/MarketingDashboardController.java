package com.jaasielsilva.portalceo.controller.marketing;

import com.jaasielsilva.portalceo.model.CampanhaMarketing;
import com.jaasielsilva.portalceo.service.CampanhaMarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class MarketingDashboardController {

    private final CampanhaMarketingService campanhaService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Marketing - Dashboard");
        model.addAttribute("moduleCSS", "marketing");

        Map<CampanhaMarketing.StatusCampanha, Long> estatisticasStatus = campanhaService.getEstatisticasPorStatus();
        Map<CampanhaMarketing.TipoCampanha, Long> estatisticasTipo = campanhaService.getEstatisticasPorTipo();
        model.addAttribute("estatisticasStatus", estatisticasStatus);
        model.addAttribute("estatisticasTipo", estatisticasTipo);

        List<CampanhaMarketing> campanhasAtivas = campanhaService.findCampanhasAtivas();
        model.addAttribute("campanhasAtivas", campanhasAtivas);

        BigDecimal roiTotal = campanhaService.calcularROITotal();
        model.addAttribute("roiTotal", roiTotal);

        List<CampanhaMarketing> campanhasExpiradas = campanhaService.findCampanhasExpiradas();
        model.addAttribute("campanhasExpiradas", campanhasExpiradas);

        return "marketing/Dashboard/index";
    }
}
