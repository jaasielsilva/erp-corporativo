package com.jaasielsilva.portalceo.controller.rh.colaborador;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.ColaboradorBeneficio;
import com.jaasielsilva.portalceo.model.HistoricoColaborador;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.AdesaoPlanoSaudeService;
import com.jaasielsilva.portalceo.service.BeneficioService;
import com.jaasielsilva.portalceo.service.CargoService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.HistoricoColaboradorService;
import com.jaasielsilva.portalceo.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private ColaboradorRepository colaboradorRepository;
    @Autowired
    private HistoricoColaboradorService historicoColaboradorService;

    @GetMapping("/listar")
    public String listar(Model model) {
        List<Colaborador> colaboradores = colaboradorRepository.findAllWithCargo();
        model.addAttribute("colaboradores", colaboradores);
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

        // 🔹 Criar e vincular automaticamente o usuário
        usuarioService.criarUsuarioParaColaborador(colaborador);

        redirectAttributes.addFlashAttribute("mensagem", "Colaborador e benefícios salvos com sucesso!");
        return "redirect:/rh/colaboradores/listar";
    }

    /**
     * Endpoint REST para criação de novo colaborador com usuário automático
     * POST /rh/colaboradores/novo
     */
    @PostMapping(value = "/novo", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> criarNovoColaborador(@Valid @RequestBody Colaborador colaborador) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar se CPF já existe
            if (colaboradorService.existeByCpf(colaborador.getCpf())) {
                response.put("success", false);
                response.put("message", "CPF já cadastrado no sistema");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar se email já existe
            if (colaboradorService.existeByEmail(colaborador.getEmail())) {
                response.put("success", false);
                response.put("message", "Email já cadastrado no sistema");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Salvar o colaborador
            Colaborador colaboradorSalvo = colaboradorService.salvar(colaborador);
            
            // Salvar os benefícios do colaborador se existirem
            if (colaborador.getBeneficios() != null && !colaborador.getBeneficios().isEmpty()) {
                beneficioService.salvarBeneficiosDoColaborador(colaboradorSalvo);
            }
            
            // Criar e vincular automaticamente o usuário
            usuarioService.criarUsuarioParaColaborador(colaboradorSalvo);
            
            // Recarregar o colaborador com o usuário vinculado
            colaboradorSalvo = colaboradorService.findById(colaboradorSalvo.getId());
            
            response.put("success", true);
            response.put("message", "Colaborador criado com sucesso!");
            response.put("colaborador", Map.of(
                "id", colaboradorSalvo.getId(),
                "nome", colaboradorSalvo.getNome(),
                "email", colaboradorSalvo.getEmail(),
                "cpf", colaboradorSalvo.getCpf(),
                "matricula", colaboradorSalvo.getUsuario() != null ? colaboradorSalvo.getUsuario().getMatricula() : null,
                "cargo", colaboradorSalvo.getCargo() != null ? colaboradorSalvo.getCargo().getNome() : null,
                "departamento", colaboradorSalvo.getDepartamento() != null ? colaboradorSalvo.getDepartamento().getNome() : null
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao criar colaborador: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
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

    public String formatarUltimoAcesso(LocalDateTime dataHora) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
        LocalDateTime agora = LocalDateTime.now();

        if (dataHora.toLocalDate().equals(agora.toLocalDate())) {
            // Se foi hoje, exibe só hora
            return "Hoje, " + dataHora.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return dataHora.format(formatter);
        }
    }

    // metodo para exibir a ficha do colaborador
    @GetMapping("/ficha/{id}")
    public String ficha(@PathVariable Long id, Model model) {
        Colaborador colaborador = colaboradorService.findById(id);
        if (colaborador == null) {
            model.addAttribute("erro", "Colaborador não encontrado");
            return "rh/colaboradores/listar";
        }
        List<HistoricoColaborador> historico = historicoColaboradorService.listarPorColaborador(id);
        // Tempo na empresa
        String tempoNaEmpresa = colaboradorService.calcularTempoNaEmpresa(colaborador.getDataAdmissao());

        // Formatar último acesso
        String ultimoAcesso = "-";
        if (colaborador.getUltimoAcesso() != null) {
            ultimoAcesso = formatarUltimoAcesso(colaborador.getUltimoAcesso());
        }
        model.addAttribute("historico", historico);
        model.addAttribute("colaborador", colaborador);
        model.addAttribute("tempoNaEmpresa", tempoNaEmpresa);
        model.addAttribute("ultimoAcesso", ultimoAcesso);
        model.addAttribute("beneficios", colaborador.getBeneficios());
        model.addAttribute("cargos", cargoService.listarTodos());

        return "rh/colaboradores/ficha";
    }

    // método para exibir o histórico completo do colaborador
    @GetMapping("/historico/{id}")
    public String historico(@PathVariable Long id, Model model) {
        Colaborador colaborador = colaboradorService.findById(id);
        if (colaborador == null) {
            model.addAttribute("erro", "Colaborador não encontrado");
            return "rh/colaboradores/listar";
        }
        List<HistoricoColaborador> historico = historicoColaboradorService.listarPorColaborador(id);

        model.addAttribute("colaborador", colaborador);
        model.addAttribute("historico", historico);

        return "rh/colaboradores/historico";
    }

    // método para processar a promoção do colaborador
    @PostMapping("/promover/{id}")
    public String promover(@PathVariable Long id,
            @RequestParam Long novoCargoId,
            @RequestParam java.math.BigDecimal novoSalario,
            @RequestParam(required = false) String descricao,
            RedirectAttributes redirectAttributes) {
        try {
            Colaborador colaborador = colaboradorService.findById(id);
            if (colaborador == null) {
                redirectAttributes.addFlashAttribute("erro", "Colaborador não encontrado");
                return "redirect:/rh/colaboradores/listar";
            }

            // Validações básicas
            if (colaborador.getCargo().getId().equals(novoCargoId)) {
                redirectAttributes.addFlashAttribute("erro", "O novo cargo deve ser diferente do cargo atual");
                return "redirect:/rh/colaboradores/ficha/" + id;
            }

            if (novoSalario.compareTo(colaborador.getSalario()) <= 0) {
                redirectAttributes.addFlashAttribute("erro", "O novo salário deve ser maior que o salário atual");
                return "redirect:/rh/colaboradores/ficha/" + id;
            }

            // Buscar o novo cargo
            var novoCargo = cargoService.findById(novoCargoId);
            if (novoCargo == null) {
                redirectAttributes.addFlashAttribute("erro", "Cargo não encontrado");
                return "redirect:/rh/colaboradores/ficha/" + id;
            }

            // Registrar a promoção no histórico
            colaboradorService.registrarPromocao(colaborador, novoCargo.getNome(), novoSalario, descricao);

            // Atualizar os dados do colaborador
            colaborador.setCargo(novoCargo);
            colaborador.setSalario(novoSalario);
            colaboradorService.salvar(colaborador);

            redirectAttributes.addFlashAttribute("mensagem",
                    "Colaborador promovido com sucesso para " + novoCargo.getNome() + "!");
            return "redirect:/rh/colaboradores/ficha/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao promover colaborador: " + e.getMessage());
            return "redirect:/rh/colaboradores/ficha/" + id;
        }
    }

    @GetMapping("/relatorio")
    public String relatorioColaboradores(Model model) {
        // Adicione dados ao model se precisar
        return "rh/colaboradores/relatorios";
    }

}
