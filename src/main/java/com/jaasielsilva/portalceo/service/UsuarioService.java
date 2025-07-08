package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private JavaMailSender mailSender;

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

        if (usuario.getFotoPerfil() == null) {
            try {
                ClassPathResource imagemPadrao = new ClassPathResource("static/img/gerente.png");
                byte[] fotoPadrao = Files.readAllBytes(imagemPadrao.getFile().toPath());
                usuario.setFotoPerfil(fotoPadrao);
            } catch (IOException e) {
                e.printStackTrace();
                usuario.setFotoPerfil(null);
            }
        }

        try {
            usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            String msg = Optional.ofNullable(e.getCause()).map(Throwable::getMessage).orElse(e.getMessage());
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("cpf")) throw new Exception("CPF já cadastrado no sistema.");
                if (lower.contains("email")) throw new Exception("Email já cadastrado no sistema.");
                if (lower.contains("matricula")) throw new Exception("Matrícula já cadastrada no sistema.");
            }
            throw e;
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void excluirUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário com ID " + id + " não encontrado.");
        }
        usuarioRepository.deleteById(id);
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

    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    public boolean usuarioTemPermissaoParaExcluir(String matricula) {
        return usuarioRepository.findByMatricula(matricula)
                .map(usuario -> usuario.getPerfis().stream()
                        .anyMatch(perfil -> perfil.getNome().equalsIgnoreCase("ADMIN")))
                .orElse(false);
    }

    public List<Usuario> buscarPorNomeOuEmail(String busca) {
        return usuarioRepository.buscarPorNomeOuEmail(busca);
    }

    // ---------------------- RESET DE SENHA ----------------------

    public boolean autenticarAdmin(String loginAdmin, String senhaAdmin) {
        Optional<Usuario> adminOpt = usuarioRepository.findByEmail(loginAdmin);
        if (adminOpt.isEmpty()) return false;

        Usuario admin = adminOpt.get();
        boolean isAdmin = admin.getPerfis().stream()
                .anyMatch(perfil -> perfil.getNome().equalsIgnoreCase("ADMIN"));
        return isAdmin && passwordEncoder.matches(senhaAdmin, admin.getSenha());
    }

    public String recuperarSenhaDescriptografada(String loginUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginUsuario);
        return usuarioOpt.map(usuario -> "senha-descriptografada-exemplo").orElse(null);
    }

public boolean enviarSenhaPorEmail(String emailDestinatario, String senha) {
    try {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(emailDestinatario);
        if (usuarioOpt.isEmpty()) return false;

        Usuario usuario = usuarioOpt.get();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(emailDestinatario);
        helper.setSubject("Recuperação de senha - Painel do CEO");

        String htmlMsg = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; background-color: #f5f8fa; padding: 20px; }" +
                "    .container { max-width: 600px; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                "    h2 { color: #333333; }" +
                "    p { font-size: 16px; color: #555555; }" +
                "    .senha { font-size: 18px; font-weight: bold; color: #2d89ef; background: #e3f2fd; padding: 10px; border-radius: 4px; display: inline-block; }" +
                "    .footer { margin-top: 30px; font-size: 12px; color: #999999; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <h2>Olá " + usuario.getNome() + ",</h2>" +
                "    <p>Sua senha foi resetada com sucesso.</p>" +
                "    <p>Sua nova senha é:</p>" +
                "    <p class='senha'>" + senha + "</p>" +
                "    <p>Recomendamos que altere sua senha após o login para garantir sua segurança.</p>" +
                "    <div class='footer'>Este é um e-mail automático, por favor não responda.</div>" +
                "  </div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlMsg, true);  // 'true' indica que é HTML

        mailSender.send(mimeMessage);

        return true;
    } catch (MessagingException e) {
        e.printStackTrace();
        return false;
    }
}


    public void resetarSenhaPorId(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário com ID " + id + " não encontrado.");
        }

        Usuario usuario = usuarioOpt.get();

        String novaSenha = "123456"; // aqui pode ser uma senha gerada dinamicamente
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // Envia a nova senha por e-mail
        boolean enviado = enviarSenhaPorEmail(usuario.getEmail(), novaSenha);
        if (!enviado) {
            throw new RuntimeException("Senha resetada, mas falha ao enviar o e-mail.");
        }
    }
}
