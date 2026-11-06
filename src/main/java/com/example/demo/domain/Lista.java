package com.example.demo.domain;

/**
 * Modelo de dominio de una lista de favoritos.
 * En el TP3 se incorpora usuarioId para vincular la lista con su dueño.
 */
public record Lista(
        Long id,
        String nombre,
        Long usuarioId
) {
    public Lista(Long id, String nombre) {
        this(id, nombre, null);
    }
}
