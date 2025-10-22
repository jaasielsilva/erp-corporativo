package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * ControllerAdvice para injeção automática do usuário logado
 * em todos os controllers que precisam dessa informação.
 */
@ControllerAdvice(basePackages = {
    "com.jaasielsilva.portalceo.controller.financeiro",
    "com.jaasielsilva.portalceo.controller"
})
public class UsuarioLogadoControllerAdvice {

    /**
     * Injeta automaticamente o usuário logado em todos os controllers
     * que estão nos pacotes especificados no @ControllerAdvice.
     * 
     * @return Usuario logado ou null se não houver usuário autenticado
     */
    @ModelAttribute("usuarioLogado")
    public Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Usuario) {
            Usuario usuario = (Usuario) principal;
            
            // Validar se o usuário está ativo
            if (usuario.getStatus() == Usuario.Status.ATIVO) {
                return usuario;
            }
        }
        
        return null;
    }

    /**
     * Injeta o ID do usuário logado para facilitar o uso em operações
     * que precisam apenas do identificador.
     * 
     * @return ID do usuário logado ou null se não houver usuário autenticado
     */
    @ModelAttribute("usuarioLogadoId")
    public Long getUsuarioLogadoId() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null ? usuario.getId() : null;
    }

    /**
     * Injeta o nome do usuário logado para exibição em interfaces.
     * 
     * @return Nome do usuário logado ou string vazia se não houver usuário autenticado
     */
    @ModelAttribute("usuarioLogadoNome")
    public String getUsuarioLogadoNome() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null ? usuario.getNome() : "";
    }

    /**
     * Verifica se existe um usuário logado válido.
     * 
     * @return true se existe usuário logado e ativo, false caso contrário
     */
    @ModelAttribute("temUsuarioLogado")
    public boolean temUsuarioLogado() {
        return getUsuarioLogado() != null;
    }
}