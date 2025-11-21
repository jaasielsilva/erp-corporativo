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

    public ColaboradorResumoFolhaDTO criarResumo(Colaborador c, YearMonth ym) {
        int diasMes = ym.lengthOfMonth();
        int diasTrabalhados;
        int diasUteisMes = contarDiasUteisNoMes(ym);
        if (c.getStatus() != null && c.getStatus() == Colaborador.StatusColaborador.INATIVO) {
            diasTrabalhados = 0;
        } else {
            if (YearMonth.from(LocalDate.now()).equals(ym)) {
                diasTrabalhados = contarDiasUteisAte(LocalDate.now().withDayOfMonth(LocalDate.now().getDayOfMonth()));
            } else {
                diasTrabalhados = contarDiasUteisNoMes(ym);
            }
        }

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