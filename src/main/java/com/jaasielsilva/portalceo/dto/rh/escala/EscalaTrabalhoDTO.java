package com.jaasielsilva.portalceo.dto.rh.escala;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EscalaTrabalhoDTO {
    public String nome;
    public String descricao;
    public String tipo;
    public LocalTime horarioEntrada1;
    public LocalTime horarioSaida1;
    public LocalTime horarioEntrada2;
    public LocalTime horarioSaida2;
    public List<String> diasTrabalhados; // ex: ["SEG", "TER", "QUA"]
    public Integer intervaloMinimo;
    public Boolean toleranciaAtraso;
    public Integer minutosTolerancia;
    public LocalDate dataVigenciaInicio;
    public LocalDate dataVigenciaFim;
    public List<String> departamentos; // ["TI", "Vendas"]
    public List<String> turnos; // ["manha", "tarde", "noite", "comercial"]
}
