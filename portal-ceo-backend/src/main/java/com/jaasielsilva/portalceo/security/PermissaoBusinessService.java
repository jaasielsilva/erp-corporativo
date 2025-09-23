package com.jaasielsilva.portalceo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Serviço para verificação e gerenciamento de permissões
 * Centraliza a lógica de autorização do sistema
 */
@Service
public class PermissaoBusinessService {

    private static final Logger logger = LoggerFactory.getLogger(PermissaoBusinessService.class);

    /**
     * Verifica se um perfil tem uma permissão específica
     */
    public boolean temPermissao(PerfilUsuario perfil, Permissao permissao) {
        if (perfil == null || permissao == null) {
            logger.warn("Tentativa de verificação de permissão com parâmetros nulos: perfil={}, permissao={}", 
                       perfil, permissao);
            return false;
        }

        boolean temPermissao = perfil.temPermissao(permissao);
        
        logger.debug("Verificação de permissão: perfil={}, permissao={}, resultado={}", 
                    perfil.getCodigo(), permissao.getCodigo(), temPermissao);
        
        return temPermissao;
    }

    /**
     * Verifica se um perfil tem pelo menos uma das permissões especificadas
     */
    public boolean temAlgumaPermissao(PerfilUsuario perfil, Permissao... permissoes) {
        if (perfil == null || permissoes == null || permissoes.length == 0) {
            return false;
        }

        for (Permissao permissao : permissoes) {
            if (temPermissao(perfil, permissao)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Verifica se um perfil tem todas as permissões especificadas
     */
    public boolean temTodasPermissoes(PerfilUsuario perfil, Permissao... permissoes) {
        if (perfil == null || permissoes == null || permissoes.length == 0) {
            return false;
        }

        for (Permissao permissao : permissoes) {
            if (!temPermissao(perfil, permissao)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Obtém todas as permissões de um perfil
     */
    public Set<Permissao> obterPermissoes(PerfilUsuario perfil) {
        if (perfil == null) {
            return Set.of();
        }
        
        return perfil.getPermissoes();
    }

    /**
     * Verifica se um usuário pode atualizar status de chamado
     * Regras específicas baseadas na ação solicitada
     */
    public boolean podeAtualizarStatus(PerfilUsuario perfil, String acao) {
        if (perfil == null || acao == null) {
            return false;
        }

        switch (acao.toLowerCase()) {
            case "iniciar":
                return temPermissao(perfil, Permissao.CHAMADO_INICIAR);
            
            case "resolver":
                return temPermissao(perfil, Permissao.CHAMADO_RESOLVER);
            
            case "fechar":
                return temPermissao(perfil, Permissao.CHAMADO_FECHAR);
            
            case "reabrir":
                return temPermissao(perfil, Permissao.CHAMADO_REABRIR);
            
            default:
                logger.warn("Ação desconhecida para verificação de permissão: {}", acao);
                return false;
        }
    }

    /**
     * Verifica se um usuário pode atribuir chamados
     */
    public boolean podeAtribuirChamados(PerfilUsuario perfil) {
        return temPermissao(perfil, Permissao.CHAMADO_ATRIBUIR);
    }

    /**
     * Verifica se um usuário pode visualizar todos os chamados
     */
    public boolean podeVisualizarTodosChamados(PerfilUsuario perfil) {
        return temAlgumaPermissao(perfil, 
                                 Permissao.CHAMADO_VISUALIZAR, 
                                 Permissao.TECNICO_ATENDER_CHAMADOS);
    }

    /**
     * Verifica se um usuário pode criar chamados
     */
    public boolean podeCriarChamados(PerfilUsuario perfil) {
        return temAlgumaPermissao(perfil, 
                                 Permissao.CHAMADO_CRIAR, 
                                 Permissao.USUARIO_CRIAR_CHAMADOS);
    }

    /**
     * Lança exceção se o usuário não tiver a permissão necessária
     */
    public void verificarPermissaoObrigatoria(PerfilUsuario perfil, Permissao permissao) {
        if (!temPermissao(perfil, permissao)) {
            String mensagem = String.format("Usuário com perfil '%s' não tem permissão '%s'", 
                                           perfil != null ? perfil.getDescricao() : "NULO", 
                                           permissao != null ? permissao.getDescricao() : "NULA");
            
            logger.warn("Acesso negado: {}", mensagem);
            throw new SecurityException("Acesso negado: " + mensagem);
        }
    }

    /**
     * Lança exceção se o usuário não puder executar a ação
     */
    public void verificarPermissaoAtualizarStatus(PerfilUsuario perfil, String acao) {
        if (!podeAtualizarStatus(perfil, acao)) {
            String mensagem = String.format("Usuário com perfil '%s' não pode executar a ação '%s'", 
                                           perfil != null ? perfil.getDescricao() : "NULO", 
                                           acao);
            
            logger.warn("Acesso negado: {}", mensagem);
            throw new SecurityException("Acesso negado: " + mensagem);
        }
    }
}