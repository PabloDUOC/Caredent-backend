package cl.caredent.backend.controller;

import cl.caredent.backend.dto.HoraDtos.*;
import cl.caredent.backend.security.AuthUser;
import cl.caredent.backend.service.HoraService;
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
@RequestMapping("/api/horas")
@Tag(name = "Agenda de horas", description = "Horas agendadas por recepción (rol RECEPCIONISTA)")
public class HoraController {
    private final HoraService service;

    public HoraController(HoraService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('RECEPCIONISTA')")
    @Operation(summary = "Listar horas agendadas")
    public List<HoraDto> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RECEPCIONISTA')")
    @Operation(summary = "Agendar hora", description = "Rechaza con 409 si el profesional ya tiene una hora en ese día y horario.")
    public HoraDto agendar(@Valid @RequestBody HoraRequest req, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user) {
        return service.agendar(req, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RECEPCIONISTA')")
    @Operation(summary = "Anular hora")
    public void anular(@PathVariable Long id) {
        service.anular(id);
    }
}
