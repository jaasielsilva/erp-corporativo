package com.jaasielsilva.portalceo.controller;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilRepository perfilRepository;

    @GetMapping("/cadastro")
    public String mostrarFormCadastro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        model.addAttribute("perfis", perfilRepository.findAll());
        return "usuarios/cadastro";
    }

    // recebe os dados diretamente do banco e atualiza em tempo real
    @GetMapping("/index")
    public String mostrarFormindex(Model model) {
    model.addAttribute("usuario", new Usuario());

    EstatisticasUsuariosDTO stats = usuarioService.buscarEstatisticas();
    model.addAttribute("totalUsuarios", stats.getTotalUsuarios());
    model.addAttribute("ativos", stats.getTotalAtivos());
    model.addAttribute("administradores", stats.getTotalAdministradores());
    model.addAttribute("bloqueados", stats.getTotalBloqueados());

    return "usuarios/index";
}


    @GetMapping("/listar")
    public String mostrarFormlist(Model model) {
        model.addAttribute("usuarios", usuarioService.buscarTodos());
        return "usuarios/listar";
    }

    @PostMapping("/cadastrar")
    public String cadastrarUsuario(
    @Valid @ModelAttribute("usuario") Usuario usuario,
    BindingResult bindingResult,
    @RequestParam("confirmSenha") String confirmSenha,
    @RequestParam(value = "foto", required = false) MultipartFile foto,
    @RequestParam("perfilId") Long perfilId,
    Model model) {

    if (!usuario.getSenha().equals(confirmSenha)) {
        bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
    }

    if (bindingResult.hasErrors()) {
        model.addAttribute("perfis", perfilRepository.findAll());
        return "usuarios/cadastro";
    }

    try {
        usuario.setPerfis(Set.of(perfilRepository.findById(perfilId).orElseThrow()));

        if (foto != null && !foto.isEmpty()) {
            usuario.setFotoPerfil(foto.getBytes()); // converte MultipartFile para byte[]
        }

        usuarioService.salvarUsuario(usuario);

    } catch (Exception e) {
        model.addAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
        model.addAttribute("perfis", perfilRepository.findAll());
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
}
