package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/solicitacoes-simple")
public class SolicitacoesSimpleController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/todas")
    public String listarTodasSolicitacoesSimple(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Principal principal,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            return "redirect:/dashboard?erro=Acesso negado";
        }

        // Criar página vazia para teste
        Pageable pageable = PageRequest.of(page, size);
        Page solicitacoes = new PageImpl(new ArrayList(), pageable, 0);

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 0);
        model.addAttribute("usuarioLogado", usuarioLogado);

        // Estatísticas simples
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("solicitacoesMes", 0L);
        estatisticas.put("totalPendentes", 0L);
        estatisticas.put("totalAprovadas", 0L);
        estatisticas.put("totalRejeitadas", 0L);
        model.addAttribute("estatisticas", estatisticas);

        return "solicitacoes/todas";
    }
}