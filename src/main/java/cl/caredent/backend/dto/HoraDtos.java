package cl.caredent.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class HoraDtos {
    private HoraDtos() {}

    public record HoraRequest(
            @NotNull(message = "Selecciona un paciente.") Long pacienteId,
            @NotNull(message = "Selecciona un profesional.") Long profesionalId,
            @NotNull(message = "Selecciona la fecha.") @FutureOrPresent(message = "La fecha no puede ser anterior a hoy.") LocalDate fecha,
            @NotBlank(message = "Selecciona la hora.")
            @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora debe tener formato HH:mm.")
            String hora,
            @NotBlank(message = "Ingresa el motivo de la consulta.") @Size(max = 300) String motivo) {}

    public record HoraDto(
            Long id, Long pacienteId, String pacienteNombre, Long profesionalId, String profesionalNombre,
            LocalDate fecha, String hora, String motivo, String agendadaPor) {}
}
