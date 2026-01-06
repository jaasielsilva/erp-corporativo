package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mapa_permissoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapaPermissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String modulo; // ex: Dashboard, RH, Financeiro

    @Column(nullable = false)
    private String recurso; // ex: Gráfico de Adesão, Botão Salvar

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecurso tipo; // MENU, TELA, BOTAO, GRAFICO, RELATORIO, ACAO

    @Column(nullable = false, unique = true)
    private String permissao; // ex: ROLE_DASHBOARD_ADESAO_VISUALIZAR

    @Column(nullable = false)
    private String descricao; // O que o recurso permite fazer

    @Column(columnDefinition = "TEXT")
    private String perfis; // Lista de perfis que possuem esta permissão

    public enum TipoRecurso {
        MENU,
        TELA,
        BOTAO,
        GRAFICO,
        RELATORIO,
        ACAO
    }
}
