# album

Microservicio del catálogo de álbumes musicales de Music Store. Generado desde el arquetipo `microservice-base-archetype` y completado con el dominio `Album`.

## Estructura

| Paquete | Contenido |
|---|---|
| `com.duoc.album` | `AlbumApplication`, clase principal |
| `controller` | `AlbumController` (`/api/albums`) y `StatusController` (`/api/status`) |
| `dto` | `AlbumRequest`, `AlbumResponse`, `StatusResponse` |
| `model` | Entidad `Album`, con Builder de Lombok |
| `repository` | `AlbumRepository` |
| `service` | `AlbumService` |
| `error` | `ErrorResponse`, `GlobalExceptionHandler`, `ResourceNotFoundException` |
| `pattern` | `ErrorResponseBuilder`, patrón Builder para las respuestas de error |
| `security` | `SecurityConfig`, `UserRemoteAuth`, `RemoteProfile` |

## Requisitos

- Java 21
- Oracle Cloud con wallet
- Microservicio de usuarios en ejecución (`http://localhost:8081` por defecto)

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de Oracle | obligatoria |
| `DB_USER` | Usuario de la base de datos | obligatoria |
| `DB_PASSWORD` | Contraseña de la base de datos | obligatoria |
| `SERVER_PORT` | Puerto HTTP | `8080` |
| `USER_SERVICE_URL` | URL del microservicio de usuarios | `http://localhost:8081` |
| `CORS_ORIGIN` | Origen permitido del frontend | `http://localhost:4200` |

Con wallet de Oracle Cloud, la URL indica el alias de `tnsnames.ora` y la ruta del wallet, sin espacios. El wallet no se sube al repositorio (`wallet/` está en `.gitignore`).

```powershell
$env:DB_URL = "jdbc:oracle:thin:@alias_tp?TNS_ADMIN=C:/ruta/wallet"
$env:DB_USER = "USUARIO"
$env:DB_PASSWORD = "CONTRASEÑA"
```

## Base de datos

Flyway crea y carga la tabla al iniciar:

| Script | Contenido |
|---|---|
| `V1__create_album_table.sql` | Tabla `ALBUM` |
| `V2__insert_initial_albums.sql` | 20 álbumes iniciales |

Hibernate usa `ddl-auto=validate`: comprueba que la entidad coincida con la tabla y no crea ni modifica nada. El schema debe estar vacío la primera vez que se ejecuta Flyway.

## Seguridad

La autenticación es HTTP Basic delegada al microservicio de usuarios (`GET /api/auth/profile`), que define los roles.

| Petición | Anónimo | `USER` | `ADMIN` |
|---|---|---|---|
| `GET /api/status` | Sí | Sí | Sí |
| `GET /api/albums`, `GET /api/albums/{id}` | Sí | Sí | Sí |
| `POST /api/albums`, `PUT /api/albums/{id}`, `DELETE /api/albums/{id}` | No (401) | No (403) | Sí |

El catálogo es público para que los clientes puedan ver los álbumes sin iniciar sesión. Si se envían credenciales incorrectas, la respuesta es 401 aunque la ruta sea pública.

## Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/status` | 200, estado del servicio |
| GET | `/api/albums` | 200, lista de álbumes |
| GET | `/api/albums/{id}` | 200, o 404 si no existe |
| POST | `/api/albums` | 201, álbum creado |
| PUT | `/api/albums/{id}` | 200, álbum actualizado |
| DELETE | `/api/albums/{id}` | 204 |

Cuerpo para `POST` y `PUT`:

```json
{
  "artist": "Boris",
  "title": "Pink",
  "year": 2005,
  "genre": "Stoner Rock",
  "price": 31990,
  "stock": 69,
  "coverArt": null
}
```

`artist`, `title`, `genre`, `price` y `stock` son obligatorios. `year` va de 1900 a 2100.

## Errores

Todas las respuestas de error tienen el mismo formato, construido con `ErrorResponseBuilder`:

```json
{
  "timestamp": "2026-11-22T06:40:00Z",
  "status": 400,
  "message": "Solicitud inválida",
  "path": "/api/albums",
  "fields": { "genre": "El género es obligatorio" }
}
```

| Código | Caso |
|---|---|
| 400 | Validación, JSON incorrecto o id no numérico |
| 401 | Sin credenciales o credenciales incorrectas |
| 403 | El rol no tiene permiso |
| 404 | Álbum inexistente |
| 503 | El microservicio de usuarios no responde |

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
```

```powershell
Invoke-RestMethod http://localhost:8080/api/status
```

## Pruebas

```powershell
.\mvnw.cmd test
```

No necesitan base de datos ni el microservicio de usuarios:

- `AlbumControllerTest`: acceso por rol, validaciones y 404 en los endpoints de álbumes.
- `SecurityRulesTest`: reglas de seguridad generales y respuestas 401 y 403.
- `ErrorResponseBuilderTest`: patrón Builder.
