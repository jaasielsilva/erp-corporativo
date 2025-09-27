package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.exception.BusinessValidationException;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ColaboradorValidationService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    /**
     * Valida regras de negócio para colaborador
     */
    public void validarColaborador(Colaborador colaborador) {
        validarCpfUnico(colaborador);
        validarEmailUnico(colaborador);
        validarDataAdmissao(colaborador);
        validarSupervisor(colaborador);
    }

    /**
     * Valida se CPF é único no sistema
     */
    private void validarCpfUnico(Colaborador colaborador) {
        if (colaborador.getCpf() != null) {
            Optional<Colaborador> existente = colaboradorRepository.findByCpf(colaborador.getCpf());
            if (existente.isPresent() && !existente.get().getId().equals(colaborador.getId())) {
                throw new BusinessValidationException("CPF já cadastrado no sistema");
            }
        }
    }

    /**
     * Valida se email é único no sistema
     */
    private void validarEmailUnico(Colaborador colaborador) {
        if (colaborador.getEmail() != null) {
            Optional<Colaborador> existente = colaboradorRepository.findByEmail(colaborador.getEmail());
            if (existente.isPresent() && !existente.get().getId().equals(colaborador.getId())) {
                throw new BusinessValidationException("Email já cadastrado no sistema");
            }
        }
    }

    /**
     * Valida se data de admissão não é futura
     */
    private void validarDataAdmissao(Colaborador colaborador) {
        if (colaborador.getDataAdmissao() != null && colaborador.getDataAdmissao().isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Data de admissão não pode ser futura");
        }
    }

    /**
     * Valida se supervisor não é o próprio colaborador
     */
    private void validarSupervisor(Colaborador colaborador) {
        if (colaborador.getSupervisor() != null &&
                colaborador.getSupervisor().getId() != null &&
                colaborador.getId() != null &&
                colaborador.getId().equals(colaborador.getSupervisor().getId())) {
            throw new BusinessValidationException("Colaborador não pode ser supervisor de si mesmo");
        }
    }

}