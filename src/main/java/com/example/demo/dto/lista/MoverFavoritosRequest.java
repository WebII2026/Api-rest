package com.example.demo.dto.lista;

import jakarta.validation.constraints.NotNull;

public record MoverFavoritosRequest(
        @NotNull(message = "listaDestinoId es obligatorio")
        Long listaDestinoId
) {
}
