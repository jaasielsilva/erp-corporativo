package com.jaasielsilva.portalceo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.portalceo.dto.AdesaoDTO;
import com.jaasielsilva.portalceo.exception.BusinessValidationException;
import com.jaasielsilva.portalceo.exception.CargoNotFoundException;
import com.jaasielsilva.portalceo.exception.ColaboradorNotFoundException;
import com.jaasielsilva.portalceo.exception.DepartamentoNotFoundException;
import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ColaboradorBeneficio;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.AdesaoPlanoSaudeService;
import com.jaasielsilva.portalceo.service.BeneficioService;
import com.jaasielsilva.portalceo.service.CargoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.PlanoSaudeService;
import com.jaasielsilva.portalceo.service.UsuarioService;

/**
 * Controller responsável pelo módulo de Recursos Humanos (RH)
 * Gerencia colaboradores, folha de pagamento, benefícios e ponto/escalas
 */
@Controller
@RequestMapping("/rh")
public class RhController {

    private static final Logger logger = LoggerFactory.getLogger(RhController.class);

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
    private PlanoSaudeService planoSaudeService;

    @Autowired
    private AdesaoPlanoSaudeService adesaoPlanoSaudeService;

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
        Colaborador colaborador = new Colaborador();

        // Inicializa a lista se for null
        if (colaborador.getBeneficios() == null) {
            colaborador.setBeneficios(new ArrayList<>());
        }

        // Adiciona 3 benefícios vazios
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

    /**
     * Processa o formulário de cadastro de novo colaborador
     */
    @PostMapping("/colaboradores/salvar")
    public String salvarColaborador(@Valid @ModelAttribute Colaborador colaborador,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Processando cadastro do colaborador: {}", colaborador.getNome());

        // Verificar erros de validação
        if (result.hasErrors()) {
            logger.warn("Erros de validação encontrados para colaborador {}", colaborador.getNome());

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais());

            // Adicionar mensagens de erro
            StringBuilder errorMessages = new StringBuilder();
            result.getAllErrors().forEach(error -> {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            });

            model.addAttribute("erro", "Erros de validação: " + errorMessages.toString());
            return "rh/colaboradores/novo";
        }

        try {
            Colaborador salvo = colaboradorService.salvar(colaborador);
            logger.info("Colaborador {} cadastrado com sucesso (ID: {})", salvo.getNome(), salvo.getId());

            redirectAttributes.addFlashAttribute("mensagem",
                    "Colaborador " + salvo.getNome() + " cadastrado com sucesso!");
            return "redirect:/rh/colaboradores/listar";

        } catch (BusinessValidationException e) {
            logger.warn("Erro de validação de negócio: {}", e.getMessage());

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais());
            model.addAttribute("erro", e.getMessage());

