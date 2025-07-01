package com.jaasielsilva.portalceo.controller;

import java.util.Optional;

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

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/novo")
    public String mostrarFormCadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/cadastro";  // template Thymeleaf
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Usuario usuario,
                         @RequestParam("foto") MultipartFile foto) {
        try {
            usuarioService.salvarUsuario(usuario, foto);
        } catch (Exception e) {
            e.printStackTrace();
            // opcional: adicionar mensagem de erro no model para mostrar ao usuário
        }
        return "redirect:/usuarios/novo";
    }

    @PostMapping("/cadastrar")
    public String cadastrarUsuario(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult,
            @RequestParam("confirmSenha") String confirmSenha,
            @RequestParam("fotoPerfil") MultipartFile fotoPerfil,
            Model model) {

        if (!usuario.getSenha().equals(confirmSenha)) {
            bindingResult.rejectValue("senha", "error.usuario", "As senhas não conferem.");
        }

        if (bindingResult.hasErrors()) {
            return "usuarios/cadastro";
        }

        try {
            usuarioService.salvarUsuario(usuario, fotoPerfil);
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar usuário: " + e.getMessage());
            return "usuarios/cadastro";
        }

        return "redirect:/login?cadastroSucesso";
    }

    // Endpoint para retornar a foto do usuário por ID
    @GetMapping("/usuarios/{id}/foto")
    public ResponseEntity<byte[]> getFotoPerfil(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id); // método no serviço para buscar usuário
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getFotoPerfil() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] foto = usuarioOpt.get().getFotoPerfil();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // ajuste se for outro formato
        return new ResponseEntity<>(foto, headers, HttpStatus.OK);
    }
}
