# TP2 · Persistencia, migraciones y arquitectura hexagonal

Evolución del proyecto del TP1 incorporando persistencia en **PostgreSQL**, **Spring Data JPA / Hibernate**, versionado de esquema con **Flyway**, transacciones `@Transactional`, relaciones entre entidades y el patrón de **Puertos y Adaptadores (Arquitectura Hexagonal)**.

---

## Cómo levantar el proyecto

Requiere Java 25. Usar siempre el Maven Wrapper (`mvnw` o `mvnw.cmd`).

### 1. Base de datos PostgreSQL

#### Opción A — Docker (recomendada)

Desde la raíz del repositorio:

```bash
docker compose up -d
```

Esto levanta un contenedor con la imagen oficial `postgres:17` configurado según `docker-compose.yml`:
- **Host:** `localhost`
- **Puerto:** `5432`
- **Base de datos:** `webii_tp2`
- **Usuario:** `webii_tp2`
- **Contraseña:** `webii_tp2`
- **Volumen persistente:** `demo-postgres-data`

Para verificar que el contenedor está corriendo:
```bash
docker compose ps
```

#### Opción B — PostgreSQL local

Si no se dispone de Docker, instalar PostgreSQL localmente y ejecutar en la consola SQL (por ejemplo desde pgAdmin sobre la base `postgres`):

```sql
CREATE ROLE webii_tp2 WITH LOGIN PASSWORD 'webii_tp2' SUPERUSER;
CREATE DATABASE webii_tp2 OWNER webii_tp2;
```

Los parámetros de conexión se encuentran centralizados en `src/main/resources/application.properties`.

---

### 2. Aplicación y Migraciones Flyway

Para iniciar la aplicación:

```bash
# En Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run

# En macOS / Linux
./mvnw spring-boot:run
```

#### Cómo se aplican las migraciones Flyway
Al arrancar la aplicación, **Flyway** toma el control del esquema de la base de datos (con `spring.jpa.hibernate.ddl-auto=validate` para que Hibernate solo valide el esquema y no compita con Flyway):
1. Flyway busca los scripts SQL en `src/main/resources/db/migration/` siguiendo la convención `V{versión}__{descripción}.sql`.
2. Consulta la tabla interna `flyway_schema_history` creada automáticamente en PostgreSQL.
3. Aplica en orden estricto las migraciones pendientes dentro de una transacción.
4. Calcula y almacena el checksum de cada migración aplicada.

Para confirmar que las migraciones corrieron con éxito, se puede consultar la tabla directamente en PostgreSQL:

```bash
docker exec demo-postgres psql -U webii_tp2 -d webii_tp2 -c "SELECT installed_rank, version, description, success FROM flyway_schema_history;"
```

Salida esperada:
```
 installed_rank | version |       description        | success 
----------------+---------+--------------------------+---------
              1 | 1       | create favoritos         | t
              2 | 2       | create listas            | t
              3 | 3       | add lista id a favoritos | t
              4 | 4       | lista id obligatorio     | t
```

Cuando la aplicación finaliza la inicialización, queda disponible en `http://localhost:8080`.

---

## Endpoints de la API

| Tag | Método | Path | Descripción | Código de éxito |
|---|---|---|---|---|
| **productos** | GET | `/api/productos` | Catálogo de productos (DummyJSON, solo lectura) | `200 OK` |
| **productos** | GET | `/api/productos/{id}` | Obtener producto puntual por ID | `200 OK` |
| **listas** | POST | `/api/listas` | Crear una nueva lista de favoritos | `201 Created` |
| **listas** | GET | `/api/listas` | Listar todas las listas | `200 OK` |
| **listas** | GET | `/api/listas/{id}` | Obtener una lista por ID | `200 OK` |
| **listas** | GET | `/api/listas/{id}/favoritos` | Favoritos pertenecientes a una lista | `200 OK` |
| **listas** | DELETE | `/api/listas/{id}` | Eliminar lista vacía (da 409 si tiene favoritos) | `204 No Content` |
| **listas** | POST | `/api/listas/{origenId}/mover-favoritos` | Mover favoritos y eliminar lista origen de forma atómica | `204 No Content` |
| **favoritos** | GET | `/api/favoritos` | Listar todos los favoritos | `200 OK` |
| **favoritos** | GET | `/api/favoritos/{id}` | Obtener un favorito puntual por ID | `200 OK` |
| **favoritos** | POST | `/api/favoritos` | Crear favorito asociado a una lista | `201 Created` |
| **favoritos** | PUT | `/api/favoritos/{id}` | Actualizar un favorito | `200 OK` |
| **favoritos** | DELETE | `/api/favoritos/{id}` | Eliminar un favorito puntual | `204 No Content` |

