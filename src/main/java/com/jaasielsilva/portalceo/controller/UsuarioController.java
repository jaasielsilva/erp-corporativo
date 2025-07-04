package com.jaasielsilva.portalceo.controller;

import java.util.List;
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

import com.jaasielsilva.portalceo.model.Perfil;
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

    @GetMapping("/index")
    public String mostrarFormindex(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
            model.addAttribute("perfis", perfilRepository.findAll());
        }
        return "usuarios/index";
    }
    @GetMapping("/cadastro")
    public String mostrarFormCadastro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
            model.addAttribute("perfis", perfilRepository.findAll());
        }
        return "usuarios/cadastro";  // Apontando para cadastro.html
    }


    @PostMapping("/cadastrar")
    public String cadastrarUsuario(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult bindingResult,
        @RequestParam("confirmSenha") String confirmSenha,
        @RequestParam("foto") MultipartFile foto,
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
            Perfil perfilSelecionado = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil selecionado não encontrado"));
            usuario.setPerfis(Set.of(perfilSelecionado));

            usuarioService.salvarUsuario(usuario, foto);
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("perfis", perfilRepository.findAll()); // Para evitar erro no reload
            return "usuarios/cadastro";
        }

        return "redirect:/dashboard";
}


    // Endpoint pra buscar a imagem salva no banco de dados exemplo http://localhost:8080/usuarios/2/foto 
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

    @GetMapping("/listar")
    public String listarUsuarios(Model model) {
    List<Usuario> usuarios = usuarioService.buscarTodos();
    model.addAttribute("usuarios", usuarios);
    return "usuarios/listar";
    }

}
