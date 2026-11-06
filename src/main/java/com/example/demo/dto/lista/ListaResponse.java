package com.example.demo.dto.lista;

public record ListaResponse(
        Long id,
        String nombre,
        Long usuarioId
) {
    public ListaResponse(Long id, String nombre) {
        this(id, nombre, null);
    }
}
