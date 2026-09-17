package com.example.demo.dto.favorito;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FavoritoRequest(
    @NotNull(message = "El ID del producto externo es obligatorio")
    Long idProductoExterno,
    
    @NotBlank(message = "La nota personal no puede estar vacía")
    String notaPersonal
) {}