            return "rh/colaboradores/novo";

        } catch (CargoNotFoundException | DepartamentoNotFoundException e) {
            logger.error("Entidade não encontrada: {}", e.getMessage());

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais());
            model.addAttribute("erro", e.getMessage());

            return "rh/colaboradores/novo";

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar colaborador {}: {}",
                    colaborador.getNome(), e.getMessage(), e);

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais());
            model.addAttribute("erro",
                    "Erro interno do sistema. Tente novamente ou contate o suporte.");

            return "rh/colaboradores/novo";
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

        // calcula tempo na empresa
        String tempoNaEmpresa = colaboradorService.calcularTempoNaEmpresa(colaborador.getDataAdmissao());
        model.addAttribute("colaborador", colaborador);
        model.addAttribute("tempoNaEmpresa", tempoNaEmpresa);
        model.addAttribute("ultimoAcesso", colaborador.getUltimoAcesso() != null
                ? colaborador.getUltimoAcesso().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Nunca");

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
        model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais(id));
        return "rh/colaboradores/editar";
    }

    /**
     * Processa o formulário de edição de colaborador
     */
    @PostMapping("/colaboradores/atualizar")
    public String atualizarColaborador(@Valid @ModelAttribute Colaborador colaborador,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Processando atualização do colaborador: {} (ID: {})",
                colaborador.getNome(), colaborador.getId());

        // Verificar erros de validação
        if (result.hasErrors()) {
            logger.warn("Erros de validação encontrados para colaborador {}", colaborador.getNome());

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais(colaborador.getId()));

            // Adicionar mensagens de erro
            StringBuilder errorMessages = new StringBuilder();
            result.getAllErrors().forEach(error -> {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            });

            model.addAttribute("erro", "Erros de validação: " + errorMessages.toString());
            return "rh/colaboradores/editar";
        }

        try {
            Colaborador salvo = colaboradorService.salvar(colaborador);
            logger.info("Colaborador {} atualizado com sucesso (ID: {})", salvo.getNome(), salvo.getId());

            redirectAttributes.addFlashAttribute("mensagem",
                    "Colaborador " + salvo.getNome() + " atualizado com sucesso!");
            return "redirect:/rh/colaboradores/listar";

        } catch (BusinessValidationException e) {
            logger.warn("Erro de validação de negócio: {}", e.getMessage());

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais(colaborador.getId()));
            model.addAttribute("erro", e.getMessage());

            return "rh/colaboradores/editar";

        } catch (CargoNotFoundException | DepartamentoNotFoundException | ColaboradorNotFoundException e) {
            logger.error("Entidade não encontrada: {}", e.getMessage());

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais(colaborador.getId()));
            model.addAttribute("erro", e.getMessage());

            return "rh/colaboradores/editar";

        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar colaborador {}: {}",
                    colaborador.getNome(), e.getMessage(), e);

            // Recarregar dados necessários para o formulário
            model.addAttribute("colaborador", colaborador);
            model.addAttribute("cargos", cargoService.listarTodos());
            model.addAttribute("departamentos", departamentoService.listarTodos());
            model.addAttribute("colaboradores", colaboradorService.buscarSupervisoresPotenciais(colaborador.getId()));
            model.addAttribute("erro",
                    "Erro interno do sistema. Tente novamente ou contate o suporte.");

            return "rh/colaboradores/editar";
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

    /*
     * ===============================================
     * FOLHA DE PAGAMENTO
     * ===============================================
     */

    @GetMapping("/folha-pagamento/gerar")
    public String gerarFolhaPagamento(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        return "rh/folha-pagamento/gerar";
    }

    @GetMapping("/folha-pagamento/holerite")
    public String holerites() {
        return "rh/folha-pagamento/holerite";
    }

    @GetMapping("/folha-pagamento/holerite/{id}")
    public String verHolerite(@PathVariable Long id, Model model) {
        // Futuramente você poderá buscar o holerite pelo ID
        model.addAttribute("holeriteId", id);
        return "rh/folha-pagamento/holerite";
    }

    @GetMapping("/folha-pagamento/descontos")
    public String descontosFolhaPagamento() {
        return "rh/folha-pagamento/descontos";
    }

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
    @GetMapping("/beneficios/plano-saude")
    public String planoSaude(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("beneficios", beneficioService.listarTodos());
        model.addAttribute("planos", planoSaudeService.listarTodosAtivos());

        // Resumo de beneficiários
        Long titulares = adesaoPlanoSaudeService.contarTitulares();
        Long dependentes = adesaoPlanoSaudeService.contarDependentes();
        Long total = titulares + dependentes;

        model.addAttribute("titulares", titulares);
        model.addAttribute("dependentes", dependentes);
        model.addAttribute("totalBeneficiarios", total);

        // Resumo de custos
        BigDecimal custoMensal = adesaoPlanoSaudeService.calcularCustoMensalTotal();
        BigDecimal custoEmpresa = adesaoPlanoSaudeService.calcularCustoEmpresa();
        BigDecimal descontoColaboradores = adesaoPlanoSaudeService.calcularDescontoColaboradoresPercentual();

        model.addAttribute("descontoColaboradores", descontoColaboradores);
        model.addAttribute("custoMensal", custoMensal);
        model.addAttribute("custoEmpresa", custoEmpresa);

        return "rh/beneficios/plano-saude";
    }

    @GetMapping("/beneficios/adesao")
    public String adesaoBeneficios(Model model,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 5); // 5 registros por página
        Page<AdesaoPlanoSaude> adesoesPage = adesaoPlanoSaudeService.listarTodosPaginado(pageable);

        List<PlanoSaude> planosDeSaude = planoSaudeService.listarTodosAtivos();
        List<Colaborador> colaboradores = colaboradorService.listarAtivos();
        List<Beneficio> beneficios = beneficioService.listarTodos();

        model.addAttribute("colaboradores", colaboradores);
        model.addAttribute("beneficios", beneficios);
        model.addAttribute("plano_de_saude", planosDeSaude);

        // ----------------------------
        // CÁLCULO DO CUSTO TOTAL CORRETO
        // ----------------------------
        BigDecimal custoTotal = adesoesPage.getContent().stream()
                .filter(AdesaoPlanoSaude::isAtiva)
                .map(AdesaoPlanoSaude::getValorTotalMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoEmpresa = custoTotal.multiply(BigDecimal.valueOf(0.6));
        BigDecimal custoColaborador = custoTotal.multiply(BigDecimal.valueOf(0.4));
        
        model.addAttribute("custoMensal", custoTotal);
        model.addAttribute("custoEmpresa", custoEmpresa);
        model.addAttribute("custoColaborador", custoColaborador);

        // Lista de adesões para exibição
        List<Map<String, Object>> adesoesComValores = adesoesPage.getContent().stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("adesao", a);
            map.put("valorTotalAtual", a.getValorTotalMensal());
            return map;
        }).collect(Collectors.toList());

        model.addAttribute("adesoes", adesoesComValores);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", adesoesPage.getTotalPages());

        return "rh/beneficios/adesao";
    }

    // metodo para salvar adesão a benefícios
    @PostMapping("/beneficios/adesao")
    public ResponseEntity<Void> criarAdesao(@RequestBody AdesaoDTO dto) {

        // 1. Buscar colaborador
        Colaborador colaborador = colaboradorService.findById(dto.getColaboradorId());

        // 2. Buscar plano de saúde pelo ID
        PlanoSaude plano = planoSaudeService.findById(dto.getPlanoId())
                .orElseThrow(() -> new RuntimeException("Plano de saúde não encontrado: " + dto.getPlanoId()));

        // 3. Criar nova adesão
        AdesaoPlanoSaude adesao = new AdesaoPlanoSaude();
        adesao.setColaborador(colaborador);
        adesao.setPlanoSaude(plano);
        adesao.setQuantidadeDependentes(dto.getQuantidadeDependentes());
        adesao.setDataAdesao(LocalDate.now());
        adesao.setDataVigenciaInicio(LocalDate.now());

        // 4. Calcular valor total
        BigDecimal valorTitular = plano.getValorTitular();
        BigDecimal valorDependentes = plano.getValorDependente()
                .multiply(BigDecimal.valueOf(dto.getQuantidadeDependentes()));
        adesao.setValorTotalMensal(valorTitular.add(valorDependentes));

        // 5. Definir status
        adesao.setStatus(AdesaoPlanoSaude.StatusAdesao.ATIVA);

        // 6. Salvar adesão
        adesaoPlanoSaudeService.salvar(adesao);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/beneficios/vale-transporte")
    public String valeTransporte(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("beneficios", beneficioService.listarTodos());
        return "rh/beneficios/vale-transporte";
    }

    @GetMapping("/beneficios/vale-refeicao")
    public String valeRefeicao(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        model.addAttribute("beneficios", beneficioService.listarTodos());
        return "rh/beneficios/vale-refeicao";
    }

    /*
     * ===============================================
     * PONTO E ESCALAS
     * ===============================================
     */

    @GetMapping("/ponto-escalas/registros")
    public String registrosPonto(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/registros";
    }

    @GetMapping("/ponto-escalas/correcoes")
    public String correcoesPonto(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/correcoes";
    }

    @GetMapping("/ponto-escalas/escalas")
    public String escalasTrabalho(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/escalas";
    }

    /*
     * ===============================================
     * COLABORADORES
     * ===============================================
     */

    @GetMapping("/colaboradores/relatorio")
    public String relatoriosColaborador(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarTodos());
        return "rh/colaboradores/relatorios";
    }

    @GetMapping("/colaboradores/ficha")
    public String fichaColaborador(Model model) {
        // Futuramente você poderá buscar um colaborador específico
        model.addAttribute("colaboradores", colaboradorService.listarTodos());
        return "rh/colaboradores/fichageral";
    }
}