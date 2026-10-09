package com.example.demo.service;

import com.example.demo.dto.favorito.FavoritoRequest;
import com.example.demo.dto.favorito.FavoritoResponse;

import java.util.List;

/**
 * Contrato de negocio del CRUD de favoritos, siempre en términos de DTOs —
 * nunca expone la entidad Favorito hacia afuera.
 */
public interface FavoritoService {
    List<FavoritoResponse> listar();
    FavoritoResponse obtener(Long id);
    FavoritoResponse crear(FavoritoRequest request);
    FavoritoResponse actualizar(Long id, FavoritoRequest request);
    void eliminar(Long id);
}
