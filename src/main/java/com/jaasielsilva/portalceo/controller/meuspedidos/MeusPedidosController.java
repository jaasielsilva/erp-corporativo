package com.jaasielsilva.portalceo.controller.meuspedidos;

import com.jaasielsilva.portalceo.model.SolicitacaoAcesso;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.StatusSolicitacao;
import com.jaasielsilva.portalceo.service.meuspedidos.MeusPedidosAcessoService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/meus-pedidos")
public class MeusPedidosController {

    private final MeusPedidosAcessoService acessoService;

    public MeusPedidosController(MeusPedidosAcessoService acessoService) {
        this.acessoService = acessoService;
    }

    // Lista de solicitações de acesso
    @GetMapping
    public String meusPedidos(
            @RequestParam(name = "status", required = false) StatusSolicitacao status,
            @RequestParam(name = "q", required = false) String busca,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {

        model.addAttribute("pageTitle", "Meus Pedidos - Acesso");
        model.addAttribute("pageSubtitle", "Solicitações de acesso ao ERP");
        model.addAttribute("moduleIcon", "fas fa-key");
        model.addAttribute("moduleCSS", "meus-pedidos");

        Page<SolicitacaoAcesso> solicitacoes;
        if (busca != null && !busca.isBlank()) {
            solicitacoes = acessoService.listarDoSolicitante(busca, page, size);
        } else if (status != null) {
            solicitacoes = acessoService.listarPorStatus(status, page, size);
        } else {
            solicitacoes = acessoService.listarTodas(page, size);
        }

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("busca", busca);

        return "meus-pedidos/index";
    }

    // Detalhe da solicitação
    @GetMapping("/{id}")
    public String detalhe(@PathVariable("id") Long id, Model model) {
        SolicitacaoAcesso s = acessoService.buscarPorId(id)
                .orElse(null);

        model.addAttribute("pageTitle", "Detalhe da Solicitação de Acesso");
        model.addAttribute("moduleIcon", "fas fa-file-alt");
        model.addAttribute("solicitacao", s);

        return "meus-pedidos/detalhe";
    }

    // Nova solicitação (form básico)
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Nova Solicitação de Acesso");
        model.addAttribute("moduleIcon", "fas fa-plus-circle");
        model.addAttribute("solicitacao", new SolicitacaoAcesso());
        return "meus-pedidos/novo";
    }

    // Relatórios simples
    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("pageTitle", "Relatórios de Acesso");
        model.addAttribute("moduleIcon", "fas fa-chart-pie");
        model.addAttribute("pendentes", acessoService.contarPorStatus(StatusSolicitacao.PENDENTE));
        model.addAttribute("emAnalise", acessoService.contarPorStatus(StatusSolicitacao.EM_ANALISE));
        model.addAttribute("aprovados", acessoService.contarPorStatus(StatusSolicitacao.APROVADO));
        model.addAttribute("rejeitados", acessoService.contarPorStatus(StatusSolicitacao.REJEITADO));
        model.addAttribute("usuarioCriado", acessoService.contarPorStatus(StatusSolicitacao.USUARIO_CRIADO));
        model.addAttribute("cancelados", acessoService.contarPorStatus(StatusSolicitacao.CANCELADO));
        return "meus-pedidos/relatorios";
    }
}