package com.example.demo.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder o modificar un recurso que no le pertenece.
 * Se traduce a HTTP 403 Forbidden en GlobalExceptionHandler.
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
