package com.jaasielsilva.portalceo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Usuario usuario,
                         @RequestParam("foto") MultipartFile foto) {
        try {
            usuarioService.salvarUsuario(usuario, foto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/usuarios/form";
    }
}
