package com.example.demo.domain;

import java.time.LocalDateTime;

/**
 * Modelo de dominio de un favorito: una referencia a un producto externo
 * (por id), una lista a la que pertenece (por id) más una nota personal.
 * No guarda nombre/precio/imagen del producto — esos datos ya viven en
 * ProductoService y duplicarlos acá los dejaría desactualizados si el
 * producto cambia.
 */
public record Favorito(
        Long id,
        Long productoId,
        String nota,
        LocalDateTime fechaAgregado,
        Long listaId
) {
}
