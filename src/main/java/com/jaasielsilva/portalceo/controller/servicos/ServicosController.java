package com.jaasielsilva.portalceo.controller.servicos;

import com.jaasielsilva.portalceo.model.servicos.StatusSolicitacao;
import com.jaasielsilva.portalceo.service.servicos.AprovacaoService;
import com.jaasielsilva.portalceo.service.servicos.CatalogoService;
import com.jaasielsilva.portalceo.service.servicos.SolicitacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/servicos")
public class ServicosController {

    private final CatalogoService catalogoService;
    private final SolicitacaoService solicitacaoService;
    private final AprovacaoService aprovacaoService;

    public ServicosController(CatalogoService catalogoService,
                              SolicitacaoService solicitacaoService,
                              AprovacaoService aprovacaoService) {
        this.catalogoService = catalogoService;
        this.solicitacaoService = solicitacaoService;
        this.aprovacaoService = aprovacaoService;
    }

    // Página principal de serviços: redireciona para catálogo
    @GetMapping
    public String servicos(Model model) {
        model.addAttribute("servicos", catalogoService.listarCatalogo());
        return "servicos/catalogo";
    }

    // Catálogo
    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        model.addAttribute("servicos", catalogoService.listarCatalogo());
        return "servicos/catalogo";
    }

    // Detalhe do serviço
    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        // Para demonstração: apenas reutiliza lista e seleciona por id na view
        model.addAttribute("servicos", catalogoService.listarCatalogo());
        model.addAttribute("servicoId", id);
        return "servicos/detalhe";
    }

    // Minhas solicitações (para demo: usa status CRIADA)
    @GetMapping("/solicitacoes")
    public String solicitacoes(Model model) {
        model.addAttribute("solicitacoes", solicitacaoService.listarPorStatus(StatusSolicitacao.CRIADA));
        return "servicos/solicitacoes";
    }

    // Painel de aprovações
    @GetMapping("/aprovacoes")
    public String aprovacoes(Model model) {
        model.addAttribute("aprovacoes", aprovacaoService.listarPendentes());
        return "servicos/aprovacoes";
    }

    // Avaliações (para demo: lista concluídas)
    @GetMapping("/avaliacoes")
    public String avaliacoes(Model model) {
        model.addAttribute("concluidas", solicitacaoService.listarPorStatus(StatusSolicitacao.CONCLUIDA));
        return "servicos/avaliacoes";
    }

    // Relatórios (demo: KPIs simples via contagens)
    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("catalogoCount", catalogoService.listarCatalogo().size());
        model.addAttribute("solicitacoesCriadas", solicitacaoService.listarPorStatus(StatusSolicitacao.CRIADA).size());
        model.addAttribute("solicitacoesConcluidas", solicitacaoService.listarPorStatus(StatusSolicitacao.CONCLUIDA).size());
        model.addAttribute("aprovacoesPendentes", aprovacaoService.listarPendentes().size());
        return "servicos/relatorios";
    }

    // Administração do catálogo
    @GetMapping("/admin/catalogo")
    public String adminCatalogo(Model model) {
        model.addAttribute("servicos", catalogoService.listarCatalogo());
        return "servicos/admin/catalogo";
    }
}