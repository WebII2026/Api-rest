package com.example.demo.repository;

import com.example.demo.domain.Favorito;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para favoritos (puerto). El service depende
 * de esta interfaz, no de la implementación concreta — en el TP1 fue en
 * memoria, en el TP2 pasa a ser un adapter respaldado por JPA/PostgreSQL.
 */
public interface FavoritoRepository {
    List<Favorito> findAll();
    Optional<Favorito> findById(Long id);
    Favorito save(Favorito favorito);
    void deleteById(Long id);
    boolean existsById(Long id);
    List<Favorito> findByListaId(Long listaId);
}
