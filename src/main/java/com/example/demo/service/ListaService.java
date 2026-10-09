package com.example.demo.service;

import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.dto.lista.MoverFavoritosRequest;

import java.util.List;

/**
 * Contrato de negocio para operaciones sobre listas de favoritos.
 */
public interface ListaService {
    List<ListaResponse> listar();
    ListaResponse obtener(Long id);
    ListaResponse crear(ListaRequest request);
    void eliminar(Long id);
    List<FavoritoResponse> obtenerFavoritos(Long id);
    void moverFavoritos(Long origenId, MoverFavoritosRequest request);
}
