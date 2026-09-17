package com.example.demo.repository;

import com.example.demo.model.Favorito;
import java.util.List;
import java.util.Optional;

public interface FavoritoRepository {
    Favorito guardar(Favorito favorito);
    List<Favorito> listarTodos();
    Optional<Favorito> buscarPorId(Long id);
    Favorito actualizar(Favorito favorito);
    boolean eliminar(Long id);
}
