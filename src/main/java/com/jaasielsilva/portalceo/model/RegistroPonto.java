package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "registro_ponto")
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(nullable = false)
    private LocalDate data;

    @Column
    private LocalTime entrada1;

    @Column
    private LocalTime saida1;

    @Column
    private LocalTime entrada2;

    @Column
    private LocalTime saida2;

    @Column
    private LocalTime entrada3;

    @Column
    private LocalTime saida3;

    @Column
    private LocalTime entrada4;

    @Column
    private LocalTime saida4;

    @Column
    private Integer totalMinutosTrabalhados;

    @Column
    private Integer totalMinutosIntervalo;

    @Column
    private Integer minutosAtraso = 0;

    @Column
    private Integer minutosHoraExtra = 0;

    @Column
    private Integer minutosDebitoJornada = 0;

    @Column
    private Integer minutosJornadaPrevista;

    @Column
    private LocalTime horarioPrevistoEntrada1;

    @Column
    private Boolean toleranciaAtrasoAtiva;

    @Column
    private Integer minutosToleranciaAtraso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPonto status = StatusPonto.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column
    private TipoRegistro tipoRegistro = TipoRegistro.AUTOMATICO;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false)
    private Boolean falta = false;

    @Column(nullable = false)
    private Boolean abono = false;

    @OneToMany(mappedBy = "registroPonto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CorrecaoPonto> correcoes;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum StatusPonto {
        NORMAL,
        ATRASO,
        FALTA,
        HORA_EXTRA,
        ABONO,
        FERIADO,
        FERIAS,
        ATESTADO
    }

    public enum TipoRegistro {
        AUTOMATICO,
        MANUAL,
        CORRECAO
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusPonto.NORMAL;
        }
        if (tipoRegistro == null) {
            tipoRegistro = TipoRegistro.AUTOMATICO;
        }
        if (falta == null) {
            falta = false;
        }
        if (abono == null) {
            abono = false;
        }
        calcularHoras();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularHoras();
    }

    private void calcularHoras() {
        if (falta) {
            totalMinutosTrabalhados = 0;
            totalMinutosIntervalo = 0;
            minutosAtraso = 0;
            minutosHoraExtra = 0;
            minutosDebitoJornada = 0;
            return;
        }

        int totalMinutos = 0;
        int totalIntervalo = 0;

        // Calcular período 1 (manhã)
        if (entrada1 != null && saida1 != null) {
            totalMinutos += Duration.between(entrada1, saida1).toMinutes();
        }

        // Calcular período 2 (tarde)
        if (entrada2 != null && saida2 != null) {
            totalMinutos += Duration.between(entrada2, saida2).toMinutes();
        }

        // Calcular período 3 (extra)
        if (entrada3 != null && saida3 != null) {
            totalMinutos += Duration.between(entrada3, saida3).toMinutes();
        }

        // Calcular período 4 (extra)
        if (entrada4 != null && saida4 != null) {
            totalMinutos += Duration.between(entrada4, saida4).toMinutes();
        }

        // Calcular intervalos
        if (saida1 != null && entrada2 != null) {
            totalIntervalo += Duration.between(saida1, entrada2).toMinutes();
        }
        if (saida2 != null && entrada3 != null) {
            totalIntervalo += Duration.between(saida2, entrada3).toMinutes();
        }
        if (saida3 != null && entrada4 != null) {
            totalIntervalo += Duration.between(saida3, entrada4).toMinutes();
        }

        totalMinutosTrabalhados = totalMinutos;
        totalMinutosIntervalo = totalIntervalo;

        int jornadaNormal = minutosJornadaPrevista != null ? Math.max(0, minutosJornadaPrevista) : 480;

        if (totalMinutos < jornadaNormal) {
            minutosDebitoJornada = jornadaNormal - totalMinutos;
            minutosHoraExtra = 0;
        } else {
            minutosDebitoJornada = 0;
            minutosHoraExtra = totalMinutos - jornadaNormal;
        }

        int atrasoEntrada = 0;
        if (entrada1 != null && horarioPrevistoEntrada1 != null) {
            long diff = Duration.between(horarioPrevistoEntrada1, entrada1).toMinutes();
            atrasoEntrada = (int) Math.max(0, diff);
            boolean aplicaTol = Boolean.TRUE.equals(toleranciaAtrasoAtiva);
            int tol = minutosToleranciaAtraso != null ? Math.max(0, minutosToleranciaAtraso) : 0;
            if (aplicaTol && tol > 0) {
                atrasoEntrada = Math.max(0, atrasoEntrada - tol);
            }
        }
        minutosAtraso = atrasoEntrada;

        // Definir status baseado nos cálculos
        if (minutosAtraso > 0 && !abono) {
            status = StatusPonto.ATRASO;
        } else if (minutosHoraExtra > 0) {
            status = StatusPonto.HORA_EXTRA;
        } else if (abono) {
            status = StatusPonto.ABONO;
        } else {
            status = StatusPonto.NORMAL;
        }
    }

    public String getStatusDescricao() {
        switch (status) {
            case NORMAL:
                return "Normal";
            case ATRASO:
                return "Atraso";
            case FALTA:
                return "Falta";
            case HORA_EXTRA:
                return "Hora Extra";
            case ABONO:
                return "Abono";
            case FERIADO:
                return "Feriado";
            case FERIAS:
                return "Férias";
            case ATESTADO:
                return "Atestado";
            default:
                return status.toString();
        }
    }

    public String getTipoRegistroDescricao() {
        switch (tipoRegistro) {
            case AUTOMATICO:
                return "Automático";
            case MANUAL:
                return "Manual";
            case CORRECAO:
                return "Correção";
            default:
                return tipoRegistro.toString();
        }
    }

    public String getHorasTrabalhadasFormatadas() {
        if (totalMinutosTrabalhados == null) return "00:00";
        int horas = totalMinutosTrabalhados / 60;
        int minutos = totalMinutosTrabalhados % 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    public String getHorasExtraFormatadas() {
        if (minutosHoraExtra == null || minutosHoraExtra == 0) return "00:00";
        int horas = minutosHoraExtra / 60;
        int minutos = minutosHoraExtra % 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    public String getAtrasoFormatado() {
        if (minutosAtraso == null || minutosAtraso == 0) return "00:00";
        int horas = minutosAtraso / 60;
        int minutos = minutosAtraso % 60;
        return String.format("%02d:%02d", horas, minutos);
    }
}
