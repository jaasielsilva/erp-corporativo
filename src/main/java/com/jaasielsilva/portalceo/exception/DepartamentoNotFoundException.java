package com.jaasielsilva.portalceo.exception;

public class DepartamentoNotFoundException extends RuntimeException {
    public DepartamentoNotFoundException(String message) {
        super(message);
    }
    
    public DepartamentoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DepartamentoNotFoundException(Long id) {
        super("Departamento não encontrado com ID: " + id);
    }
}