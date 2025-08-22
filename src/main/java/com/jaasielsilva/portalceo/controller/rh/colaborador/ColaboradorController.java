package com.jaasielsilva.portalceo.controller.rh.colaborador;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ColaboradorBeneficio;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.AdesaoPlanoSaudeService;
import com.jaasielsilva.portalceo.service.BeneficioService;
import com.jaasielsilva.portalceo.service.CargoService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/rh/colaboradores")
public class ColaboradorController {

    @Autowired
    private ColaboradorService colaboradorService;
    @Autowired
    private DepartamentoService departamentoService;
    @Autowired
    private CargoService cargoService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private BeneficioService beneficioService;
    @Autowired
    private AdesaoPlanoSaudeService adesaoPlanoSaudeService;

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarTodos());
        model.addAttribute("usuarios", usuarioService.findAll());
        return "rh/colaboradores/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Colaborador colaborador = new Colaborador();

        if (colaborador.getBeneficios() == null) {
            colaborador.setBeneficios(new ArrayList<>());
        }
        while (colaborador.getBeneficios().size() < 3) {
            colaborador.getBeneficios().add(new ColaboradorBeneficio());
        }

        model.addAttribute("colaborador", colaborador);
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("cargos", cargoService.listarTodos());
        model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais());
        model.addAttribute("beneficios", beneficioService.listarTodos());
        return "rh/colaboradores/novo";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Colaborador colaborador,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais());
            model.addAttribute("beneficios", beneficioService.listarTodos());
            model.addAttribute("erro", "Erros de validação");
            return "rh/colaboradores/novo";
        }

        // Salva o colaborador
        colaboradorService.salvar(colaborador);

        // Salva os benefícios do colaborador
        beneficioService.salvarBeneficiosDoColaborador(colaborador);

        redirectAttributes.addFlashAttribute("mensagem", "Colaborador e benefícios salvos com sucesso!");
        return "redirect:/rh/colaboradores/listar";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Colaborador colaborador = colaboradorService.findById(id);
        model.addAttribute("colaborador", colaborador);
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("cargos", cargoService.listarTodos());
        model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais(id));
        return "rh/colaboradores/editar";
    }

    @PostMapping("/atualizar")
    public String atualizar(@Valid @ModelAttribute Colaborador colaborador,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais(colaborador.getId()));
            model.addAttribute("erro", "Erros de validação");
            return "rh/colaboradores/editar";
        }

        colaboradorService.salvar(colaborador);
        redirectAttributes.addFlashAttribute("mensagem", "Colaborador atualizado com sucesso!");
        return "redirect:/rh/colaboradores/listar";
    }

    @PostMapping("/desativar/{id}")
    public String desligar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        colaboradorService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagem", "Colaborador desligado com sucesso!");
        return "redirect:/rh/colaboradores/listar";
    }

    // metodo para exibir a ficha do colaborador
    @GetMapping("/ficha/{id}")
    public String ficha(@PathVariable Long id, Model model) {
        // Busca colaborador
        Colaborador colaborador = colaboradorService.findById(id);
        if (colaborador == null) {
            model.addAttribute("erro", "Colaborador não encontrado");
            return "rh/colaboradores/listar";
        }

        // Busca benefícios do colaborador
        model.addAttribute("beneficios", colaborador.getBeneficios());

        // Busca adesões ao plano de saúde do colaborador
       

        model.addAttribute("colaborador", colaborador);
        return "rh/colaboradores/ficha"; 
    }

}
