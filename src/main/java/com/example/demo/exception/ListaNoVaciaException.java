package com.example.demo.exception;

/**
 * Excepción lanzada cuando se intenta eliminar una lista que todavía contiene favoritos.
 * Se traduce a HTTP 409 Conflict en GlobalExceptionHandler.
 */
public class ListaNoVaciaException extends RuntimeException {
    public ListaNoVaciaException(String message) {
        super(message);
    }
}
