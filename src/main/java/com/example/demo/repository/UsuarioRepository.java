package com.example.demo.repository;

import com.example.demo.domain.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para usuarios (puerto).
 */
public interface UsuarioRepository {
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    Usuario save(Usuario usuario);
    void deleteById(Long id);
}
