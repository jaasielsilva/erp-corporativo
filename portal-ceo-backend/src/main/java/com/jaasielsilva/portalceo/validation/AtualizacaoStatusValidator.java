package com.jaasielsilva.portalceo.validation;

import com.jaasielsilva.portalceo.dto.AtualizarStatusRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador customizado para regras de negócio de atualização de status
 * Implementa validações condicionais baseadas na ação solicitada
 */
public class AtualizacaoStatusValidator implements ConstraintValidator<ValidAtualizacaoStatus, AtualizarStatusRequest> {

    @Override
    public void initialize(ValidAtualizacaoStatus constraintAnnotation) {
        // Inicialização se necessária
    }

    @Override
    public boolean isValid(AtualizarStatusRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getAcao() == null) {
            return true; // Deixa outras validações lidarem com nulos
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        String acao = request.getAcao().toLowerCase();

        switch (acao) {
            case "reabrir":
                // Para reabertura, motivo é obrigatório
                if (request.getMotivoReabertura() == null || request.getMotivoReabertura().trim().isEmpty()) {
                    context.buildConstraintViolationWithTemplate(
                        "Motivo da reabertura é obrigatório quando a ação for 'reabrir'"
                    ).addPropertyNode("motivoReabertura").addConstraintViolation();
                    isValid = false;
                }
                break;

            case "iniciar":
                // Para iniciar, técnico responsável é obrigatório
                if (request.getTecnicoResponsavel() == null || request.getTecnicoResponsavel().trim().isEmpty()) {
                    context.buildConstraintViolationWithTemplate(
                        "Técnico responsável é obrigatório quando a ação for 'iniciar'"
                    ).addPropertyNode("tecnicoResponsavel").addConstraintViolation();
                    isValid = false;
                }
                break;

            case "resolver":
                // Para resolver, observações são recomendadas mas não obrigatórias
                // Técnico responsável é obrigatório
                if (request.getTecnicoResponsavel() == null || request.getTecnicoResponsavel().trim().isEmpty()) {
                    context.buildConstraintViolationWithTemplate(
                        "Técnico responsável é obrigatório quando a ação for 'resolver'"
                    ).addPropertyNode("tecnicoResponsavel").addConstraintViolation();
                    isValid = false;
                }
                break;

            case "fechar":
                // Para fechar, nenhuma validação adicional específica
                break;

            default:
                // Ação inválida (já validada por @Pattern, mas por segurança)
                context.buildConstraintViolationWithTemplate(
                    "Ação inválida: " + acao
                ).addPropertyNode("acao").addConstraintViolation();
                isValid = false;
        }

        return isValid;
    }
}