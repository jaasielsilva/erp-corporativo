package com.jaasielsilva.portalceo.security;

/**
 * Enum que define as permissões disponíveis no sistema
 * Usado para controlar acesso a operações específicas
 */
public enum Permissao {
    
    // Permissões para chamados
    CHAMADO_CRIAR("Criar novos chamados"),
    CHAMADO_VISUALIZAR("Visualizar chamados"),
    CHAMADO_EDITAR("Editar chamados"),
    CHAMADO_ATRIBUIR("Atribuir chamados a técnicos"),
    CHAMADO_INICIAR("Iniciar atendimento de chamados"),
    CHAMADO_RESOLVER("Resolver chamados"),
    CHAMADO_FECHAR("Fechar chamados"),
    CHAMADO_REABRIR("Reabrir chamados"),
    CHAMADO_AVALIAR("Avaliar chamados"),
    
    // Permissões administrativas
    ADMIN_GERENCIAR_USUARIOS("Gerenciar usuários do sistema"),
    ADMIN_GERENCIAR_PERMISSOES("Gerenciar permissões"),
    ADMIN_VISUALIZAR_RELATORIOS("Visualizar relatórios administrativos"),
    
    // Permissões de técnico
    TECNICO_ATENDER_CHAMADOS("Atender chamados como técnico"),
    TECNICO_GERENCIAR_PROPRIOS_CHAMADOS("Gerenciar próprios chamados"),
    
    // Permissões de usuário comum
    USUARIO_CRIAR_CHAMADOS("Criar chamados como usuário"),
    USUARIO_VISUALIZAR_PROPRIOS_CHAMADOS("Visualizar próprios chamados");

    private final String descricao;

    Permissao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCodigo() {
        return this.name();
    }
}