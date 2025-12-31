package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UsuarioRepository usuarioRepository;
    private final com.jaasielsilva.portalceo.service.juridico.ProcessoJuridicoService processoJuridicoService;
    private final com.jaasielsilva.portalceo.service.ContratoLegalService contratoLegalService;
    private Usuario usuarioCache;
    private String emailCache;

    public GlobalControllerAdvice(UsuarioRepository usuarioRepository, 
                                  com.jaasielsilva.portalceo.service.juridico.ProcessoJuridicoService processoJuridicoService,
                                  com.jaasielsilva.portalceo.service.ContratoLegalService contratoLegalService) {
        this.usuarioRepository = usuarioRepository;
        this.processoJuridicoService = processoJuridicoService;
        this.contratoLegalService = contratoLegalService;
    }

    @ModelAttribute("alertasJuridicosGlobais")
    public java.util.List<String> alertasJuridicosGlobais() {
        Usuario usuario = usuarioLogado();
        // Apenas carrega alertas se o usuário estiver logado e tiver acesso ao jurídico (assumindo acesso básico por enquanto)
        if (usuario == null) {
            return java.util.Collections.emptyList();
        }
        
        java.util.List<String> alertas = new java.util.ArrayList<>();
        java.time.LocalDate amanha = java.time.LocalDate.now().plusDays(1);
        
        try {
            // Contratos vencendo amanhã
            java.util.List<com.jaasielsilva.portalceo.model.ContratoLegal> proximosVencimentos = contratoLegalService.findContratosVencendoEm(2);
            if (proximosVencimentos != null) {
                for (com.jaasielsilva.portalceo.model.ContratoLegal c : proximosVencimentos) {
                    if (c.getDataVencimento() != null && c.getDataVencimento().isEqual(amanha)) {
                        alertas.add("O contrato <strong>" + c.getTitulo() + "</strong> vence amanhã (" + 
                            c.getDataVencimento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
                    }
                }
            }

            // Processos com prazos urgentes (amanhã)
            java.util.List<java.util.Map<String, Object>> urgentes = processoJuridicoService.obterProcessosUrgentes(2);
            if (urgentes != null) {
                for (java.util.Map<String, Object> p : urgentes) {
                    Object prazoObj = p.get("proximoPrazo");
                    if (prazoObj != null) {
                        java.time.LocalDate prazoData = null;
                        if (prazoObj instanceof java.time.LocalDate) {
                            prazoData = (java.time.LocalDate) prazoObj;
                        } else if (prazoObj instanceof String) {
                            try {
                                prazoData = java.time.LocalDate.parse((String) prazoObj);
                            } catch (Exception e) {}
                        }
                        
                        if (prazoData != null && prazoData.isEqual(amanha)) {
                            alertas.add("O processo <strong>" + p.get("numero") + "</strong> tem um prazo vencendo amanhã.");
                        }
                    }
                }
            }

            // Audiências amanhã
            java.util.List<java.util.Map<String, Object>> audiencias = processoJuridicoService.obterProximasAudiencias(2);
            if (audiencias != null) {
                for (java.util.Map<String, Object> a : audiencias) {
                    Object dataObj = a.get("dataHora");
                    if (dataObj != null) {
                        java.time.LocalDateTime dataHora = null;
                        if (dataObj instanceof java.time.LocalDateTime) {
                            dataHora = (java.time.LocalDateTime) dataObj;
                        } else if (dataObj instanceof String) {
                            try {
                                dataHora = java.time.LocalDateTime.parse((String) dataObj);
                            } catch (Exception e) {}
                        }
                        
                        if (dataHora != null && dataHora.toLocalDate().isEqual(amanha)) {
                            String horaFormatada = dataHora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                            String processoNum = (String) a.get("processoNumero");
                            alertas.add("Audiência amanhã às <strong>" + horaFormatada + "</strong> no processo <strong>" + (processoNum != null ? processoNum : "N/A") + "</strong>.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail to avoid breaking global navigation
            System.err.println("Erro ao carregar alertas jurídicos globais: " + e.getMessage());
        }

        return alertas;
    }

    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se está autenticado e não é usuário anonimo
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();

            // Cache simples para evitar múltiplas consultas na mesma requisição
            if (usuarioCache != null && email.equals(emailCache)) {
                return usuarioCache;
            }

            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            usuarioCache = usuario.orElse(null);
            emailCache = email;
            return usuarioCache;
        }

        // Limpar cache se não autenticado
        usuarioCache = null;
        emailCache = null;
        return null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getPerfis() != null) {
            return usuario.getPerfis().stream()
                    .anyMatch(perfil -> "ADMIN".equalsIgnoreCase(perfil.getNome()));
        }
        return false;
    }

    @ModelAttribute("isMaster")
    public boolean isMaster() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null && usuario.getNivelAcesso() == NivelAcesso.MASTER;
    }

    @ModelAttribute("isGerencial")
    public boolean isGerencial() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null && usuario.getNivelAcesso().ehGerencial();
    }

    @ModelAttribute("podeGerenciarUsuarios")
    public boolean podeGerenciarUsuarios() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null && usuario.getNivelAcesso().podeGerenciarUsuarios();
    }

    @ModelAttribute("podeAcessarFinanceiro")
    public boolean podeAcessarFinanceiro() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null && usuario.getNivelAcesso().podeAcessarFinanceiro();
    }

    @ModelAttribute("podeGerenciarRH")
    public boolean podeGerenciarRH() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null && usuario.getNivelAcesso().podeGerenciarRH();
    }

    @ModelAttribute("podeGerenciarVendas")
    public boolean podeGerenciarVendas() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null && usuario.getNivelAcesso().podeGerenciarVendas();
    }

    @ModelAttribute("nivelAcesso")
    public String nivelAcesso() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null ? usuario.getNivelAcesso().getDescricao() : "Visitante";
    }

    @ModelAttribute("nivelAcessoEnum")
    public NivelAcesso nivelAcessoEnum() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null ? usuario.getNivelAcesso() : NivelAcesso.VISITANTE;
    }

    @ModelAttribute("usuarioLogadoId")
    public Long usuarioLogadoId() {
        Usuario usuario = usuarioLogado();
        return usuario != null ? usuario.getId() : null;
    }

    @ModelAttribute("usuarioLogadoNome")
    public String usuarioLogadoNome() {
        Usuario usuario = usuarioLogado();
        return usuario != null ? usuario.getNome() : "";
    }

    @ModelAttribute("temUsuarioLogado")
    public boolean temUsuarioLogado() {
        return usuarioLogado() != null;
    }

    // Variáveis específicas para controle de acesso por área

    @ModelAttribute("isRH")
    public boolean isRH() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("rh") || cargoNome.contains("recursos humanos") ||
                    cargoNome.contains("gerente de rh") || cargoNome.contains("analista de rh");
        }
        return false;
    }

    @ModelAttribute("isFinanceiro")
    public boolean isFinanceiro() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("financeiro") || cargoNome.contains("contabil") || cargoNome.contains("contábil") ||
                    cargoNome.contains("tesouraria") || cargoNome.contains("controller");
        }
        return false;
    }

    @ModelAttribute("isVendas")
    public boolean isVendas() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("vendas") || cargoNome.contains("comercial") ||
                    cargoNome.contains("vendedor") || cargoNome.contains("representante");
        }
        return false;
    }

    @ModelAttribute("isEstoque")
    public boolean isEstoque() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("estoque") || cargoNome.contains("almoxarifado") ||
                    cargoNome.contains("logistica") || cargoNome.contains("logística") || 
                    cargoNome.contains("armazem") || cargoNome.contains("armazém");
        }
        return false;
    }

    @ModelAttribute("isCompras")
    public boolean isCompras() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("compras") || cargoNome.contains("suprimentos") ||
                    cargoNome.contains("procurement") || cargoNome.contains("aquisicoes") || cargoNome.contains("aquisições");
        }
        return false;
    }

    @ModelAttribute("isMarketing")
    public boolean isMarketing() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("marketing") || cargoNome.contains("comunicacao") || cargoNome.contains("comunicação") ||
                    cargoNome.contains("publicidade") || cargoNome.contains("branding");
        }
        return false;
    }

    @ModelAttribute("isTI")
    public boolean isTI() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("ti") || cargoNome.contains("tecnologia") ||
                    cargoNome.contains("desenvolvedor") || cargoNome.contains("analista de sistemas") ||
                    cargoNome.contains("suporte") || cargoNome.contains("infraestrutura");
        }
        return false;
    }

    @ModelAttribute("isJuridico")
    public boolean isJuridico() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        if (usuario != null && usuario.getCargo() != null) {
            String cargoNome = usuario.getCargo().getNome().toLowerCase();
            return cargoNome.contains("juridico") || cargoNome.contains("jurídico") || 
                    cargoNome.contains("advogado") || cargoNome.contains("legal") || 
                    cargoNome.contains("compliance");
        }
        return false;
    }

    // Variáveis para controle de acesso específico por área

    @ModelAttribute("isEstagiario")
    public boolean isEstagiario() {
        Usuario usuario = usuarioLogado(); // Garantir que o usuário seja carregado
        return usuario != null && usuario.getNivelAcesso() == NivelAcesso.ESTAGIARIO;
    }

    @ModelAttribute("podeAcessarRH")
    public boolean podeAcessarRH() {
        return isRH() || podeGerenciarRH() || isMaster() || isAdmin();
    }

    @ModelAttribute("podeAcessarVendas")
    public boolean podeAcessarVendas() {
        return isVendas() || isGerencial() || isMaster() || isAdmin();
    }

    @ModelAttribute("podeAcessarEstoque")
    public boolean podeAcessarEstoque() {
        return isEstoque() || isCompras() || isGerencial() || isMaster() || isAdmin();
    }

    @ModelAttribute("podeAcessarCompras")
    public boolean podeAcessarCompras() {
        return isCompras() || isGerencial() || isMaster() || isAdmin();
    }

    @ModelAttribute("podeAcessarMarketing")
    public boolean podeAcessarMarketing() {
        return isMarketing() || isGerencial() || isMaster() || isAdmin();
    }

    @ModelAttribute("podeAcessarTI")
    public boolean podeAcessarTI() {
        return isTI() || isMaster() || isAdmin();
    }

    @ModelAttribute("podeAcessarJuridico")
    public boolean podeAcessarJuridico() {
        return isJuridico() || isGerencial() || isMaster() || isAdmin();
    }

    @ModelAttribute("podeAcessarProjetos")
    public boolean podeAcessarProjetos() {
        return isTI() || isMaster() || isAdmin() || isGerencial();
    }

    @ModelAttribute("podeGerenciarProjetos")
    public boolean podeGerenciarProjetos() {
        return isMaster() || isAdmin() || isGerencial();
    }

}
