package com.example.demo.repository;

import com.example.demo.entity.ListaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
    List<ListaEntity> findByUsuarioId(Long usuarioId);
}
