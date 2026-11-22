package cl.caredent.backend.controller;

import cl.caredent.backend.dto.AuthDtos.*;
import cl.caredent.backend.security.AuthUser;
import cl.caredent.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticación", description = "Login, renovación de tokens JWT y datos de la sesión")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Iniciar sesión", description = "Devuelve accessToken (corto) y refreshToken (largo) junto con los datos del usuario.")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Renovar tokens", description = "Canjea un refreshToken válido por un nuevo accessToken y un nuevo refreshToken.")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return auth.renovar(req);
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Usuario de la sesión actual", description = "Sirve para validar el token guardado cuando se recarga la página.")
    public UsuarioSesion me(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser user) {
        return auth.sesion(user.id());
    }

    @GetMapping("/salud")
    @Operation(summary = "Health check", description = "Público. Útil para comprobar que el servidor está arriba (por ejemplo en EC2).")
    public Map<String, String> salud() {
        return Map.of("estado", "ok", "servicio", "caredent-backend");
    }
}
