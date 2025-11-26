package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "escala_trabalho", indexes = {
    @Index(name = "idx_escala_vigencia", columnList = "ativo, dataVigenciaInicio, dataVigenciaFim")
})
public class EscalaTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEscala tipo;

    @Column(nullable = false)
    private LocalTime horarioEntrada1;

    @Column(nullable = false)
    private LocalTime horarioSaida1;

    @Column
    private LocalTime horarioEntrada2;

    @Column
    private LocalTime horarioSaida2;

    @Column(nullable = false)
    private Integer cargaHorariaDiaria; // em minutos

    @Column(nullable = false)
    private Integer cargaHorariaSemanal; // em minutos

    @Column(nullable = false)
    private Integer intervaloMinimo; // em minutos

    @Column(nullable = false)
    private Boolean trabalhaSegunda = true;

    @Column(nullable = false)
    private Boolean trabalhaTerca = true;

    @Column(nullable = false)
    private Boolean trabalhaQuarta = true;

    @Column(nullable = false)
    private Boolean trabalhaQuinta = true;

    @Column(nullable = false)
    private Boolean trabalhaSexta = true;

    @Column(nullable = false)
    private Boolean trabalhaSabado = false;

    @Column(nullable = false)
    private Boolean trabalhaDomingo = false;

    @Column(nullable = false)
    private Boolean toleranciaAtraso = true;

    @Column
    private Integer minutosTolerancia = 10;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column
    private LocalDate dataVigenciaInicio;

    @Column
    private LocalDate dataVigenciaFim;

    @OneToMany(mappedBy = "escalaTrabalho", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColaboradorEscala> colaboradores;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum TipoEscala {
        NORMAL, // 8h com intervalo
        CORRIDA, // 6h sem intervalo
        TURNO_12X36, // 12h trabalhadas, 36h descanso
        TURNO_6X1, // 6 dias trabalhados, 1 dia descanso
        FLEXIVEL, // horário flexível
        PERSONALIZADA // escala personalizada
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
        if (toleranciaAtraso == null) {
            toleranciaAtraso = true;
        }
        if (minutosTolerancia == null) {
            minutosTolerancia = 10;
        }
        if (dataVigenciaInicio == null) {
            dataVigenciaInicio = LocalDate.now();
        }
        calcularCargaHoraria();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularCargaHoraria();
    }

    private void calcularCargaHoraria() {
        if (horarioEntrada1 != null && horarioSaida1 != null) {
            int minutosPeriodo1 = (int) java.time.Duration.between(horarioEntrada1, horarioSaida1).toMinutes();
            int minutosPeriodo2 = 0;
            
            if (horarioEntrada2 != null && horarioSaida2 != null) {
                minutosPeriodo2 = (int) java.time.Duration.between(horarioEntrada2, horarioSaida2).toMinutes();
            }
            
            cargaHorariaDiaria = minutosPeriodo1 + minutosPeriodo2;
            
            // Calcular carga semanal baseada nos dias trabalhados
            int diasTrabalhados = 0;
            if (trabalhaSegunda) diasTrabalhados++;
            if (trabalhaTerca) diasTrabalhados++;
            if (trabalhaQuarta) diasTrabalhados++;
            if (trabalhaQuinta) diasTrabalhados++;
            if (trabalhaSexta) diasTrabalhados++;
            if (trabalhaSabado) diasTrabalhados++;
            if (trabalhaDomingo) diasTrabalhados++;
            
            cargaHorariaSemanal = cargaHorariaDiaria * diasTrabalhados;
        }
    }

    public String getTipoDescricao() {
        switch (tipo) {
            case NORMAL:
                return "Normal (8h com intervalo)";
            case CORRIDA:
                return "Corrida (6h sem intervalo)";
            case TURNO_12X36:
                return "Turno 12x36";
            case TURNO_6X1:
                return "Turno 6x1";
            case FLEXIVEL:
                return "Flexível";
            case PERSONALIZADA:
                return "Personalizada";
            default:
                return tipo.toString();
        }
    }

    public String getCargaHorariaDiariaFormatada() {
        if (cargaHorariaDiaria == null) return "00:00";
        int horas = cargaHorariaDiaria / 60;
        int minutos = cargaHorariaDiaria % 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    public String getCargaHorariaSemanalFormatada() {
        if (cargaHorariaSemanal == null) return "00:00";
        int horas = cargaHorariaSemanal / 60;
        int minutos = cargaHorariaSemanal % 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    public String getDiasTrabalhados() {
        StringBuilder dias = new StringBuilder();
        if (trabalhaSegunda) dias.append("Seg ");
        if (trabalhaTerca) dias.append("Ter ");
        if (trabalhaQuarta) dias.append("Qua ");
        if (trabalhaQuinta) dias.append("Qui ");
        if (trabalhaSexta) dias.append("Sex ");
        if (trabalhaSabado) dias.append("Sáb ");
        if (trabalhaDomingo) dias.append("Dom ");
        return dias.toString().trim();
    }

    public boolean trabalhaEm(java.time.DayOfWeek diaSemana) {
        switch (diaSemana) {
            case MONDAY: return trabalhaSegunda;
            case TUESDAY: return trabalhaTerca;
            case WEDNESDAY: return trabalhaQuarta;
            case THURSDAY: return trabalhaQuinta;
            case FRIDAY: return trabalhaSexta;
            case SATURDAY: return trabalhaSabado;
            case SUNDAY: return trabalhaDomingo;
            default: return false;
        }
    }

    public boolean isVigente() {
        LocalDate hoje = LocalDate.now();
        boolean inicioOk = dataVigenciaInicio == null || !hoje.isBefore(dataVigenciaInicio);
        boolean fimOk = dataVigenciaFim == null || !hoje.isAfter(dataVigenciaFim);
        return ativo && inicioOk && fimOk;
    }
}
