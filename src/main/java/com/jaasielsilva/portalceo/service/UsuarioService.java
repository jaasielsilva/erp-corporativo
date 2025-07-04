package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;

import jakarta.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
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

    public void salvarUsuario(Usuario usuario) throws Exception {
    Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());

    if (existente.isPresent() && (usuario.getId() == null || !existente.get().getId().equals(usuario.getId()))) {
        throw new Exception("Email já cadastrado!");
    }

    if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
    }

    if (usuario.getPerfis() == null || usuario.getPerfis().isEmpty()) {
        Perfil perfilPadrao = perfilRepository.findByNome("USER")
            .orElseThrow(() -> new RuntimeException("Perfil padrão 'USER' não encontrado"));
        usuario.setPerfis(Set.of(perfilPadrao));
    }

    try {
        usuarioRepository.save(usuario);
    } catch (DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        // Tenta identificar a constraint pelo nome na mensagem de erro
        String message = cause != null ? cause.getMessage() : e.getMessage();
        if (message != null) {
            String lowerMsg = message.toLowerCase();
            if (lowerMsg.contains("cpf")) {
                throw new Exception("CPF já cadastrado no sistema.");
            } else if (lowerMsg.contains("email")) {
                throw new Exception("Email já cadastrado no sistema.");
            } else if (lowerMsg.contains("matricula")) {
                throw new Exception("Matrícula já cadastrada no sistema.");
            }
        }
        // Se não for erro esperado, relança
        throw e;}
    }



    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    public long totalUsuarios() {
        return usuarioRepository.count();
    }

    public long totalAtivos() {
        return usuarioRepository.countByStatus(Usuario.Status.ATIVO);
    }

    public long totalInativos() {
        return usuarioRepository.countByStatus(Usuario.Status.INATIVO);
    }

    public long totalAdministradores() {
        return usuarioRepository.countUsuariosPorPerfil("ADMIN");
    }

    public long totalBloqueados() {
        return totalInativos();
    }

    public EstatisticasUsuariosDTO buscarEstatisticas() {
        return new EstatisticasUsuariosDTO(
            totalUsuarios(),
            totalAtivos(),
            totalAdministradores(),
            totalBloqueados()
        );
    }
}
