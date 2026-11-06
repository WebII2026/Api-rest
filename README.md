# TP3 · Seguridad, pruebas e integración continua

Evolución del proyecto de los TP1 y TP2 incorporando:
- **Autenticación con JWT:** Registro (`POST /api/auth/registro`) y login (`POST /api/auth/login`) con contraseñas hasheadas mediante BCrypt y emisión de tokens firmados HMAC-SHA256.
- **Autorización basada en roles (RBAC) y propiedad de recursos:** Roles `USER` y `ADMIN`. Cada usuario opera exclusivamente sobre sus propias listas y favoritos, mientras que los administradores pueden consultar y gestionar cualquier lista.
- **Pruebas automatizadas separadas:** Pruebas unitarias rápidas con JUnit 5 y Mockito (`*Test`), y pruebas de integración de punta a punta con **Testcontainers** levantando PostgreSQL real en un contenedor temporal (`*IT`).
- **Cobertura de código con JaCoCo:** Reporte HTML navegable generado al verificar el proyecto.
- **Integración continua (CI):** Pipeline automatizado con GitHub Actions que valida compilación, pruebas unitarias, pruebas de integración contra base limpia y reporte de cobertura en cada push y pull request.
- **Despliegue continuo (CD):** Containerización multi-stage con Docker y configuración de Blueprint para Render (`render.yaml`).

---

## Cómo levantar el proyecto

Requiere **Java 25**. Usar siempre el Maven Wrapper (`mvnw` en Linux/macOS o `mvnw.cmd` en Windows).

### 1. Variables de entorno y configuración
La configuración sensible y de despliegue se gestiona mediante variables de entorno con valores por defecto para desarrollo local (`src/main/resources/application.properties`):
- `PORT`: Puerto HTTP (por defecto `8080`).
- `DB_URL`: URL JDBC de PostgreSQL (por defecto `jdbc:postgresql://localhost:5432/webii_tp2`).
- `DB_USERNAME`: Usuario de la base de datos (por defecto `webii_tp2`).
- `DB_PASSWORD`: Contraseña de la base de datos (por defecto `webii_tp2`).
- `JWT_SECRETO`: Clave secreta para firmar los tokens JWT (por defecto clave de desarrollo de 256 bits).
- `JWT_EXPIRACION_MS`: Tiempo de expiración del token en milisegundos (por defecto 86400000 ms = 24 horas).

### 2. Base de datos PostgreSQL local

#### Opción A — Docker Compose
Desde la raíz del proyecto:
```bash
docker compose up -d
```

#### Opción B — PostgreSQL instalado en el sistema
Crear la base y usuario correspondientes (ej. desde pgAdmin o `psql`):
```sql
CREATE ROLE webii_tp2 WITH LOGIN PASSWORD 'webii_tp2' SUPERUSER;
CREATE DATABASE webii_tp2 OWNER webii_tp2;
```

### 3. Iniciar la aplicación
```bash
# En Windows
.\mvnw.cmd spring-boot:run

# En Linux / macOS
./mvnw spring-boot:run
```

---

## Esquema y Migraciones Flyway

Flyway gestiona de forma reproducible el esquema de la base de datos en `src/main/resources/db/migration/`:
- `V1__create_favoritos.sql`: Crea la tabla inicial `favoritos`.
- `V2__create_listas.sql`: Crea la tabla `listas`.
- `V3__add_lista_id_a_favoritos.sql`: Agrega la columna `lista_id` nullable en `favoritos` y su clave foránea.
- `V4__lista_id_obligatorio.sql`: Crea la lista por defecto, realiza el backfill de favoritos huérfanos y establece `NOT NULL` sobre `lista_id`.
- `V5__create_usuarios.sql`: Crea la tabla `usuarios` con columnas `id`, `email` (único), `password_hash` y `rol` (`USER` / `ADMIN`).
- `V6__add_usuario_id_a_listas.sql`: Agrega `usuario_id` a la tabla `listas` vinculándola con `usuarios(id)`.

---

## Endpoints de la API

