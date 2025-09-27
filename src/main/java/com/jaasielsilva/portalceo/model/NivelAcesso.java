package com.jaasielsilva.portalceo.model;

public enum NivelAcesso {
    MASTER("Master", 1, "Acesso total ao sistema - NUNCA pode ser editado ou excluído"),
    ADMIN("Administrador", 2, "Acesso administrativo completo"),
    GERENTE("Gerente", 3, "Acesso gerencial com permissões de supervisão"),
    COORDENADOR("Coordenador", 4, "Acesso de coordenação departamental"),
    SUPERVISOR("Supervisor", 5, "Acesso de supervisão de equipe"),
    ANALISTA("Analista", 6, "Acesso analítico e operacional"),
    OPERACIONAL("Operacional", 7, "Acesso operacional básico"),
    USER("Usuário", 8, "Acesso básico do sistema"),
    ESTAGIARIO("Estagiário", 9, "Acesso limitado para estagiários"),
    TERCEIRIZADO("Terceirizado", 10, "Acesso restrito para terceirizados"),
    CONSULTOR("Consultor", 11, "Acesso específico para consultores"),
    VISITANTE("Visitante", 12, "Acesso muito limitado para visitantes");

    private final String descricao;
    private final int nivel;
    private final String observacao;

    NivelAcesso(String descricao, int nivel, String observacao) {
        this.descricao = descricao;
        this.nivel = nivel;
        this.observacao = observacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getNivel() {
        return nivel;
    }

    public String getObservacao() {
        return observacao;
    }

    /**
     * Verifica se este nível tem autoridade sobre outro nível
     * @param outroNivel o nível a ser comparado
     * @return true se este nível é superior (número menor = maior autoridade)
     */
    public boolean temAutoridadeSobre(NivelAcesso outroNivel) {
        return this.nivel < outroNivel.nivel;
    }

    /**
     * Verifica se este nível é igual ou superior a outro
     * @param outroNivel o nível a ser comparado
     * @return true se este nível é igual ou superior
     */
    public boolean ehIgualOuSuperiorA(NivelAcesso outroNivel) {
        return this.nivel <= outroNivel.nivel;
    }

    /**
     * Retorna true se o nível é considerado administrativo
     */
    public boolean ehAdministrativo() {
        return this == MASTER || this == ADMIN;
    }

    /**
     * Retorna true se o nível é considerado gerencial
     */
    public boolean ehGerencial() {
        return this == MASTER || this == ADMIN || this == GERENTE || this == COORDENADOR || this == SUPERVISOR;
    }

    /**
     * Retorna true se o nível pode gerenciar usuários
     */
    public boolean podeGerenciarUsuarios() {
        return this == MASTER || this == ADMIN || this == GERENTE;
    }

    /**
     * Retorna true se o nível pode acessar relatórios financeiros
     */
    public boolean podeAcessarFinanceiro() {
        return this == MASTER || this == ADMIN || this == GERENTE || this == COORDENADOR;
    }

    /**
     * Retorna true se o nível pode gerenciar RH
     */
    public boolean podeGerenciarRH() {
        return this == MASTER || this == ADMIN || this == GERENTE;
    }

    /**
     * Retorna true se o nível é protegido (não pode ser editado/excluído)
     */
    public boolean ehProtegido() {
        return this == MASTER;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
