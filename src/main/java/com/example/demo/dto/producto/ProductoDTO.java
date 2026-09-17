package com.example.demo.dto.producto;

public record ProductoDTO(
    Long id,
    String titulo,
    String descripcion,
    String marca,
    double precio
) {}
