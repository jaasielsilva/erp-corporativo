package com.jaasielsilva.portalceo.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum que define os perfis de usuário e suas permissões
 * Cada perfil tem um conjunto específico de permissões
 */
public enum PerfilUsuario {
    
    ADMINISTRADOR("Administrador do Sistema", 
        Permissao.CHAMADO_CRIAR,
        Permissao.CHAMADO_VISUALIZAR,
        Permissao.CHAMADO_EDITAR,
        Permissao.CHAMADO_ATRIBUIR,
        Permissao.CHAMADO_INICIAR,
        Permissao.CHAMADO_RESOLVER,
        Permissao.CHAMADO_FECHAR,
        Permissao.CHAMADO_REABRIR,
        Permissao.CHAMADO_AVALIAR,
        Permissao.ADMIN_GERENCIAR_USUARIOS,
        Permissao.ADMIN_GERENCIAR_PERMISSOES,
        Permissao.ADMIN_VISUALIZAR_RELATORIOS,
        Permissao.TECNICO_ATENDER_CHAMADOS,
        Permissao.TECNICO_GERENCIAR_PROPRIOS_CHAMADOS,
        Permissao.USUARIO_CRIAR_CHAMADOS,
        Permissao.USUARIO_VISUALIZAR_PROPRIOS_CHAMADOS
    ),
    
    TECNICO("Técnico de Suporte",
        Permissao.CHAMADO_VISUALIZAR,
        Permissao.CHAMADO_INICIAR,
        Permissao.CHAMADO_RESOLVER,
        Permissao.CHAMADO_FECHAR,
        Permissao.TECNICO_ATENDER_CHAMADOS,
        Permissao.TECNICO_GERENCIAR_PROPRIOS_CHAMADOS,
        Permissao.USUARIO_CRIAR_CHAMADOS,
        Permissao.USUARIO_VISUALIZAR_PROPRIOS_CHAMADOS
    ),
    
    SUPERVISOR("Supervisor de Suporte",
        Permissao.CHAMADO_CRIAR,
        Permissao.CHAMADO_VISUALIZAR,
        Permissao.CHAMADO_EDITAR,
        Permissao.CHAMADO_ATRIBUIR,
        Permissao.CHAMADO_INICIAR,
        Permissao.CHAMADO_RESOLVER,
        Permissao.CHAMADO_FECHAR,
        Permissao.CHAMADO_REABRIR,
        Permissao.ADMIN_VISUALIZAR_RELATORIOS,
        Permissao.TECNICO_ATENDER_CHAMADOS,
        Permissao.TECNICO_GERENCIAR_PROPRIOS_CHAMADOS,
        Permissao.USUARIO_CRIAR_CHAMADOS,
        Permissao.USUARIO_VISUALIZAR_PROPRIOS_CHAMADOS
    ),
    
    USUARIO("Usuário Comum",
        Permissao.USUARIO_CRIAR_CHAMADOS,
        Permissao.USUARIO_VISUALIZAR_PROPRIOS_CHAMADOS,
        Permissao.CHAMADO_AVALIAR
    );

    private final String descricao;
    private final Set<Permissao> permissoes;

    PerfilUsuario(String descricao, Permissao... permissoes) {
        this.descricao = descricao;
        this.permissoes = Arrays.stream(permissoes).collect(Collectors.toSet());
    }

    public String getDescricao() {
        return descricao;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public boolean temPermissao(Permissao permissao) {
        return permissoes.contains(permissao);
    }

    public List<String> getPermissoesComoString() {
        return permissoes.stream()
                .map(Permissao::getCodigo)
                .sorted()
                .collect(Collectors.toList());
    }

    public String getCodigo() {
        return this.name();
    }
}