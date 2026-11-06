package com.example.demo.repository;

import com.example.demo.domain.Lista;
import com.example.demo.entity.ListaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter de persistencia para ListaRepository respaldado por Spring Data JPA y PostgreSQL.
 * Convierte ListaEntity <-> Lista aislando el dominio de la infraestructura de persistencia.
 */
@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository, UsuarioJpaRepository usuarioJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public List<Lista> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return jpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity entity = new ListaEntity();
        if (lista.id() != null) {
            entity.setId(lista.id());
        }
        entity.setNombre(lista.nombre());
        if (lista.usuarioId() != null) {
            entity.setUsuario(usuarioJpaRepository.getReferenceById(lista.usuarioId()));
        }
        ListaEntity guardado = jpaRepository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    private Lista aDominio(ListaEntity entity) {
        Long usuarioId = entity.getUsuario() != null ? entity.getUsuario().getId() : null;
        return new Lista(entity.getId(), entity.getNombre(), usuarioId);
    }
}
