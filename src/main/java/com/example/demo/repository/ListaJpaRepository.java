package com.example.demo.repository;

import com.example.demo.entity.ListaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad ListaEntity.
 */
@Repository
public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
}
