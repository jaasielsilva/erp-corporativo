package com.jaasielsilva.portalceo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.CargoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.UsuarioService;

/**
 * Controller responsável pelo módulo de Recursos Humanos (RH)
 * Gerencia colaboradores, folha de pagamento, benefícios e ponto/escalas
 */
@Controller
@RequestMapping("/rh")
public class RhController {

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private CargoService cargoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Redireciona para a página principal do módulo RH
     */
    @GetMapping
    public String index() {
        return "redirect:/rh/colaboradores/listar";
    }

    /*
     * ===============================================
     * COLABORADORES
     * ===============================================
     */

    /**
     * Lista todos os colaboradores com paginação
     */
    @GetMapping("/colaboradores/listar")
    public String listarColaboradores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Colaborador> colaboradoresPage = colaboradorService.listarTodosPaginado(pageable);
        List<Usuario> usuarios = usuarioService.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("colaboradores", colaboradoresPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", colaboradoresPage.getTotalPages());
        model.addAttribute("totalElements", colaboradoresPage.getTotalElements());
        model.addAttribute("hasNext", colaboradoresPage.hasNext());
        model.addAttribute("hasPrevious", colaboradoresPage.hasPrevious());

        return "rh/colaboradores/listar";
    }

    /**
     * Exibe o formulário para cadastro de novo colaborador
     */
    @GetMapping("/colaboradores/novo")
    public String novoColaborador(Model model) {
        model.addAttribute("colaborador", new Colaborador());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("cargos", cargoService.listarTodos());
        return "rh/colaboradores/novo";
    }

    /**
     * Processa o formulário de cadastro de novo colaborador
     */
    @PostMapping("/colaboradores/salvar")
    public String salvarColaborador(@ModelAttribute Colaborador colaborador,
            RedirectAttributes redirectAttributes) {
        try {
            colaboradorService.salvar(colaborador);
            redirectAttributes.addFlashAttribute("mensagem", "Colaborador cadastrado com sucesso!");
            return "redirect:/rh/colaboradores/listar";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar colaborador. Verifique os dados.");
            return "redirect:/rh/colaboradores/novo";
        }
    }

    /**
     * Exibe a ficha completa do colaborador
     */
    @GetMapping("/colaboradores/ficha/{id}")
    public String fichaColaborador(@PathVariable Long id, Model model) {
        Colaborador colaborador = colaboradorService.findById(id);
        if (colaborador == null) {
            return "redirect:/rh/colaboradores/listar";
        }

        model.addAttribute("colaborador", colaborador);
        return "rh/colaboradores/ficha";
    }

    /**
     * Exibe o formulário para edição de colaborador
     */
    @GetMapping("/colaboradores/editar/{id}")
    public String editarColaborador(@PathVariable Long id, Model model) {
        Colaborador colaborador = colaboradorService.findById(id);

        if (colaborador == null) {
            return "redirect:/rh/colaboradores/listar";
        }

        model.addAttribute("colaborador", colaborador);
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("cargos", cargoService.listarTodos());
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/colaboradores/editar";
    }

    /**
     * Processa o formulário de edição de colaborador
     */
    @PostMapping("/colaboradores/atualizar")
    public String atualizarColaborador(@ModelAttribute Colaborador colaborador,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            RedirectAttributes redirectAttributes) {
        try {
            // Lógica para processar a foto do colaborador seria implementada aqui
            if (foto != null && !foto.isEmpty()) {
                // Processar upload da foto quando implementado
            }

            colaboradorService.salvar(colaborador);
            redirectAttributes.addFlashAttribute("mensagem", "Colaborador atualizado com sucesso!");
            return "redirect:/rh/colaboradores/ficha/" + colaborador.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar colaborador: " + e.getMessage());
            return "redirect:/rh/colaboradores/editar/" + colaborador.getId();
        }
    }

