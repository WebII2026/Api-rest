package com.example.demo.service;

import com.example.demo.domain.Favorito;
import com.example.demo.domain.Lista;
import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.dto.lista.MoverFavoritosRequest;
import com.example.demo.exception.ListaNoVaciaException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListaServiceImpl implements ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaServiceImpl(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Override
    public List<ListaResponse> listar() {
        return listaRepository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Override
    public ListaResponse obtener(Long id) {
        return aResponse(buscarOFallar(id));
    }

    @Override
    public ListaResponse crear(ListaRequest request) {
        Lista nueva = new Lista(null, request.nombre());
        return aResponse(listaRepository.save(nueva));
    }

    @Override
    public void eliminar(Long id) {
        Lista lista = buscarOFallar(id);
        if (!favoritoRepository.findByListaId(lista.id()).isEmpty()) {
            throw new ListaNoVaciaException("La lista " + id + " todavía tiene favoritos, no se puede eliminar");
        }
        listaRepository.deleteById(id);
    }

    @Override
    public List<FavoritoResponse> obtenerFavoritos(Long id) {
        buscarOFallar(id);
        return favoritoRepository.findByListaId(id).stream()
                .map(f -> new FavoritoResponse(f.id(), f.productoId(), f.nota(), f.fechaAgregado(), f.listaId()))
                .toList();
    }

    @Override
    @Transactional
    public void moverFavoritos(Long origenId, MoverFavoritosRequest request) {
        Lista origen = buscarOFallar(origenId);
        Lista destino = buscarOFallar(request.listaDestinoId());

        for (Favorito f : favoritoRepository.findByListaId(origen.id())) {
            favoritoRepository.save(new Favorito(
                    f.id(),
                    f.productoId(),
                    f.nota(),
                    f.fechaAgregado(),
                    destino.id()
            ));
        }

        listaRepository.deleteById(origen.id());
    }

    private Lista buscarOFallar(Long id) {
        return listaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lista con id " + id));
    }

    private ListaResponse aResponse(Lista lista) {
        return new ListaResponse(lista.id(), lista.nombre());
    }
}
