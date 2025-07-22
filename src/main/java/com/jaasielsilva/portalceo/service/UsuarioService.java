package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.PasswordResetToken;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.PasswordResetTokenRepository;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UsuarioService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordResetTokenRepository tokenRepository;

    // ===============================
    // MÉTODOS DE CADASTRO E ATUALIZAÇÃO
    // ===============================

    /**
     * Salva ou atualiza um usuário no banco.
     * Valida e impede alteração do admin master.
     * Garante senha criptografada e perfil padrão.
     */
    public void salvarUsuario(Usuario usuario) throws Exception {
        if (usuario.getId() != null) {
            Optional<Usuario> existente = usuarioRepository.findById(usuario.getId());
            if (existente.isPresent() && "admin@teste.com".equalsIgnoreCase(existente.get().getEmail())) {
                throw new IllegalStateException("O usuário administrador principal não pode ser alterado.");
            }
        }

        Optional<Usuario> existenteEmail = usuarioRepository.findByEmail(usuario.getEmail());
        if (existenteEmail.isPresent() && (usuario.getId() == null || !existenteEmail.get().getId().equals(usuario.getId()))) {
            throw new Exception("Email já cadastrado!");
        }

        // Criptografa senha se não estiver criptografada
        if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // Define perfil padrão caso não haja perfis atribuídos
        if (usuario.getPerfis() == null || usuario.getPerfis().isEmpty()) {
            Perfil perfilPadrao = perfilRepository.findByNome("USER")
                .orElseThrow(() -> new RuntimeException("Perfil padrão 'USER' não encontrado"));
            usuario.setPerfis(Set.of(perfilPadrao));
        }

        // Define foto padrão se nenhuma for enviada
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

    /**
     * Atualiza os perfis de um usuário,
     * garantindo que não seja removido o último ADMIN.
     */
    public void atualizarPerfisUsuario(Long userId, Set<Perfil> novosPerfis) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean temPerfilAdmin = usuario.getPerfis().stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));
        boolean novoTemPerfilAdmin = novosPerfis.stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

        if (temPerfilAdmin && !novoTemPerfilAdmin) {
            long outrosAdmins = usuarioRepository.countByPerfilNomeExcludingUser("ADMIN", userId);
            if (outrosAdmins == 0) {
                throw new RuntimeException("Não é permitido remover o último usuário ADMIN.");
            }
        }

        usuario.setPerfis(novosPerfis);
        usuarioRepository.save(usuario);
    }

    // ===============================
    // MÉTODOS DE BUSCA E LISTAGEM
    // ===============================

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    public List<Usuario> buscarPorNomeOuEmail(String busca) {
        return usuarioRepository.buscarPorNomeOuEmail(busca);
    }

    public Optional<Usuario> buscarPorMatricula(String matricula) {
    return usuarioRepository.findByMatricula(matricula);
}

    public boolean usuarioTemPermissaoParaExcluir(String matricula) {
        return usuarioRepository.findByMatricula(matricula)
            .map(usuario -> usuario.getPerfis().stream()
                .anyMatch(perfil -> perfil.getNome().equalsIgnoreCase("ADMIN")))
            .orElse(false);
    }

    // ===============================
    // MÉTODOS DE ESTATÍSTICAS
    // ===============================

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

    // ===============================
    // MÉTODOS DE EXCLUSÃO
    // ===============================

    /**
     * Exclui usuário validando permissões e regras de negócio.
     * Remove tokens associados antes da exclusão.
     */
    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void excluirUsuario(Long id, String matriculaSolicitante) {
    Usuario usuario = usuarioRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuário com ID " + id + " não encontrado."));

    if ("admin@teste.com".equalsIgnoreCase(usuario.getEmail())) {
        throw new IllegalStateException("Este usuário administrador não pode ser excluído.");
    }

    if (usuario.getMatricula().equalsIgnoreCase(matriculaSolicitante)) {
        throw new IllegalStateException("Usuário não pode se excluir sozinho.");
    }

    if (!usuarioTemPermissaoParaExcluir(matriculaSolicitante)) {
        throw new IllegalStateException("Usuário não pode ser excluído: matrícula inválida.");
    }

    boolean ehAdmin = usuario.getPerfis().stream()
        .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

    if (ehAdmin && usuarioRepository.countUsuariosPorPerfil("ADMIN") <= 1) {
        throw new IllegalStateException("Não é possível excluir o último administrador do sistema.");
    }

    // Em vez de deletar, atualiza o status para DEMITIDO e registra a data de desligamento
    usuario.setStatus(Usuario.Status.DEMITIDO);
    usuario.setDataDesligamento(java.time.LocalDate.now());

    // Opcional: limpar informações sensíveis, telefone, ramal, etc (dependendo da política)
    usuario.setTelefone(null);
    usuario.setRamal(null);
    usuarioRepository.save(usuario);

    // Também pode deletar tokens, se necessário
    tokenRepository.deleteByUsuarioId(usuario.getId());
}


    // ===============================
    // MÉTODOS DE RESET DE SENHA E TOKEN
    // ===============================

    /**
     * Solicita reset de senha por ID de usuário.
     * Envia email com link para redefinição.
     */
    public void resetarSenhaPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário com ID " + id + " não encontrado."));

        boolean enviado = enviarLinkRedefinicaoSenha(usuario.getEmail());
        if (!enviado) {
            throw new RuntimeException("Falha ao enviar o e-mail com link de redefinição.");
        }
    }

    /**
     * Gera e envia o link de redefinição de senha para o email informado.
     */
    public boolean enviarLinkRedefinicaoSenha(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) return false;

        Usuario usuario = usuarioOpt.get();
        String token = gerarTokenRedefinicao(usuario);
        String url = "http://localhost:8080/resetar-senha?token=" + token;

        String html = "<html><body>"
                + "<h3>Olá " + usuario.getNome() + ",</h3>"
                + "<p>Recebemos sua solicitação para redefinição de senha.</p>"
                + "<p><a href=\"" + url + "\">Clique aqui para redefinir sua senha</a></p>"
                + "<p><small>O link expira em 1 hora.</small></p>"
                + "</body></html>";

        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Redefinição de senha - Painel do CEO");
            helper.setText(html, true);
            mailSender.send(mensagem);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Optional<Usuario> validarToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) return Optional.empty();

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiracao().isBefore(LocalDateTime.now()) || resetToken.isUsado()) {
            return Optional.empty();
        }

        return Optional.of(resetToken.getUsuario());
    }

    @Transactional
    public boolean redefinirSenhaComToken(String token, String novaSenha) {
        try {
            Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
            if (tokenOpt.isEmpty()) return false;

            PasswordResetToken resetToken = tokenOpt.get();

            if (resetToken.getExpiracao().isBefore(LocalDateTime.now()) || resetToken.isUsado()) {
                return false;
            }

            Usuario usuario = resetToken.getUsuario();
            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuarioRepository.save(usuario);

            resetToken.setUsado(true);
            tokenRepository.save(resetToken);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao redefinir a senha. Por favor, tente novamente mais tarde.");
        }
    }

    /**
     * Gera um token único para redefinição de senha válido por 1 hora.
     */
    @Transactional
    public String gerarTokenRedefinicao(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiracao = LocalDateTime.now().plusHours(1);

        PasswordResetToken novoToken = new PasswordResetToken(token, usuario, expiracao);
        novoToken.setUsado(false);
        tokenRepository.save(novoToken);

        return token;
    }
    
}
