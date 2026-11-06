package com.example.demo.service;

import com.example.demo.domain.Favorito;
import com.example.demo.domain.Lista;
import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.dto.lista.MoverFavoritosRequest;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaNoVaciaException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.ContextoSeguridad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListaServiceImpl implements ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;
    private final ContextoSeguridad contexto;

    public ListaServiceImpl(
            ListaRepository listaRepository,
            FavoritoRepository favoritoRepository,
            ContextoSeguridad contexto
    ) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
        this.contexto = contexto;
    }

    @Override
    public List<ListaResponse> listar() {
        if (contexto.esAdmin()) {
            return listaRepository.findAll().stream()
                    .map(this::aResponse)
                    .toList();
        }
        Long usuarioId = contexto.usuarioActualId();
        if (usuarioId == null) {
            return List.of();
        }
        return listaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::aResponse)
                .toList();
    }

    @Override
    public ListaResponse obtener(Long id) {
        return aResponse(buscarPropiaOFallar(id));
    }

    @Override
    public ListaResponse crear(ListaRequest request) {
        Long usuarioId = contexto.usuarioActualId();
        Lista nueva = new Lista(null, request.nombre(), usuarioId);
        return aResponse(listaRepository.save(nueva));
    }

    @Override
    public void eliminar(Long id) {
        Lista lista = buscarPropiaOFallar(id);
        if (!favoritoRepository.findByListaId(lista.id()).isEmpty()) {
            throw new ListaNoVaciaException("La lista " + id + " todavía tiene favoritos, no se puede eliminar");
        }
        listaRepository.deleteById(id);
    }

    @Override
    public List<FavoritoResponse> obtenerFavoritos(Long id) {
        Lista lista = buscarPropiaOFallar(id);
        return favoritoRepository.findByListaId(lista.id()).stream()
                .map(f -> new FavoritoResponse(f.id(), f.productoId(), f.nota(), f.fechaAgregado(), f.listaId()))
                .toList();
    }

    @Override
    @Transactional
    public void moverFavoritos(Long origenId, MoverFavoritosRequest request) {
        Lista origen = buscarPropiaOFallar(origenId);
        Lista destino = buscarPropiaOFallar(request.listaDestinoId());

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

    private Lista buscarPropiaOFallar(Long id) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lista con id " + id));
        boolean esDueno = lista.usuarioId() != null
                && lista.usuarioId().equals(contexto.usuarioActualId());
        if (!esDueno && !contexto.esAdmin()) {
            throw new AccesoDenegadoException("La lista " + id + " no te pertenece");
        }
        return lista;
    }

    private ListaResponse aResponse(Lista lista) {
        return new ListaResponse(lista.id(), lista.nombre(), lista.usuarioId());
    }
}
