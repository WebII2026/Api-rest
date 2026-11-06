package com.example.demo.controller;

import com.example.demo.domain.Rol;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.entity.UsuarioEntity;
import com.example.demo.repository.UsuarioJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ListaControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));

    @DynamicPropertySource
    static void propiedadesDeConexion(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private String registrarYLoguear(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistroRequest(email, password))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    private Long crearLista(String token, String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/listas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ListaRequest(nombre))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void login_conCredencialesValidas_devuelve200ConToken() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistroRequest("usuario.valido@test.com", "12345678"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("usuario.valido@test.com", "12345678"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void login_conCredencialesInvalidas_devuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("noexiste@test.com", "claveIncorrecta"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void accederAListas_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accederAListaPropia_devuelve200() throws Exception {
        String token = registrarYLoguear("dueno@test.com", "12345678");
        Long listaId = crearLista(token, "Mi lista propia");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId))
                .andExpect(jsonPath("$.nombre").value("Mi lista propia"));
    }

    @Test
    void accederAListaAjena_devuelve403() throws Exception {
        String tokenAna = registrarYLoguear("ana.duena@test.com", "12345678");
        Long listaId = crearLista(tokenAna, "Lista de Ana");

        String tokenBob = registrarYLoguear("bob.intruso@test.com", "12345678");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail", is("La lista " + listaId + " no te pertenece")));
    }

    @Test
    void admin_accediendoAListaAjena_devuelve200() throws Exception {
        String tokenAna = registrarYLoguear("ana2@test.com", "12345678");
        Long listaId = crearLista(tokenAna, "Lista protegida de Ana");

        String tokenAdmin = registrarYLoguear("admin@test.com", "12345678");

        // Promovemos al usuario admin en la base de datos
        UsuarioEntity adminEntity = usuarioJpaRepository.findByEmail("admin@test.com").orElseThrow();
        adminEntity.setRol(Rol.ADMIN);
        usuarioJpaRepository.save(adminEntity);

        // Volvemos a loguear para obtener el token con rol ADMIN
        MvcResult loginAdmin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@test.com", "12345678"))))
                .andExpect(status().isOk())
                .andReturn();
        String nuevoTokenAdmin = objectMapper.readTree(loginAdmin.getResponse().getContentAsString()).get("token").asText();

        // El ADMIN puede acceder a la lista de Ana
        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header("Authorization", "Bearer " + nuevoTokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId));
    }

    @Test
    void registro_conRolAdminEnElBody_loGuardaComoUser() throws Exception {
        // Un cliente malicioso intenta auto-asignarse rol ADMIN en el JSON
        String payload = "{\"email\":\"hacker@test.com\",\"password\":\"12345678\",\"rol\":\"ADMIN\"}";

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USER"));

        UsuarioEntity usuario = usuarioJpaRepository.findByEmail("hacker@test.com").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(Rol.USER, usuario.getRol());
    }

    @Test
    void eliminarListaAjena_devuelve403() throws Exception {
        String tokenAna = registrarYLoguear("ana3@test.com", "12345678");
        Long listaId = crearLista(tokenAna, "Lista de Ana para borrar");

        String tokenBob = registrarYLoguear("bob3@test.com", "12345678");

        mockMvc.perform(delete("/api/listas/{id}", listaId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isForbidden());
    }
}
