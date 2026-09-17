package com.example.demo.service;

import com.example.demo.client.dummyjson.DummyJsonClient;
import com.example.demo.client.dummyjson.DummyJsonProducto;
import com.example.demo.dto.producto.ProductoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    private final DummyJsonClient client;

    public ProductoService(DummyJsonClient client) {
        this.client = client;
    }

    public List<ProductoDTO> obtenerProductos(int limit, int skip) {
        return client.getProducts(limit, skip).products().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProductoDTO obtenerProductoPorId(Long id) {
        DummyJsonProducto p = client.getProductById(id);
        if (p == null) return null;
        return mapToDTO(p);
    }

    private ProductoDTO mapToDTO(DummyJsonProducto p) {
        return new ProductoDTO(
            p.id(),
            p.title(),
            p.description(),
            p.brand(),
            p.price()
        );
    }
}
