package com.jaasielsilva.portalceo.controller;

import java.util.Optional;
import java.util.Set;

import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.Genero;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.model.Usuario;
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

import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PerfilRepository perfilRepository;

    public UsuarioController(UsuarioService usuarioService, PerfilRepository perfilRepository) {
        this.usuarioService = usuarioService;
        this.perfilRepository = perfilRepository;
    }

    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("generos", Genero.values());
        model.addAttribute("status", Usuario.Status.values());
        model.addAttribute("niveis", NivelAcesso.values());
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
            model.addAttribute("perfis", perfilRepository.findAll());
            model.addAttribute("generos", Genero.values());
            model.addAttribute("status", Usuario.Status.values());
            model.addAttribute("niveis", NivelAcesso.values());
            model.addAttribute("usuario", usuario);
            return "usuarios/cadastro";
        }

        try {
            usuario.setPerfis(Set.of(perfilRepository.findById(perfilId)
                    .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"))));
            usuarioService.salvarUsuario(usuario);
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("perfis", perfilRepository.findAll());
            model.addAttribute("generos", Genero.values());
            model.addAttribute("status", Usuario.Status.values());
            model.addAttribute("niveis", NivelAcesso.values());
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

    // Mostrar formulário editar - GET /usuarios/{id}/editar
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isEmpty()) {
            // Pode usar RedirectAttributes para mensagem de erro
            return "redirect:/usuarios/listar";
        }
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", usuarioOpt.get());
        }
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("generos", Genero.values());
        model.addAttribute("status", Usuario.Status.values());
        model.addAttribute("niveis", NivelAcesso.values());
        return "usuarios/editar"; // nome do template Thymeleaf editar.html
    }

    // Salvar edição - POST /usuarios/{id}/editar
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
            model.addAttribute("perfis", perfilRepository.findAll());
            model.addAttribute("generos", Genero.values());
            model.addAttribute("status", Usuario.Status.values());
            model.addAttribute("niveis", NivelAcesso.values());
            return "usuarios/editar";
        }

        try {
            usuario.setId(id);
            usuario.setPerfis(Set.of(perfilRepository.findById(perfilId)
                    .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"))));
            usuarioService.salvarUsuario(usuario);
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("perfis", perfilRepository.findAll());
            model.addAttribute("generos", Genero.values());
            model.addAttribute("status", Usuario.Status.values());
            model.addAttribute("niveis", NivelAcesso.values());
            return "usuarios/editar";
        }

        return "redirect:/usuarios/listar";
    }

   // Mostrar formulário para digitar CPF - GET /usuarios/editar
@GetMapping("/editar")
public String mostrarFormularioBuscaPorCpf() {
    return "usuarios/editar";  // seu template com o formulário de busca por CPF
}

// Receber CPF do formulário e buscar usuário - POST /usuarios/editar
@PostMapping("/editar")
public String buscarUsuarioPorCpf(@RequestParam("cpf") String cpf, Model model) {
    Optional<Usuario> usuarioOpt = usuarioService.buscarPorCpf(cpf);

    if (usuarioOpt.isEmpty()) {
        model.addAttribute("erro", "Usuário com CPF " + cpf + " não encontrado.");
        return "usuarios/editar"; // volta para o formulário de busca com mensagem de erro
    }

    // Usuário encontrado, preparar dados para o formulário de edição completo
    model.addAttribute("usuario", usuarioOpt.get());
    model.addAttribute("perfis", perfilRepository.findAll());
    model.addAttribute("generos", Genero.values());
    model.addAttribute("status", Usuario.Status.values());
    model.addAttribute("niveis", NivelAcesso.values());

    return "usuarios/editarcadastro";  // template com o formulário completo para editar dados do usuário
}

}
