package com.example.demo.client.dummyjson;

import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ServicioExternoException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class DummyJsonClient {
    private final RestClient restClient;

    public DummyJsonClient(RestClient dummyJsonRestClient) {
        this.restClient = dummyJsonRestClient;
    }

    public DummyJsonProductosResponse getProducts(int limit, int skip) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products")
                            .queryParam("limit", limit)
                            .queryParam("skip", skip)
                            .build())
                    .retrieve()
                    .body(DummyJsonProductosResponse.class);
        } catch (Exception e) {
            throw new ServicioExternoException("Error al consultar DummyJSON: " + e.getMessage(), e);
        }
    }

    public DummyJsonProducto getProductById(Long id) {
        try {
            return restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProducto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("Producto no encontrado en el sistema externo (ID: " + id + ")");
        } catch (Exception e) {
            throw new ServicioExternoException("Error al consultar DummyJSON (ID: " + id + "): " + e.getMessage(), e);
        }
    }
}