| Tag | Método | Path | Seguridad | Descripción | Código de éxito |
|---|---|---|---|---|---|
| **auth** | POST | `/api/auth/registro` | Público | Registra un nuevo usuario (`USER`). Retorna ID, email y rol (409 si el email ya existe) | `201 Created` |
| **auth** | POST | `/api/auth/login` | Público | Autentica credenciales y emite token Bearer JWT (401 si son inválidas) | `200 OK` |
| **health** | GET | `/health` | Público | Endpoint de diagnóstico y healthcheck (devuelve status, service y version) | `200 OK` |
| **productos** | GET | `/api/productos` | Público | Catálogo externo de productos (DummyJSON) | `200 OK` |
| **productos** | GET | `/api/productos/{id}` | Público | Obtener producto individual | `200 OK` |
| **listas** | POST | `/api/listas` | Bearer JWT | Crea una nueva lista asociándola al usuario autenticado | `201 Created` |
| **listas** | GET | `/api/listas` | Bearer JWT | Lista las listas del usuario (o todas si es `ADMIN`) | `200 OK` |
| **listas** | GET | `/api/listas/{id}` | Bearer JWT | Obtiene una lista por ID (403 si pertenece a otro usuario y no es `ADMIN`) | `200 OK` |
| **listas** | GET | `/api/listas/{id}/favoritos` | Bearer JWT | Favoritos de una lista propia o como `ADMIN` | `200 OK` |
| **listas** | DELETE | `/api/listas/{id}` | Bearer JWT | Elimina lista vacía (409 si tiene favoritos, 403 si es ajena) | `204 No Content` |
| **listas** | POST | `/api/listas/{origenId}/mover-favoritos` | Bearer JWT | Mueve favoritos y elimina la lista origen atómicamente | `204 No Content` |
| **favoritos** | GET | `/api/favoritos` | Bearer JWT | Listar todos los favoritos | `200 OK` |
| **favoritos** | GET | `/api/favoritos/{id}` | Bearer JWT | Obtener un favorito puntual por ID | `200 OK` |
| **favoritos** | POST | `/api/favoritos` | Bearer JWT | Crear favorito asociado a una lista | `201 Created` |
| **favoritos** | PUT | `/api/favoritos/{id}` | Bearer JWT | Actualizar un favorito | `200 OK` |
| **favoritos** | DELETE | `/api/favoritos/{id}` | Bearer JWT | Eliminar un favorito | `204 No Content` |

### Documentación Swagger / OpenAPI
Con la aplicación levantada:
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) (soporta botón *Authorize* para ingresar `Bearer <token>`).
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Pruebas automatizadas y Cobertura

Las pruebas están separadas por convención para aislar velocidad de completitud:
- `*Test`: Pruebas unitarias ejecutadas por **Maven Surefire**. Se resuelven en milisegundos con dependencias mockeadas (Mockito) y **no requieren Docker**.
- `*IT`: Pruebas de integración ejecutadas por **Maven Failsafe**. Levantan el contexto completo de Spring Boot y un contenedor real de PostgreSQL vía **Testcontainers** (requiere Docker).

### 1. Ejecutar solo pruebas unitarias (rápido, sin Docker)
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

Clases cubiertas:
- `HealthControllerTest`: Verifica el endpoint `/health` y versión.
- `UsuarioServiceImplTest`: Registro exitoso con hash BCrypt y rechazo de email duplicado (`409`).
- `AuthServiceImplTest`: Autenticación exitosa, token JWT y rechazo por credenciales erróneas (`401`).
- `ListaServiceImplTest`: Reglas de negocio de listas, asignación de dueño, acceso permitido al dueño o admin, y rechazo con `AccesoDenegadoException` (`403`) para listas ajenas.
- `JwtServiceTest`: Generación de token, extracción de subject (ID) y claim de rol, y validación contra tokens expirados o adulterados.

### 2. Ejecutar todas las pruebas (unitarias + integración con Testcontainers)
```bash
# Windows
.\mvnw.cmd verify

# Linux / macOS
./mvnw verify
```

Clase de integración:
- `ListaControllerIT`: Flujo completo de punta a punta con MockMvc y base PostgreSQL limpia:
  - Registro y login para Ana, Bob y Administrador.
  - Creación de listas asignadas al usuario en sesión.
  - Bloqueo 401 sin token.
  - Bloqueo 403 cuando Bob intenta leer o borrar una lista de Ana.
  - Acceso 200 concedido a un `ADMIN` sobre recursos de otros usuarios.
  - Imposibilidad de elevar privilegios adulterando el payload del token.

### 3. Reporte de cobertura con JaCoCo
Al ejecutar `verify`, el plugin de JaCoCo genera el reporte navegable en:
```
target/site/jacoco/index.html
```
Para abrirlo en Windows:
```powershell
start target\site\jacoco\index.html
```

---

## Integración Continua (CI) con GitHub Actions

