package com.jaasielsilva.portalceo.exception;

public class ColaboradorNotFoundException extends RuntimeException {
    public ColaboradorNotFoundException(String message) {
        super(message);
    }
    
    public ColaboradorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ColaboradorNotFoundException(Long id) {
        super("Colaborador não encontrado com ID: " + id);
    }
}