### Documentación Interactiva (Swagger UI)
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Especificación OpenAPI (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Justificaciones de Diseño y Arquitectura

### 1. Puertos y Adaptadores: Qué cambió y qué no cambió

#### Clases que quedaron exactamente iguales o no modificaron su rol:
- **`FavoritoService` / `FavoritoServiceImpl`:** La lógica de negocio no cambió en absoluto respecto al almacenamiento. Sigue dependiendo exclusivamente de la interfaz `FavoritoRepository`. Solamente se propagó el campo `listaId` en los mapeos con los DTOs.
- **`FavoritoController`:** El controlador REST no sufrió ningún cambio en sus endpoints, parámetros ni respuestas HTTP. Desconoce por completo que los datos ahora van a PostgreSQL en lugar de la memoria RAM.
- **`FavoritoRequest` / `FavoritoResponse`:** Mantuvieron su estructura intacta, sumando la referencia por ID `listaId` requerida por la relación.
- **`Favorito` (record de dominio):** El dominio continuó siendo un record inmutable desacoplado de anotaciones de Hibernate o base de datos.
- **`GlobalExceptionHandler`:** Los handlers previos de 404, 400 y 500 quedaron intactos; únicamente se sumó un nuevo `@ExceptionHandler` para la nueva regla de negocio (409 Conflict).

#### Clases nuevas y reemplazadas:
- **`InMemoryFavoritoRepository`:** Se **eliminó por completo**. Mantener dos beans de Spring que implementan `FavoritoRepository` generaría ambigüedad en la inyección de dependencias.
- **`FavoritoRepositoryAdapter`:** Nuevo adapter que implementa el puerto `FavoritoRepository`. Convierte entre el modelo inmutable de dominio (`Favorito`) y la entidad mutable de persistencia (`FavoritoEntity`), delegando en Spring Data JPA.
- **`FavoritoEntity` y `ListaEntity`:** Entidades JPA mutables con constructor por defecto, anotaciones `@Entity`, `@Table`, `@Id`, `@GeneratedValue(IDENTITY)` y `@Column`.
- **`FavoritoJpaRepository` y `ListaJpaRepository`:** Interfaces de infraestructura que extienden `JpaRepository` de Spring Data.
- **Módulo Listas completo:** Dominio `Lista`, puerto `ListaRepository`, adapter `ListaRepositoryAdapter`, `ListaService`, `ListaServiceImpl` y `ListaController`.

#### ¿Por qué fue posible cambiar el almacenamiento sin alterar la lógica de negocio?
Porque `FavoritoRepository` actúa como un **puerto** (un contrato en la capa de dominio/aplicación). La aplicación depende de abstracciones y no de implementaciones concretas (Principio de Inversión de Dependencias). Tanto `InMemoryFavoritoRepository` (TP1) como `FavoritoRepositoryAdapter` (TP2) son **adapters intercambiables** que implementan la misma interfaz. El Service y el Controller hablan con el puerto; los detalles de infraestructura (PostgreSQL, tablas, JPA, Hibernate) quedan confinados detrás del adapter.

---

### 2. Evolución del Esquema con Flyway: Migración V4 vs Modificar Migraciones Previas

#### ¿Por qué la evolución se resuelve con una migración nueva (`V4`) y no editando `V1`, `V2` o `V3`?
1. **Inmutabilidad y validación de Checksums:** Flyway calcula un hash criptográfico (checksum) de cada script SQL al momento de aplicarlo y lo persiste en `flyway_schema_history`. Si se alterara un archivo ya ejecutado (como `V1` o `V3`), en el siguiente inicio Flyway detectaría una discrepancia de checksum y abortaría el arranque con un error fatal de validación.
2. **Compatibilidad con entornos y datos existentes:** En entornos reales (desarrollo, testing, staging, producción), las migraciones `V1` a `V3` ya fueron ejecutadas y pueden existir filas de favoritos con `lista_id` nulo. Si se hubiese agregado `NOT NULL` en `V3`, la migración hubiera fallado en bases con datos previos. `V4__lista_id_obligatorio.sql` resuelve el ciclo de vida de forma segura en tres pasos ordenados:
   - Crea una lista por defecto ("Sin clasificar") si no existe.
   - Realiza el **backfill** actualizando los favoritos huérfanos (`WHERE lista_id IS NULL`) para asociarlos a dicha lista.
   - Recién una vez garantizado que ninguna fila tiene `NULL`, aplica la restricción `ALTER TABLE favoritos ALTER COLUMN lista_id SET NOT NULL;`.

---

### 3. Transacciones y Atomicidad (ACID): Operación `mover-favoritos`

La operación `POST /api/listas/{origenId}/mover-favoritos` ejecuta múltiples escrituras:
1. Reasigna todos los favoritos de la lista origen a la lista destino (N operaciones `save`).
2. Elimina la lista origen (`deleteById`).

El método `ListaServiceImpl.moverFavoritos` está delimitado con `@Transactional`.

#### Justificación teórica ACID:
- **A (Atomicidad):** La atomicidad garantiza que un conjunto de operaciones en la base de datos se ejecute como una única unidad indivisible: **o se aplican todas con éxito (COMMIT), o no se aplica ninguna (ROLLBACK)**.
- **¿Qué podría quedar inconsistente sin `@Transactional`?**
  Si no se utilizara `@Transactional` y ocurriera una falla en la mitad del proceso (por ejemplo, pérdida de conectividad, caída del servidor, timeout o falla en el `deleteById` posterior):
  - Los favoritos ya habrían sido reasignados a la lista destino.
  - La lista origen **no se eliminaría** y permanecería en la base de datos, ahora vacía o parcialmente vacía si la falla ocurrió a mitad del bucle de reasignación.
  - El sistema quedaría en un estado híbrido e inconsistente que viola las reglas de negocio de la operación (que exige que el traspaso y la eliminación del origen ocurran juntos). Con `@Transactional`, cualquier excepción produce un rollback automático que restaura el estado original previo a la transacción.

---

### 4. Regla de Negocio: HTTP 409 Conflict

Al invocar `DELETE /api/listas/{id}`:
- Si la lista contiene favoritos, el servicio lanza `ListaNoVaciaException`.
- El `@RestControllerAdvice` (`GlobalExceptionHandler`) captura esta excepción y genera un `ProblemDetail` con código de estado HTTP **`409 CONFLICT`**.
- Esto previene errores genéricos de integridad referencial de base de datos (`500 Internal Server Error`), comunicando explícitamente al cliente la causa del rechazo de la solicitud.

---

## Verificación y Pruebas

### 1. Pruebas Automáticas con Maven
Se incluye una suite de pruebas de integración en `src/test/java/com/example/demo/controller/ListaAndFavoritoIntegrationTest.java` que valida:
- Flujo completo de creación de listas y favoritos.
- Verificación del error `409 Conflict` al intentar eliminar listas con favoritos asociados.
- Ejecución atómica de `mover-favoritos` y eliminación de la lista origen.
- Verificación de borrado exitoso (`204 No Content`) de listas vacías.
- Manejo de errores de validación (`400 Bad Request`) y recursos no encontrados (`404 Not Found`).

Para ejecutar todos los tests:
```bash
.\mvnw.cmd test
```

### 2. Colección de Requests HTTP (`tp2-requests.http`)
En la raíz del proyecto se encuentra el archivo `tp2-requests.http`, listo para ser utilizado con la extensión **REST Client** de VS Code o el cliente HTTP integrado de IntelliJ IDEA, cubriendo casos de éxito y de error para todos los endpoints nuevos y preexistentes.
