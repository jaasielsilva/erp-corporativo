package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.service.AdesaoPlanoSaudeService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.PlanoSaudeService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rh/beneficios/plano-saude")
public class PlanoSaudeController {

    @Autowired
    private PlanoSaudeService planoSaudeService;

    @Autowired
    private AdesaoPlanoSaudeService adesaoPlanoSaudeService;

    @Autowired
    private ColaboradorService colaboradorService;

    @GetMapping
    public String listar(Model model) {
         List<PlanoSaude> planos = planoSaudeService.listarTodosAtivos();
        model.addAttribute("planos", planos);
        model.addAttribute("custoMensal", adesaoPlanoSaudeService.calcularCustoMensalTotal());
        model.addAttribute("custoEmpresa", adesaoPlanoSaudeService.calcularCustoEmpresa());
        model.addAttribute("descontoColaboradores", adesaoPlanoSaudeService.calcularDescontoColaboradoresPercentual());
        model.addAttribute("totalBeneficiarios", adesaoPlanoSaudeService.contarTotalBeneficiarios());
        model.addAttribute("titulares", adesaoPlanoSaudeService.contarTitulares());
        model.addAttribute("dependentes", adesaoPlanoSaudeService.contarDependentes());
        return "rh/beneficios/plano-de-saude/plano-saude";
    }

   @PostMapping("/adesao/salvar")
@ResponseBody
public String salvarAdesao(@RequestParam Long colaboradorId,
                           @RequestParam(required = false) Long planoSaudeId,
                           @RequestParam String dataInicio,
                           @RequestParam(required = false) Integer quantidadeDependentes,
                           @RequestParam(required = false) String observacoes) {

    AdesaoPlanoSaude adesao = new AdesaoPlanoSaude();
    adesao.setColaborador(colaboradorService.buscarPorId(colaboradorId));

    if (planoSaudeId != null) {
        adesao.setPlanoSaude(planoSaudeService.buscarPorId(planoSaudeId));
    }

    adesao.setDataAdesao(LocalDate.parse(dataInicio));
    adesao.setQuantidadeDependentes(quantidadeDependentes != null ? quantidadeDependentes : 0);
    adesao.setObservacoes(observacoes);

    adesaoPlanoSaudeService.salvar(adesao);
    return "Adesão salva com sucesso!";
}
}

