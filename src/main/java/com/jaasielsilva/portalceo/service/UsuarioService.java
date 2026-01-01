package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.EstatisticasUsuariosDTO;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.model.PasswordResetToken;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
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

import com.jaasielsilva.portalceo.security.UsuarioDetailsService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;
    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
    }

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
            if (existente.isPresent()) {
                Usuario usuarioExistente = existente.get();
                // Protege usuário MASTER e admin principal
                if (usuarioExistente.getNivelAcesso() == NivelAcesso.MASTER ||
                        "admin@teste.com".equalsIgnoreCase(usuarioExistente.getEmail()) ||
                        "master@sistema.com".equalsIgnoreCase(usuarioExistente.getEmail())) {
                    throw new IllegalStateException("Este usuário é protegido e não pode ser alterado.");
                }
            }
        }

        Optional<Usuario> existenteEmail = usuarioRepository.findByEmail(usuario.getEmail());
        if (existenteEmail.isPresent()
                && (usuario.getId() == null || !existenteEmail.get().getId().equals(usuario.getId()))) {
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
                try (java.io.InputStream in = imagemPadrao.getInputStream()) {
                    byte[] fotoPadrao = in.readAllBytes();
                    usuario.setFotoPerfil(fotoPadrao);
                }
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
                if (lower.contains("cpf"))
                    throw new Exception("CPF já cadastrado no sistema.");
                if (lower.contains("email"))
                    throw new Exception("Email já cadastrado no sistema.");
                if (lower.contains("matricula"))
                    throw new Exception("Matrícula já cadastrada no sistema.");
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
        usuarioDetailsService.evictAuthorities(usuario.getEmail());
    }

    /**
     * Atualiza as configurações de notificação sonora de um usuário.
     */
    public void atualizarConfiguracoesNotificacao(Long userId, boolean notificacoesSonorasAtivadas, String somNotificacao) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setNotificacoesSonorasAtivadas(notificacoesSonorasAtivadas);
        usuario.setSomNotificacao(somNotificacao);
        usuarioRepository.save(usuario);
    }

    /**
     * Atualiza preferências de alertas de segurança do usuário.
     */
    public void atualizarPreferenciasSeguranca(Long userId, Boolean mutarToasts, Boolean preferirBannerSeguranca,
                                               Integer volumeNotificacao, Boolean naoPerturbeAtivo,
                                               LocalDateTime naoPerturbeAte) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (mutarToasts != null) usuario.setMutarToasts(mutarToasts);
        if (preferirBannerSeguranca != null) usuario.setPreferirBannerSeguranca(preferirBannerSeguranca);
        if (volumeNotificacao != null) usuario.setVolumeNotificacao(Math.max(0, Math.min(100, volumeNotificacao)));
        if (naoPerturbeAtivo != null) usuario.setNaoPerturbeAtivo(naoPerturbeAtivo);
        usuario.setNaoPerturbeAte(naoPerturbeAte);

        usuarioRepository.save(usuario);
    }

    // ===============================
    // MÉTODOS DE BUSCA E LISTAGEM
    // ===============================

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorEmailLeve(String email) {
        return usuarioRepository.findByEmailSimple(email);
    }

    @Transactional
    public void setOnline(String email, boolean online) {
        usuarioRepository.findByEmail(email).ifPresent(u -> { u.setOnline(online); usuarioRepository.save(u); });
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    // Método que retorna todos os usuários
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    public List<Usuario> buscarPorNomeOuEmail(String busca) {
        return usuarioRepository.buscarPorNomeOuEmail(busca);
    }
    
    public List<Usuario> buscarUsuariosOnline() {
        return usuarioRepository.findByOnlineTrueAndStatus(Usuario.Status.ATIVO);
    }

    public Optional<Usuario> buscarPorMatricula(String matricula) {
        return usuarioRepository.findByMatricula(matricula);
    }

    /**
     * Gera uma matrícula única para um novo usuário
     */
    public String gerarMatriculaUnica() {
        long startTime = System.currentTimeMillis();
        System.out.println("[PERFORMANCE] Iniciando geração de matrícula única");
        
        // Otimização: buscar a maior matrícula existente em vez de contar todos os usuários
        long countStart = System.currentTimeMillis();
        String ultimaMatricula = usuarioRepository.findTopByMatriculaStartingWithOrderByMatriculaDesc("USR")
                .map(Usuario::getMatricula)
                .orElse("USR0000");
        long countEnd = System.currentTimeMillis();
        System.out.println("[PERFORMANCE] Tempo para buscar última matrícula: " + (countEnd - countStart) + "ms");
        
        // Extrair número da última matrícula e incrementar
        int proximoNumero = 1;
        if (!"USR0000".equals(ultimaMatricula)) {
            try {
                proximoNumero = Integer.parseInt(ultimaMatricula.substring(3)) + 1;
            } catch (NumberFormatException e) {
                System.err.println("Erro ao extrair número da matrícula: " + ultimaMatricula);
                // Fallback para método original
                long totalUsuarios = usuarioRepository.count();
                proximoNumero = (int) totalUsuarios + 1;
            }
        }
        
        String matricula;
        int tentativas = 0;
        do {
            matricula = String.format("USR%04d", proximoNumero);
            proximoNumero++;
            tentativas++;
            
            if (tentativas > 100) {
                throw new RuntimeException("Não foi possível gerar matrícula única após 100 tentativas");
            }
        } while (usuarioRepository.findByMatricula(matricula).isPresent());
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERFORMANCE] Matrícula gerada: " + matricula + " em " + tentativas + " tentativas, tempo total: " + (endTime - startTime) + "ms");
        
        return matricula;
    }

    public boolean usuarioTemPermissaoParaExcluir(String matricula) {
        return usuarioRepository.findByMatricula(matricula)
                .map(usuario -> usuario.getPerfis().stream()
                        .anyMatch(perfil -> perfil.getNome().equalsIgnoreCase("ADMIN")))
                .orElse(false);
    }

    /**
     * Busca usuários que podem gerenciar outros usuários (ADMIN, MASTER, etc.)
     */
    public List<Usuario> buscarUsuariosComPermissaoGerenciarUsuarios() {
        return usuarioRepository.findByPerfisContaining(
            perfilRepository.findById(1L).orElse(null) // Assumindo que o perfil ADMIN tem ID 1
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    /**
     * Busca usuários com permissão para gerenciar RH
     */
    public List<Usuario> buscarUsuariosComPermissaoGerenciarRH() {
        // Buscar usuários com perfil ADMIN ou com nível de acesso que permite gerenciar RH
        return usuarioRepository.findUsuariosComPermissaoGerenciarRH();
    }

    /**
     * Busca usuários aprovadores (ADMIN, MASTER, RH) para notificações de novos processos
     */
    public List<Usuario> buscarUsuariosAprovadores() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getStatus() == Usuario.Status.ATIVO)
                .filter(usuario -> usuario.getPerfis() != null && !usuario.getPerfis().isEmpty())
                .filter(usuario -> usuario.getPerfis().stream()
                    .anyMatch(perfil -> perfil.getNome().equals("ADMIN") || 
                                      perfil.getNome().equals("MASTER") || 
                                      perfil.getNome().equals("RH")))
                .collect(Collectors.toList());
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
                totalBloqueados());
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

        // Protege usuário MASTER e admin principal
        if (usuario.getNivelAcesso() == NivelAcesso.MASTER ||
                "admin@teste.com".equalsIgnoreCase(usuario.getEmail()) ||
                "master@sistema.com".equalsIgnoreCase(usuario.getEmail())) {
            throw new IllegalStateException("Este usuário é protegido e não pode ser excluído.");
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

        // Em vez de deletar, atualiza o status para DEMITIDO e registra a data de
        // desligamento
        usuario.setStatus(Usuario.Status.DEMITIDO);
        usuario.setDataDesligamento(java.time.LocalDate.now());

        // Opcional: limpar informações sensíveis, telefone, ramal, etc (dependendo da
        // política)
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
        if (usuarioOpt.isEmpty())
            return false;

        Usuario usuario = usuarioOpt.get();
        String token = gerarTokenRedefinicao(usuario);
        String url = "http://localhost:8080/resetar-senha?token=" + token;

        try {
            // Carrega o template HTML personalizado
            String templatePath = "src/main/resources/templates/email/recuperacao-senha.html";
            String html = carregarTemplateEmail(templatePath, usuario.getNome(), email, url);

            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("🔐 Redefinição de Senha - ERP Corporativo");
            helper.setText(html, true);
            mailSender.send(mensagem);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Carrega e processa o template de email substituindo os placeholders.
     */
    private String carregarTemplateEmail(String templatePath, String nomeUsuario, String emailUsuario,
            String linkRedefinicao) {
        try {
            // Lê o arquivo de template
            Path path = Paths.get(templatePath);
            String template = Files.readString(path, StandardCharsets.UTF_8);

            // Substitui os placeholders
            template = template.replace("{{NOME_USUARIO}}", nomeUsuario);
            template = template.replace("{{EMAIL_USUARIO}}", emailUsuario);
            template = template.replace("{{LINK_REDEFINICAO}}", linkRedefinicao);

            return template;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback para HTML simples em caso de erro
            return "<!DOCTYPE html>"
                    + "<html lang=\"pt-BR\">"
                    + "<head>"
                    + "  <meta charset=\"UTF-8\">"
                    + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                    + "  <title>Redefinição de Senha</title>"
                    + "  <style>"
                    + "    body{margin:0;padding:0;background:#f5f6f8;font-family:Arial,Helvetica,sans-serif;color:#333;} "
                    + "    .wrap{max-width:600px;margin:0 auto;background:#ffffff;} "
                    + "    .header{background:#4b6cb7;background:linear-gradient(135deg,#4b6cb7 0%,#182848 100%);padding:24px;text-align:center;color:#fff;} "
                    + "    .content{padding:24px 24px 8px 24px;} "
                    + "    h3{margin:0 0 12px 0;font-size:20px;line-height:1.3;color:#2c3e50;} "
                    + "    p{margin:0 0 16px 0;font-size:14px;line-height:1.6;} "
                    + "    .cta{display:inline-block;padding:12px 20px;border-radius:6px;text-decoration:none;font-weight:bold;"
                    + "         background:#667eea;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:#ffffff;} "
                    + "    .box{background:#f7f8fa;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin-top:16px;font-size:12px;word-break:break-all;} "
                    + "    .footer{padding:20px;text-align:center;font-size:12px;color:#6b7280;background:#f3f4f6;} "
                    + "    a{color:#3b82f6;} "
                    + "  </style>"
                    + "</head>"
                    + "<body>"
                    + "  <div class=\"wrap\">"
                    + "    <div class=\"header\">"
                    + "      <h2 style=\"margin:0;font-size:22px;\">ERP Corporativo</h2>"
                    + "      <div style=\"opacity:.85;font-size:13px;\">Redefinição de Senha</div>"
                    + "    </div>"
                    + "    <div class=\"content\">"
                    + "      <h3>Olá " + nomeUsuario + ",</h3>"
                    + "      <p>Recebemos sua solicitação para redefinir a sua senha de acesso ao <strong>ERP Corporativo</strong>.</p>"
                    + "      <p>Para continuar com segurança, clique no botão abaixo:</p>"
                    + "      <p style=\"text-align:center;margin:20px 0 8px 0;\">"
                    + "        <a class=\"cta\" href=\"" + linkRedefinicao
                    + "\" target=\"_blank\" rel=\"noopener\">Redefinir minha senha</a>"
                    + "      </p>"
                    + "      <div class=\"box\">"
                    + "        <div style=\"font-weight:bold;margin-bottom:8px;\">Se o botão não funcionar, copie e cole este link no navegador:</div>"
                    + "        <div>" + linkRedefinicao + "</div>"
                    + "      </div>"
                    + "      <p style=\"margin-top:16px;font-size:12px;color:#6b7280;\">"
                    + "        • O link expira em <strong>1 hora</strong> e pode ser usado apenas uma vez.<br>"
                    + "        • Se você não solicitou esta redefinição, ignore este e-mail."
                    + "      </p>"
                    + "    </div>"
                    + "    <div class=\"footer\">"
                    + "      Este é um e-mail automático. Por favor, não responda."
                    + "    </div>"
                    + "  </div>"
                    + "</body>"
                    + "</html>";
        }
    }

    @Transactional
    public Optional<Usuario> validarToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty())
            return Optional.empty();

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
            if (tokenOpt.isEmpty())
                return false;

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

    // Calcula performance financeira baseada na eficiência de gestão de usuários
    public int calcularPerformanceFinanceiro() {
        long totalUsuarios = totalUsuarios();
        long usuariosAtivos = totalAtivos();
        long usuariosBloqueados = totalBloqueados();

        if (totalUsuarios == 0) {
            return 85; // Valor padrão
        }

        // Calcula percentual de usuários ativos (menos bloqueados = melhor gestão)
        double percentualAtivos = ((double) usuariosAtivos / totalUsuarios) * 100;
        double percentualBloqueados = ((double) usuariosBloqueados / totalUsuarios) * 100;

        // Performance baseada em usuários ativos e poucos bloqueados
        double performance = percentualAtivos - (percentualBloqueados * 0.5);

        return (int) Math.min(100, Math.max(0, performance));
    }


    public Optional<Usuario> findByUsuario(String usuario) {
        // Método alterado para usar email em vez de campo 'usuario' inexistente
        return usuarioRepository.findByEmail(usuario);
    }

    public Usuario findByNome(String nome) {
        return usuarioRepository.findByNome(nome)
                                .orElse(null);
    }
                    
    // Registra o último acesso do usuário e marca como online
    @Transactional
    public void registrarUltimoAcesso(String login) {
        usuarioRepository.findByEmail(login).ifPresent(usuario -> {
            usuario.setUltimoAcesso(LocalDateTime.now());
            usuario.setOnline(true); 
            usuarioRepository.save(usuario);
        });
    }


    @Transactional
    public void criarUsuarioParaColaborador(Colaborador colaborador) {
        // Verificar se o colaborador já possui um usuário
        if (colaborador.getUsuario() != null) {
            System.out.println("Colaborador já possui usuário vinculado: " + colaborador.getUsuario().getMatricula());
            return;
        }

        Usuario usuario = new Usuario();
        
        // Copiar campos necessários do colaborador para o usuário
        usuario.setNome(colaborador.getNome());
        usuario.setEmail(colaborador.getEmail());
        usuario.setCpf(colaborador.getCpf());
        usuario.setTelefone(colaborador.getTelefone());
        
        // Copiar cargo e departamento se existirem
        if (colaborador.getCargo() != null) {
            usuario.setCargo(colaborador.getCargo());
        }
        if (colaborador.getDepartamento() != null) {
            usuario.setDepartamento(colaborador.getDepartamento());
        }
        
        // Copiar dados pessoais
        usuario.setDataNascimento(colaborador.getDataNascimento());
        usuario.setDataAdmissao(colaborador.getDataAdmissao());
        
        // Gerar matrícula única
        usuario.setMatricula(gerarMatriculaUnica());
        
        // Configurações padrão do usuário
        usuario.setSenha(passwordEncoder.encode("senha123")); // senha inicial padrão
        usuario.setStatus(Usuario.Status.ATIVO);
        usuario.setNivelAcesso(NivelAcesso.USER); // nível de acesso padrão
        usuario.setColaborador(colaborador);
        usuario.setOnline(false);
        
        // Salvar o usuário
        usuarioRepository.save(usuario);

        // Vincular usuário ao colaborador
        colaborador.setUsuario(usuario);
        colaboradorRepository.save(colaborador);
        
        System.out.println("Usuário criado automaticamente para colaborador: " + colaborador.getNome() + 
                          " - Matrícula: " + usuario.getMatricula());
    }

}
