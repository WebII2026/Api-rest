package com.example.demo.exception;

/**
 * Excepción lanzada cuando el email o la contraseña son incorrectos en el login.
 * Se traduce a HTTP 401 Unauthorized en GlobalExceptionHandler.
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
