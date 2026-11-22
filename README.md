# Caredent Backend (Spring Boot)

API REST de Caredent: Spring Boot 3.3, Java 17, Spring Data JPA, Spring Security + JWT, Bean Validation y Swagger (springdoc).

## Ejecutar (desarrollo, sin instalar base de datos)

```bash
mvn spring-boot:run
```

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/api/salud`
- Base de datos: H2 en archivo `./data/caredentdb` (perfil `dev`, por defecto).

Al primer arranque se cargan los usuarios y pacientes de demostración:

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Recepcionista | `recepcion` | `recepcion123` |
| Profesional | `profesional` | `profesional123` |

## Probar la API en Swagger
1. `POST /api/auth/login` con `{"usuario":"profesional","clave":"profesional123"}`.
2. Copia `accessToken`, pulsa **Authorize** y pégalo.
3. Prueba cualquier endpoint. Con el token de otro rol los endpoints ajenos responden `403`.

## Pruebas

```bash
mvn test
```

`ApiIntegrationTest` levanta la aplicación con H2 y valida login, JWT, roles, CRUD y reglas de negocio.

## Producción (MySQL)

Se activa con `SPRING_PROFILES_ACTIVE=prod` y variables de entorno:

| Variable | Descripción | Obligatoria |
|---|---|:-:|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Conexión MySQL (por defecto `localhost`, `3306`, `caredent`) | No |
| `DB_USER` | Usuario MySQL (por defecto `caredent`) | No |
| `DB_PASSWORD` | Clave MySQL | **Sí** |
| `JWT_SECRET` | Secreto de firma JWT, mínimo 32 caracteres | **Sí** |
| `JWT_ACCESS_MINUTES` / `JWT_REFRESH_DAYS` | Duración de los tokens (30 min / 7 días) | No |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend, separados por coma | No |

```bash
mvn clean package
java -jar target/caredent-backend-1.0.0.jar
```

El despliegue completo en AWS EC2 está en el instructivo `03_Instructivo_Despliegue_AWS_EC2`.

## Estructura

```
cl.caredent.backend
 ├─ controller   Endpoints REST (@PreAuthorize por rol, @Operation para Swagger)
 ├─ service      Lógica de negocio y transacciones
 ├─ repository   Spring Data JPA
 ├─ model        Entidades JPA
 ├─ dto          Contratos JSON con validaciones
 ├─ security     JwtService, JwtAuthFilter, AuthUser
 ├─ config       SecurityConfig (CORS, JWT, roles), OpenApiConfig, DataSeeder
 ├─ exception    ApiException y GlobalExceptionHandler
 └─ util         RutUtil
```

## Endpoints principales

Autenticación `/api/auth/*` · Pacientes `/api/pacientes/**` · Usuarios `/api/usuarios/**` · Horas `/api/horas/**`. La tabla completa con roles está en el Documento de Integración y en Swagger.
