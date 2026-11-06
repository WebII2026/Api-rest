package com.example.demo.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un email que ya existe.
 * Se traduce a HTTP 409 Conflict en GlobalExceptionHandler.
 */
public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String message) {
        super(message);
    }
}