El archivo `.github/workflows/ci.yml` automatiza la verificación en cada `push` y `pull request` en un runner limpio de Ubuntu (`ubuntu-latest` con Docker nativo):
1. **Checkout:** Descarga el código del repositorio.
2. **Setup Java 25:** Configura Eclipse Temurin JDK 25 con caché para dependencias de Maven.
3. **test-compile:** `./mvnw --batch-mode test-compile` compila clases principales y de pruebas.
4. **Pruebas unitarias:** `./mvnw --batch-mode test` corre la suite unitaria.
5. **Pruebas de integración y cobertura:** `./mvnw --batch-mode verify` levanta PostgreSQL en Testcontainers, aplica migraciones Flyway de cero, valida los tests de integración y genera las métricas de JaCoCo.
6. **Artifact de Cobertura:** Sube el reporte HTML (`target/site/jacoco`) como artifact descargable de la corrida (`reporte-de-cobertura`).

---

## Despliegue en Render (CD)

El proyecto incluye soporte nativo para despliegue automático en la nube a través de Docker y Render Blueprints:

### 1. Dockerfile Multi-Stage
- **Etapa de build (`eclipse-temurin:25-jdk`):** Compila el empaquetado `.jar` mediante Maven Wrapper sin volver a correr tests (`-DskipTests`, ya validados por la CI).
- **Etapa de ejecución (`eclipse-temurin:25-jre`):** Imagen liviana de solo runtime, ejecuta la aplicación bajo un usuario sin privilegios (`app`) y configura `-XX:MaxRAMPercentage=75.0`.

### 2. Blueprint de Render (`render.yaml`)
Define la infraestructura como código:
- Un servicio de base de datos PostgreSQL gestionado (`web2-db`).
- Un Web Service Docker (`web2-demo`) con `autoDeployTrigger: checksPass` (Render solo despliega una nueva versión si los chequeos de GitHub Actions están en verde).
- Enlace dinámico de variables de entorno: `DB_USERNAME` y `DB_PASSWORD` se obtienen de la base creada; `JWT_SECRETO` se genera de forma segura y aleatoria; `DB_URL` se conecta vía formato JDBC.
- Health check configurado en `/health`.

### Pasos para desplegar en Render:
1. Asegurarse de que el repositorio esté en GitHub con la CI en verde.
2. En [Render Dashboard](https://dashboard.render.com), ir a **New +** → **Blueprint**.
3. Conectar el repositorio de GitHub.
4. Render detectará automáticamente `render.yaml` y listará los recursos `web2-db` y `web2-demo`.
5. Al solicitar el valor de `DB_URL`, ingresar temporalmente `pendiente` y hacer clic en **Apply**.
6. Una vez provisionada la base `web2-db`, copiar su **Internal Database URL** (reemplazando `postgres://` por `jdbc:postgresql://`).
7. Actualizar la variable de entorno `DB_URL` en el servicio `web2-demo` y guardar los cambios. Render redeplegará el contenedor y la aplicación pasará a estado **Live** tras verificar el endpoint `/health`.

---

## Decisiones de Diseño y Arquitectura

### ¿Por qué la autorización vive en el Service y no en el Controller? (Consigna 4.5)

Una pregunta habitual de arquitectura es: *"¿Por qué validar si el usuario es dueño de la lista dentro del Service y no mediante una anotación o filtro en el Controller?"*.

1. **El Controller solo conoce el transporte HTTP:**
   El Controller recibe lo que llega en la petición: el ID de la lista en la ruta (`/api/listas/{id}`) y el ID del usuario en el token JWT. El Controller **no sabe quién es el dueño de esa lista** hasta consultar su estado actual.
2. **Acceso a datos y responsabilidad de capas:**
   Determinar la pertenencia del recurso requiere consultar la base de datos. Si esa verificación se realizara en el Controller, se violaría la arquitectura en capas obligando a inyectar repositorios directamente en el Controller, o bien obligando a invocar al Service dos veces (una para verificar pertenencia y otra para ejecutar la acción), introduciendo llamadas redundantes y dispersión de lógica.
3. **Regla de negocio vs Regla de protocolo:**
   *"Solo el dueño de una lista o un administrador pueden verla, modificarla o eliminarla"* es una **regla de negocio intrínseca al dominio**, no un detalle de transporte HTTP. Ubicarla en el Service garantiza que la regla se aplique siempre de manera consistente, independientemente de qué adaptador de entrada invoque la operación (un Controller REST, un comando CLI, un consumidor de mensajería o un batch).
4. **Mantenimiento del Controller delgado (Thin Controller):**
   El Controller delega la ejecución al Service y se limita a transformar la respuesta o permitir que el `GlobalExceptionHandler` capture la excepción `AccesoDenegadoException` y responda con el código HTTP adecuado (`403 Forbidden`).
