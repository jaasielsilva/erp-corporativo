package com.jaasielsilva.portalceo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.Genero;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.service.UsuarioService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PerfilRepository perfilRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;

    public UsuarioController(UsuarioService usuarioService,
                             PerfilRepository perfilRepository,
                             CargoRepository cargoRepository,
                             DepartamentoRepository departamentoRepository) {
        this.usuarioService = usuarioService;
        this.perfilRepository = perfilRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
    }

    @GetMapping
    public String listarUsuarios(@RequestParam(value = "busca", required = false) String busca, Model model) {
    List<Usuario> usuarios;
    if (busca == null || busca.isEmpty()) {
        usuarios = usuarioService.buscarTodos();
    } else {
        usuarios = usuarioService.buscarPorNomeOuEmail(busca);
    }
    model.addAttribute("usuarios", usuarios);
    return "usuarios/listar"; // ou "usuarios/lista" — o que você usar
}

    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        adicionarAtributosComuns(model);
        return "usuarios/cadastro";
    }

    @GetMapping("/index")
    public String mostrarEstatisticasUsuarios(Model model) {
        model.addAttribute("usuario", new Usuario());

        EstatisticasUsuariosDTO stats = usuarioService.buscarEstatisticas();
        model.addAttribute("totalUsuarios", stats.getTotalUsuarios());
        model.addAttribute("ativos", stats.getTotalAtivos());
        model.addAttribute("administradores", stats.getTotalAdministradores());
        model.addAttribute("bloqueados", stats.getTotalBloqueados());

        return "usuarios/index";
    }

    @GetMapping("/listar")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.buscarTodos());
        return "usuarios/listar";
    }

    @PostMapping("/cadastrar")
    public String cadastrarUsuario(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult bindingResult,
        @RequestParam("confirmSenha") String confirmSenha,
        @RequestParam("perfilId") Long perfilId,
        Model model) {

        if (!usuario.getSenha().equals(confirmSenha)) {
            bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
        }

        if (bindingResult.hasErrors()) {
            adicionarAtributosComuns(model);
            model.addAttribute("usuario", usuario);
            return "usuarios/cadastro";
        }

        try {
            usuario.setPerfis(Set.of(perfilRepository.findById(perfilId)
                    .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"))));

            if (usuario.getStatus() != Usuario.Status.DEMITIDO) {
                usuario.setDataDesligamento(null);
            }

            usuarioService.salvarUsuario(usuario);
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            adicionarAtributosComuns(model);
            model.addAttribute("usuario", usuario);
            return "usuarios/cadastro";
        }

        return "redirect:/usuarios/listar";
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> exibirFoto(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);

        if (usuarioOpt.isPresent() && usuarioOpt.get().getFotoPerfil() != null) {
            byte[] foto = usuarioOpt.get().getFotoPerfil();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(foto, headers, HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/usuarios/listar";
        }
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", usuarioOpt.get());
        }
        adicionarAtributosComuns(model);
        return "usuarios/editar";
    }

    @PostMapping("/{id}/editar")
    public String salvarEdicaoUsuario(
            @PathVariable Long id,
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult,
            @RequestParam("confirmSenha") String confirmSenha,
            @RequestParam("perfilId") Long perfilId,
            Model model) {

        if (!usuario.getSenha().equals(confirmSenha)) {
            bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
        }

        if (bindingResult.hasErrors()) {
            adicionarAtributosComuns(model);
            return "usuarios/editar";
        }

        try {
            if (usuario.getDataDesligamento() != null) {
                usuarioService.excluirUsuario(id);
                return "redirect:/usuarios/listar";
            }

            usuario.setId(id);
            usuario.setPerfis(Set.of(perfilRepository.findById(perfilId)
                    .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"))));
            usuarioService.salvarUsuario(usuario);

        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            adicionarAtributosComuns(model);
            return "usuarios/editar";
        }

        return "redirect:/usuarios/listar";
    }

    @GetMapping("/editar")
    public String mostrarFormularioBuscaPorCpf() {
        return "usuarios/editar";  // formulário para digitar CPF
    }

    @PostMapping("/editar")
    public String buscarUsuarioPorCpf(@RequestParam("cpf") String cpf, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCpf(cpf);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("erro", "Usuário com CPF " + cpf + " não encontrado.");
            return "usuarios/editar";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        adicionarAtributosComuns(model);

        return "usuarios/editarcadastro"; // formulário completo para editar dados do usuário
    }

    @PostMapping("/{id}/excluir")
    public String excluirUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.excluirUsuario(id);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário excluído com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir usuário: " + e.getMessage());
        }
        return "redirect:/usuarios/listar";
    }

    private void adicionarAtributosComuns(Model model) {
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("generos", Genero.values());
        model.addAttribute("status", Usuario.Status.values());
        model.addAttribute("niveis", NivelAcesso.values());
        model.addAttribute("cargos", cargoRepository.findAll());
        model.addAttribute("departamentos", departamentoRepository.findAll());
    }

    // Endpoint AJAX para validar matrícula
    @GetMapping("/validar-matricula")
    @ResponseBody
    public Map<String, Boolean> validar(@RequestParam String matricula) {
        boolean autorizado = usuarioService.usuarioTemPermissaoParaExcluir(matricula);
        return Map.of("autorizado", autorizado);
    }
}
