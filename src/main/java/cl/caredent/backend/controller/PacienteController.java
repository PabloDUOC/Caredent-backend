package cl.caredent.backend.controller;

import cl.caredent.backend.dto.PacienteDtos.*;
import cl.caredent.backend.security.AuthUser;
import cl.caredent.backend.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Pacientes, fichas clínicas, tratamientos, diagnósticos y controles (rol PROFESIONAL)")
public class PacienteController {
    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    /* ---- Paciente ---- */

    @GetMapping
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Listar pacientes con su ficha completa")
    public List<PacienteDto> listar() {
        return service.listar();
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('PROFESIONAL','RECEPCIONISTA')")
    @Operation(summary = "Listar pacientes (datos mínimos)", description = "Sin información clínica. Lo usa recepción para agendar.")
    public List<PacienteResumenDto> resumen() {
        return service.listarResumen();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Obtener un paciente por id")
    public PacienteDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Registrar paciente", description = "Valida el RUT (formato, dígito verificador y unicidad) y crea la ficha clínica inicial.")
    public PacienteDto crear(@Valid @RequestBody PacienteRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Modificar datos del paciente")
    public PacienteDto modificar(@PathVariable Long id, @Valid @RequestBody PacienteRequest req) {
        return service.modificar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Eliminar paciente", description = "Elimina también su ficha, tratamientos y horas agendadas.")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    /* ---- Tratamientos ---- */

    @PostMapping("/{id}/tratamientos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Registrar tratamiento")
    public TratamientoDto agregarTratamiento(@PathVariable Long id, @Valid @RequestBody TratamientoRequest req,
                                             @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user) {
        return service.agregarTratamiento(id, req, user);
    }

    @PutMapping("/{id}/tratamientos/{tratamientoId}")
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Modificar tratamiento")
    public TratamientoDto modificarTratamiento(@PathVariable Long id, @PathVariable Long tratamientoId,
                                               @Valid @RequestBody TratamientoRequest req) {
        return service.modificarTratamiento(id, tratamientoId, req);
    }

    @DeleteMapping("/{id}/tratamientos/{tratamientoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Eliminar tratamiento")
    public void eliminarTratamiento(@PathVariable Long id, @PathVariable Long tratamientoId) {
        service.eliminarTratamiento(id, tratamientoId);
    }

    /* ---- Diagnosticos ---- */

    @PostMapping("/{id}/diagnosticos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Registrar diagnóstico")
    public DiagnosticoDto agregarDiagnostico(@PathVariable Long id, @Valid @RequestBody DiagnosticoRequest req,
                                             @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user) {
        return service.agregarDiagnostico(id, req, user);
    }

    @DeleteMapping("/{id}/diagnosticos/{diagnosticoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Eliminar diagnóstico")
    public void eliminarDiagnostico(@PathVariable Long id, @PathVariable Long diagnosticoId) {
        service.eliminarDiagnostico(id, diagnosticoId);
    }

    /* ---- Control ---- */

    @PutMapping("/{id}/control")
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Registrar o modificar el próximo control", description = "Reemplaza el control anterior y actualiza el seguimiento del paciente.")
    public ControlDto establecerControl(@PathVariable Long id, @Valid @RequestBody ControlRequest req) {
        return service.establecerControl(id, req);
    }

    @DeleteMapping("/{id}/control")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESIONAL')")
    @Operation(summary = "Eliminar el próximo control")
    public void eliminarControl(@PathVariable Long id) {
        service.eliminarControl(id);
    }
}