    /**
     * Exibe o histórico do colaborador
     */
    @GetMapping("/colaboradores/historico/{id}")
    public String historicoColaborador(@PathVariable Long id, Model model) {
        Colaborador colaborador = colaboradorService.findById(id);
        if (colaborador == null) {
            return "redirect:/rh/colaboradores/listar";
        }

        model.addAttribute("colaborador", colaborador);
        return "rh/colaboradores/historico";
    }

    /**
     * Exibe a página de documentos do colaborador
     */
    @GetMapping("/colaboradores/documentos/{id}")
    public String documentosColaborador(@PathVariable Long id, Model model) {
        Colaborador colaborador = colaboradorService.findById(id);
        if (colaborador == null) {
            return "redirect:/rh/colaboradores/listar";
        }

        model.addAttribute("colaborador", colaborador);
        return "rh/colaboradores/documentos";
    }

    /**
     * Desliga um colaborador (exclusão lógica)
     */
    @PostMapping("/colaboradores/desativar/{id}")
    public String desligarColaborador(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Colaborador colaborador = colaboradorService.findById(id);
            if (colaborador == null) {
                redirectAttributes.addFlashAttribute("erro", "Colaborador não encontrado.");
                return "redirect:/rh/colaboradores/listar";
            }

            colaboradorService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagem",
                    "Colaborador " + colaborador.getNome() + " foi desligado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao desligar colaborador: " + e.getMessage());
        }
        return "redirect:/rh/colaboradores/listar";
    }

    /*
     * ===============================================
     * FOLHA DE PAGAMENTO
     * ===============================================
     */

    /**
     * Exibe a página para gerar folha de pagamento
     */
    @GetMapping("/folha-pagamento/gerar")
    public String gerarFolhaPagamento(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        return "rh/folha-pagamento/gerar";
    }

    /**
     * Exibe a página de holerites
     */
    @GetMapping("/folha-pagamento/holerite")
    public String holerites() {
        return "rh/folha-pagamento/holerite";
    }

    /**
     * Exibe um holerite específico
     */
    @GetMapping("/folha-pagamento/holerite/{id}")
    public String verHolerite(@PathVariable Long id, Model model) {
        // Aqui seria implementada a lógica para buscar o holerite específico
        return "rh/folha-pagamento/holerite";
    }

    /**
     * Exibe a página de descontos da folha de pagamento
     */
    @GetMapping("/folha-pagamento/descontos")
    public String descontosFolhaPagamento() {
        return "rh/folha-pagamento/descontos";
    }

    /**
     * Exibe a página de relatórios da folha de pagamento
     */
    @GetMapping("/folha-pagamento/relatorios")
    public String relatoriosFolhaPagamento(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        return "rh/folha-pagamento/relatorios";
    }

    /*
     * ===============================================
     * BENEFÍCIOS
     * ===============================================
     */

    /**
     * Exibe a página de plano de saúde
     */
    @GetMapping("/beneficios/plano-saude")
    public String planoSaude(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/beneficios/plano-saude";
    }

    /**
     * Exibe a página de vale transporte
     */
    @GetMapping("/beneficios/vale-transporte")
    public String valeTransporte(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/beneficios/vale-transporte";
    }

    /**
     * Exibe a página de vale refeição
     */
    @GetMapping("/beneficios/vale-refeicao")
    public String valeRefeicao(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/beneficios/vale-refeicao";
    }

    /**
     * Exibe a página de adesão a benefícios
     */
    @GetMapping("/beneficios/adesao")
    public String adesaoBeneficios(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/beneficios/adesao";
    }

    /*
     * ===============================================
     * PONTO E ESCALAS
     * ===============================================
     */

    /**
     * Exibe a página de registros de ponto
     */
    @GetMapping("/ponto-escalas/registros")
    public String registrosPonto(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/registros";
    }

    /**
     * Exibe a página de correções de ponto
     */
    @GetMapping("/ponto-escalas/correcoes")
    public String correcoesPonto(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/correcoes";
    }

    /**
     * Exibe a página de escalas de trabalho
     */
    @GetMapping("/ponto-escalas/escalas")
    public String escalasTrabalho(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/escalas";
    }
}