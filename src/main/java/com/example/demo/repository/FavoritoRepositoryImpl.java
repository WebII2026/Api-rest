package com.example.demo.repository;

import com.example.demo.model.Favorito;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FavoritoRepositoryImpl implements FavoritoRepository {
    private final ConcurrentHashMap<Long, Favorito> db = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public Favorito guardar(Favorito favorito) {
        if (favorito.getId() == null) {
            favorito.setId(seq.getAndIncrement());
        }
        db.put(favorito.getId(), favorito);
        return favorito;
    }

    @Override
    public List<Favorito> listarTodos() {
        return new ArrayList<>(db.values());
    }

    @Override
    public Optional<Favorito> buscarPorId(Long id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public Favorito actualizar(Favorito favorito) {
        if (!db.containsKey(favorito.getId())) {
            return null;
        }
        db.put(favorito.getId(), favorito);
        return favorito;
    }

    @Override
    public boolean eliminar(Long id) {
        return db.remove(id) != null;
    }
}
