package cl.caredent.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** Los nombres de campo coinciden con el JSON que ya usaba el frontend (localStorage). */
public final class PacienteDtos {
    private PacienteDtos() {}

    public record AntecedentesDto(String medicos, String odontologicos, String alergias) {}

    public record FichaClinicaDto(String grupoSanguineo, LocalDate fechaCreacion, String observaciones) {}

    public record DiagnosticoDto(Long id, LocalDate fecha, String descripcion, String profesional) {}

    public record TratamientoDto(Long id, String nombre, String estado, LocalDate fechaInicio, String profesional, String descripcion) {}

    public record AtencionDto(Long id, LocalDate fecha, String motivo, String profesional, String notas) {}

    public record SeguimientoDto(Long id, LocalDate fecha, String titulo, String descripcion, boolean futuro) {}

    public record ControlDto(LocalDate fecha, String motivo, String estado) {}

    public record PacienteDto(
            Long id, String nombre, String apellidos, String rut, LocalDate fechaNacimiento,
            String telefono, String correo, String direccion, String estado, LocalDate ultimaAtencion,
            AntecedentesDto antecedentes, FichaClinicaDto fichaClinica,
            List<DiagnosticoDto> diagnosticos, List<TratamientoDto> tratamientos,
            List<AtencionDto> atenciones, List<SeguimientoDto> seguimiento, ControlDto proximoControl) {}

    /** Datos minimos para el selector de la agenda (no expone informacion clinica). */
    public record PacienteResumenDto(Long id, String nombre, String apellidos, String rut, String estado) {}

    public record PacienteRequest(
            @NotBlank(message = "Ingresa el nombre del paciente.") @Size(max = 80) String nombre,
            @NotBlank(message = "Ingresa los apellidos del paciente.") @Size(max = 100) String apellidos,
            @NotBlank(message = "Ingresa el RUT.")
            @Pattern(regexp = "^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$", message = "Ingresa un RUT válido, formato 12.345.678-9.")
            String rut,
            @NotNull(message = "Selecciona la fecha de nacimiento.")
            @PastOrPresent(message = "La fecha de nacimiento no puede ser futura.")
            LocalDate fechaNacimiento,
            @NotBlank(message = "Ingresa un teléfono de contacto.")
            @Pattern(regexp = "^\\+?\\d[\\d\\s]{6,}$", message = "Ingresa un teléfono de contacto válido.")
            String telefono,
            @NotBlank(message = "Ingresa un correo electrónico.")
            @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Ingresa un correo electrónico válido.")
            String correo,
            @Size(max = 200) String direccion,
            /** activo | en-tratamiento | pendiente | inactivo (solo al modificar). */
            String estado,
            @Valid AntecedentesDto antecedentes,
            @Size(max = 20) String grupoSanguineo,
            /** Solo al registrar: crea la primera atencion. */
            @Size(max = 2000) String motivoConsulta) {}

    public record TratamientoRequest(
            @NotBlank(message = "Ingresa el nombre del tratamiento.") @Size(max = 150) String nombre,
            @Size(max = 1000) String descripcion,
            /** pendiente | en-curso | finalizado */
            String estado) {}

    public record DiagnosticoRequest(
            @NotBlank(message = "Ingresa la descripción del diagnóstico.") @Size(max = 500) String descripcion,
            LocalDate fecha) {}

    public record ControlRequest(
            @NotNull(message = "Selecciona la fecha del control.") LocalDate fecha,
            @NotBlank(message = "Ingresa el motivo del control.") @Size(max = 300) String motivo) {}
}
