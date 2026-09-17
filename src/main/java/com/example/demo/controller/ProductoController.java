package com.example.demo.controller;

import com.example.demo.dto.producto.ProductoDTO;
import com.example.demo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Catálogo de Productos", description = "Endpoints de solo lectura para el catálogo externo")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener lista de productos")
    public List<ProductoDTO> listar(
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        return service.obtenerProductos(limit, skip);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por su ID")
    public ProductoDTO obtener(@PathVariable Long id) {
        return service.obtenerProductoPorId(id);
    }
}
