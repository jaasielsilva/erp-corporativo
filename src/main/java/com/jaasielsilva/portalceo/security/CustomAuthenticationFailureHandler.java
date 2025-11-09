package com.jaasielsilva.portalceo.security;

import com.jaasielsilva.portalceo.model.ti.AlertaSeguranca;
import com.jaasielsilva.portalceo.repository.ti.AlertaSegurancaRepository;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final AlertaSegurancaRepository alertaRepo;
    private final UsuarioRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public CustomAuthenticationFailureHandler(AlertaSegurancaRepository alertaRepo,
            UsuarioRepository usuarioRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.alertaRepo = alertaRepo;
        this.usuarioRepository = usuarioRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private static final String MSG_INVALID_CREDENTIALS = "Usuário ou senha inválidos.";
    private static final String MSG_USER_DISABLED = "Usuário demitido não pode acessar o sistema.";
    private static final String MSG_USER_INATIVO = "Usuário inativo não pode fazer login.";
    private static final String MSG_USER_BLOQUEADO = "Usuário bloqueado não pode fazer login.";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage = MSG_INVALID_CREDENTIALS;
        Throwable cause = exception.getCause();

        if (exception instanceof DisabledException ||
                (cause != null && cause instanceof DisabledException)) {
            errorMessage = MSG_USER_DISABLED;

        } else if (exception instanceof LockedException ||
                (cause != null && cause instanceof LockedException)) {
            String msg = exception.getMessage();
            if (msg != null) {
                if (msg.toLowerCase().contains("inativo")) {
                    errorMessage = MSG_USER_INATIVO;
                } else if (msg.toLowerCase().contains("bloqueado")) {
                    errorMessage = MSG_USER_BLOQUEADO;
                } else {
                    errorMessage = "Usuário inativo ou bloqueado não pode fazer login.";
                }
            }

        } else if (exception instanceof InternalAuthenticationServiceException) {
            if (cause != null) {
                String msg = cause.getMessage();
                if (msg != null) {
                    msg = msg.toLowerCase();
                    if (msg.contains("demitido")) {
                        errorMessage = MSG_USER_DISABLED;
                    } else if (msg.contains("inativo")) {
                        errorMessage = MSG_USER_INATIVO;
                    } else if (msg.contains("bloqueado")) {
                        errorMessage = MSG_USER_BLOQUEADO;
                    }
                }
            }
        }

        // Captura informações da tentativa
        String ip = request.getRemoteAddr();

        // 🔹 Se o login for local, exibe "localhost" em vez do IPv6 padrão
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "localhost";
        }

        String usuario = request.getParameter("username");

        // Cria alerta e salva
        AlertaSeguranca alerta = new AlertaSeguranca();
        alerta.setData(LocalDateTime.now());
        alerta.setTitulo("Tentativa de login suspeita");
        alerta.setSeveridade("MEDIA");
        alerta.setOrigem("Login Web (" + ip + ")");
        alerta.setDetalhes("Falha de login para o usuário: " + usuario + " — Motivo: " + errorMessage);

        alertaRepo.save(alerta);

        System.out.println("🚨 Alerta salvo: " + alerta.getDetalhes());

        // Enviar alerta em tempo real para usuários ADMIN
        try {
            List<Usuario> admins = usuarioRepository.findAll().stream()
                    .filter(u -> u.getStatus() == Usuario.Status.ATIVO)
                    .filter(u -> u.getPerfis() != null && u.getPerfis().stream()
                            .anyMatch(p -> p.getNome() != null && p.getNome().equalsIgnoreCase("ADMIN")))
                    .collect(Collectors.toList());

            Map<String, Object> payload = new HashMap<>();
            payload.put("title", "Alerta de Segurança: Falha de Login");
            payload.put("message", "Usuário: " + usuario + " | IP: " + ip + " | Motivo: " + errorMessage);
            payload.put("type", "security");
            payload.put("priority", "HIGH");
            payload.put("timestamp", System.currentTimeMillis());

            for (Usuario admin : admins) {
                if (admin.getEmail() != null && !admin.getEmail().isBlank()) {
                    String destination = "/queue/notifications/" + admin.getEmail();
                    messagingTemplate.convertAndSend(destination, payload);
                }
            }
        } catch (Exception e) {
            System.err.println("Falha ao enviar notificação em tempo real para ADMIN: " + e.getMessage());
        }

        // Redireciona com erro
        response.sendRedirect("/login?error=true&message=" +
                URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));

    }
}
