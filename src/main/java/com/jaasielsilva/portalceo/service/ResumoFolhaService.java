package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.ColaboradorResumoFolhaDTO;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.repository.ColaboradorEscalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class ResumoFolhaService {
    @Autowired
    private ColaboradorEscalaRepository colaboradorEscalaRepository;

    @Autowired
    private com.jaasielsilva.portalceo.repository.RegistroPontoRepository registroPontoRepository;

    public ColaboradorResumoFolhaDTO criarResumo(Colaborador c, YearMonth ym) {
        int diasMes = ym.lengthOfMonth();
        int diasUteisMes = contarDiasUteisNoMes(ym);
        
        LocalDate dataInicio = ym.atDay(1);
        LocalDate dataFim = ym.atEndOfMonth();
        com.jaasielsilva.portalceo.repository.RegistroPontoRepository.PontoResumoProjection projection = 
            registroPontoRepository.aggregateResumoByColaboradorAndPeriodo(c.getId(), dataInicio, dataFim);
            
        int diasTrabalhados = projection != null && projection.getDiasComRegistro() != null 
            ? projection.getDiasComRegistro().intValue() 
            : 0;

        String cargoNome = c.getCargo() != null ? c.getCargo().getNome() : "—";
        String departamentoNome = c.getDepartamento() != null ? c.getDepartamento().getNome() : "—";
        String status = c.getStatus() != null ? c.getStatus().name() : "";

        LocalDate dataCheck = YearMonth.from(LocalDate.now()).equals(ym) ? LocalDate.now() : ym.atEndOfMonth();
        boolean temEscala = !colaboradorEscalaRepository.findVigenteByColaboradorAndData(c.getId(), dataCheck).isEmpty();

        return new ColaboradorResumoFolhaDTO(
                c.getId(),
                c.getNome(),
                cargoNome,
                departamentoNome,
                c.getSalario(),
                diasTrabalhados,
                diasMes,
                diasUteisMes,
                status,
                temEscala
        );
    }

    public java.util.List<ColaboradorResumoFolhaDTO> criarResumoBatch(java.util.List<Colaborador> colaboradores, YearMonth ym) {
        int diasMes = ym.lengthOfMonth();
        int diasUteisMes = contarDiasUteisNoMes(ym);
        java.time.LocalDate dataCheck = java.time.YearMonth.from(java.time.LocalDate.now()).equals(ym) ? java.time.LocalDate.now() : ym.atEndOfMonth();
        java.util.List<com.jaasielsilva.portalceo.model.ColaboradorEscala> vigentes = colaboradorEscalaRepository.findVigentesByData(dataCheck);
        java.util.Set<Long> idsComEscala = vigentes.stream().map(e -> e.getColaborador().getId()).collect(java.util.stream.Collectors.toSet());
        
        LocalDate dataInicio = ym.atDay(1);
        LocalDate dataFim = ym.atEndOfMonth();
        
        java.util.List<com.jaasielsilva.portalceo.repository.RegistroPontoRepository.PontoResumoPorColaboradorProjection> projections = 
            registroPontoRepository.aggregateResumoPorPeriodoGroupByColaborador(dataInicio, dataFim);
            
        java.util.Map<Long, Long> diasPorColaborador = projections.stream()
            .collect(java.util.stream.Collectors.toMap(
                com.jaasielsilva.portalceo.repository.RegistroPontoRepository.PontoResumoPorColaboradorProjection::getColaboradorId,
                p -> p.getDiasComRegistro() != null ? p.getDiasComRegistro() : 0L
            ));

        return colaboradores.stream().map(c -> {
            int diasTrabalhados = diasPorColaborador.getOrDefault(c.getId(), 0L).intValue();
            String cargoNome = c.getCargo() != null ? c.getCargo().getNome() : "—";
            String departamentoNome = c.getDepartamento() != null ? c.getDepartamento().getNome() : "—";
            String status = c.getStatus() != null ? c.getStatus().name() : "";
            boolean temEscala = idsComEscala.contains(c.getId());
            return new ColaboradorResumoFolhaDTO(
                    c.getId(),
                    c.getNome(),
                    cargoNome,
                    departamentoNome,
                    c.getSalario(),
                    diasTrabalhados,
                    diasMes,
                    diasUteisMes,
                    status,
                    temEscala
            );
        }).collect(java.util.stream.Collectors.toList());
    }

    private int contarDiasUteisNoMes(YearMonth ym) {
        int count = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate date = ym.atDay(d);
            if (isDiaUtil(date)) count++;
        }
        return count;
    }

    private int contarDiasUteisAte(LocalDate data) {
        YearMonth ym = YearMonth.from(data);
        int count = 0;
        for (int d = 1; d <= data.getDayOfMonth(); d++) {
            LocalDate date = ym.atDay(d);
            if (isDiaUtil(date)) count++;
        }
        return count;
    }

    private boolean isDiaUtil(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}
