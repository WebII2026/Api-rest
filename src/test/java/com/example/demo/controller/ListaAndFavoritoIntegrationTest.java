package com.example.demo.controller;

import com.example.demo.dto.favorito.FavoritoRequest;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.MoverFavoritosRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ListaAndFavoritoIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void flujoCompletoListasYFavoritosConTransaccionYConflict() throws Exception {
        // 1. Crear dos listas: Origen y Destino
        ListaRequest reqOrigen = new ListaRequest("Lista Origen");
        MvcResult resOrigen = mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqOrigen)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Lista Origen"))
                .andReturn();

        Long origenId = objectMapper.readTree(resOrigen.getResponse().getContentAsString()).get("id").asLong();

        ListaRequest reqDestino = new ListaRequest("Lista Destino");
        MvcResult resDestino = mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDestino)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Lista Destino"))
                .andReturn();

        Long destinoId = objectMapper.readTree(resDestino.getResponse().getContentAsString()).get("id").asLong();

        // 2. Crear favoritos asociados a la lista origen
        FavoritoRequest favReq1 = new FavoritoRequest(101L, "Nota producto 101", origenId);
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favReq1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productoId").value(101))
                .andExpect(jsonPath("$.listaId").value(origenId));

        FavoritoRequest favReq2 = new FavoritoRequest(102L, "Nota producto 102", origenId);
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favReq2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productoId").value(102))
                .andExpect(jsonPath("$.listaId").value(origenId));

        // 3. Consultar favoritos de la lista origen
        mockMvc.perform(get("/api/listas/{id}/favoritos", origenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 4. Intentar eliminar la lista origen mientras tiene favoritos -> 409 Conflict
        mockMvc.perform(delete("/api/listas/{id}", origenId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail", containsString("todavía tiene favoritos")));

        // 5. Mover favoritos de origen a destino
        MoverFavoritosRequest moverReq = new MoverFavoritosRequest(destinoId);
        mockMvc.perform(post("/api/listas/{origenId}/mover-favoritos", origenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moverReq)))
                .andExpect(status().isNoContent());

        // 6. Verificar que la lista origen ya no existe (404)
        mockMvc.perform(get("/api/listas/{id}", origenId))
                .andExpect(status().isNotFound());

        // 7. Verificar que los favoritos ahora pertenecen a la lista destino
        mockMvc.perform(get("/api/listas/{id}/favoritos", destinoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].listaId").value(destinoId))
                .andExpect(jsonPath("$[1].listaId").value(destinoId));

        // 8. Crear una lista vacía y eliminarla -> 204 No Content
        ListaRequest reqVacia = new ListaRequest("Lista Vacia");
        MvcResult resVacia = mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVacia)))
                .andExpect(status().isCreated())
                .andReturn();

        Long vaciaId = objectMapper.readTree(resVacia.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/listas/{id}", vaciaId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/listas/{id}", vaciaId))
                .andExpect(status().isNotFound());
    }

    @Test
    void validacionCamposListaYFavorito() throws Exception {
        // Validación nombre vacío en lista
        mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Validación listaId nulo en favorito
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"nota\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void moverFavoritosConListaInexistenteDa404() throws Exception {
        MoverFavoritosRequest moverReq = new MoverFavoritosRequest(99999L);
        mockMvc.perform(post("/api/listas/{origenId}/mover-favoritos", 99998L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moverReq)))
                .andExpect(status().isNotFound());
    }
}
