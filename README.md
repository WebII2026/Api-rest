# TP1 · Spring Boot, API REST y arquitectura en capas

## Cómo levantar el proyecto

Requiere Java 25. Usar siempre el wrapper, nunca un `mvn` instalado aparte:

```
# Windows
.\mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

Cuando el log muestre `Started DemoApplication`, la app queda escuchando en `http://localhost:8080`.

Para compilar y correr los tests: `./mvnw test` (o `.\mvnw.cmd test`).

## Endpoints de la API

La aplicación cuenta con dos grupos de endpoints principales, los cuales pueden explorarse interactiva y visualmente a través de la documentación de Swagger:

- **Swagger UI:** http://localhost:8080/swagger-ui.html

### Catálogo de productos (consumo de un servicio externo)

- **GET `/api/productos`**: Retorna el listado de productos de DummyJSON (mapeados a un DTO propio). Soporta los parámetros `limit` y `skip`.
- **GET `/api/productos/{id}`**: Retorna los datos de un producto específico.

### Favoritos (CRUD completo en memoria)

- **POST `/api/favoritos`**: Crea un nuevo favorito enviando un body JSON.
- **GET `/api/favoritos`**: Lista todos los favoritos almacenados.
- **GET `/api/favoritos/{id}`**: Retorna un favorito específico.
- **PUT `/api/favoritos/{id}`**: Actualiza los datos de un favorito.
- **DELETE `/api/favoritos/{id}`**: Elimina un favorito.

## Dependencias utilizadas

- `spring-boot-starter-webmvc` — Spring MVC + Tomcat embebido.
- `spring-boot-starter-validation` — Bean Validation (`@NotNull`, `@NotBlank`, ...).
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI / OpenAPI.

## Pruebas de la API (Casos de éxito y error)

En la raíz de este repositorio se incluye el archivo **`api-tests.http`**. 
Este archivo contiene la **colección de peticiones HTTP** requerida para evidenciar los casos de éxito y de error para cada uno de los recursos (Productos y Favoritos). 

Para ejecutar las pruebas:
1. Instalar la extensión **REST Client** en VS Code.
2. Levantar la aplicación.
3. Abrir `api-tests.http` y hacer click en *Send Request* arriba de cada bloque para verificar las respuestas correctas y los errores controlados (400, 404, etc.).
