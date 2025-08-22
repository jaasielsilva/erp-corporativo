package com.jaasielsilva.portalceo.controller.rh.folha;

import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rh/folha-pagamento")
public class FolhaPagamentoController {

    @Autowired
    private ColaboradorService colaboradorService;
    @Autowired
    private DepartamentoService departamentoService;

    @GetMapping("/gerar")
    public String gerar(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        return "rh/folha-pagamento/gerar";
    }

    @GetMapping("/holerite")
    public String holerite() {
        return "rh/folha-pagamento/holerite";
    }

    @GetMapping("/holerite/{id}")
    public String verHolerite(@PathVariable Long id, Model model) {
        model.addAttribute("holeriteId", id);
        return "rh/folha-pagamento/holerite";
    }

    @GetMapping("/descontos")
    public String descontos() {
        return "rh/folha-pagamento/descontos";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        return "rh/folha-pagamento/relatorios";
    }
}
