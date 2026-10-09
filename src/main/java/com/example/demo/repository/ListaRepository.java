package com.example.demo.repository;

import com.example.demo.domain.Lista;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para listas de favoritos (puerto).
 */
public interface ListaRepository {
    List<Lista> findAll();
    Optional<Lista> findById(Long id);
    Lista save(Lista lista);
    void deleteById(Long id);
    boolean existsById(Long id);
}
