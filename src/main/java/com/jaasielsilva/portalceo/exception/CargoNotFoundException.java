package com.jaasielsilva.portalceo.exception;

public class CargoNotFoundException extends RuntimeException {
    public CargoNotFoundException(String message) {
        super(message);
    }
    
    public CargoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CargoNotFoundException(Long id) {
        super("Cargo não encontrado com ID: " + id);
    }
}