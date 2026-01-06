package com.jaasielsilva.portalceo.security;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.AcaoUsuario;
import com.jaasielsilva.portalceo.repository.AcaoUsuarioRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final AcaoUsuarioRepository acaoUsuarioRepository;
    private final TaskExecutor taskExecutor;
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    public CustomAuthenticationSuccessHandler(UsuarioRepository usuarioRepository,
                                              AcaoUsuarioRepository acaoUsuarioRepository,
                                              TaskExecutor taskExecutor) {
        this.usuarioRepository = usuarioRepository;
        this.acaoUsuarioRepository = acaoUsuarioRepository;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String email = authentication.getName();

        final String ipAddressRaw = request.getRemoteAddr();
        final String ipAddress = "0:0:0:0:0:0:0:1".equals(ipAddressRaw) ? "localhost" : ipAddressRaw;
        taskExecutor.execute(() -> {
            long t0 = System.nanoTime();
            try {
                usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                    usuario.setUltimoAcesso(LocalDateTime.now());
                    usuario.setOnline(true);
                    long tBeforeSave = System.nanoTime();
                    usuarioRepository.save(usuario);
                    long tAfterSave = System.nanoTime();
                    try {
                        AcaoUsuario acao = new AcaoUsuario(LocalDateTime.now(), "Login", usuario, null);
                        acao.setIp(ipAddress);
                        acaoUsuarioRepository.save(acao);
                        long tAfterAudit = System.nanoTime();
                        if (log.isDebugEnabled()) {
                            long lookupMs = (tBeforeSave - t0) / 1_000_000;
                            long saveUserMs = (tAfterSave - tBeforeSave) / 1_000_000;
                            long saveAuditMs = (tAfterAudit - tAfterSave) / 1_000_000;
                            log.debug("Login success timing: lookup={}ms, updateUser={}ms, audit={}ms for user={} ip={}",
                                    lookupMs, saveUserMs, saveAuditMs, email, ipAddress);
                        }
                    } catch (Exception ignored) {}
                });
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Async login audit failure for user={} ip={}", email, ipAddress);
                }
            }
        });

        // Redirecionamento Inteligente baseado em permissões
        String targetUrl = determineTargetUrl(authentication);
        response.sendRedirect(targetUrl);
    }

    private String determineTargetUrl(Authentication authentication) {
        // 1. Dashboard Principal (Prioridade Máxima)
        boolean hasMainDashboard = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("DASHBOARD_EXECUTIVO_VISUALIZAR") ||
                               a.getAuthority().equals("DASHBOARD_OPERACIONAL_VISUALIZAR") ||
                               a.getAuthority().equals("DASHBOARD_FINANCEIRO_VISUALIZAR") ||
                               a.getAuthority().equals("ROLE_MASTER") ||
                               a.getAuthority().equals("ROLE_ADMIN"));

        if (hasMainDashboard) {
            return "/dashboard";
        }

        // 2. Dashboards Setoriais
        if (hasAuthority(authentication, "MENU_VENDAS_DASHBOARD")) return "/vendas";
        if (hasAuthority(authentication, "MENU_FINANCEIRO_DASHBOARD")) return "/financeiro";
        if (hasAuthority(authentication, "MENU_TI_DASHBOARD")) return "/ti";
        if (hasAuthority(authentication, "MENU_JURIDICO_DASHBOARD")) return "/juridico";

        // 3. Módulos Operacionais (Listagens Principais)
        if (hasAuthority(authentication, "MENU_CLIENTES_LISTAR")) return "/clientes";
        if (hasAuthority(authentication, "MENU_VENDAS_PEDIDOS")) return "/vendas/pedidos";
        if (hasAuthority(authentication, "MENU_COMPRAS")) return "/fornecedores";
        if (hasAuthority(authentication, "MENU_ESTOQUE_PRODUTOS")) return "/produtos";

        // 4. Recursos Humanos (Prioridade para funcionalidades de gestão)
        if (hasAuthority(authentication, "MENU_RH_COLABORADORES_LISTAR")) return "/rh/colaboradores/listar";
        if (hasAuthority(authentication, "MENU_RH_FOLHA_LISTAR")) return "/rh/folha-pagamento/listar";
        if (hasAuthority(authentication, "MENU_RH_RECRUTAMENTO_VAGAS")) return "/rh/recrutamento/vagas";

        // 5. Serviços (Acesso Básico)
        if (hasAuthority(authentication, "MENU_SERVICOS_SOLICITACOES_GERENCIAR")) return "/solicitacoes/pendentes";
        
        // 6. Fallback seguro (Ajuda ou Perfil)
        return "/ajuda";
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
