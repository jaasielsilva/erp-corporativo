package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.EstatisticasTermosDTO;
import com.jaasielsilva.portalceo.dto.TermoDTO;
import com.jaasielsilva.portalceo.dto.TermoAceiteDTO;
import com.jaasielsilva.portalceo.model.Termo;
import com.jaasielsilva.portalceo.model.TermoAceite;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.TermoService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/termos")
public class TermosController {

    @Autowired
    private TermoService termoService;

    @Autowired
    private UsuarioService usuarioService;

    // ===============================
    // PÁGINAS PRINCIPAIS
    // ===============================

    @GetMapping
    public String index(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Termos de Uso");
        model.addAttribute("pageSubtitle", "Gerenciamento de termos e políticas");
        model.addAttribute("moduleIcon", "fas fa-file-contract");
        model.addAttribute("moduleCSS", "termos");

        // Buscar estatísticas
        EstatisticasTermosDTO estatisticas = termoService.buscarEstatisticas();
        model.addAttribute("estatisticas", estatisticas);

        // Buscar termos ativos
        List<Termo> termosAtivos = termoService.buscarTermosAtivos();
        model.addAttribute("termosAtivos", termoService.converterParaDTO(termosAtivos));

        // Ações da página
        java.util.List<java.util.Map<String, String>> pageActions = new java.util.ArrayList<>();
        
        java.util.Map<String, String> uso = new java.util.HashMap<>();
        uso.put("type", "link");
        uso.put("url", "/termos/uso");
        uso.put("label", "Termos de Uso");
        uso.put("icon", "fas fa-file-contract");
        pageActions.add(uso);
        
        java.util.Map<String, String> privacidade = new java.util.HashMap<>();
        privacidade.put("type", "link");
        privacidade.put("url", "/termos/privacidade");
        privacidade.put("label", "Privacidade");
        privacidade.put("icon", "fas fa-shield-alt");
        pageActions.add(privacidade);
        
        java.util.Map<String, String> historico = new java.util.HashMap<>();
        historico.put("type", "link");
        historico.put("url", "/termos/historico");
        historico.put("label", "Histórico");
        historico.put("icon", "fas fa-history");
        pageActions.add(historico);

        java.util.Map<String, String> aceites = new java.util.HashMap<>();
        aceites.put("type", "link");
        aceites.put("url", "/termos/aceites");
        aceites.put("label", "Aceites de Usuários");
        aceites.put("icon", "fas fa-users-check");
        pageActions.add(aceites);
        
        model.addAttribute("pageActions", pageActions);
        
        return "termos/index";
    }

    @GetMapping("/uso")
    public String termosUso(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Termos de Uso");
        model.addAttribute("pageSubtitle", "Leia e aceite os termos de uso");
        model.addAttribute("moduleIcon", "fas fa-file-contract");
        model.addAttribute("moduleCSS", "termos");

        // Buscar termo de uso mais recente
        Optional<Termo> termoUso = termoService.buscarTermoMaisRecentePorTipo(Termo.TipoTermo.TERMOS_USO);
        if (termoUso.isPresent()) {
            model.addAttribute("termo", termoService.converterParaDTO(termoUso.get()));
            
            // Verificar se usuário já aceitou
            if (authentication != null) {
                Optional<Usuario> usuario = usuarioService.buscarPorEmail(authentication.getName());
                if (usuario.isPresent()) {
                    boolean jaAceitou = termoService.usuarioAceitouTermo(termoUso.get().getId(), usuario.get());
                    model.addAttribute("jaAceitou", jaAceitou);
                }
            }
        }

        return "termos/uso";
    }

    @GetMapping("/privacidade")
    public String politicaPrivacidade(Model model) {
        model.addAttribute("pageTitle", "Política de Privacidade");
        model.addAttribute("pageSubtitle", "Nossa política de privacidade e proteção de dados");
        model.addAttribute("moduleIcon", "fas fa-shield-alt");
        model.addAttribute("moduleCSS", "termos");

        // Buscar política de privacidade mais recente
        Optional<Termo> politica = termoService.buscarTermoMaisRecentePorTipo(Termo.TipoTermo.POLITICA_PRIVACIDADE);
        if (politica.isPresent()) {
            model.addAttribute("termo", termoService.converterParaDTO(politica.get()));
        }

        return "termos/privacidade";
    }

    @GetMapping("/historico")
    public String historico(Model model) {
        model.addAttribute("pageTitle", "Histórico de Versões");
        model.addAttribute("pageSubtitle", "Histórico de alterações dos termos");
        model.addAttribute("moduleIcon", "fas fa-history");
        model.addAttribute("moduleCSS", "termos");

        // Buscar todos os termos ordenados por data
        List<Termo> todosTermos = termoService.buscarTodos();
        model.addAttribute("termos", termoService.converterParaDTO(todosTermos));

        return "termos/historico";
    }

    @GetMapping("/aceites")
    public String aceites(Model model) {
        model.addAttribute("pageTitle", "Aceites de Usuários");
        model.addAttribute("pageSubtitle", "Controle de aceites dos termos pelos usuários");
        model.addAttribute("moduleIcon", "fas fa-users-check");
        model.addAttribute("moduleCSS", "termos");

        // Buscar estatísticas
        EstatisticasTermosDTO estatisticas = termoService.buscarEstatisticas();
        model.addAttribute("estatisticas", estatisticas);

        return "termos/aceites";
    }

    // ===============================
    // AÇÕES DE ACEITE
    // ===============================

    @PostMapping("/aceitar/{id}")
    @ResponseBody
    public ResponseEntity<?> aceitarTermo(@PathVariable Long id, 
                                         Authentication authentication,
                                         HttpServletRequest request) {
        try {
            Optional<Usuario> usuario = usuarioService.buscarPorEmail(authentication.getName());
            if (!usuario.isPresent()) {
                return ResponseEntity.badRequest().body("Usuário não encontrado");
            }

            String ipAceite = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            TermoAceite aceite = termoService.aceitarTermo(id, usuario.get(), ipAceite, userAgent);
            
            return ResponseEntity.ok().body("Termo aceito com sucesso!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}