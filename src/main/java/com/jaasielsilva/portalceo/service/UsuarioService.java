package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void salvarUsuario(Usuario usuario, MultipartFile foto) throws IOException {
        // Criptografa a senha se ainda não estiver criptografada
        if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // Salvar foto se existir
        if (foto != null && !foto.isEmpty()) {
            String diretorio = "uploads/";
            File pasta = new File(diretorio);
            if (!pasta.exists()) {
                pasta.mkdirs(); // cria a pasta se não existir
            }

            String caminho = diretorio + foto.getOriginalFilename();
            foto.transferTo(new File(caminho));
            usuario.setFotoPerfil(caminho);
        }

        usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
