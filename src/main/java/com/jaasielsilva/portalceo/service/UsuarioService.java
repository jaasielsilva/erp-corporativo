package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void salvarUsuario(Usuario usuario, MultipartFile foto) throws IOException, Exception {
        // Verifica se já existe usuário com o mesmo email
        Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());

        if (existente.isPresent()) {
            // Se for cadastro novo, bloqueia
            if (usuario.getId() == null) {
                throw new Exception("Email já cadastrado!");
            }
            // Se for edição, permite salvar só se o id do existente for igual ao do usuário atual
            else if (!existente.get().getId().equals(usuario.getId())) {
                throw new Exception("Email já cadastrado para outro usuário!");
            }
        }

        // Criptografa senha se não estiver criptografada
        if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // Salva bytes da foto diretamente na entidade
        if (foto != null && !foto.isEmpty()) {
            usuario.setFotoPerfil(foto.getBytes());
        }

        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
