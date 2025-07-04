package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private PerfilRepository perfilRepository;

    public void salvarUsuario(Usuario usuario, MultipartFile foto) throws IOException, Exception {
        // Verifica duplicidade de e-mail
        Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());

        if (existente.isPresent() && (usuario.getId() == null || !existente.get().getId().equals(usuario.getId()))) {
            throw new Exception("Email já cadastrado!");
        }

        // Criptografa a senha se ainda não estiver
        if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // Define a foto, se enviada
        if (foto != null && !foto.isEmpty()) {
            usuario.setFotoPerfil(foto.getBytes());
        }

        // Se nenhum perfil foi setado (formulário não enviou), define o padrão USER
        if (usuario.getPerfis() == null || usuario.getPerfis().isEmpty()) {
            Perfil perfilPadrao = perfilRepository.findByNome("USER")
                .orElseThrow(() -> new RuntimeException("Perfil padrão 'USER' não encontrado"));
            usuario.setPerfis(Set.of(perfilPadrao));
        }

        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // listar todos os usuarios do banco de dados
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }
}
