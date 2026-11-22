package cl.caredent.backend.controller;

import cl.caredent.backend.dto.UsuarioDtos.*;
import cl.caredent.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Administración de médicos y recepcionistas (solo ADMINISTRADOR)")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios activos (profesionales y recepcionistas)")
    public List<UsuarioDto> listar() {
        return service.listarActivos();
    }

    @GetMapping("/papelera")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios en la papelera")
    public List<UsuarioDto> papelera() {
        return service.listarPapelera();
    }

    @GetMapping("/profesionales")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA')")
    @Operation(summary = "Listar profesionales (id y nombre)", description = "Lo usa recepción para elegir profesional al agendar.")
    public List<ProfesionalDto> profesionales() {
        return service.listarProfesionales();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear usuario", description = "Rol permitido: profesional o recepcionista. La clave se guarda con BCrypt.")
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest req) {
        return service.crear(req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Enviar usuario a la papelera (borrado lógico)")
    public void papelera(@PathVariable Long id) {
        service.enviarAPapelera(id);
    }

    @PostMapping("/{id}/restaurar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Restaurar usuario desde la papelera")
    public UsuarioDto restaurar(@PathVariable Long id) {
        return service.restaurar(id);
    }

    @DeleteMapping("/{id}/definitivo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar definitivamente un usuario de la papelera")
    public void eliminarDefinitivo(@PathVariable Long id) {
        service.eliminarDefinitivo(id);
    }
}
