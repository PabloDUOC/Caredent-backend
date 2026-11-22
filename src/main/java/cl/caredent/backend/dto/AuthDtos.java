package cl.caredent.backend.dto;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank(message = "Ingresa tu usuario.") String usuario,
            @NotBlank(message = "Ingresa tu contraseña.") String clave,
            /** Opcional: rol elegido en la pantalla de login. Si no coincide con el del usuario, se rechaza. */
            String rol) {}

    public record RefreshRequest(@NotBlank(message = "Falta el refresh token.") String refreshToken) {}

    public record UsuarioSesion(Long id, String usuario, String nombre, String rol) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            UsuarioSesion user) {}
}
