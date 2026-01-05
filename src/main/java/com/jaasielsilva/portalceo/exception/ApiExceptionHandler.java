package com.jaasielsilva.portalceo.exception;

import com.jaasielsilva.portalceo.controller.ChamadoRestController;
import com.jaasielsilva.portalceo.controller.SuporteApiController;
import com.jaasielsilva.portalceo.dto.ChamadoStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler específico para APIs REST do módulo de Suporte/Chamados
 * Padroniza respostas de erro em formato JSON para endpoints de API
 */
@RestControllerAdvice(assignableTypes = {ChamadoRestController.class, SuporteApiController.class})
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Trata erros de validação de campos (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ChamadoStatusResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = "Dados inválidos: " + 
            errors.entrySet().stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining("; "));

        logger.warn("Erro de validação na API: {}", errorMessage);
        
        ChamadoStatusResponse response = ChamadoStatusResponse.erro(errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata erros de validação de constraints (@Pattern, @Size, etc.)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ChamadoStatusResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        String errorMessage = "Dados inválidos: " + 
            ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        logger.warn("Erro de constraint na API: {}", errorMessage);
        
        ChamadoStatusResponse response = ChamadoStatusResponse.erro(errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata exceções de negócio customizadas
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ChamadoStatusResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        logger.warn("Erro de argumento inválido na API: {}", ex.getMessage());
        
        ChamadoStatusResponse response = ChamadoStatusResponse.erro(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata exceções de estado inválido
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ChamadoStatusResponse> handleIllegalStateException(
            IllegalStateException ex) {
        
        logger.warn("Erro de estado inválido na API: {}", ex.getMessage());
        
        ChamadoStatusResponse response = ChamadoStatusResponse.erro(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata exceções de entidade não encontrada
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ChamadoStatusResponse> handleRuntimeException(RuntimeException ex) {
        
        // Verifica se é uma exceção conhecida de "não encontrado"
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("não encontrado")) {
            logger.warn("Entidade não encontrada na API: {}", ex.getMessage());
            ChamadoStatusResponse response = ChamadoStatusResponse.erro(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        logger.error("Erro de runtime na API: {}", ex.getMessage(), ex);
        ChamadoStatusResponse response = ChamadoStatusResponse.erro(
            "Erro interno do servidor. Tente novamente mais tarde."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Trata exceções gerais não capturadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChamadoStatusResponse> handleGenericException(Exception ex) {
        
        logger.error("Erro interno do servidor na API: {}", ex.getMessage(), ex);
        
        ChamadoStatusResponse response = ChamadoStatusResponse.erro(
            "Erro interno do servidor. Tente novamente mais tarde."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}