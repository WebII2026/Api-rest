package com.example.demo.service;

import com.example.demo.dto.favorito.FavoritoRequest;
import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritoService {
    private final FavoritoRepository repository;

    public FavoritoService(FavoritoRepository repository) {
        this.repository = repository;
    }

    public FavoritoResponse crear(FavoritoRequest request) {
        Favorito fav = new Favorito();
        fav.setIdProductoExterno(request.idProductoExterno());
        fav.setNotaPersonal(request.notaPersonal());
        fav.setFechaAgregado(LocalDateTime.now());
        
        Favorito guardado = repository.guardar(fav);
        return mapToDTO(guardado);
    }

    public List<FavoritoResponse> listar() {
        return repository.listarTodos().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FavoritoResponse buscarPorId(Long id) {
        Favorito f = repository.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado con ID: " + id));
        return mapToDTO(f);
    }

    public FavoritoResponse actualizar(Long id, FavoritoRequest request) {
        Favorito f = repository.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado con ID: " + id));
        
        f.setIdProductoExterno(request.idProductoExterno());
        f.setNotaPersonal(request.notaPersonal());
        
        Favorito actualizado = repository.actualizar(f);
        return mapToDTO(actualizado);
    }

    public void eliminar(Long id) {
        if (!repository.eliminar(id)) {
            throw new RecursoNoEncontradoException("Favorito no encontrado con ID: " + id);
        }
    }

    private FavoritoResponse mapToDTO(Favorito f) {
        return new FavoritoResponse(
            f.getId(),
            f.getIdProductoExterno(),
            f.getNotaPersonal(),
            f.getFechaAgregado()
        );
    }
}
