package com.jaasielsilva.portalceo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Manipulador global de exceções para a aplicação
 * Centraliza o tratamento de erros e melhora a experiência do usuário
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata exceções de validação de Bean Validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationExceptions(MethodArgumentNotValidException ex, 
                                            Model model, 
                                            RedirectAttributes redirectAttributes) {
        
        logger.warn("Erro de validação: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        redirectAttributes.addFlashAttribute("erro", "Erros de validação: " + errorMessage);
        
        // Retornar para a página anterior ou uma página padrão
        return "redirect:/rh/colaboradores/listar";
    }

    /**
     * Trata exceções de violação de constraints
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException ex,
                                                    RedirectAttributes redirectAttributes) {
        
        logger.warn("Violação de constraint: {}", ex.getMessage());
        
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));
        
        redirectAttributes.addFlashAttribute("erro", "Erros de validação: " + errorMessage);
        return "redirect:/rh/colaboradores/listar";
    }

    /**
     * Trata exceções de validação de negócio
     */
    @ExceptionHandler(BusinessValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessValidationException(BusinessValidationException ex,
                                                   RedirectAttributes redirectAttributes) {
        
        logger.warn("Erro de validação de negócio: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/rh/colaboradores/listar";
    }

    /**
     * Trata exceções de entidade não encontrada
     */
    @ExceptionHandler({ColaboradorNotFoundException.class, 
                      CargoNotFoundException.class, 
                      DepartamentoNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFoundException(RuntimeException ex,
                                              RedirectAttributes redirectAttributes) {
        
        logger.error("Entidade não encontrada: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/rh/colaboradores/listar";
    }

    /**
     * Trata exceções de recursos estáticos não encontrados
     * Evita que erros de recursos como @vite/client sejam tratados como erro interno
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFoundException(NoResourceFoundException ex) {
        // Log apenas como debug para não poluir os logs
        logger.debug("Recurso estático não encontrado: {}", ex.getMessage());
        // Não redireciona nem exibe erro ao usuário
    }

    /**
     * Trata exceções gerais não capturadas
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex,
                                        RedirectAttributes redirectAttributes) {
        
        logger.error("Erro interno do sistema: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("erro", 
            "Erro interno do sistema. Tente novamente ou contate o suporte.");
        return "redirect:/rh/colaboradores/listar";
    }

    /**
     * Trata exceções de acesso negado (Spring Security)
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex,
                                         RedirectAttributes redirectAttributes) {
        
        logger.warn("Acesso negado (Spring Security): {}", ex.getMessage());
        return "redirect:/acesso-negado";
    }

    /**
     * Trata exceções de segurança genéricas
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSecurityException(SecurityException ex,
                                         RedirectAttributes redirectAttributes) {
        
        logger.warn("Acesso negado: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("erro", 
            "Você não tem permissão para realizar esta operação.");
        return "redirect:/acesso-negado";
    }

    /**
     * Trata exceções de argumentos ilegais
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex,
                                                RedirectAttributes redirectAttributes) {
        
        logger.warn("Argumento inválido: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("erro", 
            "Dados inválidos fornecidos: " + ex.getMessage());
        return "redirect:/rh/colaboradores/listar";
    }
}