package com.example.demo.domain;

/**
 * Modelo de dominio de un usuario: quien se registra, inicia sesión y es dueño
 * de sus listas de favoritos.
 */
public record Usuario(
        Long id,
        String email,
        String passwordHash,
        Rol rol
) {
}
