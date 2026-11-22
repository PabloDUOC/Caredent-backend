package cl.caredent.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class UsuarioDtos {
    private UsuarioDtos() {}

    public record UsuarioRequest(
            @NotBlank(message = "Ingresa el nombre.") @Size(max = 100) String nombre,
            @NotBlank(message = "Ingresa el usuario.")
            @Pattern(regexp = "^[A-Za-z0-9._-]{3,30}$", message = "El usuario debe tener 3 a 30 caracteres (letras, números, punto, guion).")
            String usuario,
            @NotBlank(message = "Ingresa la contraseña.")
            @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres.")
            String clave,
            @NotBlank(message = "Selecciona el rol.") String rol) {}

    public record UsuarioDto(Long id, String nombre, String usuario, String rol, LocalDateTime eliminadoEl) {}

    /** Lista minima de profesionales para la agenda (la ve recepcion). */
    public record ProfesionalDto(Long id, String nombre) {}
}
