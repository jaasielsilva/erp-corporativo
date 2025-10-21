package com.jaasielsilva.portalceo.controller.marketing;

import com.jaasielsilva.portalceo.model.CampanhaMarketing;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.CampanhaMarketingService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/marketing/campanhas")
@RequiredArgsConstructor
@PreAuthorize("@globalControllerAdvice.podeAcessarMarketing()")
public class CampanhaController {

    private final CampanhaMarketingService campanhaService;
    private final UsuarioService usuarioService;

    // LISTAR
    @GetMapping
    public String listarCampanhas(
            Model model,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataCriacao").descending());
        Page<CampanhaMarketing> campanhasPage;

        if (status != null && !status.isEmpty()) {
            try {
                campanhasPage = campanhaService.findByStatus(
                        CampanhaMarketing.StatusCampanha.valueOf(status), pageable);
            } catch (IllegalArgumentException e) {
                campanhasPage = campanhaService.findAll(pageable);
            }
        } else {
            campanhasPage = campanhaService.findAll(pageable);
        }

        model.addAttribute("campanhas", campanhasPage.getContent());
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", campanhasPage.getTotalPages());
        model.addAttribute("statusOptions", CampanhaMarketing.StatusCampanha.values());
        model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
        model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());

        model.addAttribute("pageTitle", "Campanhas de Marketing");
        model.addAttribute("moduleCSS", "marketing");

        return "marketing/campanhas/index";
    }

    // NOVA
    @GetMapping("/novo")
    public String novaCampanha(Model model) {
        model.addAttribute("pageTitle", "Nova Campanha");
        model.addAttribute("campanha", new CampanhaMarketing());
        model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
        model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
        model.addAttribute("usuarios", usuarioService.findAll());
        return "marketing/campanhas/form";
    }

    // EDITAR
    @GetMapping("/editar/{id}")
    public String editarCampanha(@PathVariable Long id, Model model) {
        Optional<CampanhaMarketing> campanhaOpt = campanhaService.findById(id);
        if (campanhaOpt.isEmpty())
            return "redirect:/marketing/campanhas";

        CampanhaMarketing campanha = campanhaOpt.get();
        model.addAttribute("pageTitle", "Editar Campanha");
        model.addAttribute("campanha", campanha);
        model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
        model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
        model.addAttribute("usuarios", usuarioService.findAll());
        return "marketing/campanhas/form";
    }

    // SALVAR (novo ou editar)
    @PostMapping("/salvar")
    public String salvarCampanha(@Valid @ModelAttribute("campanha") CampanhaMarketing campanha,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", campanha.getId() == null ? "Nova Campanha" : "Editar Campanha");
            model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
            model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
            model.addAttribute("usuarios", usuarioService.buscarTodos()); // <-- usa buscarTodos()
            return "marketing/campanhas/form";
        }

        // Recupera o usuário responsável pelo ID selecionado no formulário
        if (campanha.getUsuarioResponsavel() != null && campanha.getUsuarioResponsavel().getId() != null) {
            Usuario usuarioResponsavel = usuarioService.buscarPorId(campanha.getUsuarioResponsavel().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Usuário responsável com ID " + campanha.getUsuarioResponsavel().getId()
                                    + " não encontrado."));
            campanha.setUsuarioResponsavel(usuarioResponsavel);
        }

        // Salva a campanha
        campanhaService.save(campanha);
        redirectAttributes.addFlashAttribute("successMessage", "Campanha salva com sucesso!");
        return "redirect:/marketing/campanhas";
    }

    // VISUALIZAR
    @GetMapping("/{id}")
    public String visualizarCampanha(@PathVariable Long id, Model model) {
        Optional<CampanhaMarketing> campanhaOpt = campanhaService.findById(id);
        if (campanhaOpt.isEmpty())
            return "redirect:/marketing/campanhas";

        CampanhaMarketing campanha = campanhaOpt.get();
        model.addAttribute("campanha", campanha);
        model.addAttribute("pageTitle", "Campanha: " + campanha.getNome());
        model.addAttribute("podeEditar", campanha.podeSerEditada());
        return "marketing/campanhas/detalhes";
    }

    // DELETAR
    @PostMapping("/deletar/{id}")
    public String deletarCampanha(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        campanhaService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Campanha deletada com sucesso!");
        return "redirect:/marketing/campanhas";
    }

    // AÇÕES (iniciar, pausar, retomar etc.)
    @PostMapping("/{id}/iniciar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            campanhaService.iniciarCampanha(id, usuario);

            response.put("success", true);
            response.put("mensagem", "Campanha iniciada com sucesso!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensagem", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
