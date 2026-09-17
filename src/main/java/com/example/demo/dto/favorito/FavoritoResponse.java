package com.example.demo.dto.favorito;

import java.time.LocalDateTime;

public record FavoritoResponse(
    Long id,
    Long idProductoExterno,
    String notaPersonal,
    LocalDateTime fechaAgregado
) {}
