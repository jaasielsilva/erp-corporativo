package com.jaasielsilva.portalceo.dto;

public class EstatisticasUsuariosDTO {
    private long totalUsuarios;
    private long totalAtivos;
    private long totalAdministradores;
    private long totalBloqueados;

    public EstatisticasUsuariosDTO() {}

    public EstatisticasUsuariosDTO(long totalUsuarios, long totalAtivos, long totalAdministradores, long totalBloqueados) {
        this.totalUsuarios = totalUsuarios;
        this.totalAtivos = totalAtivos;
        this.totalAdministradores = totalAdministradores;
        this.totalBloqueados = totalBloqueados;
    }

    // Getters e setters
    public long getTotalUsuarios() { return totalUsuarios; }
    public void setTotalUsuarios(long totalUsuarios) { this.totalUsuarios = totalUsuarios; }

    public long getTotalAtivos() { return totalAtivos; }
    public void setTotalAtivos(long totalAtivos) { this.totalAtivos = totalAtivos; }

    public long getTotalAdministradores() { return totalAdministradores; }
    public void setTotalAdministradores(long totalAdministradores) { this.totalAdministradores = totalAdministradores; }

    public long getTotalBloqueados() { return totalBloqueados; }
    public void setTotalBloqueados(long totalBloqueados) { this.totalBloqueados = totalBloqueados; }
}
