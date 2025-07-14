package com.jaasielsilva.portalceo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

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
            } else {
                errorMessage = "Usuário inativo ou bloqueado não pode fazer login.";
            }

        } else if (exception instanceof InternalAuthenticationServiceException) {
            // Examina a causa interna e define mensagem conforme
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

        // Log para depuração
        System.out.println("Falha de autenticação: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());

        // Redireciona com a mensagem codificada na URL
        response.sendRedirect("/login?error=true&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }
}
