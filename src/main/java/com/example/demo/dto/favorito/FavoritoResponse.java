package com.example.demo.dto.favorito;

import java.time.LocalDateTime;

/**
 * DTO de salida: lo que la API devuelve.
 */
public record FavoritoResponse(
        Long id,
        Long productoId,
        String nota,
        LocalDateTime fechaAgregado,
        Long listaId
) {
}
