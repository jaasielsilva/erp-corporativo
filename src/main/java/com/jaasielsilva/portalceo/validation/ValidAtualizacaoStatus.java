package com.jaasielsilva.portalceo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotação para validação customizada de atualização de status
 * Valida regras de negócio específicas para cada tipo de ação
 */
@Documented
@Constraint(validatedBy = AtualizacaoStatusValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAtualizacaoStatus {
    
    String message() default "Dados inválidos para atualização de status";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}